package pro.felixo.proto3

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModule
import pro.felixo.proto3.schema.Message
import pro.felixo.proto3.wire.WireBuffer
import pro.felixo.proto3.wire.WireValue

class Proto3(
    override val serializersModule: SerializersModule
) : BinaryFormat {
    private val schemaGenerator = SchemaGenerator(serializersModule)

    override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
        val output = WireBuffer()
        val encoder = (schemaGenerator.add(serializer.descriptor).type as Message).encoder(output, true)
        serializer.serialize(encoder, value)
        return output.getBytes()
    }

    override fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        val decoder = (schemaGenerator.add(deserializer.descriptor).type as Message)
            .decoder(listOf(WireValue.Len(WireBuffer(bytes))))
        return deserializer.deserialize(decoder)
    }
}
