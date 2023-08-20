package pro.felixo.protobuf.wire

import pro.felixo.protobuf.FieldNumber
import kotlin.jvm.JvmInline

/**
 * Represents a field tag as it appears inside a protobuf message, consisting of a field number and a wire type.
 */
@JvmInline
@Suppress("MagicNumber")
value class Tag(
    /**
     * The numeric value of the tag, as it is encoded in the protobuf binary format.
     */
    val value: Int
) {
    val fieldNumber: FieldNumber get() = FieldNumber(value ushr 3)
    val wireType: WireType get() = WireType.of(value and 0b111)

    companion object {
        /**
         * Creates a tag from a field number and a [WireType].
         */
        fun of(fieldNumber: FieldNumber, wireType: WireType) = of(fieldNumber.value, wireType.value)

        /**
         * Creates a tag from a field number and a numeric wire type. This can be used to create invalid tags for
         * testing purposes.
         */
        fun of(fieldNumber: Int, wireType: Int) = Tag((fieldNumber shl 3) or wireType)
    }
}
