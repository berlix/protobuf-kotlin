package pro.felixo.protobuf.serialization.generation.internal

import pro.felixo.protobuf.Identifier
import pro.felixo.protobuf.serialization.Field
import pro.felixo.protobuf.serialization.Message
import pro.felixo.protobuf.serialization.encoding.FieldEncoding

fun TypeContext.syntheticMessage(
    syntheticMessageName: Identifier,
    field: TypeContext.() -> Field
): FieldEncoding.MessageReference = putOrGetMessage(name = syntheticMessageName.value) {
    typeContext {
        Message(
            syntheticMessageName,
            listOf(field()),
            localTypes,
            encoder = { _, _, _ -> error("Synthetic messages don't have encoders") },
            decoder = { error("Synthetic messages don't have decoders") }
        )
    }
}
