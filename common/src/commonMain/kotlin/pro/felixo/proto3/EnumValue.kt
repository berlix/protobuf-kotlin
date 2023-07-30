package pro.felixo.proto3

data class EnumValue(
    val name: Identifier,
    val number: Int
) : Comparable<EnumValue> {
    fun validate() = name.validate()

    override fun compareTo(other: EnumValue): Int = number.compareTo(other.number)
}
