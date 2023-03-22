package pro.felixo.proto3.encoding

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule
import pro.felixo.proto3.SchemaGenerator
import pro.felixo.proto3.schema.Field
import pro.felixo.proto3.wire.WireValue
import pro.felixo.proto3.wire.decodeMessage

class SyntheticDecoder(
    private val schemaGenerator: SchemaGenerator,
    wireValues: List<WireValue>,
    private val field: Field
) : Decoder {

    override val serializersModule: SerializersModule
        get() = schemaGenerator.serializersModule

    private val values = mutableListOf<WireValue>()

    init {
        wireValues.forEach {
            (it as WireValue.Len).value.decodeMessage { fieldNumber, wireValue ->
                if (fieldNumber == field.number)
                    values.add(wireValue)
            }
        }
    }

    private val fieldDecoder by lazy { field.decoder(values) }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder = fieldDecoder.beginStructure(descriptor)
    override fun decodeBoolean(): Boolean = fieldDecoder.decodeBoolean()
    override fun decodeByte(): Byte = fieldDecoder.decodeByte()
    override fun decodeChar(): Char = fieldDecoder.decodeChar()
    override fun decodeDouble(): Double = fieldDecoder.decodeDouble()
    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = fieldDecoder.decodeEnum(enumDescriptor)
    override fun decodeFloat(): Float = fieldDecoder.decodeFloat()
    override fun decodeInline(descriptor: SerialDescriptor): Decoder = fieldDecoder.decodeInline(descriptor)
    override fun decodeInt(): Int = fieldDecoder.decodeInt()
    override fun decodeLong(): Long = fieldDecoder.decodeLong()
    override fun decodeShort(): Short = fieldDecoder.decodeShort()
    override fun decodeString(): String = fieldDecoder.decodeString()

    @ExperimentalSerializationApi
    override fun decodeNotNullMark(): Boolean = fieldDecoder.decodeNotNullMark()

    @ExperimentalSerializationApi
    override fun decodeNull(): Nothing? = null
}
