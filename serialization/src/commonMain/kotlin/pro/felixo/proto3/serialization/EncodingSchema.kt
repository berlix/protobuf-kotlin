package pro.felixo.proto3.serialization

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import pro.felixo.proto3.EnumValue
import pro.felixo.proto3.FieldNumber
import pro.felixo.proto3.FieldRule
import pro.felixo.proto3.Identifier
import pro.felixo.proto3.serialization.encoding.HybridDecoder
import pro.felixo.proto3.serialization.encoding.HybridEncoder
import pro.felixo.proto3.serialization.encoding.FieldEncoding
import pro.felixo.proto3.serialization.generation.SchemaGenerator
import pro.felixo.proto3.serialization.util.simpleTypeName
import pro.felixo.proto3.wire.WireBuffer
import pro.felixo.proto3.wire.WireValue
import kotlin.reflect.KType

class EncodingSchema internal constructor(
    override val serializersModule: SerializersModule,
    val types: Map<String, Type>
) : BinaryFormat {

    override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
        val output = WireBuffer()
        val simpleTypeName = simpleTypeName(serializer.descriptor)
        val message = types[simpleTypeName] as? Message ?: error("Not a message type: $simpleTypeName")
        val encoder = message.encoder(output, true)
        serializer.serialize(encoder, value)
        return output.getBytes()
    }

    override fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        val simpleTypeName = simpleTypeName(deserializer.descriptor)
        val message = types[simpleTypeName] as? Message ?: error("Not a message type: $simpleTypeName")
        val decoder = message.decoder(listOf(WireValue.Len(WireBuffer(bytes))))
        return deserializer.deserialize(decoder)
    }

    companion object {
        fun of(
            descriptors: List<SerialDescriptor> = emptyList(),
            typesFromSerializersModule: List<KType> = emptyList(),
            serializersModule: SerializersModule = EmptySerializersModule(),
        ): EncodingSchema {
            val schemaGenerator = SchemaGenerator(serializersModule)
            descriptors.forEach { schemaGenerator.add(it) }
            typesFromSerializersModule.forEach { schemaGenerator.addFromSerializersModule(it) }
            return schemaGenerator.schema()
        }
    }
}

sealed class Type {
    abstract val name: Identifier
}

class Message(
    override val name: Identifier,
    val members: List<Member> = emptyList(),
    val nestedTypes: List<Type> = emptyList(),
    val encoder: (output: WireBuffer, isStandalone: Boolean) -> HybridEncoder,
    val decoder: (value: List<WireValue>) -> HybridDecoder
) : Type() {
    val fields: List<Field> =
        members.filterIsInstance<Field>() + members.filterIsInstance<OneOf>().flatMap { it.fields }
}

sealed interface Member {
    val name: Identifier
}

class Field(
    override val name: Identifier,
    val type: FieldEncoding,
    val number: FieldNumber,
    val rule: FieldRule = FieldRule.Singular,
    val encoder: ((WireBuffer) -> Encoder),
    val decoder: ((List<WireValue>) -> Decoder)
) : Member

class OneOf(
    override val name: Identifier,
    val fields: List<Field>
) : Member

class Enum(
    override val name: Identifier,
    val values: List<EnumValue>
) : Type() {
    private val numberByElementIndex: List<Int> by lazy { values.map { it.number } }
    private val elementIndexByNumber = numberByElementIndex.withIndex().associate { it.value to it.index }
    private val defaultElementIndex: Int = numberByElementIndex.indexOf(0)

    fun decode(number: Int): Int = elementIndexByNumber[number] ?: defaultElementIndex
    fun encode(elementIndex: Int): Int = numberByElementIndex[elementIndex]
}
