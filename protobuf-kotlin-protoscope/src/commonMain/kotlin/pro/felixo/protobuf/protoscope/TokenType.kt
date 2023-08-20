package pro.felixo.protobuf.protoscope

enum class TokenType(val regex: Regex, val getToken: (groupValues: List<String>) -> Token? = { null }) {
    Whitespace(Regex("""\s+""")),
    Comment(Regex("""#.*?(?:\r\n?|\n|$)""")),
    OpenBrace(Regex("""\{"""), { Token.OpenBrace }),
    OpenGroupBrace(Regex("""!\{"""), { Token.OpenGroupBrace }),

    @Suppress("RegExpRedundantEscape") // JS target requires the escape
    CloseBrace(Regex("""\}"""), { Token.CloseBrace }),

    Tag(
        Regex("""((\d+)|(0x([\da-fA-F]+))):(VARINT|I64|LEN|SGROUP|EGROUP|I32|\d)?"""),
        { Token.Tag.of(it[2], it[4], it[5]) }
    ),
    StringLiteral(
        Regex(""""((?:[^"\\]|\\\\|\\"|\\x\d\d|\\\d\d\d|\\n|\r\n?|\n)*)""""),
        { Token.StringLiteral.of(it[1]) }
    ),
    BytesLiteral(Regex("""`(([\da-fA-F]{2})*?)`"""), { Token.BytesLiteral.of(it[1]) }),
    FloatLiteral(
        Regex(
            """(-?)((0x([\dA-Fa-f]*)\.([\dA-Fa-f]*)([pP]([+-]?\d+))?)|(\d*\.\d+([eE]-?\d+)?))(i(32|64))?"""
        ),
        { Token.FloatLiteral.of(it[1], it[8], it[3], it[11]) }
    ),
    InfiniteFloatLiteral(Regex("""(-?)inf(32|64)"""), { Token.FloatLiteral.ofInfinity(it[1], it[2]) }),
    IntegerLiteral(
        Regex("""(-?)((0x([\da-fA-F]+))|(\d+))(z|i32|i64)?"""),
        { Token.IntegerLiteral.of(it[1], it[5], it[4], it[6]) }
    ),
    BooleanLiteral(Regex("""true|false"""), { Token.BooleanLiteral.of(it[0]) }),
    LongForm(Regex("""long-form:(\d+)"""), { Token.LongForm.of(it[1]) })
}
