package pro.felixo.protobuf.schemadocument

/**
 * Represents a token in a .proto file.
 */
sealed class Token {
    data class Identifier(override val text: String) : Token() {
        val components: List<String> = text.split('.')
        override fun toString(): String = text
    }
    data class NumberLiteral(override val text: String) : Token() {
        val value: Int = text.toInt()
        override fun toString(): String = text
    }
    data class StringLiteral(override val text: String) : Token() {
        val value: String = text.substring(1, text.length - 1).replace("\\\"", "\"").replace("\\\\", "\\")
        override fun toString(): String = text
    }
    object OpenBrace : Token() {
        override val text: String = "{"
    }
    object CloseBrace : Token() {
        override val text: String = "}"
    }
    object OpenBracket : Token() {
        override val text: String = "["
    }
    object CloseBracket : Token() {
        override val text: String = "]"
    }
    object OpenParen : Token() {
        override val text: String = "("
    }
    object CloseParen : Token() {
        override val text: String = ")"
    }
    object Semicolon : Token() {
        override val text: String = ";"
    }
    object Equals : Token() {
        override val text: String = "="
    }
    object Comma : Token() {
        override val text: String = ","
    }

    abstract val text: String
    override fun toString(): String = text
}
