package pro.felixo.protobuf.serialization.generation.internal

import kotlinx.serialization.descriptors.SerialDescriptor
import pro.felixo.protobuf.Identifier
import pro.felixo.protobuf.serialization.Message
import pro.felixo.protobuf.serialization.encoding.FieldEncoding
import pro.felixo.protobuf.serialization.encoding.MessageDecoder
import pro.felixo.protobuf.serialization.encoding.MessageEncoder
import pro.felixo.protobuf.serialization.util.simpleTypeName

fun TypeContext.messageOfObject(descriptor: SerialDescriptor): FieldEncoding.MessageReference =
    putOrGetMessage(descriptor) {
        Message(
            Identifier(simpleTypeName(descriptor)),
            encoder =
            { output, isStandalone, encodeZeroValue ->
                MessageEncoder(
                    serializersModule,
                    emptyList(),
                    isStandalone,
                    output,
                    encodeZeroValue,
                    encodeZeroValues
                )
            },
            decoder = { MessageDecoder(serializersModule, emptyList(), it) }
        )
    }
