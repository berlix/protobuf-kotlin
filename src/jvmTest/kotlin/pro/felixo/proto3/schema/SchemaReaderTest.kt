package pro.felixo.proto3.schema

import assertk.assertThat
import assertk.assertions.isEqualTo
import pro.felixo.proto3.testutil.MAXIMAL_SCHEMA
import pro.felixo.proto3.testutil.MAXIMAL_SCHEMA_TEXT
import org.junit.Test

class SchemaReaderTest {
    @Test
    fun `reads schema`() =
        assertThat(SchemaReader().readSchema(MAXIMAL_SCHEMA_TEXT)).isEqualTo(MAXIMAL_SCHEMA)
}
