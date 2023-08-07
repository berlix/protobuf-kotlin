package pro.felixo.protobuf

data class EnumValue(
    val name: Identifier,
    val number: Int
) : Comparable<EnumValue> {
    override fun compareTo(other: EnumValue): Int = number.compareTo(other.number)
}
