@file:Suppress("MagicNumber")

package pro.felixo.protobuf.protoscope

import kotlin.math.min

/**
 * Converts Protoscope code into sequences of tokens.
 */
class ProtoscopeTokenizer {
    fun tokenize(input: String): Sequence<Token> = sequence {
        var position = 0

        outer@ while (position < input.length) {
            for (type in TokenType.entries) {
                val matchResult = type.regex.find(input, position)

                if (matchResult != null && matchResult.range.first == position) {
                    type.getToken(matchResult.groupValues)?.let { yield(it) }
                    position += matchResult.value.length
                    continue@outer
                }
            }

            throw IllegalArgumentException(
                "Unrecognized token at position $position: ${
                    input.substring(
                        position,
                        min(input.length, position + PARSE_ERROR_MESSAGE_LOOKAROUND_CHARS)
                    )
                }"
            )
        }
    }

    companion object {
        private const val PARSE_ERROR_MESSAGE_LOOKAROUND_CHARS = 30
    }
}
