@file:OptIn(ExperimentalSerializationApi::class)

package pro.felixo.protobuf.serialization.generation.internal

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import pro.felixo.protobuf.FieldNumber
import pro.felixo.protobuf.FieldRule
import pro.felixo.protobuf.Identifier
import pro.felixo.protobuf.serialization.Field
import pro.felixo.protobuf.serialization.Message
import pro.felixo.protobuf.serialization.ProtoIntegerType
import pro.felixo.protobuf.serialization.ProtoMapEntry
import pro.felixo.protobuf.serialization.encoding.MapDecoder
import pro.felixo.protobuf.serialization.encoding.MapEncoder
import pro.felixo.protobuf.serialization.encoding.SyntheticDecoder
import pro.felixo.protobuf.serialization.encoding.SyntheticEncoder

fun TypeContext.optionalMapField(
    name: Identifier,
    annotations: List<Annotation>,
    descriptor: SerialDescriptor,
    number: FieldNumber
): Field {
    lateinit var field: Field
    return Field(
        name,
        syntheticMessage(Identifier("${name.value.replaceFirstChar { it.uppercase() }}Value")) {
            mapField(Identifier("map"), FieldNumber(1), annotations, descriptor.actual)
                .also { field = it }
        },
        number,
        FieldRule.Optional,
        { SyntheticEncoder(serializersModule, it, field, number) },
        { SyntheticDecoder(serializersModule, it, field) }
    )
}

fun TypeContext.mapField(
    name: Identifier,
    number: FieldNumber,
    annotations: List<Annotation>,
    descriptor: SerialDescriptor
): Field {
    val mapEntryAnnotation = annotations.filterIsInstance<ProtoMapEntry>().firstOrNull() ?: ProtoMapEntry()

    val entryMessageName = mapEntryAnnotation.messageName.takeIf { it.isNotEmpty() }
        ?: "${name.value.replaceFirstChar { it.uppercase() }}Entry"

    lateinit var keyField: Field
    lateinit var valueField: Field

    val entryMessage = putOrGetMessage(name = entryMessageName) {
        typeContext {
            Message(
                Identifier(entryMessageName),
                listOf(
                    field(
                        Identifier(mapEntryAnnotation.keyName),
                        FieldNumber(mapEntryAnnotation.keyNumber),
                        descriptor.getElementAnnotations(0) +
                            ProtoIntegerType(mapEntryAnnotation.keyIntegerType),
                        descriptor.getElementDescriptor(0)
                    ).also { keyField = it },
                    field(
                        Identifier(mapEntryAnnotation.valueName),
                        FieldNumber(mapEntryAnnotation.valueNumber),
                        descriptor.getElementAnnotations(1) +
                            ProtoIntegerType(mapEntryAnnotation.valueIntegerType),
                        descriptor.getElementDescriptor(1)
                    ).also { valueField = it },
                ),
                localTypes,
                encoder = { _, _, _ -> error("Map entry messages don't have encoders") },
                decoder = { error("Map entry messages don't have decoders") }
            )
        }
    }
    return Field(
        name,
        entryMessage,
        number,
        FieldRule.Repeated,
        encoder = { MapEncoder(serializersModule, number, keyField, valueField, it) },
        decoder = { MapDecoder(serializersModule, keyField, valueField, it) }
    )
}
