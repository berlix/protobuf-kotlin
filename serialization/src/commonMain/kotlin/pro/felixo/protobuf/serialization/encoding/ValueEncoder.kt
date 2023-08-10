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
import pro.felixo.protobuf.wire.WireValue
import pro.felixo.protobuf.wire.encodeValue

@OptIn(ExperimentalSerializationApi::class)
class ValueEncoder(
    override val serializersModule: SerializersModule,
    private val output: WireBuffer,
    private val encoding: FieldEncoding,
    private val encodeZeroValue: Boolean,
    private val fieldNumber: FieldNumber? = null
) : Encoder {
    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder =
        ((encoding as FieldEncoding.Reference).type as Message)
            .encoder(output, fieldNumber, encodeZeroValue)

    @ExperimentalSerializationApi
    override fun encodeNull() {
        // not writing tag in this case
    }

    @ExperimentalSerializationApi
    override fun encodeInline(descriptor: SerialDescriptor): Encoder = this

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) = write(
        ((encoding as FieldEncoding.Reference).type as Enum).encode(index, encodeZeroValue)
    )

    override fun encodeBoolean(value: Boolean) = write(
        (encoding as FieldEncoding.Bool).encode(value, encodeZeroValue)
    )

    override fun encodeByte(value: Byte) = write(
        (encoding as FieldEncoding.Integer32)
            .encode(if (encoding.isUnsigned) value.toUByte().toInt() else value.toInt(), encodeZeroValue)
    )

    override fun encodeChar(value: Char) = write(
        (encoding as FieldEncoding.Integer32).encode(value.code, encodeZeroValue)
    )

    override fun encodeDouble(value: Double) = write(
        (encoding as FieldEncoding.Double).encode(value, encodeZeroValue)
    )

    override fun encodeFloat(value: Float) = write(
        (encoding as FieldEncoding.Float).encode(value, encodeZeroValue)
    )

    override fun encodeInt(value: Int) = write(
        (encoding as FieldEncoding.Integer32).run {
            if (isUnsigned)
                encode(value, -1, encodeZeroValue)
            else
                encode(value, encodeZeroValue)
        }
    )

    override fun encodeLong(value: Long) = write(
        (encoding as FieldEncoding.Integer64).encode(value, encodeZeroValue)
    )

    override fun encodeShort(value: Short) = write(
        (encoding as FieldEncoding.Integer32).encode(
            if (encoding.isUnsigned) value.toUShort().toInt() else value.toInt(),
            encodeZeroValue
        )
    )

    override fun encodeString(value: String) = write(
        (encoding as FieldEncoding.String).encode(value, encodeZeroValue)
    )

    private fun write(wireValue: WireValue?) {
        if (wireValue != null) {
            writeTag()
            output.encodeValue(wireValue)
        }
    }

    private fun writeTag() {
        if (fieldNumber != null)
            output.writeVarInt(Tag.of(fieldNumber, encoding.wireType).value)
    }
}
