package pro.felixo.proto3.schemadocument.validation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isSameAs
import pro.felixo.proto3.schemadocument.SchemaElement
import kotlin.test.Test

class ValidationScopeTest {
    private val rootElement = object : SchemaElement {
        override val elementType: String = "root"
        override val elementName: String = "rootName"
    }

    private val nestedElement = object : SchemaElement {
        override val elementType: String = "nested"
        override val elementName: String = "nestedName"
    }

    @Test
    fun `rootValidationScope creates a root scope`() {
        lateinit var scope: ValidationScope
        rootValidationScope(rootElement) {
            scope = this
        }
        assertThat(scope.element).isSameAs(rootElement)
        assertThat(scope.parent).isNull()
        assertThat(scope.toString()).isEqualTo("root rootName")
    }

    @Test
    fun `validationScope creates a nested scope`() {
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
}
