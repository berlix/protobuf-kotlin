package pro.felixo.protobuf.schemadocument

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.kotest.core.spec.style.StringSpec

class SchemaDocumentTokenizerTest : StringSpec({
    "parses schema" {
        val input = """
        syntax = "proto3"; // This is a single line comment
        package sample.package;

        /* This is a
           multi-line
           comment */
        message Person {
            string name = 1;
            // Single line comment
            int32 age=2;
        }
    """.trimIndent()

        assertThat(SchemaDocumentTokenizer().tokenize(input).toList()).isEqualTo(
            listOf(
                Token.Identifier("syntax"),
                Token.Equals,
                Token.StringLiteral("\"proto3\""),
                Token.Semicolon,
                Token.Identifier("package"),
                Token.Identifier("sample.package"),
                Token.Semicolon,
                Token.Identifier("message"),
                Token.Identifier("Person"),
                Token.OpenBrace,
                Token.Identifier("string"),
                Token.Identifier("name"),
                Token.Equals,
                Token.NumberLiteral("1"),
                Token.Semicolon,
                Token.Identifier("int32"),
                Token.Identifier("age"),
                Token.Equals,
                Token.NumberLiteral("2"),
                Token.Semicolon,
                Token.CloseBrace
            )
        )
    }
})
