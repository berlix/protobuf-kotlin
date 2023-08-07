package pro.felixo.protobuf.serialization.testutil

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import pro.felixo.protobuf.schemadocument.SchemaDocumentReader
import pro.felixo.protobuf.schemadocument.validation.ValidationResult
import pro.felixo.protobuf.schemadocument.validation.validate

fun schemaOf(proto3: String) = SchemaDocumentReader().readSchema(
    "syntax=\"proto3\";$proto3"
).also {
    assertThat(validate(it)).isEqualTo(ValidationResult.OK)
}

@OptIn(ExperimentalSerializationApi::class)
data class ListDescriptor(override val serialName: String, val elementDescriptor: SerialDescriptor) : SerialDescriptor {
    override val kind: SerialKind get() = StructureKind.LIST
    override val elementsCount: Int = 1

    override fun getElementName(index: Int): String = index.toString()
    override fun getElementIndex(name: String): Int =
        requireNotNull(name.toIntOrNull()) { "$name is not a well-formed list index" }

    override fun isElementOptional(index: Int): Boolean = false
    override fun getElementAnnotations(index: Int): List<Annotation> = emptyList()

    override fun getElementDescriptor(index: Int): SerialDescriptor {
        require(index == 0) { "$serialName: expected index 0, got $index" }
        return elementDescriptor
    }

    override fun toString(): String = "$serialName($elementDescriptor)"
}
