package pro.felixo.proto3.wire

import pro.felixo.proto3.schema.FieldNumber

@Suppress("MagicNumber")
class WireInput(
    private val bytes: ByteArray,
    private val offset: Int = 0,
    private val length: Int = bytes.size - offset
) {
    private var position = offset

    val remaining: Int get() = length - (position - offset)

    /**
     * Returns the raw varint: does not perform zigzag decoding.
     */
    fun readVarIntAsInt(): Int {
        var result = 0
        var shift = 0
        while (true) {
            val byte = readByte()
            result = result or ((byte.toInt() and 0x7F) shl shift)
            if (byte >= 0)
                return result
            shift += 7
        }
    }

    /**
     * Returns the raw varint: does not perform zigzag decoding.
     */
    fun readVarIntAsLong(): Long {
        var result = 0L
        var shift = 0
        while (true) {
            val byte = readByte()
            result = result or ((byte.toLong() and 0x7F) shl shift)
            if (byte >= 0)
                return result
            shift += 7
        }
    }

    fun readFixed32(): Int =
        readByteAsInt() or (readByteAsInt() shl 8) or (readByteAsInt() shl 16) or (readByteAsInt() shl 24)

    fun readFixed64(): Long =
        readByteAsLong() or (readByteAsLong() shl 8) or (readByteAsLong() shl 16) or
            (readByteAsLong() shl 24) or (readByteAsLong() shl 32) or (readByteAsLong() shl 40) or
            (readByteAsLong() shl 48) or (readByteAsLong() shl 56)

    fun readLengthDelimited(): WireInput {
        val length = readVarIntAsInt()
        require(length >= 0) { "Length must be non-negative, but was $length" }
        val ret = WireInput(bytes, position, length)
        position += length
        return ret
    }

    fun readBytes(): ByteArray {
        val ret = ByteArray(remaining)
        System.arraycopy(bytes, position, ret, 0, remaining)
        position += remaining
        return ret
    }

    fun readByte(): Byte = bytes[position++]
    fun readByteAsInt(): Int = readByte().toUByte().toInt()
    fun readByteAsLong(): Long = readByte().toUByte().toLong()

    fun writeTo(destination: ByteArray, destinationOffset: Int) {
        bytes.copyInto(destination, destinationOffset, 0, remaining)
        position += remaining
    }
}

fun WireInput.decodeMessage(onValue: (FieldNumber, WireValue) -> Unit) {
    while (remaining > 0) {
        val tag = Tag(readVarIntAsInt())
        when (tag.wireType) {
            WireType.VarInt -> onValue(tag.fieldNumber, WireValue.VarInt(readVarIntAsLong()))
            WireType.Fixed64 -> onValue(tag.fieldNumber, WireValue.Fixed64(readFixed64()))
            WireType.Len -> onValue(tag.fieldNumber, WireValue.Len(readLengthDelimited()))
            WireType.Fixed32 -> onValue(tag.fieldNumber, WireValue.Fixed32(readFixed32()))
            WireType.SGroup, WireType.EGroup -> {}
        }
    }
}

fun WireInput.decodeValue(wireType: WireType): WireValue? = if (remaining == 0)
    null
else when (wireType) {
    WireType.VarInt -> WireValue.VarInt(readVarIntAsLong())
    WireType.Fixed64 -> WireValue.Fixed64(readFixed64())
    WireType.Len -> WireValue.Len(readLengthDelimited())
    WireType.Fixed32 -> WireValue.Fixed32(readFixed32())
    WireType.SGroup, WireType.EGroup -> throw IllegalArgumentException("Cannot decode SGroup or EGroup as a value")
}
