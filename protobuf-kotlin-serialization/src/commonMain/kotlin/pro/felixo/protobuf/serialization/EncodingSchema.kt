package pro.felixo.protobuf.serialization

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import pro.felixo.protobuf.EnumValue
import pro.felixo.protobuf.FieldNumber
import pro.felixo.protobuf.FieldRule
import pro.felixo.protobuf.Identifier
import pro.felixo.protobuf.serialization.encoding.FieldEncoding
import pro.felixo.protobuf.serialization.encoding.HybridDecoder
import pro.felixo.protobuf.serialization.encoding.HybridEncoder
import pro.felixo.protobuf.serialization.encoding.varInt
import pro.felixo.protobuf.serialization.generation.encodingSchema
import pro.felixo.protobuf.serialization.util.simpleTypeName
import pro.felixo.protobuf.wire.WireBuffer
import pro.felixo.protobuf.wire.WireValue

/**
 * A [BinaryFormat] for encoding and decoding protobuf messages. Applications should obtain instances of this class
 * using the [encodingSchema] function.
 */
class EncodingSchema(
    override val serializersModule: SerializersModule,
    val types: Map<String, Type>
) : BinaryFormat {

    override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
        val output = WireBuffer()
        val simpleTypeName = simpleTypeName(serializer.descriptor)
        val message = types[simpleTypeName] as? Message ?: error("Not a message type: $simpleTypeName")
        val encoder = message.encoder(output, null, true)
        serializer.serialize(encoder, value)
        return output.getBytes()
    }

    override fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        val simpleTypeName = simpleTypeName(deserializer.descriptor)
        val message = types[simpleTypeName] as? Message ?: error("Not a message type: $simpleTypeName")
        val decoder = message.decoder(listOf(WireValue.Len(WireBuffer(bytes))))
        return deserializer.deserialize(decoder)
    }
}

sealed class Type {
    abstract val name: Identifier
}

class Message(
    override val name: Identifier,
    val members: List<Member> = emptyList(),
    val nestedTypes: List<Type> = emptyList(),
    val encoder: (output: WireBuffer, fieldNumber: FieldNumber?, encodeZeroValue: Boolean) -> HybridEncoder,
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
    val encoding: FieldEncoding,
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
    fun encode(elementIndex: Int, encodeZeroValue: Boolean): WireValue.VarInt? = varInt(
        numberByElementIndex[elementIndex],
        encodeZeroValue
    )
}
