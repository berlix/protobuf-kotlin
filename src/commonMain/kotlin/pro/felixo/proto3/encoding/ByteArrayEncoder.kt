package pro.felixo.proto3.encoding

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import pro.felixo.proto3.wire.WireOutput

class ByteArrayEncoder(
    private val output: WireOutput,
    override val serializersModule: SerializersModule
) : CompositeEncoder {

    private val buffer = WireOutput()

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        require(index == buffer.length) { "Expected index ${buffer.length}, got $index" }
        buffer.writeByte(value)
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        val bytes = buffer.getBytes()
        output.writeVarInt(bytes.size)
        output.write(bytes)
    }

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
