package pro.felixo.proto3.serialization.encoding

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule
import pro.felixo.proto3.FieldNumber
import pro.felixo.proto3.serialization.Field
import pro.felixo.proto3.wire.WireValue
import pro.felixo.proto3.wire.decodeMessage

class PolymorphicDecoder(
    override val serializersModule: SerializersModule,
    fieldByDescriptor: Map<FieldNumber, Pair<SerialDescriptor, Field>>,
    wireValues: List<WireValue>
) : HybridDecoder() {
    private val values = mutableListOf<WireValue>()
    private val descriptor: SerialDescriptor
    private val field: Field

    init {
        var fieldNumber: FieldNumber? = null
        wireValues.forEach {
            (it as WireValue.Len).value.decodeMessage { number, wireValue ->
                if (number != fieldNumber) {
                    values.clear()
                    fieldNumber = number
                }
                values.add(wireValue)
            }
        }
        if (fieldNumber == null)
            error("No values found in message for PolymorphicDecoder")

        val descriptorFieldPair =
            fieldByDescriptor[fieldNumber] ?: error("Invalid field number for polymorphic type: $fieldNumber")
        descriptor = descriptorFieldPair.first
        field = descriptorFieldPair.second
    }

    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean =
        error("PolymorphicDecoder does not support Boolean elements")

    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte =
        error("PolymorphicDecoder does not support Byte elements")

    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char =
        error("PolymorphicDecoder does not support Char elements")

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double =
        error("PolymorphicDecoder does not support Double elements")

    private var currentElementIndex = -1
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        currentElementIndex++
        return if (currentElementIndex >= 2)
            CompositeDecoder.DECODE_DONE
        else
            currentElementIndex
    }

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float =
        error("PolymorphicDecoder does not support Float elements")

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder =
        error("PolymorphicDecoder does not support Inline elements")

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int =
        error("PolymorphicDecoder does not support Int elements")

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long =
        error("PolymorphicDecoder does not support Long elements")

    @ExperimentalSerializationApi
    override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T?>,
        previousValue: T?
    ): T = error("PolymorphicDecoder does not support nullable elements")

    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T = deserializer.deserialize(ValueDecoder(serializersModule, values, field.type))

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short =
        error("PolymorphicDecoder does not support Short elements")


    /**
     * This may be called by the Serializer to find out what the subtype is.
     */
    @OptIn(ExperimentalSerializationApi::class)
    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String = this.descriptor.serialName

    override fun endStructure(descriptor: SerialDescriptor) {
        // Nothing for us to do here.
    }
}
