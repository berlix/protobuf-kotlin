package pro.felixo.proto3.wire

import pro.felixo.proto3.schema.FieldNumber

@JvmInline
value class Tag(val value: Int) {
    val fieldNumber: FieldNumber get() = FieldNumber(value ushr 3)
    val wireType: WireType get() = WireType.of(value and 0b111)

    companion object {
        fun of(fieldNumber: FieldNumber, wireType: WireType) = Tag((fieldNumber.value shl 3) or wireType.value)
    }
}
