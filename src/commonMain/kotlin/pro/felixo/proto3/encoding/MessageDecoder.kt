package pro.felixo.proto3.encoding

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule
import pro.felixo.proto3.FieldType
import pro.felixo.proto3.SchemaGenerator
import pro.felixo.proto3.internal.castItems
import pro.felixo.proto3.schema.Field
import pro.felixo.proto3.schema.FieldNumber
import pro.felixo.proto3.schema.FieldRule
import pro.felixo.proto3.wire.WireValue
import pro.felixo.proto3.wire.decodeMessage

class MessageDecoder(
    private val schemaGenerator: SchemaGenerator,
    private val fieldByElementIndex: List<Field>,
    wireValues: List<WireValue>
) : CompositeDecoder {

    override val serializersModule: SerializersModule
        get() = schemaGenerator.serializersModule

    private val values = mutableMapOf<FieldNumber, MutableList<WireValue>>()

    init {
        wireValues.forEach {
            (it as WireValue.Len).value.decodeMessage { fieldNumber, wireValue ->
                values.getOrPut(fieldNumber) { mutableListOf() }.add(wireValue)
            }
        }
    }

    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean {
        val field = fieldByElementIndex[index]
        return decodeLast(values[field.number], field.type as FieldType.Bool) ?: false
    }

    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte =
        decodeIntElement(descriptor, index).toByte()

    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char =
        decodeIntElement(descriptor, index).toChar()

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double {
        val field = fieldByElementIndex[index]
        return decodeLast(values[field.number], field.type as FieldType.Double) ?: 0.0
    }

    private var currentElementIndex = -1
    @OptIn(ExperimentalSerializationApi::class)
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        currentElementIndex++
        return if (currentElementIndex >= fieldByElementIndex.size)
            CompositeDecoder.DECODE_DONE
        else
            // skip elements which are optional and do not have a value
            if (
                fieldByElementIndex[currentElementIndex].rule == FieldRule.Optional &&
                descriptor.isElementOptional(currentElementIndex) &&
                values[fieldByElementIndex[currentElementIndex].number].isNullOrEmpty()
            )
                decodeElementIndex(descriptor)
            else
                currentElementIndex
    }

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float {
        val field = fieldByElementIndex[index]
        return decodeLast(values[field.number], field.type as FieldType.Float) ?: 0f
    }

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
        val field = fieldByElementIndex[index]
        val values = values[field.number] ?: emptyList()
        return field.decoder(values)
    }

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int {
        val field = fieldByElementIndex[index]
        return decodeLast(values[field.number], field.type as FieldType.Integer32) ?: 0
    }

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long {
        val field = fieldByElementIndex[index]
        return decodeLast(values[field.number], field.type as FieldType.Integer64) ?: 0L
    }

    @ExperimentalSerializationApi
    override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T?>,
        previousValue: T?
    ): T? {
        val field = fieldByElementIndex[index]
        val values = values[field.number] ?: return null
        return deserializer.deserialize(field.decoder(values))
    }

    // TODO optimisation: if serializer is kotlin.ByteArray and field is of type bytes, read bytes directly
    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T {
        val field = fieldByElementIndex[index]
        val values = values[field.number] ?: emptyList()
        return deserializer.deserialize(field.decoder(values))
    }

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short =
        decodeIntElement(descriptor, index).toShort()

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String {
        val field = fieldByElementIndex[index]
        check(field.type is FieldType.String)
        val wireValues = values[field.number]?.castItems<WireValue.Len>()
        var ret = ""
        if (wireValues != null)
            FieldType.String.decode(concatLenValues(wireValues)) { ret = it }
        return ret
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        // Nothing for us to do here.
    }
}