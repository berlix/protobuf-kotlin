@file:OptIn(ExperimentalSerializationApi::class)

package pro.felixo.protobuf.serialization.generation.internal

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.getContextualDescriptor
import pro.felixo.protobuf.FieldNumber
import pro.felixo.protobuf.FieldRule
import pro.felixo.protobuf.Identifier
import pro.felixo.protobuf.serialization.Field
import pro.felixo.protobuf.serialization.encoding.FieldEncoding

fun TypeContext.namedType(descriptor: SerialDescriptor): FieldEncoding.Reference<*> = when (descriptor.kind) {
    PolymorphicKind.OPEN -> messageOfOpenPolymorphicClass(descriptor)
    PolymorphicKind.SEALED -> messageOfSealedPolymorphicClass(descriptor)
    SerialKind.CONTEXTUAL -> namedType(
        serializersModule.getContextualDescriptor(descriptor)
            ?: error("No contextual serializer found for ${descriptor.serialName}")
    )
    SerialKind.ENUM -> enum(descriptor)
    StructureKind.CLASS -> messageOfClass(descriptor)
    StructureKind.OBJECT -> messageOfObject(descriptor)
    StructureKind.LIST -> error("Cannot create named type for StructureKind.LIST (${descriptor.serialName})")
    StructureKind.MAP -> error("Cannot create named type for StructureKind.MAP (${descriptor.serialName})")
    is PrimitiveKind -> error("Cannot create named type for PrimitiveKind.* (${descriptor.serialName})")
}

fun TypeContext.field(
    name: Identifier,
    number: FieldNumber,
    annotations: List<Annotation>,
    descriptor: SerialDescriptor,
    forceEncodeZeroValue: Boolean = false
): Field {
    val typeDescriptor = descriptor.actual
    val nullable = descriptor.isNullable || typeDescriptor.isNullable
    return when (val kind = typeDescriptor.kind) {
        is PrimitiveKind ->
            field(name, scalar(annotations, kind), number, nullable, forceEncodeZeroValue)
        StructureKind.CLASS, StructureKind.OBJECT, SerialKind.ENUM, is PolymorphicKind ->
            field(name, root.namedType(typeDescriptor), number, nullable, forceEncodeZeroValue)
        SerialKind.CONTEXTUAL ->
            field(
                name,
                number,
                annotations,
                serializersModule.getContextualDescriptor(typeDescriptor)
                    ?: error("No contextual serializer found for ${typeDescriptor.serialName}")
            )
        StructureKind.LIST ->
            if (typeDescriptor.getElementDescriptor(0).kind == PrimitiveKind.BYTE)
                field(name, FieldEncoding.Bytes, number, nullable, forceEncodeZeroValue)
            else if (nullable)
                optionalListField(typeDescriptor, name, number, annotations)
            else
                listField(name, number, typeDescriptor.actual, annotations)
        StructureKind.MAP ->
            if (nullable)
                optionalMapField(name, annotations, typeDescriptor, number)
            else
                mapField(name, number, annotations, typeDescriptor.actual)
    }
}

private fun TypeContext.field(
    name: Identifier,
    type: FieldEncoding,
    number: FieldNumber,
    optional: Boolean,
    encodeZeroValue: Boolean
): Field = Field(
    name,
    type,
    number,
    if (optional) FieldRule.Optional else FieldRule.Singular,
    fieldEncoder(type, number, encodeZeroValue || optional),
    fieldDecoder(type)
)
