package pro.felixo.proto3.encoding

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

abstract class HybridEncoder : Encoder, CompositeEncoder {
    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = this

    override fun encodeBoolean(value: Boolean) = error("HybridEncoder does not support encodeBoolean")
    override fun encodeByte(value: Byte) = error("HybridEncoder does not support encodeByte")
    override fun encodeChar(value: Char) = error("HybridEncoder does not support encodeChar")
    override fun encodeDouble(value: Double) = error("HybridEncoder does not support encodeDouble")
    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) =
        error("HybridEncoder does not support encodeEnum")
    override fun encodeFloat(value: Float) = error("HybridEncoder does not support encodeFloat")
    override fun encodeInline(descriptor: SerialDescriptor): Encoder =
        error("HybridEncoder does not support encodeInline")

    override fun encodeInt(value: Int) = error("HybridEncoder does not support encodeInt")
    override fun encodeLong(value: Long) = error("HybridEncoder does not support encodeLong")
    @ExperimentalSerializationApi override fun encodeNull() = error("HybridEncoder does not support encodeNull")
    override fun encodeShort(value: Short) = error("HybridEncoder does not support encodeShort")
    override fun encodeString(value: String) = error("HybridEncoder does not support encodeString")
}

abstract class HybridDecoder : Decoder, CompositeDecoder {
    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder = this

    override fun decodeBoolean(): Boolean = error("HybridDecoder does not support decodeBoolean")
    override fun decodeByte(): Byte = error("HybridDecoder does not support decodeByte")
    override fun decodeChar(): Char = error("HybridDecoder does not support decodeChar")
    override fun decodeDouble(): Double = error("HybridDecoder does not support decodeDouble")
    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = error("HybridDecoder does not support decodeEnum")
    override fun decodeFloat(): Float = error("HybridDecoder does not support decodeFloat")
    override fun decodeInline(descriptor: SerialDescriptor): Decoder =
        error("HybridDecoder does not support decodeInline")
    override fun decodeInt(): Int = error("HybridDecoder does not support decodeInt")
    override fun decodeLong(): Long = error("HybridDecoder does not support decodeLong")
    override fun decodeShort(): Short = error("HybridDecoder does not support decodeShort")
    override fun decodeString(): String = error("HybridDecoder does not support decodeString")

    @ExperimentalSerializationApi
    override fun decodeNotNullMark(): Boolean = error("HybridDecoder does not support decodeNotNullMark")

    @ExperimentalSerializationApi
    override fun decodeNull(): Nothing = error("HybridDecoder does not support decodeNull")
}
