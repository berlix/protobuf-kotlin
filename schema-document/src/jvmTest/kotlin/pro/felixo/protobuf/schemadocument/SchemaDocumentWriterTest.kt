package pro.felixo.protobuf.schemadocument

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class SchemaDocumentWriterTest {
    @Test
    fun `writes schema`() {
        val written = StringBuilder().also {
            SchemaDocumentWriter(it).write(MAXIMAL_SCHEMA)
        }.toString()
        assertThat(written.trim()).isEqualTo(MAXIMAL_SCHEMA_TEXT.trim())
    }
}
