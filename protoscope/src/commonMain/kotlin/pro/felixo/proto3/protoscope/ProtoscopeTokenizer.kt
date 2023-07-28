@file:Suppress("MagicNumber")

package pro.felixo.proto3.protoscope

import pro.felixo.proto3.wire.WireType
import java.lang.Integer.min
import kotlin.math.pow

sealed class Token {
    data object OpenBrace : Token()
    data object OpenGroupBrace : Token()
    data object CloseBrace : Token()

    data class Tag(val number: Int, val type: Int?) : Token() {
        companion object {
            fun of(numberDecimal: String, numberHex: String, wireType: String) =
                Tag(
                    if (numberDecimal.isNotEmpty()) numberDecimal.toInt() else numberHex.toInt(16),
                    when (wireType) {
                        "" -> null
                        "VARINT" -> WireType.VarInt.value
                        "I64" -> WireType.Fixed64.value
                        "LEN" -> WireType.Len.value
                        "SGROUP" -> WireType.SGroup.value
                        "EGROUP" -> WireType.EGroup.value
                        "I32" -> WireType.Fixed32.value
                        else -> wireType.toInt().also { require(it in 0..7) }
                    }
                )
        }
    }

    data class StringLiteral(val text: String) : Token() {
        companion object {
            fun of(text: String) = StringLiteral(
                text.replace(Regex("""\\(\\|"|n|x(\d\d)|(\d{1,3}))""")) {
                    if (it.groupValues[2].isNotEmpty())
                        it.groupValues[2].toInt(radix = 16).toChar().toString()
                    else if (it.groupValues[3].isNotEmpty())
                        it.groupValues[3].toInt(radix = 8).toChar().toString()
                    else if (it.groupValues[1] == "n")
                        "\n"
                    else
                        it.groupValues[1]
                }
            )
        }
    }

    data class BooleanLiteral(val value: Boolean) : Token() {
        companion object {
            fun of(text: String) = BooleanLiteral(text.toBooleanStrict())
        }
    }

    data class IntegerLiteral(val value: Long, val type: Type) : Token() {
        enum class Type {
            Fixed32, Fixed64, VarInt, SVarInt
        }
        companion object {
            fun of(sign: String, decimal: String, hex: String, type: String): IntegerLiteral {
                return IntegerLiteral(
                    if (sign == "-")
                        "-$decimal$hex".toLong(if (decimal.isNotEmpty()) 10 else 16)
                    else
                        "$decimal$hex".toULong(if (decimal.isNotEmpty()) 10 else 16).toLong(),
                    when (type) {
                        "z" -> Type.SVarInt
                        "i32" -> Type.Fixed32
                        "i64" -> Type.Fixed64
                        else -> Type.VarInt
                    })
            }
        }
    }

    data class FloatLiteral(val value: Double, val isDouble: Boolean) : Token() {
        companion object {
            fun ofInfinity(sign: String, width: String) = FloatLiteral(
                if (sign == "-") Double.NEGATIVE_INFINITY else Double.POSITIVE_INFINITY,
                width == "64"
            )

            fun of(sign: String, decimal: String, hex: String, width: String) = FloatLiteral(
                (if (decimal.isNotEmpty()) decimal.toDouble() else parseHex(hex)).let {
                    if (sign == "-") -it else it
                },
                width != "32"
            )

            private fun parseHex(hexString: String): Double {
                val regex = Regex("""0x([\dA-Fa-f]+)\.([\dA-Fa-f]*)([pP]([+-]?\d+))?""")
                val match =
                    regex.matchEntire(hexString) ?: error("Invalid hexadecimal floating-point format: $hexString")
                val (wholePart, fractionalPart, _, exponentPart) = match.destructured
                val wholeValue = wholePart.toLong(radix = 16).toDouble()
                val fractionalValue = fractionalPart.toIntOrNull(radix = 16)?.toDouble() ?: 0.0
                val exponent = exponentPart.ifEmpty { "0" }.toInt()
                val normalizedFraction = fractionalValue / 2.0.pow(fractionalPart.length * 4.0)
                return (wholeValue + normalizedFraction) * 2.0.pow(exponent.toDouble())
            }
        }
    }

    data class BytesLiteral(val bytes: ByteArray) : Token() {
        companion object {
            fun of(text: String) = BytesLiteral(text.chunked(2).map { it.toInt(16).toByte() }.toByteArray())
        }

        override fun equals(other: Any?): Boolean = bytes.contentEquals((other as? BytesLiteral)?.bytes)
        override fun hashCode() = bytes.contentHashCode()
    }

    data class LongForm(val extraBytes: Int): Token() {
        companion object {
            fun of(extraBytes: String) = LongForm(extraBytes.toInt())
        }
    }
}

enum class TokenType(val regex: Regex, val getToken: (groupValues: List<String>) -> Token? = { null }) {
    Whitespace(Regex("""\s+""")),
    Comment(Regex("""#.*?(\R|$)""")),
    OpenBrace(Regex("""\{"""), { Token.OpenBrace }),
    OpenGroupBrace(Regex("""!\{"""), { Token.OpenGroupBrace }),
    CloseBrace(Regex("}"), { Token.CloseBrace }),
    Tag(
        Regex("""((\d+)|(0x([\da-fA-F]+))):(VARINT|I64|LEN|SGROUP|EGROUP|I32|\d)?"""),
        { Token.Tag.of(it[2], it[4], it[5]) }
    ),
    StringLiteral(Regex(""""((?:[^"\\]|\\\\|\\"|\\x\d\d|\\\d\d\d|\\n|\R)*)""""), { Token.StringLiteral.of(it[1]) }),
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
