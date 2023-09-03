package pro.felixo.protobuf.serialization.encoding

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import pro.felixo.protobuf.FieldNumber
import pro.felixo.protobuf.wire.Tag
import pro.felixo.protobuf.wire.WireBuffer
import pro.felixo.protobuf.wire.WireType
import pro.felixo.protobuf.wire.WireValue
import pro.felixo.protobuf.wire.encodeField

class ByteArrayEncoder(
    override val serializersModule: SerializersModule,
    private val output: WireBuffer,
    private val fieldNumber: FieldNumber,
    private val encodeZeroValue: Boolean
) : HybridEncoder() {

    private val buffer = WireBuffer()

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        require(index == buffer.length) { "Expected index ${buffer.length}, got $index" }
        buffer.writeByte(value)
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        if (encodeZeroValue || buffer.length > 0)
            output.encodeField(Tag.of(fieldNumber, WireType.Len), WireValue.Len(buffer))
    }

    @ExperimentalSerializationApi
    override fun encodeNull() {}

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) = error("Unsupported")
    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) = error("Unsupported")
    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) = error("Unsupported")
    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) = error("Unsupported")
    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder = error("Unsupported")
    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) = error("Unsupported")
    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) = error("Unsupported")
    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) = error("Unsupported")
    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) = error("Unsupported")

    @ExperimentalSerializationApi
    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?
    ) = error("Unsupported")

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) = error("Unsupported")
}
