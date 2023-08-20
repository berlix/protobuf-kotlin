package pro.felixo.protobuf.schemadocument

/**
 * Converts .proto syntax into sequences of tokens.
 */
class SchemaDocumentTokenizer {
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
