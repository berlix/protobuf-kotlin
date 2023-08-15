package pro.felixo.protobuf.serialization.encoding

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import pro.felixo.protobuf.FieldNumber
import pro.felixo.protobuf.serialization.Field
import pro.felixo.protobuf.wire.Tag
import pro.felixo.protobuf.wire.WireBuffer
import pro.felixo.protobuf.wire.WireType
import pro.felixo.protobuf.wire.WireValue
import pro.felixo.protobuf.wire.encodeField

@OptIn(ExperimentalSerializationApi::class)
class MessageEncoder(
    override val serializersModule: SerializersModule,
    private val fieldByElementIndex: List<Field>,
    private val fieldNumber: FieldNumber?,
    private val output: WireBuffer,
    private val encodeZeroValue: Boolean,
    private val encodeZeroValues: Boolean
) : HybridEncoder() {
    private val buffer = if (fieldNumber != null) WireBuffer() else output

    override fun endStructure(descriptor: SerialDescriptor) {
        if (fieldNumber != null && (encodeZeroValue || buffer.length > 0))
            output.encodeField(Tag.of(fieldNumber, WireType.Len), WireValue.Len(buffer))
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
        val type = field.encoding as FieldEncoding.Bool
        writeField(field, type.encode(value, encodeZeroValues))
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        val field = fieldByElementIndex[index]
        val type = field.encoding as FieldEncoding.Integer32
        writeField(field, type.encode(value.toInt(), encodeZeroValues))
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        val field = fieldByElementIndex[index]
        val type = field.encoding as FieldEncoding.Integer32
        writeField(field, type.encode(value.code, encodeZeroValues))
    }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        val field = fieldByElementIndex[index]
        val type = field.encoding as FieldEncoding.Double
        writeField(field, type.encode(value, encodeZeroValues))
    }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        val field = fieldByElementIndex[index]
        val type = field.encoding as FieldEncoding.Float
        writeField(field, type.encode(value, encodeZeroValues))
    }

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        val field = fieldByElementIndex[index]
        val type = field.encoding as FieldEncoding.Integer32
        writeField(field, type.encode(value, encodeZeroValues))
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        val field = fieldByElementIndex[index]
        val type = field.encoding as FieldEncoding.Integer64
        writeField(field, type.encode(value, encodeZeroValues))
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        val field = fieldByElementIndex[index]
        val type = field.encoding as FieldEncoding.Integer32
        writeField(field, type.encode(value.toInt(), encodeZeroValues))
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        val field = fieldByElementIndex[index]
        val type = field.encoding as FieldEncoding.String
        writeField(field, type.encode(value, encodeZeroValues))
    }

    private fun writeField(field: Field, value: WireValue?) {
        value?.let {
            buffer.encodeField(Tag.of(field.number, field.encoding.wireType), it)
        }
    }
}
