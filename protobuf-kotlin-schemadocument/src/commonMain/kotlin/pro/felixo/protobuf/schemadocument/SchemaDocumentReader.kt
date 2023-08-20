package pro.felixo.protobuf.schemadocument

import pro.felixo.protobuf.EnumValue
import pro.felixo.protobuf.FieldNumber
import pro.felixo.protobuf.FieldRule
import pro.felixo.protobuf.Identifier

/**
 * Reads .proto syntax into [SchemaDocument]s. Note that only a subset of the .proto syntax is supported, and only
 * the "proto3" syntax.
 */
class SchemaDocumentReader(private val tokenizer: SchemaDocumentTokenizer = SchemaDocumentTokenizer()) {
    fun readSchema(input: String): SchemaDocument {
        val tokens = tokenizer.tokenize(input).iterator()

        expectSyntaxDeclaration(tokens)

        var packageName: Token.Identifier? = null
        val types = mutableListOf<Type>()

        while (tokens.hasNext())
            when (val token = tokens.expectToken()) {
                Token.Identifier("package") ->
                    if (packageName == null)
                        packageName = readPackageName(tokens)
                    else
                        error("Duplicate package declaration")
                Token.Identifier("message") -> types.add(readMessage(tokens))
                Token.Identifier("enum") -> types.add(readEnum(tokens))
                else -> error("Unexpected token: $token")
            }
        return SchemaDocument(types)
    }

    private fun readPackageName(tokens: Iterator<Token>): Token.Identifier {
        val packageName = tokens.expect<Token.Identifier>()
        tokens.expect(Token.Semicolon)
        return packageName
    }

    private fun expectSyntaxDeclaration(tokens: Iterator<Token>) {
        tokens.expect(Token.Identifier("syntax"))
        tokens.expect(Token.Equals)
        tokens.expect(Token.StringLiteral("\"proto3\""))
        tokens.expect(Token.Semicolon)
    }

    private fun expectType(token: Token): FieldType = when (token) {
        is Token.Identifier ->
            SCALARS.firstOrNull {
                it.name == token.text
            }
                ?: FieldType.Reference(token.components.map { Identifier(it) })
        else -> error("Expected identifier, but got: $token")
    }

    private fun readMessage(tokens: Iterator<Token>): Message {
        val name = Identifier(tokens.expect<Token.Identifier>().text)
        tokens.expect<Token.OpenBrace>()

        val members = mutableListOf<Member>()
        val types = mutableListOf<Type>()
        val reservedNames = mutableListOf<Identifier>()
        val reservedNumbers = mutableListOf<IntRange>()
        while (true) {
            when (val token = tokens.expectToken()) {
                is Token.CloseBrace -> break
                Token.Identifier("reserved") -> putReserved(reservedNames, reservedNumbers, tokens)
                Token.Identifier("message") -> types.add(readMessage(tokens))
                Token.Identifier("enum") -> types.add(readEnum(tokens))
                is Token.Identifier -> members.putMember(token, tokens)
                else -> error("Unexpected token in message $name: $token")
            }
        }
        return Message(
            name,
            members,
            types,
            reservedNames,
            reservedNumbers
        )
    }

    private fun readEnum(tokens: Iterator<Token>): Enum {
        val name = Identifier(tokens.expect<Token.Identifier>().text)
        tokens.expect<Token.OpenBrace>()

        val values = mutableListOf<EnumValue>()
        val reservedNames = mutableListOf<Identifier>()
        val reservedNumbers = mutableListOf<IntRange>()
        var allowAlias = false
        while (true) {
            when (val token = tokens.expectToken()) {
                is Token.CloseBrace -> break
                Token.Identifier("option") -> allowAlias = readAllowAliasOption(tokens)
                Token.Identifier("reserved") -> putReserved(reservedNames, reservedNumbers, tokens)
                is Token.Identifier -> values.putEnumValue(token, tokens)
                else -> error("Unexpected token in enum $name: $token")
            }
        }
        return Enum(
            name,
            values,
            allowAlias,
            reservedNames,
            reservedNumbers
        )
    }

    private fun readAllowAliasOption(tokens: Iterator<Token>): Boolean {
        tokens.expect(Token.Identifier("allow_alias"))
        tokens.expect(Token.Equals)
        return tokens.expectBoolean()
    }

    private fun Iterator<Token>.expectBoolean(): Boolean {
        val value = expect<Token.Identifier>()
        expect<Token.Semicolon>()
        return when (val text = value.text) {
            "true" -> true
            "false" -> false
            else -> error("Expected boolean, but got: $text")
        }
    }

