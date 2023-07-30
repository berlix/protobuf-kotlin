package pro.felixo.proto3.schemadocument

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

class SchemaDocumentReaderTest {
    @Test
    fun `reads schema`() =
        assertThat(SchemaDocumentReader().readSchema(MAXIMAL_SCHEMA_TEXT)).isEqualTo(MAXIMAL_SCHEMA)
}
