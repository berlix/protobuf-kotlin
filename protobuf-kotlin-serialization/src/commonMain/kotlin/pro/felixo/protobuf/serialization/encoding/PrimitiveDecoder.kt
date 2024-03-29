package pro.felixo.protobuf.serialization.encoding

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule
import pro.felixo.protobuf.serialization.util.castItems
import pro.felixo.protobuf.wire.WireValue

@OptIn(ExperimentalSerializationApi::class)
class PrimitiveDecoder(
    override val serializersModule: SerializersModule,
    private val values: List<WireValue>,
    private val type: FieldEncoding?
) : Decoder {
    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder =
        error("PrimitiveDecoder cannot decode structures.")

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int =
        (type as FieldEncoding.EnumReference).type
            .decode(decodeLast(values, FieldEncoding.Int32) ?: 0)

    @ExperimentalSerializationApi
    override fun decodeNotNullMark(): Boolean = values.isNotEmpty()

    @ExperimentalSerializationApi
    override fun decodeNull(): Nothing? = null

    override fun decodeInline(descriptor: SerialDescriptor): Decoder = this

    override fun decodeBoolean(): Boolean {
        require(type != null && type is FieldEncoding.Bool) { "Cannot decode Boolean from type $type" }
        return decodeLast(values, type) ?: false
    }

    override fun decodeByte(): Byte {
        require(type != null && type is FieldEncoding.Integer32) { "Cannot decode Byte from type $type" }
        return decodeLast(values, type)?.toByte() ?: 0
    }

    override fun decodeChar(): Char {
        require(type != null && type is FieldEncoding.Integer32) { "Cannot decode Char from type $type" }
        return decodeLast(values, type)?.toChar() ?: '\u0000'
    }

    override fun decodeDouble(): Double {
        require(type != null && type is FieldEncoding.Double) { "Cannot decode Double from type $type" }
        return decodeLast(values, type) ?: 0.0
    }

    override fun decodeFloat(): Float {
        require(type != null && type is FieldEncoding.Float) { "Cannot decode Float from type $type" }
        return decodeLast(values, type) ?: 0f
    }

    override fun decodeInt(): Int {
        require(type != null && type is FieldEncoding.Integer32) { "Cannot decode Int from type $type" }
        return decodeLast(values, type) ?: 0
    }

    override fun decodeLong(): Long {
        require(type != null && type is FieldEncoding.Integer64) { "Cannot decode Long from type $type" }
        return decodeLast(values, type) ?: 0L
    }

    override fun decodeShort(): Short {
        require(type != null && type is FieldEncoding.Integer32) { "Cannot decode Short from type $type" }
        return decodeLast(values, type)?.toShort() ?: 0
    }

    override fun decodeString(): String {
        require(type != null && type is FieldEncoding.String) { "Cannot decode String from type $type" }
        val wireValues = values.castItems<WireValue.Len>()
        var ret = ""
        FieldEncoding.String.decode(concatLenValues(wireValues)) { ret = it }
        return ret
    }
}
