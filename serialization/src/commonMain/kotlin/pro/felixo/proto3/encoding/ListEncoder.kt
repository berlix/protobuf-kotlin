package pro.felixo.proto3.encoding

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import pro.felixo.proto3.SchemaGenerator
import pro.felixo.proto3.schema.FieldNumber
import pro.felixo.proto3.wire.Tag
import pro.felixo.proto3.wire.WireInput
import pro.felixo.proto3.wire.WireOutput
import pro.felixo.proto3.wire.WireType
import pro.felixo.proto3.wire.WireValue
import pro.felixo.proto3.wire.encodeField

class ListEncoder(
    private val schemaGenerator: SchemaGenerator,
    private val fieldNumber: FieldNumber,
    private val packed: Boolean,
    private val output: WireOutput,
    private val elementEncoder: (WireOutput) -> Encoder
) : Encoder, CompositeEncoder {
    override val serializersModule: SerializersModule
        get() = schemaGenerator.serializersModule

    private val packedBuffer by lazy { WireOutput() }

    private fun getElementEncoder(): Encoder = if (packed)
        elementEncoder(packedBuffer)
    else
        elementEncoder(output)

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = this

    override fun endStructure(descriptor: SerialDescriptor) {
        if (packed && packedBuffer.length > 0)
            // TODO inefficient: perhaps we should merge WireInput and WireOutput into one class, so we don't need to
            //      getBytes()
            output.encodeField(Tag.of(fieldNumber, WireType.Len), WireValue.Len(WireInput(packedBuffer.getBytes())))
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

    override fun encodeBoolean(value: Boolean) = error("ListEncoder does not support encodeBoolean")
    override fun encodeByte(value: Byte) = error("ListEncoder does not support encodeByte")
    override fun encodeChar(value: Char) = error("ListEncoder does not support encodeChar")
    override fun encodeDouble(value: Double) = error("ListEncoder does not support encodeDouble")
    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) =
        error("ListEncoder does not support encodeEnum")
    override fun encodeFloat(value: Float) = error("ListEncoder does not support encodeFloat")
    override fun encodeInline(descriptor: SerialDescriptor): Encoder =
        error("ListEncoder does not support encodeInline")
    override fun encodeInt(value: Int) = error("ListEncoder does not support encodeInt")
    override fun encodeLong(value: Long) = error("ListEncoder does not support encodeLong")
    @ExperimentalSerializationApi override fun encodeNull() = error("ListEncoder does not support encodeNull")
    override fun encodeShort(value: Short) = error("ListEncoder does not support encodeShort")
    override fun encodeString(value: String) = error("ListEncoder does not support encodeString")
}
