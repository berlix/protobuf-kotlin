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
    private val encoding: FieldEncoding,
    private val fieldNumber: FieldNumber? = null
) : Encoder {
    private fun writeTag() {
        if (fieldNumber != null) {
            val wireType = when (encoding) {
                is FieldEncoding.Scalar<*> -> encoding.wireType
                is FieldEncoding.Reference -> when (encoding.type) {
                    is Enum -> WireType.VarInt
                    is Message -> WireType.Len
                }
            }

            output.writeVarInt(Tag.of(fieldNumber, wireType).value)
        }
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        writeTag()
        return if (encoding == FieldEncoding.Bytes)
            ByteArrayEncoder(serializersModule, output)
        else {
            val message = (encoding as FieldEncoding.Reference).type as Message
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
        writeTag()
        val enum = (encoding as FieldEncoding.Reference).type as Enum
        output.writeVarInt(enum.encode(index))
    }

    override fun encodeBoolean(value: Boolean) {
        writeTag()
        encoding as FieldEncoding.Bool
        output.encodeValue(encoding.encode(value))
    }

    override fun encodeByte(value: Byte) {
        writeTag()
        encoding as FieldEncoding.Integer32
        output.encodeValue(encoding.encode(if (encoding.isUnsigned) value.toUByte().toInt() else value.toInt()))
    }

    override fun encodeChar(value: Char) {
        writeTag()
        encoding as FieldEncoding.Integer32
        output.encodeValue(encoding.encode(value.code))
    }

    override fun encodeDouble(value: Double) {
        writeTag()
        encoding as FieldEncoding.Double
        output.encodeValue(encoding.encode(value))
    }

    override fun encodeFloat(value: Float) {
        writeTag()
        encoding as FieldEncoding.Float
        output.encodeValue(encoding.encode(value))
    }

    override fun encodeInt(value: Int) {
        writeTag()
        encoding as FieldEncoding.Integer32
        output.encodeValue(
            if (encoding.isUnsigned)
                encoding.encode(value, -1)
            else
                encoding.encode(value)
        )
    }

    override fun encodeLong(value: Long) {
        writeTag()
        encoding as FieldEncoding.Integer64
        output.encodeValue(encoding.encode(value))
    }

    override fun encodeShort(value: Short) {
        writeTag()
        encoding as FieldEncoding.Integer32
        output.encodeValue(encoding.encode(if (encoding.isUnsigned) value.toUShort().toInt() else value.toInt()))
    }

    override fun encodeString(value: String) {
        writeTag()
        encoding as FieldEncoding.String
        output.encodeValue(encoding.encode(value))
    }
}
