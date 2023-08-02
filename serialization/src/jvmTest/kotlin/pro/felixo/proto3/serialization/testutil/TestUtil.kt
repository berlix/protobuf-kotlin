package pro.felixo.proto3.serialization.testutil

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import pro.felixo.proto3.schemadocument.SchemaDocumentReader
import pro.felixo.proto3.schemadocument.validation.ValidationResult
import pro.felixo.proto3.schemadocument.validation.validate

fun schemaOf(proto3: String) = SchemaDocumentReader().readSchema(
    "syntax=\"proto3\";$proto3"
).also {
    assertThat(validate(it)).isEqualTo(ValidationResult.OK)
}

/**
 * Adapted from kotlinx.serialization.internal.ListLikeDescriptor:
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 */
@ExperimentalSerializationApi
class ListDescriptor(override val serialName: String, val elementDescriptor: SerialDescriptor) : SerialDescriptor {
    override val kind: SerialKind get() = StructureKind.LIST
    override val elementsCount: Int = 1

    override fun getElementName(index: Int): String = index.toString()
    override fun getElementIndex(name: String): Int =
        name.toIntOrNull() ?: throw IllegalArgumentException("$name is not a valid list index")

    override fun isElementOptional(index: Int): Boolean {
        require(index >= 0) { "Illegal index $index, $serialName expects only non-negative indices"}
        return false
    }

    override fun getElementAnnotations(index: Int): List<Annotation> {
        require(index >= 0) { "Illegal index $index, $serialName expects only non-negative indices"}
        return emptyList()
    }

    override fun getElementDescriptor(index: Int): SerialDescriptor {
        require(index >= 0) { "Illegal index $index, $serialName expects only non-negative indices"}
        return elementDescriptor
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ListDescriptor) return false
        return elementDescriptor == other.elementDescriptor && serialName == other.serialName
    }

    override fun hashCode(): Int = elementDescriptor.hashCode() * 31 + serialName.hashCode()
    override fun toString(): String = "$serialName($elementDescriptor)"
}
