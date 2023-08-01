package pro.felixo.proto3

@JvmInline
value class Identifier(val value: String) : Comparable<Identifier> {
    override fun compareTo(other: Identifier): Int = value.compareTo(other.value)
    override fun toString() = value
}
