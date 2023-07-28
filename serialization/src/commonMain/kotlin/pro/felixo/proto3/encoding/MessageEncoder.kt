package pro.felixo.proto3.encoding

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import pro.felixo.proto3.FieldType
import pro.felixo.proto3.SchemaGenerator
import pro.felixo.proto3.schema.Field
import pro.felixo.proto3.wire.Tag
import pro.felixo.proto3.wire.WireBuffer
import pro.felixo.proto3.wire.WireValue
import pro.felixo.proto3.wire.encodeField
import pro.felixo.proto3.wire.encodeValue

@OptIn(ExperimentalSerializationApi::class)
class MessageEncoder(
    private val schemaGenerator: SchemaGenerator,
    private val fieldByElementIndex: List<Field>,
    private val isStandalone: Boolean,
    private val output: WireBuffer
) : CompositeEncoder {
    override val serializersModule: SerializersModule
        get() = schemaGenerator.serializersModule

    private val buffer = if (!isStandalone) WireBuffer() else output

    override fun endStructure(descriptor: SerialDescriptor) {
        if (!isStandalone)
            output.encodeValue(WireValue.Len(buffer))
    }

    @ExperimentalSerializationApi
    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?
    ) {
        if (value != null)
            encodeSerializableElement(descriptor, index, serializer, value)
    }

    // TODO optimisation: if serializer is kotlin.ByteArray and value is ByteArray and field is of type bytes,
    //      write bytes directly
    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) = serializer.serialize(fieldByElementIndex[index].encoder(buffer), value)

    @OptIn(ExperimentalSerializationApi::class)
    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder =
        fieldByElementIndex[index].encoder(buffer).encodeInline(descriptor.getElementDescriptor(index))

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        val field = fieldByElementIndex[index]
        val type = field.type as FieldType.Bool
        buffer.encodeField(Tag.of(field.number, type.wireType), type.encode(value))
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        val field = fieldByElementIndex[index]
        val type = field.type as FieldType.Integer32
        buffer.encodeField(Tag.of(field.number, type.wireType), type.encode(value.toInt()))
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        val field = fieldByElementIndex[index]
        val type = field.type as FieldType.Integer32
        buffer.encodeField(Tag.of(field.number, type.wireType), type.encode(value.code))
    }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        val field = fieldByElementIndex[index]
        val type = field.type as FieldType.Double
        buffer.encodeField(Tag.of(field.number, type.wireType), type.encode(value))
    }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        val field = fieldByElementIndex[index]
        val type = field.type as FieldType.Float
        buffer.encodeField(Tag.of(field.number, type.wireType), type.encode(value))
    }

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        val field = fieldByElementIndex[index]
        val type = field.type as FieldType.Integer32
        buffer.encodeField(Tag.of(field.number, type.wireType), type.encode(value))
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        val field = fieldByElementIndex[index]
        val type = field.type as FieldType.Integer64
        buffer.encodeField(Tag.of(field.number, type.wireType), type.encode(value))
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        val field = fieldByElementIndex[index]
        val type = field.type as FieldType.Integer32
        buffer.encodeField(Tag.of(field.number, type.wireType), type.encode(value.toInt()))
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        val field = fieldByElementIndex[index]
        val type = field.type as FieldType.String
        buffer.encodeField(Tag.of(field.number, type.wireType), type.encode(value))
    }
}
