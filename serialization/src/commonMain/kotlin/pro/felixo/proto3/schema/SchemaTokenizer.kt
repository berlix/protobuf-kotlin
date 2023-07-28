package pro.felixo.proto3.schema

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

enum class TokenType(val regex: Regex, val getToken: (text: String) -> Token? = { null }) {
    Whitespace(Regex("""\s+""")),
    SingleLineComment(Regex("""//.*?\R""")),
    MultiLineComment(Regex("""/\*(?:.|\R)*?\*/""")),
    Identifier(Regex("""[a-zA-Z.][a-zA-Z0-9_.]*"""), { Token.Identifier(it) }),
    NumberLiteral(Regex("[0-9]+"), { Token.NumberLiteral(it) }),
    StringLiteral(Regex(""""(?:[^"\\]|\\.)*""""), { Token.StringLiteral(it) }),
    OpenBrace(Regex("""\{"""), { Token.OpenBrace }),
    CloseBrace(Regex("}"), { Token.CloseBrace }),
    OpenBracket(Regex("""\["""), { Token.OpenBracket }),
    CloseBracket(Regex("]"), { Token.CloseBracket }),
    OpenParen(Regex("""\("""), { Token.OpenParen }),
    CloseParen(Regex("""\)"""), { Token.CloseParen }),
    Semicolon(Regex(";"), { Token.Semicolon }),
    Equals(Regex("="), { Token.Equals }),
    Comma(Regex(","), { Token.Comma })
}

class SchemaTokenizer {
    fun tokenize(input: String): Sequence<Token> = sequence {
        var position = 0

        outer@ while (position < input.length) {
            for (type in TokenType.entries) {
                val matchResult = type.regex.find(input, position)

                if (matchResult != null && matchResult.range.first == position) {
                    val tokenText = matchResult.value
                    type.getToken(tokenText)?.let { yield(it) }
                    position += tokenText.length
                    continue@outer
                }
            }

            throw IllegalArgumentException("Unrecognized token at position $position")
        }
    }
}
