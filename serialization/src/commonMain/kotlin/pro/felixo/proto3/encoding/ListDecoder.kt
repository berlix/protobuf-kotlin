package pro.felixo.proto3.encoding

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule
import pro.felixo.proto3.FieldEncoding
import pro.felixo.proto3.SchemaGenerator
import pro.felixo.proto3.wire.WireValue
import pro.felixo.proto3.wire.decodeValue

class ListDecoder(
    private val schemaGenerator: SchemaGenerator,
    private val elementType: FieldEncoding,
    private val input: List<WireValue>,
    private val elementDecoder: (List<WireValue>) -> Decoder
) : HybridDecoder() {

    override val serializersModule: SerializersModule
        get() = schemaGenerator.serializersModule

    private var currentInputIndex = 0
    private var currentElement: WireValue? = null

    private var currentElementIndex = -1

    override fun endStructure(descriptor: SerialDescriptor) {}

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        currentElementIndex++

        return if (elementType.isPackable)
            decodeElementIndexPackable()
        else if (currentInputIndex >= input.size)
            CompositeDecoder.DECODE_DONE
        else {
            currentElement = input[currentInputIndex]
            currentInputIndex++
            currentElementIndex
        }
    }

    private fun decodeElementIndexPackable(): Int =
        if (currentInputIndex >= input.size)
            CompositeDecoder.DECODE_DONE
        else
            when (input[currentInputIndex]) {
                is WireValue.Len -> {
                    val len = input[currentInputIndex] as WireValue.Len
                    currentElement = len.value.decodeValue((elementType as FieldEncoding.Scalar<*>).wireType)
                    if (currentElement == null) {
                        currentInputIndex++
                        decodeElementIndexPackable()
                    } else
                        currentElementIndex
                }
                else -> {
                    currentElement = input[currentInputIndex]
                    currentInputIndex++
                }
            }

    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean =
        requireNotNull(currentElement?.let { elementDecoder(listOf(it)).decodeBoolean() }) {
            "Extraneous call of decodeBooleanElement($index)"
        }

    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte =
        requireNotNull(currentElement?.let { elementDecoder(listOf(it)).decodeByte() }) {
            "Extraneous call of decodeByteElement($index)"
        }

    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char =
        requireNotNull(currentElement?.let { elementDecoder(listOf(it)).decodeChar() }) {
            "Extraneous call of decodeCharElement($index)"
        }

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double =
        requireNotNull(currentElement?.let { elementDecoder(listOf(it)).decodeDouble() }) {
            "Extraneous call of decodeDoubleElement($index)"
        }

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float =
        requireNotNull(currentElement?.let { elementDecoder(listOf(it)).decodeFloat() }) {
            "Extraneous call of decodeFloatElement($index)"
        }

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int =
        requireNotNull(currentElement?.let { elementDecoder(listOf(it)).decodeInt() }) {
            "Extraneous call of decodeIntElement($index)"
        }

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long =
        requireNotNull(currentElement?.let { elementDecoder(listOf(it)).decodeLong() }) {
            "Extraneous call of decodeLongElement($index)"
        }

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short =
        requireNotNull(currentElement?.let { elementDecoder(listOf(it)).decodeShort() }) {
            "Extraneous call of decodeShortElement($index)"
        }

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String =
        requireNotNull(currentElement?.let { elementDecoder(listOf(it)).decodeString() }) {
            "Extraneous call of decodeStringElement($index)"
        }

    @ExperimentalSerializationApi
    override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T?>,
        previousValue: T?
    ): T? =
        requireNotNull(currentElement) {
            "Extraneous call of decodeNullableSerializableElement($index)"
        }.let {
            elementDecoder(listOf(it)).decodeNullableSerializableValue(deserializer)
        }

    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T =
        requireNotNull(currentElement) {
            "Extraneous call of decodeSerializableElement($index)"
        }.let {
            elementDecoder(listOf(it)).decodeSerializableValue(deserializer)
        }

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder =
        requireNotNull(currentElement?.let {
            elementDecoder(listOf(it))
        }) {
            "Extraneous call of decodeInlineElement($index)"
        }
}
