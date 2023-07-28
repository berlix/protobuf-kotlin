package pro.felixo.proto3.encoding

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import pro.felixo.proto3.FieldNumber
import pro.felixo.proto3.FieldType
import pro.felixo.proto3.SchemaGenerator
import pro.felixo.proto3.isUnsigned
import pro.felixo.proto3.wire.Tag
import pro.felixo.proto3.wire.WireOutput
import pro.felixo.proto3.wire.WireType
import pro.felixo.proto3.wire.encodeValue

@OptIn(ExperimentalSerializationApi::class)
class ValueEncoder(
    private val schemaGenerator: SchemaGenerator,
    private val output: WireOutput,
    private val type: FieldType,
    private val fieldNumber: FieldNumber? = null
) : Encoder {
    override val serializersModule: SerializersModule
        get() = schemaGenerator.serializersModule

    private fun writeTag() {
        if (fieldNumber != null) {
            val wireType = if (type is FieldType.Scalar<*>)
                type.wireType
            else
                WireType.Len
            output.writeVarInt(Tag.of(fieldNumber, wireType).value)
        }
    }

    private fun writeEnumTag() {
        if (fieldNumber != null)
            output.writeVarInt(Tag.of(fieldNumber, WireType.VarInt).value)
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        writeTag()
        return if (type == FieldType.Bytes)
            ByteArrayEncoder(output, serializersModule)
        else
            schemaGenerator.getCompositeEncoding(descriptor).encoder(output, fieldNumber == null)
    }

    @ExperimentalSerializationApi
    override fun encodeNull() {
        // not writing tag in this case
    }

    @ExperimentalSerializationApi
    override fun encodeInline(descriptor: SerialDescriptor): Encoder = this

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        writeEnumTag()
        output.writeVarInt(schemaGenerator.getEnumEncoding(enumDescriptor).encode(index))
    }

    override fun encodeBoolean(value: Boolean) {
        writeTag()
        type as FieldType.Bool
        output.encodeValue(type.encode(value))
    }

    override fun encodeByte(value: Byte) {
        writeTag()
        type as FieldType.Integer32
        output.encodeValue(type.encode(if (type.isUnsigned) value.toUByte().toInt() else value.toInt()))
    }

    override fun encodeChar(value: Char) {
        writeTag()
        type as FieldType.Integer32
        output.encodeValue(type.encode(value.code))
    }

    override fun encodeDouble(value: Double) {
        writeTag()
        type as FieldType.Double
        output.encodeValue(type.encode(value))
    }

    override fun encodeFloat(value: Float) {
        writeTag()
        type as FieldType.Float
        output.encodeValue(type.encode(value))
    }

    override fun encodeInt(value: Int) {
        writeTag()
        type as FieldType.Integer32
        output.encodeValue(
            if (type.isUnsigned)
                type.encode(value, -1)
            else
                type.encode(value)
        )
    }

    override fun encodeLong(value: Long) {
        writeTag()
        type as FieldType.Integer64
        output.encodeValue(type.encode(value))
    }

    override fun encodeShort(value: Short) {
        writeTag()
        type as FieldType.Integer32
        output.encodeValue(type.encode(if (type.isUnsigned) value.toUShort().toInt() else value.toInt()))
    }

    override fun encodeString(value: String) {
        writeTag()
        type as FieldType.String
        output.encodeValue(type.encode(value))
    }
}
