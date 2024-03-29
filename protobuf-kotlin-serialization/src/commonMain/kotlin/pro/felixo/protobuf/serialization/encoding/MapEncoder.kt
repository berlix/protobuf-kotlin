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

class MapEncoder(
    override val serializersModule: SerializersModule,
    private val fieldNumber: FieldNumber,
    private val keyField: Field,
    private val valueField: Field,
    private val output: WireBuffer,
) : HybridEncoder() {
    private val fieldNumbers = arrayOf(keyField.number, valueField.number)

    private var currentEntryBuffer: WireBuffer? = null

    private fun encoder(fieldNumber: FieldNumber, wireOutput: WireBuffer) = when (fieldNumber) {
        keyField.number -> keyField.encoder(wireOutput)
        valueField.number -> valueField.encoder(wireOutput)
        else -> error("Unknown field number: $fieldNumber")
    }

    private fun encoder(fieldIndex: Int): Encoder {
        val fieldNumber = fieldNumbers[fieldIndex % 2]
        if (fieldNumber == keyField.number) {
            writeCurrentEntry()
            currentEntryBuffer = WireBuffer()
        }
        return encoder(
            fieldNumber,
            requireNotNull(currentEntryBuffer) { "Writing an entry's value before its key is not permitted." }
        )
    }

    private fun writeCurrentEntry() {
        currentEntryBuffer?.let {
            output.encodeField(Tag.of(fieldNumber, WireType.Len), WireValue.Len(it))
            currentEntryBuffer = null
        }
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        writeCurrentEntry()
    }

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) =
        encoder(index).encodeBoolean(value)

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) =
        encoder(index).encodeByte(value)

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) =
        encoder(index).encodeChar(value)

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) =
        encoder(index).encodeDouble(value)

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) =
        encoder(index).encodeFloat(value)

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) =
        encoder(index).encodeInt(value)

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) =
        encoder(index).encodeLong(value)

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) =
        encoder(index).encodeShort(value)

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) =
        encoder(index).encodeString(value)

    @ExperimentalSerializationApi
    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?
    ) {
        encoder(index).apply {
            if (value != null)
                serializer.serialize(this, value)
            else
                encodeNull()
        }
    }

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) = encoder(index).encodeSerializableValue(serializer, value)

    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder = encoder(index)
}
