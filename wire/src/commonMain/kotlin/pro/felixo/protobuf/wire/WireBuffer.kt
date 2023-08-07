package pro.felixo.protobuf.wire

@Suppress("MagicNumber")
class WireBuffer(
    initialBytes: ByteArray? = null,
    private val offset: Int = 0,
    initialLength: Int = initialBytes?.let { it.size - offset } ?: 0
) {
    private var position = offset

    val remaining: Int get() = length - (position - offset)

    private var bytes: ByteArray = initialBytes ?: ByteArray(DEFAULT_INITIAL_BUFFER_SIZE)

    var length: Int = initialLength
        private set

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

    fun readLengthDelimited(): WireBuffer {
        val length = readVarIntAsInt()
        require(length >= 0) { "Length must be non-negative, but was $length" }
        val ret = WireBuffer(bytes, position, length)
        position += length
        return ret
    }

    fun readBytes(): ByteArray {
        val ret = ByteArray(remaining)
        System.arraycopy(bytes, position, ret, 0, remaining)
        position += remaining
        return ret
    }

    private fun readByte(): Byte = bytes[position++]
    private fun readByteAsInt(): Int = readByte().toUByte().toInt()
    private fun readByteAsLong(): Long = readByte().toUByte().toLong()

    private fun ensureCapacity(numBytes: Int) {
        if (length + numBytes <= bytes.size)
            return
        val newBuffer = ByteArray((length + numBytes).takeHighestOneBit() shl 1)
        bytes.copyInto(newBuffer)
        bytes = newBuffer
    }

    fun getBytes(): ByteArray {
        val ret = ByteArray(length)
        bytes.copyInto(ret, 0, 0, length)
        return ret
    }

    fun writeFixed32(value: Int) {
        ensureCapacity(4)
        for (i in 0..3)
            bytes[length++] = (value shr i * 8).toByte()
    }

    fun writeFixed64(value: Long) {
        ensureCapacity(8)
        for (i in 0..7)
            bytes[length++] = (value shr i * 8).toByte()
    }

    fun writeVarInt(value: Int, extraBytes: Int = 0) {
        val varIntLength = (31 - value.countLeadingZeroBits()) / 7 + extraBytes
        ensureCapacity(varIntLength + 1)
        var current: Int = value
        for (i in 0 until varIntLength) {
            bytes[length + i] = ((current and 0x7F) or 0x80).toByte()
            current = current ushr 7
        }
        bytes[length + varIntLength] = current.toByte()
        length += varIntLength + 1
    }

    fun writeVarInt(value: Long, extraBytes: Int = 0) {
        val varIntLength = (63 - value.countLeadingZeroBits()) / 7 + extraBytes
        ensureCapacity(varIntLength + 1)
        var current: Long = value
        for (i in 0 until varIntLength) {
            bytes[length + i] = ((current and 0x7F) or 0x80).toByte()
            current = current ushr 7
        }
        bytes[length + varIntLength] = current.toByte()
        length += varIntLength + 1
    }

    fun writeByte(value: Byte) {
        ensureCapacity(1)
        bytes[length] = value
        length++
    }

    fun write(value: ByteArray) {
        ensureCapacity(value.size)
        value.copyInto(bytes, length)
        length += value.size
    }

    fun writeAndConsume(value: WireBuffer) {
        val inputSize = value.remaining
        ensureCapacity(inputSize)
        value.bytes.copyInto(bytes, length, 0, value.remaining)
        value.position += value.remaining
        length += inputSize
    }

    companion object {
        private const val DEFAULT_INITIAL_BUFFER_SIZE = 32
    }
}
