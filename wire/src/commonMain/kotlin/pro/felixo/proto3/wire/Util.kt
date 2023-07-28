@file:Suppress("MagicNumber")

package pro.felixo.proto3.wire

import pro.felixo.proto3.FieldNumber

fun Int.encodeSInt32(): Int = (this shl 1) xor (this shr 31)
fun Int.decodeSInt32(): Int = (this ushr 1) xor -(this and 1)

fun Long.encodeSInt64(): Long = (this shl 1) xor (this shr 63)
fun Long.decodeSInt64(): Long = (this ushr 1) xor -(this and 1)

fun WireBuffer.decodeMessage(onValue: (FieldNumber, WireValue) -> Unit) {
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

fun WireBuffer.decodeValue(wireType: WireType): WireValue? = if (remaining == 0)
    null
else when (wireType) {
    WireType.VarInt -> WireValue.VarInt(readVarIntAsLong())
    WireType.Fixed64 -> WireValue.Fixed64(readFixed64())
    WireType.Len -> WireValue.Len(readLengthDelimited())
    WireType.Fixed32 -> WireValue.Fixed32(readFixed32())
    WireType.SGroup, WireType.EGroup -> throw IllegalArgumentException("Cannot decode SGroup or EGroup as a value")
}

fun WireBuffer.encodeField(tag: Tag, value: WireValue) {
    writeVarInt(tag.value)
    encodeValue(value)
}

fun WireBuffer.encodeValue(value: WireValue) {
    when (value) {
        is WireValue.VarInt -> writeVarInt(value.value)
        is WireValue.Fixed64 -> writeFixed64(value.value)
        is WireValue.Len -> {
            writeVarInt(value.value.remaining)
            writeAndConsume(value.value)
        }
        is WireValue.Fixed32 -> writeFixed32(value.value)
    }
}
