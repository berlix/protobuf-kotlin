package pro.felixo.protobuf.schemadocument.validation

import pro.felixo.protobuf.schemadocument.SchemaElement

data class ValidationScope(
    val element: SchemaElement,
    val parent: ValidationScope?
) {
    override fun toString(): String =
        (parent?.toString()?.let { "$it -> " } ?: "") + element.elementType + " " + element.elementName
}

fun ValidationScope.sub(element: SchemaElement) = ValidationScope(element, this)

fun <T> rootValidationScope(
    element: SchemaElement,
    block: ValidationScope.() -> T
): T = ValidationScope(element, null).block()

fun <T> ValidationScope.validationScope(
    element: SchemaElement,
    block: ValidationScope.() -> T
): T = sub(element).block()
