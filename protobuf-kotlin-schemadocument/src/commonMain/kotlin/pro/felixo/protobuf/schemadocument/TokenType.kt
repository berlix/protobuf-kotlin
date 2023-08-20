package pro.felixo.protobuf.schemadocument

/**
 * For each .proto token type, the regex that matches it and a function that converts the regex match into a
 * [Token].
 */
enum class TokenType(val regex: Regex, val getToken: (text: String) -> Token? = { null }) {
    Whitespace(Regex("""\s+""")),
    SingleLineComment(Regex("""//.*?(?:\r\n?|\n)""")),
    MultiLineComment(Regex("""/\*(?:.|\r\n?|\n)*?\*/""")),
    Identifier(Regex("""[a-zA-Z.][a-zA-Z0-9_.]*"""), { Token.Identifier(it) }),
    NumberLiteral(Regex("[0-9]+"), { Token.NumberLiteral(it) }),
    StringLiteral(Regex(""""(?:[^"\\]|\\.)*""""), { Token.StringLiteral(it) }),
    OpenBrace(Regex("""\{"""), { Token.OpenBrace }),

    @Suppress("RegExpRedundantEscape") // JS target requires the escape
    CloseBrace(Regex("""\}"""), { Token.CloseBrace }),

    OpenBracket(Regex("""\["""), { Token.OpenBracket }),

    @Suppress("RegExpRedundantEscape") // JS target requires the escape
    CloseBracket(Regex("""\]"""), { Token.CloseBracket }),

    OpenParen(Regex("""\("""), { Token.OpenParen }),
    CloseParen(Regex("""\)"""), { Token.CloseParen }),
    Semicolon(Regex(";"), { Token.Semicolon }),
    Equals(Regex("="), { Token.Equals }),
    Comma(Regex(","), { Token.Comma })
}
