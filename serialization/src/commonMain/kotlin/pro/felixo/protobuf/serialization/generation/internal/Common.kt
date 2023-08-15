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
    descriptor: SerialDescriptor
): Field =
    when (val kind = descriptor.actual.kind) {
        is PrimitiveKind ->
            field(descriptor, name, scalar(annotations, kind), number)
        StructureKind.CLASS, StructureKind.OBJECT, SerialKind.ENUM, is PolymorphicKind ->
            field(descriptor, name, root.namedType(descriptor), number)
        SerialKind.CONTEXTUAL ->
            field(
                name,
                number,
                annotations,
                serializersModule.getContextualDescriptor(descriptor)
                    ?: error("No contextual serializer found for ${descriptor.serialName}")
            )
        StructureKind.LIST ->
            if (descriptor.getElementDescriptor(0).kind == PrimitiveKind.BYTE)
                field(descriptor, name, FieldEncoding.Bytes, number)
            else if (descriptor.isNullable)
                optionalListField(descriptor, name, number)
            else
                listField(name, number, descriptor.actual)
        StructureKind.MAP ->
            if (descriptor.isNullable)
                optionalMapField(name, annotations, descriptor, number)
            else
                mapField(name, number, annotations, descriptor.actual)
    }

private fun TypeContext.field(
    descriptor: SerialDescriptor,
    name: Identifier,
    type: FieldEncoding,
    number: FieldNumber
): Field {
    val rule = descriptor.nullableToOptional()
    return Field(
        name,
        type,
        number,
        rule,
        fieldEncoder(type, number, rule != FieldRule.Singular),
        fieldDecoder(type)
    )
}
