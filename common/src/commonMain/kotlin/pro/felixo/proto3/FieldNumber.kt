package pro.felixo.proto3

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
