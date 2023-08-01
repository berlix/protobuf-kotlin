package pro.felixo.proto3.serialization.encoding

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import pro.felixo.proto3.FieldNumber
import pro.felixo.proto3.serialization.generation.SchemaGenerator
import pro.felixo.proto3.wire.Tag
import pro.felixo.proto3.wire.WireBuffer
import pro.felixo.proto3.wire.WireType
import pro.felixo.proto3.wire.WireValue
import pro.felixo.proto3.wire.encodeField

class ListEncoder(
    private val schemaGenerator: SchemaGenerator,
    private val fieldNumber: FieldNumber,
    private val packed: Boolean,
    private val output: WireBuffer,
    private val elementEncoder: (WireBuffer) -> Encoder
) : HybridEncoder() {
    override val serializersModule: SerializersModule
        get() = schemaGenerator.serializersModule

    private val packedBuffer by lazy { WireBuffer() }

    private fun getElementEncoder(): Encoder = if (packed)
        elementEncoder(packedBuffer)
    else
        elementEncoder(output)

    override fun endStructure(descriptor: SerialDescriptor) {
        if (packed && packedBuffer.length > 0)
            output.encodeField(Tag.of(fieldNumber, WireType.Len), WireValue.Len(packedBuffer))
    }

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) =
        getElementEncoder().encodeBoolean(value)

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) =
        getElementEncoder().encodeByte(value)

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) =
        getElementEncoder().encodeChar(value)

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) =
        getElementEncoder().encodeDouble(value)

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) =
        getElementEncoder().encodeFloat(value)

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) =
        getElementEncoder().encodeInt(value)

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) =
        getElementEncoder().encodeLong(value)

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) =
        getElementEncoder().encodeShort(value)

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) =
        getElementEncoder().encodeString(value)

    @ExperimentalSerializationApi
    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?
    ) {
        val encoder = getElementEncoder()

        if (value != null)
            serializer.serialize(encoder, value)
        else
            encoder.encodeNull()
    }

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) = serializer.serialize(getElementEncoder(), value)

    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder = getElementEncoder()
}
