package pro.felixo.protobuf.schemadocument.validation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isSameAs
import io.kotest.core.spec.style.StringSpec
import pro.felixo.protobuf.schemadocument.SchemaElement

class ValidationScopeTest : StringSpec({
    val rootElement = object : SchemaElement {
        override val elementType: String = "root"
        override val elementName: String = "rootName"
    }

    val nestedElement = object : SchemaElement {
        override val elementType: String = "nested"
        override val elementName: String = "nestedName"
    }

    "rootValidationScope creates a root scope" {
        lateinit var scope: ValidationScope
        rootValidationScope(rootElement) {
            scope = this
        }
        assertThat(scope.element).isSameAs(rootElement)
        assertThat(scope.parent).isNull()
        assertThat(scope.toString()).isEqualTo("root rootName")
    }

    "validationScope creates a nested scope" {
        lateinit var scope: ValidationScope
        rootValidationScope(rootElement) {
            validationScope(nestedElement) {
                scope = this@validationScope
            }
        }
        assertThat(scope.element).isSameAs(nestedElement)
        assertThat(scope.parent?.element).isSameAs(rootElement)
        assertThat(scope.toString()).isEqualTo("root rootName -> nested nestedName")
    }
})
