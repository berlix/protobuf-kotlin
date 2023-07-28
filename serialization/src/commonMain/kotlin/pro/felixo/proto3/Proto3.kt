package pro.felixo.proto3

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModule
import pro.felixo.proto3.encoding.ValueDecoder
import pro.felixo.proto3.encoding.ValueEncoder
import pro.felixo.proto3.schema.Identifier
import pro.felixo.proto3.wire.WireBuffer
import pro.felixo.proto3.wire.WireValue

class Proto3(
    override val serializersModule: SerializersModule
) : BinaryFormat {
    private val schemaGenerator = SchemaGenerator(serializersModule)

    override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
        val output = WireBuffer()
        val encoder = ValueEncoder(
            schemaGenerator,
            output,
            FieldType.Reference(listOf(Identifier("root")))
        )
        encoder.encodeSerializableValue(serializer, value)
        return output.getBytes()
    }

    override fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T =
        deserializer.deserialize(
            ValueDecoder(schemaGenerator, listOf(WireValue.Len(WireBuffer(bytes))), null)
        )
}
