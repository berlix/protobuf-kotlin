package pro.felixo.protobuf.protoscope

import pro.felixo.protobuf.util.PeekableIterator
import pro.felixo.protobuf.wire.Tag
import pro.felixo.protobuf.wire.WireBuffer
import pro.felixo.protobuf.wire.WireType
import pro.felixo.protobuf.wire.encodeSInt64

/**
 * Converts Protoscope code to its binary representation. Note that the reverse is not currently supported.
 */
class ProtoscopeConverter(private val tokenizer: ProtoscopeTokenizer = ProtoscopeTokenizer()) {
    private var currentLongForm: Token.LongForm? = null

    /**
     * Parses the given [protoscope] code and returns its corresponding binary representation.
     *
     * Maintains internal state in this instance of [ProtoscopeConverter] and must therefore not be used concurrently.
     */
    fun convert(protoscope: String): ByteArray {
        currentLongForm = null
        val result = convertBlock(PeekableIterator(tokenizer.tokenize(protoscope).iterator()), expectCloseBrace = false)
        check(currentLongForm == null) { "Unmatched long-form token encountered at end of input." }
        return result.getBytes()
    }

    private fun convertBlock(tokens: PeekableIterator<Token>, expectCloseBrace: Boolean): WireBuffer {
        val out = WireBuffer()
        while (tokens.hasNext())
            when (val token = tokens.expectToken()) {
                is Token.BooleanLiteral -> out.writeVarInt(if (token.value) 1 else 0, popExtraBytes())
                is Token.BytesLiteral -> {
                    check(currentLongForm == null) {
                        "Long-form cannot be applied to bytes literals - missing an open brace?"
                    }
                    out.write(token.bytes)
                }
                Token.CloseBrace -> {
                    check(expectCloseBrace) { "Unexpected closing brace." }
                    return out
                }
                Token.OpenBrace -> {
                    val extraBytes = popExtraBytes()
                    val bytes = convertBlock(tokens, expectCloseBrace = true)
                    check(currentLongForm == null) {
                        "Long-form cannot be applied to the end of LEN groups."
                    }
                    out.writeVarInt(bytes.length, extraBytes)
                    out.writeAndConsume(bytes)
                }
                Token.OpenGroupBrace -> {
                    error("Unexpected open group brace that does not follow a tag expression.")
                }
                is Token.StringLiteral -> {
                    check(currentLongForm == null) {
                        "Long-form cannot be applied to string literals - missing an open brace?"
                    }
                    out.write(token.text.encodeToByteArray())
                }
                is Token.FloatLiteral -> {
                    check(currentLongForm == null) { "Long-form cannot be applied to floating-point numbers." }
                    if (token.isDouble)
                        out.writeFixed64(token.value.toRawBits())
                    else {
                        val value = token.value.toFloat().toRawBits()
                        out.writeFixed32(value)
                    }
                }
                is Token.IntegerLiteral -> when (token.type) {
                    Token.IntegerLiteral.Type.Fixed32 -> {
                        check(currentLongForm == null) { "Long-form cannot be applied to fixed32." }
                        out.writeFixed32(token.value.toInt())
                    }
                    Token.IntegerLiteral.Type.Fixed64 -> {
                        check(currentLongForm == null) { "Long-form cannot be applied to fixed64." }
                        out.writeFixed64(token.value)
                    }
                    Token.IntegerLiteral.Type.VarInt -> out.writeVarInt(token.value, popExtraBytes())
                    Token.IntegerLiteral.Type.SVarInt -> out.writeVarInt(token.value.encodeSInt64(), popExtraBytes())
                }
                is Token.Tag -> {
                    var inferredType: Int? = null
                    out.writeVarInt(
                        Tag.of(token.number, token.type ?: inferType(tokens).also { inferredType = it }).value,
                        popExtraBytes()
                    )
                    if (inferredType == WireType.SGroup.value) {
                        check(tokens.expectToken() is Token.OpenGroupBrace) {
                            "Expected open group brace after inferred SGroup tag."
                        }
                        out.writeAndConsume(convertBlock(tokens, expectCloseBrace = true))
                        out.writeVarInt(
                            Tag.of(token.number, WireType.EGroup.value).value,
                            popExtraBytes()
                        )
                    }
                }
                is Token.LongForm -> {
                    check(currentLongForm == null) { "Two consecutive long-form tags are not permitted." }
                    currentLongForm = token
                }
            }
        if (expectCloseBrace)
            error("Unexpected end of input; missing closing brace.")
        return out
    }

    private fun popExtraBytes(): Int = (currentLongForm?.extraBytes ?: 0).also { currentLongForm = null }

    private fun inferType(tokens: PeekableIterator<Token>): Int =
        when (val token = tokens.peek()) {
            is Token.BooleanLiteral -> WireType.VarInt.value
            is Token.BytesLiteral -> error("Unexpected token: bytes after typeless tag - missing an open brace?")
            Token.CloseBrace -> error("Unexpected token: close brace after typeless tag - missing an open brace?")
            is Token.FloatLiteral -> if (token.isDouble) WireType.Fixed64.value else WireType.Fixed32.value
            is Token.IntegerLiteral -> when (token.type) {
                Token.IntegerLiteral.Type.Fixed32 -> WireType.Fixed32.value
                Token.IntegerLiteral.Type.Fixed64 -> WireType.Fixed64.value
                Token.IntegerLiteral.Type.VarInt -> WireType.VarInt.value
                Token.IntegerLiteral.Type.SVarInt -> WireType.VarInt.value
            }
            Token.OpenBrace -> WireType.Len.value
            Token.OpenGroupBrace -> WireType.SGroup.value
            is Token.StringLiteral -> error("Unexpected token: string after typeless tag - missing an open brace?")
            is Token.Tag -> error("Unexpected token: tag after typeless tag - missing an open brace?")
            is Token.LongForm -> inferType(tokens)
            null -> error("Unexpected end of input after typeless tag")
        }

    private fun Iterator<Token>.expectToken() = if (hasNext()) next() else error("Unexpected end of input")
}
