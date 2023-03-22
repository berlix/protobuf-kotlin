package pro.felixo.proto3.schema

import assertk.assertThat
import assertk.assertions.isEqualTo
import pro.felixo.proto3.testutil.MAXIMAL_SCHEMA
import pro.felixo.proto3.testutil.MAXIMAL_SCHEMA_TEXT
import kotlin.test.Test

class SchemaWriterTest {
    @Test
    fun `writes schema`() {
        val written = StringBuilder().also {
            SchemaWriter(it).write(MAXIMAL_SCHEMA)
        }.toString()
        assertThat(written.trim()).isEqualTo(MAXIMAL_SCHEMA_TEXT.trim())
    }
}
