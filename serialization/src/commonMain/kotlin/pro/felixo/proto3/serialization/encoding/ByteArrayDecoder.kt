package pro.felixo.proto3.serialization.encoding

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule
import pro.felixo.proto3.serialization.util.castItems
import pro.felixo.proto3.wire.WireValue

class ByteArrayDecoder(values: List<WireValue>, override val serializersModule: SerializersModule) : CompositeDecoder {

    private val bytes = concatLenValues(values.castItems()).value.readBytes()
    private var position = -1

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        position++
        return if (position >= bytes.size)
            CompositeDecoder.DECODE_DONE
        else
            position
    }
    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte = bytes[index]

    override fun endStructure(descriptor: SerialDescriptor) {}

    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean = error("Unsupported")
    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char = error("Unsupported")
    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double = error("Unsupported")
    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float = error("Unsupported")
    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder = error("Unsupported")
    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int = error("Unsupported")
    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long = error("Unsupported")
    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short = error("Unsupported")
    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String = error("Unsupported")

    @ExperimentalSerializationApi
    override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T?>,
        previousValue: T?
    ): T = error("Unsupported")

    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T = error("Unsupported")
}
