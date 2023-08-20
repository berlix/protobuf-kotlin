package pro.felixo.protobuf

/**
 * Represents a Protobuf enum value, consisting of a name and a number.
 */
data class EnumValue(
    val name: Identifier,
    val number: Int
) : Comparable<EnumValue> {
    override fun compareTo(other: EnumValue): Int = number.compareTo(other.number)
}
