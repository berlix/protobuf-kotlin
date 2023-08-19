package pro.felixo.protobuf.wire

import pro.felixo.protobuf.FieldNumber
import kotlin.jvm.JvmInline

@JvmInline
@Suppress("MagicNumber")
value class Tag(val value: Int) {
    val fieldNumber: FieldNumber get() = FieldNumber(value ushr 3)
    val wireType: WireType get() = WireType.of(value and 0b111)

    companion object {
        fun of(fieldNumber: FieldNumber, wireType: WireType) = of(fieldNumber.value, wireType.value)
        fun of(fieldNumber: Int, wireType: Int) = Tag((fieldNumber shl 3) or wireType)
    }
}
