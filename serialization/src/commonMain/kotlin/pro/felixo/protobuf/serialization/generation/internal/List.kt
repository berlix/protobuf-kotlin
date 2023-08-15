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
import pro.felixo.protobuf.serialization.encoding.ListDecoder
import pro.felixo.protobuf.serialization.encoding.ListEncoder
import pro.felixo.protobuf.serialization.encoding.SyntheticDecoder
import pro.felixo.protobuf.serialization.encoding.SyntheticEncoder
import pro.felixo.protobuf.wire.WireBuffer
import pro.felixo.protobuf.wire.WireValue

fun TypeContext.optionalListField(
    descriptor: SerialDescriptor,
    name: Identifier,
    number: FieldNumber
): Field {
    val field = listField(Identifier("list"), FieldNumber(1), descriptor.actual)
    return Field(
        name,
        syntheticMessage(Identifier("${name.value.replaceFirstChar { it.uppercase() }}Value")) {
            field
        },
        number,
        FieldRule.Optional,
        { SyntheticEncoder(serializersModule, it, field, number) },
        { SyntheticDecoder(serializersModule, it, field) }
    )
}

fun TypeContext.listField(
    name: Identifier,
    number: FieldNumber,
    descriptor: SerialDescriptor
): Field {
    val elementDescriptor = descriptor.getElementDescriptor(0).actual
    val elementAnnotations = descriptor.getElementAnnotations(0)
    val syntheticMessageName = Identifier("${name.value.replaceFirstChar { it.uppercase() }}Item")

    return repeatedField(name, number, elementDescriptor, elementAnnotations, syntheticMessageName)
}

private fun TypeContext.repeatedField(
    name: Identifier,
    number: FieldNumber,
    descriptor: SerialDescriptor,
    annotations: List<Annotation>,
    syntheticMessageName: Identifier
): Field = if (descriptor.isNullable)
    syntheticRepeatedField(name, number, syntheticMessageName, annotations, descriptor)
else
    when (val kind = descriptor.kind) {
        is PrimitiveKind -> naturalRepeatedField(name, number, scalar(annotations, kind))
        SerialKind.CONTEXTUAL -> repeatedField(
            name,
            number,
            serializersModule.getContextualDescriptor(descriptor)
                ?: error("No contextual serializer found for ${descriptor.serialName}"),
            annotations,
            syntheticMessageName
        )
        StructureKind.LIST -> if (descriptor.actual.getElementDescriptor(0).kind == PrimitiveKind.BYTE)
            naturalRepeatedField(name, number, FieldEncoding.Bytes)
        else
            syntheticRepeatedField(name, number, syntheticMessageName, annotations, descriptor)
        StructureKind.MAP -> syntheticRepeatedField(name, number, syntheticMessageName, annotations, descriptor)
        StructureKind.CLASS, StructureKind.OBJECT, SerialKind.ENUM, is PolymorphicKind ->
            naturalRepeatedField(name, number, root.namedType(descriptor))
    }

private fun TypeContext.naturalRepeatedField(
    name: Identifier,
    number: FieldNumber,
    type: FieldEncoding
): Field {
    val elementEncoder = { output: WireBuffer ->
        type.encoder(
            serializersModule,
            number.takeIf { !type.isPackable },
            output,
            encodeZeroValue = true
        )
    }
    val elementDecoder = { values: List<WireValue> -> type.decoder(serializersModule, values) }
    return Field(
        name,
        type,
        number,
        FieldRule.Repeated,
        { ListEncoder(serializersModule, number, type.isPackable, it, elementEncoder) },
        { ListDecoder(serializersModule, type, it, elementDecoder) }
    )
}

private fun TypeContext.syntheticRepeatedField(
    name: Identifier,
    number: FieldNumber,
    syntheticMessageName: Identifier,
    annotations: List<Annotation>,
    descriptor: SerialDescriptor
): Field {
    lateinit var innerField: Field

    val syntheticType = syntheticMessage(syntheticMessageName) {
        val field = field(
            Identifier("value"),
            FieldNumber(1),
            annotations,
            descriptor
        )

        innerField = field
        field
    }

    val elementEncoder = { output: WireBuffer ->
        SyntheticEncoder(serializersModule, output, innerField, number)
    }

    val elementDecoder = { values: List<WireValue> ->
        SyntheticDecoder(serializersModule, values, innerField)
    }

    return Field(
        name,
        syntheticType,
        number,
        FieldRule.Repeated,
        { ListEncoder(serializersModule, number, false, it, elementEncoder) },
        { ListDecoder(serializersModule, syntheticType, it, elementDecoder) }
    )
}
