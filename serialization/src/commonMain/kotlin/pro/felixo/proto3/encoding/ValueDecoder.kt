package pro.felixo.proto3.encoding

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule
import pro.felixo.proto3.FieldType
import pro.felixo.proto3.SchemaGenerator
import pro.felixo.proto3.internal.castItems
import pro.felixo.proto3.wire.WireValue

@OptIn(ExperimentalSerializationApi::class)
class ValueDecoder(
    private val schemaGenerator: SchemaGenerator,
    private val values: List<WireValue>,
    private val type: FieldType?
) : Decoder {
    override val serializersModule: SerializersModule
        get() = schemaGenerator.serializersModule

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder =
        if (type == FieldType.Bytes)
            ByteArrayDecoder(values, serializersModule)
        else
            schemaGenerator.getCompositeEncoding(descriptor).decoder(values)

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int =
        schemaGenerator.getEnumEncoding(enumDescriptor).decode(decodeLast(values, FieldType.Int32) ?: 0)

    @ExperimentalSerializationApi
    override fun decodeNotNullMark(): Boolean = values.isNotEmpty()

    @ExperimentalSerializationApi
    override fun decodeNull(): Nothing? = null

    override fun decodeInline(descriptor: SerialDescriptor): Decoder =
        ValueDecoder(schemaGenerator, values, type)

    override fun decodeBoolean(): Boolean {
        require(type != null && type is FieldType.Bool) { "Cannot decode Boolean from type $type" }
        return decodeLast(values, type) ?: false
    }

    override fun decodeByte(): Byte {
        require(type != null && type is FieldType.Integer32) { "Cannot decode Byte from type $type" }
        return decodeLast(values, type)?.toByte() ?: 0
    }

    override fun decodeChar(): Char {
        require(type != null && type is FieldType.Integer32) { "Cannot decode Char from type $type" }
        return decodeLast(values, type)?.toChar() ?: '\u0000'
    }

    override fun decodeDouble(): Double {
        require(type != null && type is FieldType.Double) { "Cannot decode Double from type $type" }
        return decodeLast(values, type) ?: 0.0
    }

    override fun decodeFloat(): Float {
        require(type != null && type is FieldType.Float) { "Cannot decode Float from type $type" }
        return decodeLast(values, type) ?: 0f
    }

    override fun decodeInt(): Int {
        require(type != null && type is FieldType.Integer32) { "Cannot decode Int from type $type" }
        return decodeLast(values, type) ?: 0
    }

    override fun decodeLong(): Long {
        require(type != null && type is FieldType.Integer64) { "Cannot decode Long from type $type" }
        return decodeLast(values, type) ?: 0L
    }

    override fun decodeShort(): Short {
        require(type != null && type is FieldType.Integer32) { "Cannot decode Short from type $type" }
        return decodeLast(values, type)?.toShort() ?: 0
    }

    override fun decodeString(): String {
        require(type != null && type is FieldType.String) { "Cannot decode String from type $type" }
        val wireValues = values.castItems<WireValue.Len>()
        var ret = ""
        FieldType.String.decode(concatLenValues(wireValues)) { ret = it }
        return ret
    }
}
