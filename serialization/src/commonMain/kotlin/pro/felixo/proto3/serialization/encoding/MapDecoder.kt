package pro.felixo.proto3.serialization.encoding

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule
import pro.felixo.proto3.serialization.Field
import pro.felixo.proto3.wire.WireValue

class MapDecoder(
    override val serializersModule: SerializersModule,
    private val keyField: Field,
    private val valueField: Field,
    private val entries: List<WireValue>
) : HybridDecoder() {

    private var currentEntryIndex = -1
    private var currentEntry: MessageDecoder? = null

    override fun endStructure(descriptor: SerialDescriptor) {}

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int =
        currentEntry?.decodeElementIndex(descriptor)
            ?.takeIf { it != CompositeDecoder.DECODE_DONE }
            ?.let { currentEntryIndex * 2 + it } ?:
            run {
                currentEntryIndex++
                if (currentEntryIndex >= entries.size)
                    CompositeDecoder.DECODE_DONE
                else {
                    currentEntry = MessageDecoder(
                        serializersModule,
                        listOf(keyField, valueField),
                        listOf(entries[currentEntryIndex])
                    )
                    decodeElementIndex(descriptor)
                }
            }

    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean =
        requireNotNull(currentEntry) {
            "Extraneous call of decodeBooleanElement($index)"
        }.decodeBooleanElement(descriptor, index % 2)

    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte =
        requireNotNull(currentEntry) {
            "Extraneous call of decodeByteElement($index)"
        }.decodeByteElement(descriptor, index % 2)

    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char =
        requireNotNull(currentEntry) {
            "Extraneous call of decodeCharElement($index)"
        }.decodeCharElement(descriptor, index % 2)

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double =
        requireNotNull(currentEntry) {
            "Extraneous call of decodeDoubleElement($index)"
        }.decodeDoubleElement(descriptor, index % 2)

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float =
        requireNotNull(currentEntry) {
            "Extraneous call of decodeFloatElement($index)"
        }.decodeFloatElement(descriptor, index % 2)

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int =
        requireNotNull(currentEntry) {
            "Extraneous call of decodeIntElement($index)"
        }.decodeIntElement(descriptor, index % 2)

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long =
        requireNotNull(currentEntry) {
            "Extraneous call of decodeLongElement($index)"
        }.decodeLongElement(descriptor, index % 2)

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short =
        requireNotNull(currentEntry) {
            "Extraneous call of decodeShortElement($index)"
        }.decodeShortElement(descriptor, index % 2)

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String =
        requireNotNull(currentEntry) {
            "Extraneous call of decodeStringElement($index)"
        }.decodeStringElement(descriptor, index % 2)

    @ExperimentalSerializationApi
    override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T?>,
        previousValue: T?
    ): T? =
        requireNotNull(currentEntry) {
            "Extraneous call of decodeNullableSerializableElement($index)"
        }.decodeNullableSerializableElement(descriptor, index % 2, deserializer, previousValue)

    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T =
        requireNotNull(currentEntry) {
            "Extraneous call of decodeSerializableElement($index)"
        }.decodeSerializableElement(descriptor, index % 2, deserializer, previousValue)

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder =
        requireNotNull(currentEntry) {
            "Extraneous call of decodeInlineElement($index)"
        }.decodeInlineElement(descriptor, index % 2)
}
