package pro.felixo.proto3

@JvmInline
value class Identifier(val value: String) : Comparable<Identifier> {
    fun validate() {
        require(value.isNotEmpty()) { "Identifier must not be empty" }
        require(value.first().let { it.isLetter() || it == '_' }) {
            "Identifier must start with letter or underscore, but is: $value"
        }
        require(value.all { it.isLetter() || it.isDigit() || it == '_' }) {
            "Identifier contains invalid characters: $value"
        }
    }

    override fun compareTo(other: Identifier): Int = value.compareTo(other.value)
    override fun toString() = value
}
