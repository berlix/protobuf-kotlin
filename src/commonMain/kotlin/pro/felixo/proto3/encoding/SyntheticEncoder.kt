package pro.felixo.proto3.encoding

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import pro.felixo.proto3.SchemaGenerator
import pro.felixo.proto3.schema.Field
import pro.felixo.proto3.schema.FieldNumber
import pro.felixo.proto3.wire.Tag
import pro.felixo.proto3.wire.WireOutput
import pro.felixo.proto3.wire.WireType

class SyntheticEncoder(
    private val schemaGenerator: SchemaGenerator,
    private val output: WireOutput,
    private val field: Field,
    private val fieldNumber: FieldNumber
) : Encoder, CompositeEncoder {

    override val serializersModule: SerializersModule
        get() = schemaGenerator.serializersModule

    private val buffer = WireOutput()
    private val fieldEncoder by lazy { field.encoder(buffer) }
    private lateinit var fieldCompositeEncoder: CompositeEncoder

    override fun encodeBoolean(value: Boolean) = fieldEncoder.encodeBoolean(value).also { writeBuffer() }
    override fun encodeByte(value: Byte) = fieldEncoder.encodeByte(value).also { writeBuffer() }
    override fun encodeChar(value: Char) = fieldEncoder.encodeChar(value).also { writeBuffer() }
    override fun encodeDouble(value: Double) = fieldEncoder.encodeDouble(value).also { writeBuffer() }
    override fun encodeFloat(value: Float) = fieldEncoder.encodeFloat(value).also { writeBuffer() }
    override fun encodeInt(value: Int) = fieldEncoder.encodeInt(value).also { writeBuffer() }
    override fun encodeLong(value: Long) = fieldEncoder.encodeLong(value).also { writeBuffer() }
    override fun encodeShort(value: Short) = fieldEncoder.encodeShort(value).also { writeBuffer() }
    override fun encodeString(value: String) = fieldEncoder.encodeString(value).also { writeBuffer() }

    override fun encodeInline(descriptor: SerialDescriptor): Encoder = fieldEncoder.encodeInline(descriptor)
        .also { writeBuffer() }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) =
        fieldEncoder.encodeEnum(enumDescriptor, index).also { writeBuffer() }

    @ExperimentalSerializationApi
    override fun encodeNull() = fieldEncoder.encodeNull().also { writeBuffer() }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        fieldCompositeEncoder = fieldEncoder.beginStructure(descriptor)
        return this
    }

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) =
        fieldCompositeEncoder.encodeBooleanElement(descriptor, index, value)

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) =
        fieldCompositeEncoder.encodeByteElement(descriptor, index, value)

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) =
        fieldCompositeEncoder.encodeCharElement(descriptor, index, value)

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) =
        fieldCompositeEncoder.encodeDoubleElement(descriptor, index, value)

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) =
        fieldCompositeEncoder.encodeFloatElement(descriptor, index, value)

    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder =
        fieldCompositeEncoder.encodeInlineElement(descriptor, index)

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) =
        fieldCompositeEncoder.encodeIntElement(descriptor, index, value)

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) =
        fieldCompositeEncoder.encodeLongElement(descriptor, index, value)

    @ExperimentalSerializationApi
    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?
    ) = fieldCompositeEncoder.encodeNullableSerializableElement(descriptor, index, serializer, value)

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) = fieldCompositeEncoder.encodeSerializableElement(descriptor, index, serializer, value)

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) =
        fieldCompositeEncoder.encodeShortElement(descriptor, index, value)

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) =
        fieldCompositeEncoder.encodeStringElement(descriptor, index, value)

    override fun endStructure(descriptor: SerialDescriptor) =
        fieldCompositeEncoder.endStructure(descriptor).also { writeBuffer() }

    private fun writeBuffer() {
        output.writeVarInt(Tag.of(fieldNumber, WireType.Len).value)
        val bytes = buffer.getBytes()
        output.writeVarInt(bytes.size)
        output.write(bytes)
    }
}
