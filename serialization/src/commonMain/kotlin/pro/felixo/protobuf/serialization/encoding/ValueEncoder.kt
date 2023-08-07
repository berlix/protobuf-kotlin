package pro.felixo.protobuf.serialization.encoding

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import pro.felixo.protobuf.FieldNumber
import pro.felixo.protobuf.serialization.Enum
import pro.felixo.protobuf.serialization.Message
import pro.felixo.protobuf.wire.Tag
import pro.felixo.protobuf.wire.WireBuffer
import pro.felixo.protobuf.wire.WireType
import pro.felixo.protobuf.wire.encodeValue

@OptIn(ExperimentalSerializationApi::class)
class ValueEncoder(
    override val serializersModule: SerializersModule,
    private val output: WireBuffer,
    private val type: FieldEncoding,
    private val fieldNumber: FieldNumber? = null
) : Encoder {
    private fun writeTag() {
        if (fieldNumber != null) {
            val wireType = if (type is FieldEncoding.Scalar<*>)
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
        return if (type == FieldEncoding.Bytes)
            ByteArrayEncoder(serializersModule, output)
        else {
            val message = (type as FieldEncoding.Reference).type as Message
            message.encoder(output, fieldNumber == null)
        }
    }

    @ExperimentalSerializationApi
    override fun encodeNull() {
        // not writing tag in this case
    }

    @ExperimentalSerializationApi
    override fun encodeInline(descriptor: SerialDescriptor): Encoder = this

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        val enum = (type as FieldEncoding.Reference).type as Enum
        writeEnumTag()
        output.writeVarInt(enum.encode(index))
    }

    override fun encodeBoolean(value: Boolean) {
        writeTag()
        type as FieldEncoding.Bool
        output.encodeValue(type.encode(value))
    }

    override fun encodeByte(value: Byte) {
        writeTag()
        type as FieldEncoding.Integer32
        output.encodeValue(type.encode(if (type.isUnsigned) value.toUByte().toInt() else value.toInt()))
    }

    override fun encodeChar(value: Char) {
        writeTag()
        type as FieldEncoding.Integer32
        output.encodeValue(type.encode(value.code))
    }

    override fun encodeDouble(value: Double) {
        writeTag()
        type as FieldEncoding.Double
        output.encodeValue(type.encode(value))
    }

    override fun encodeFloat(value: Float) {
        writeTag()
        type as FieldEncoding.Float
        output.encodeValue(type.encode(value))
    }

    override fun encodeInt(value: Int) {
        writeTag()
        type as FieldEncoding.Integer32
        output.encodeValue(
            if (type.isUnsigned)
                type.encode(value, -1)
            else
                type.encode(value)
        )
    }

    override fun encodeLong(value: Long) {
        writeTag()
        type as FieldEncoding.Integer64
        output.encodeValue(type.encode(value))
    }

    override fun encodeShort(value: Short) {
        writeTag()
        type as FieldEncoding.Integer32
        output.encodeValue(type.encode(if (type.isUnsigned) value.toUShort().toInt() else value.toInt()))
    }

    override fun encodeString(value: String) {
        writeTag()
        type as FieldEncoding.String
        output.encodeValue(type.encode(value))
    }
}