    private fun MutableList<EnumValue>.putEnumValue(name: Token.Identifier, tokens: Iterator<Token>) {
        tokens.expect<Token.Equals>()
        val number = tokens.expect<Token.NumberLiteral>()
        tokens.expect<Token.Semicolon>()
        add(EnumValue(Identifier(name.text), number.value))
    }

    private fun MutableList<Member>.putMember(firstToken: Token.Identifier, tokens: Iterator<Token>) {
        when (firstToken) {
            Token.Identifier("oneof") -> add(readOneOf(tokens))
            else -> add(readField(firstToken, tokens))
        }
    }

    private fun readOneOf(tokens: Iterator<Token>): OneOf {
        val name = tokens.expect<Token.Identifier>()
        tokens.expect<Token.OpenBrace>()
        val fields = mutableListOf<Field>()
        while (true)
            when (val token = tokens.expectToken()) {
                is Token.CloseBrace -> break
                is Token.Identifier -> fields.add(readField(token, tokens))
                else -> error("Unexpected token in oneof $name: $token")
            }
        return OneOf(Identifier(name.text), fields)
    }

    private fun readField(firstToken: Token.Identifier, tokens: Iterator<Token>): Field {
        val rule = when (firstToken) {
            Token.Identifier("optional") -> FieldRule.Optional
            Token.Identifier("repeated") -> FieldRule.Repeated
            Token.Identifier("singular") -> FieldRule.Singular
            Token.Identifier("map") -> error("Map fields are not supported")
            else -> null
        }
        val type = expectType(if (rule != null) tokens.expect() else firstToken)
        val name = tokens.expect<Token.Identifier>()
        tokens.expect<Token.Equals>()
        val number = tokens.expect<Token.NumberLiteral>()
        tokens.expect<Token.Semicolon>()
        return Field(
            Identifier(name.text),
            type,
            FieldNumber(number.value),
            rule ?: FieldRule.Singular
        )
    }

    private fun putReserved(
        reservedNames: MutableList<Identifier>,
        reservedNumbers: MutableList<IntRange>,
        tokens: Iterator<Token>
    ) {
        do {
            val more = when (val token = tokens.expectToken()) {
                is Token.StringLiteral -> putReservedName(reservedNames, token, tokens)
                is Token.NumberLiteral -> putReservedNumber(tokens, reservedNumbers, token)
                else -> error("Unexpected token in reserved: $token")
            }
        } while (more)
    }

    private fun putReservedName(
        reservedNames: MutableList<Identifier>,
        token: Token.StringLiteral,
        tokens: Iterator<Token>
    ): Boolean {
        reservedNames.add(Identifier(token.value))
        return when (val secondToken = tokens.expectToken()) {
            is Token.Comma -> true
            is Token.Semicolon -> false
            else -> error("Unexpected token in reserved: $secondToken")
        }
    }

    private fun putReservedNumber(
        tokens: Iterator<Token>,
        reservedNumbers: MutableList<IntRange>,
        token: Token.NumberLiteral
    ): Boolean = when (val secondToken = tokens.expectToken()) {
        is Token.Comma -> {
            reservedNumbers.add(token.value..token.value)
            true
        }
        is Token.Semicolon -> {
            reservedNumbers.add(token.value..token.value)
            false
        }
        Token.Identifier("to") -> {
            when (val upperBound = tokens.expectToken()) {
                is Token.NumberLiteral -> reservedNumbers.add(token.value..upperBound.value)
                Token.Identifier("max") -> reservedNumbers.add(token.value..Int.MAX_VALUE)
                else -> error("Unexpected token in reserved: $upperBound")
            }
            when (val terminator = tokens.expectToken()) {
                is Token.Comma -> true
                is Token.Semicolon -> false
                else -> error("Unexpected token in reserved: $terminator")
            }
        }
        else -> error("Unexpected token in reserved: $secondToken")
    }

    private fun Iterator<Token>.expect(expected: Token): Token =
        expectToken().let { if (it == expected) it else error("Expected $expected, but got: $it") }

    private inline fun <reified T : Token> Iterator<Token>.expect(): T =
        expectToken().let { if (it is T) it else error("Expected ${T::class.simpleName}, but got: $it") }

    private fun Iterator<Token>.expectToken() = if (hasNext()) next() else error("Unexpected end of input")
}
