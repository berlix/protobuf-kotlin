@file:OptIn(ExperimentalSerializationApi::class)

package pro.felixo.protobuf.serialization.generation.internal

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import pro.felixo.protobuf.FieldNumber
import pro.felixo.protobuf.Identifier
import pro.felixo.protobuf.serialization.Message
import pro.felixo.protobuf.serialization.ProtoNumber
import pro.felixo.protobuf.serialization.encoding.FieldEncoding
import pro.felixo.protobuf.serialization.encoding.MessageDecoder
import pro.felixo.protobuf.serialization.encoding.MessageEncoder
import pro.felixo.protobuf.serialization.util.FieldNumberIterator
import pro.felixo.protobuf.serialization.util.requireNoDuplicates
import pro.felixo.protobuf.serialization.util.simpleTypeName

fun TypeContext.messageOfClass(descriptor: SerialDescriptor): FieldEncoding.MessageReference =
    putOrGetMessage(descriptor) {
        typeContext {
            val numberIterator = fieldNumberIteratorFromClassElements(descriptor)

            val fields = (0 until descriptor.elementsCount).map {
                val number =
                    descriptor.getElementAnnotations(it).filterIsInstance<ProtoNumber>().firstOrNull()?.number
                        ?: numberIterator.next()
                val fieldName = descriptor.getElementName(it)
                field(
                    Identifier(fieldName),
                    FieldNumber(number),
                    descriptor.getElementAnnotations(it),
                    descriptor.getElementDescriptor(it)
                )
            }

            Message(
                Identifier(simpleTypeName(descriptor)),
                fields,
                localTypes,
                encoder =
                { output, fieldNumber, encodeZeroValue ->
                    MessageEncoder(
                        serializersModule,
                        fields,
                        fieldNumber,
                        output,
                        encodeZeroValue,
                        encodeZeroValues
                    )
                },
                decoder = { MessageDecoder(serializersModule, fields, it) }
            )
        }
    }

private fun fieldNumberIteratorFromClassElements(descriptor: SerialDescriptor) =
    FieldNumberIterator(
        (0 until descriptor.elementsCount).mapNotNull {
            descriptor.getElementAnnotations(it).filterIsInstance<ProtoNumber>().firstOrNull()?.number
        }.requireNoDuplicates { "Duplicate field number in descriptor ${descriptor.serialName}: $it" }
    )
