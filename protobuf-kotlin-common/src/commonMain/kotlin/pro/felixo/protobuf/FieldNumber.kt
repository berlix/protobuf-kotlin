package pro.felixo.protobuf

import kotlin.jvm.JvmInline

/**
 * Represents a Protobuf field number, which is an integer in one of the intervals [1,19000) and [20000,536870911].
 *
 * This class does not perform validation of field numbers.
 *
 * When choosing field numbers, keep in mind that low numbers encode more efficiently than high numbers.
 */
@JvmInline
value class FieldNumber(val value: Int) : Comparable<FieldNumber> {
    override fun compareTo(other: FieldNumber): Int = value.compareTo(other.value)

    override fun toString(): String = value.toString()

    companion object {
        const val MIN = 1
        const val RESERVED_RANGE_START = 19_000
        const val RESERVED_RANGE_END = 20_000
        const val MAX = 536_870_911
    }
}
