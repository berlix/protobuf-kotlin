package pro.felixo.proto3.encoding

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import pro.felixo.proto3.SchemaGenerator
import pro.felixo.proto3.schema.Field
import pro.felixo.proto3.schema.FieldNumber
import pro.felixo.proto3.wire.Tag
import pro.felixo.proto3.wire.WireInput
import pro.felixo.proto3.wire.WireOutput
import pro.felixo.proto3.wire.WireType
import pro.felixo.proto3.wire.WireValue
import pro.felixo.proto3.wire.encodeField

class MapEncoder(
    private val schemaGenerator: SchemaGenerator,
    private val fieldNumber: FieldNumber,
    private val keyField: Field,
    private val valueField: Field,
    private val output: WireOutput,
) : Encoder, CompositeEncoder {
    override val serializersModule: SerializersModule
        get() = schemaGenerator.serializersModule

    private val fieldNumbers = arrayOf(keyField.number, valueField.number)

    private var currentEntryBuffer: WireOutput? = null

    private fun encoder(fieldNumber: FieldNumber, wireOutput: WireOutput) = when (fieldNumber) {
        keyField.number -> keyField.encoder(wireOutput)
        valueField.number -> valueField.encoder(wireOutput)
        else -> error("Unknown field number: $fieldNumber")
    }

    private fun encoder(fieldIndex: Int): Encoder {
        val fieldNumber = fieldNumbers[fieldIndex % 2]
        if (fieldNumber == keyField.number) {
            writeCurrentEntry()
            currentEntryBuffer = WireOutput()
        }
        return encoder(
            fieldNumber,
            requireNotNull(currentEntryBuffer) { "Writing an entry's value before its key is not permitted." }
        )
    }

    private fun writeCurrentEntry() {
        currentEntryBuffer?.let {
            output.encodeField(Tag.of(fieldNumber, WireType.Len), WireValue.Len(WireInput(it.getBytes())))
            currentEntryBuffer = null
        }
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = this

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

    override fun encodeBoolean(value: Boolean) = error("MapEncoder does not support encodeBoolean")
    override fun encodeByte(value: Byte) = error("MapEncoder does not support encodeByte")
    override fun encodeChar(value: Char) = error("MapEncoder does not support encodeChar")
    override fun encodeDouble(value: Double) = error("MapEncoder does not support encodeDouble")
    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) = error("MapEncoder does not support encodeEnum")
    override fun encodeFloat(value: Float) = error("MapEncoder does not support encodeFloat")
    override fun encodeInline(descriptor: SerialDescriptor): Encoder = error("MapEncoder does not support encodeInline")
    override fun encodeInt(value: Int) = error("MapEncoder does not support encodeInt")
    override fun encodeLong(value: Long) = error("MapEncoder does not support encodeLong")
    @ExperimentalSerializationApi override fun encodeNull() = error("MapEncoder does not support encodeNull")
    override fun encodeShort(value: Short) = error("MapEncoder does not support encodeShort")
    override fun encodeString(value: String) = error("MapEncoder does not support encodeString")
}
