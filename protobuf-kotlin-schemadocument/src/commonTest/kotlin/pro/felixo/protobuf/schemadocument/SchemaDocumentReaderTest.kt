package pro.felixo.protobuf.schemadocument

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.kotest.core.spec.style.StringSpec

class SchemaDocumentReaderTest : StringSpec({
    "reads schema" {
        assertThat(SchemaDocumentReader().readSchema(MAXIMAL_SCHEMA_TEXT)).isEqualTo(MAXIMAL_SCHEMA)
    }
})
