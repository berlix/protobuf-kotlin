@file:OptIn(ExperimentalSerializationApi::class)

package pro.felixo.protobuf.serialization.generation.internal

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.descriptors.getPolymorphicDescriptors
import pro.felixo.protobuf.FieldNumber
import pro.felixo.protobuf.Identifier
import pro.felixo.protobuf.serialization.Field
import pro.felixo.protobuf.serialization.Message
import pro.felixo.protobuf.serialization.OneOf
import pro.felixo.protobuf.serialization.ProtoNumber
import pro.felixo.protobuf.serialization.encoding.FieldEncoding
import pro.felixo.protobuf.serialization.encoding.PolymorphicDecoder
import pro.felixo.protobuf.serialization.encoding.PolymorphicEncoder
import pro.felixo.protobuf.serialization.util.FieldNumberIterator
import pro.felixo.protobuf.serialization.util.requireNoDuplicates
import pro.felixo.protobuf.serialization.util.simpleTypeName

fun TypeContext.messageOfOpenPolymorphicClass(descriptor: SerialDescriptor) =
    messageOfPolymorphicClass(
        descriptor,
        serializersModule.getPolymorphicDescriptors(descriptor).sortedBy { it.serialName }
    )

fun TypeContext.messageOfSealedPolymorphicClass(descriptor: SerialDescriptor) =
    messageOfPolymorphicClass(descriptor, descriptor.elementDescriptors.drop(1).first().elementDescriptors)

private fun TypeContext.messageOfPolymorphicClass(
    descriptor: SerialDescriptor,
    subTypes: Iterable<SerialDescriptor>
): FieldEncoding.MessageReference = putOrGetMessage(descriptor) {
    typeContext {
        val numberIterator = fieldNumberIteratorFromSubTypes(subTypes)

        val fields = subTypes.map { it to fieldForSubType(it, numberIterator) }

        Message(
            Identifier(simpleTypeName(descriptor)),
            listOf(
                OneOf(
                    Identifier("subtypes"),
                    fields.map { it.second }
                )
            ),
            encoder = { output, fieldNumber, _ ->
                PolymorphicEncoder(
                    serializersModule,
                    fields.toMap(),
                    fieldNumber,
                    output
                )
            },
            decoder = { values ->
                PolymorphicDecoder(serializersModule, fields.associateBy { it.second.number }, values)
            }
        )
    }
}

private fun TypeContext.fieldForSubType(subDescriptor: SerialDescriptor, numberIterator: FieldNumberIterator): Field =
    field(
        Identifier(simpleTypeName(subDescriptor).replaceFirstChar { it.lowercaseChar() }),
        FieldNumber(
            subDescriptor.annotations.filterIsInstance<ProtoNumber>()
                .firstOrNull()?.number
                ?: numberIterator.next()
        ),
        emptyList(),
        subDescriptor,
        forceEncodeZeroValue = true
    )

private fun fieldNumberIteratorFromSubTypes(descriptors: Iterable<SerialDescriptor>): FieldNumberIterator =
    FieldNumberIterator(
        descriptors.mapNotNull {
            it.annotations.filterIsInstance<ProtoNumber>().firstOrNull()?.number
        }.requireNoDuplicates { duplicatedNumber ->
            "Duplicate field number $duplicatedNumber in sub-types: ${descriptors.map { it.serialName }}"
        }
    )
