@file:Suppress("MagicNumber")

package pro.felixo.protobuf.serialization.encoding

import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import pro.felixo.protobuf.FieldNumber
import pro.felixo.protobuf.Identifier
import pro.felixo.protobuf.serialization.Enum
import pro.felixo.protobuf.serialization.Message
import pro.felixo.protobuf.serialization.Type
import pro.felixo.protobuf.wire.WireBuffer
import pro.felixo.protobuf.wire.WireType
import pro.felixo.protobuf.wire.WireValue
import pro.felixo.protobuf.wire.decodeSInt32
import pro.felixo.protobuf.wire.decodeSInt64
import pro.felixo.protobuf.wire.encodeSInt32
import pro.felixo.protobuf.wire.encodeSInt64

sealed class FieldEncoding {
    abstract val isPackable: Boolean
    abstract val wireType: WireType

    open fun encoder(
        serializersModule: SerializersModule,
        fieldNumber: FieldNumber?,
        output: WireBuffer,
        encodeZeroValue: Boolean
    ): Encoder = PrimitiveEncoder(serializersModule, output, this, encodeZeroValue, fieldNumber)

    open fun decoder(serializersModule: SerializersModule, input: List<WireValue>): Decoder =
        PrimitiveDecoder(serializersModule, input, this)

    sealed class Scalar<DecodedType: Any>(val name: kotlin.String) : FieldEncoding() {
        abstract fun encode(value: DecodedType, encodeZeroValue: Boolean): WireValue?
        abstract fun decode(wire: WireValue, onValue: (DecodedType) -> Unit)
        override fun toString() = name
    }

    sealed class Integer32(name: kotlin.String) : Scalar<Int>(name) {
        abstract fun encode(value: Int, mask: Int, encodeZeroValue: Boolean): WireValue?
        override val isPackable = true
    }

    sealed class Integer64(name: kotlin.String) : Scalar<Long>(name) {
        override val isPackable = true
    }

    object Double : Scalar<kotlin.Double>("double")  {
        override val wireType: WireType = WireType.Fixed64
        override val isPackable = true

        override fun decode(wire: WireValue, onValue: (kotlin.Double) -> Unit) = when (wire) {
            is WireValue.Fixed64 -> onValue(kotlin.Double.fromBits(wire.value))
            is WireValue.Len ->
                while (wire.value.remaining > 0) onValue(kotlin.Double.fromBits(wire.value.readFixed64()))
            is WireValue.Fixed32 -> error("Cannot decode double from fixed32")
            is WireValue.VarInt -> error("Cannot decode double from varint")
        }

        override fun encode(value: kotlin.Double, encodeZeroValue: Boolean) =
            fixed64(value.toRawBits(), encodeZeroValue)
    }

    object Float : Scalar<kotlin.Float>("float") {
        override val wireType: WireType = WireType.Fixed32
        override val isPackable = true

        override fun decode(wire: WireValue, onValue: (kotlin.Float) -> Unit) = when (wire) {
            is WireValue.Fixed32 -> onValue(kotlin.Float.fromBits(wire.value))
            is WireValue.Len ->
                while (wire.value.remaining > 0) onValue(kotlin.Float.fromBits(wire.value.readFixed32()))
            is WireValue.Fixed64 -> error("Cannot decode float from fixed64")
            is WireValue.VarInt -> error("Cannot decode double from varint")
        }

        override fun encode(value: kotlin.Float, encodeZeroValue: Boolean) = fixed32(value.toBits(), encodeZeroValue)
    }

    object Int32 : Integer32("int32") {
        override val wireType: WireType = WireType.VarInt

        override fun decode(wire: WireValue, onValue: (Int) -> Unit) = when (wire) {
            is WireValue.VarInt -> onValue(wire.value.toInt())
            is WireValue.Len -> while (wire.value.remaining > 0) onValue(wire.value.readVarIntAsInt())
            is WireValue.Fixed32 -> error("Cannot decode int32 from fixed32")
            is WireValue.Fixed64 -> error("Cannot decode int32 from fixed64")
        }

        override fun encode(value: Int, mask: Int, encodeZeroValue: Boolean): WireValue? =
            varInt(value.toLong() and mask.toUInt().toLong(), encodeZeroValue)

        override fun encode(value: Int, encodeZeroValue: Boolean): WireValue? = varInt(value.toLong(), encodeZeroValue)
    }

    object Int64 : Integer64("int64") {
        override val wireType: WireType = WireType.VarInt

        override fun decode(wire: WireValue, onValue: (Long) -> Unit) = when (wire) {
            is WireValue.VarInt -> onValue(wire.value)
            is WireValue.Len -> while (wire.value.remaining > 0) onValue(wire.value.readVarIntAsLong())
            is WireValue.Fixed32 -> error("Cannot decode int64 from fixed32")
            is WireValue.Fixed64 -> error("Cannot decode int64 from fixed64")
        }

        override fun encode(value: Long, encodeZeroValue: Boolean): WireValue? = varInt(value, encodeZeroValue)
    }

    object UInt32 : Integer32("uint32") {
        override val wireType: WireType = WireType.VarInt

        override fun decode(wire: WireValue, onValue: (Int) -> Unit) = when (wire) {
            is WireValue.VarInt -> onValue(wire.value.toInt())
            is WireValue.Len -> while (wire.value.remaining > 0) onValue(wire.value.readVarIntAsInt())
            is WireValue.Fixed32 -> error("Cannot decode int32 from fixed32")
            is WireValue.Fixed64 -> error("Cannot decode int32 from fixed64")
        }

        override fun encode(value: Int, mask: Int, encodeZeroValue: Boolean): WireValue? =
            varInt(value.toUInt().toLong() and mask.toUInt().toLong(), encodeZeroValue)

        override fun encode(value: Int, encodeZeroValue: Boolean): WireValue? =
            varInt(value.toUInt().toLong(), encodeZeroValue)
    }

    object UInt64 : Integer64("uint64") {
        override val wireType: WireType = WireType.VarInt

        override fun decode(wire: WireValue, onValue: (Long) -> Unit) = when (wire) {
            is WireValue.VarInt -> onValue(wire.value)
            is WireValue.Len -> while (wire.value.remaining > 0) onValue(wire.value.readVarIntAsLong())
            is WireValue.Fixed32 -> error("Cannot decode int64 from fixed32")
            is WireValue.Fixed64 -> error("Cannot decode int64 from fixed64")
        }

        override fun encode(value: Long, encodeZeroValue: Boolean): WireValue? = varInt(value, encodeZeroValue)
    }

    object SInt32 : Integer32("sint32") {
        override val wireType: WireType = WireType.VarInt

        override fun decode(wire: WireValue, onValue: (Int) -> Unit) = when (wire) {
            is WireValue.VarInt -> onValue(wire.value.toInt().decodeSInt32())
            is WireValue.Len -> while (wire.value.remaining > 0) onValue(wire.value.readVarIntAsInt().decodeSInt32())
            is WireValue.Fixed32 -> error("Cannot decode sint32 from fixed32")
            is WireValue.Fixed64 -> error("Cannot decode sint32 from fixed64")
        }

        override fun encode(value: Int, mask: Int, encodeZeroValue: Boolean): WireValue? =
            varInt(value.encodeSInt32().toLong() and mask.toUInt().toLong(), encodeZeroValue)

        override fun encode(value: Int, encodeZeroValue: Boolean): WireValue? =
            varInt(value.encodeSInt32().toLong() and 0xFFFFFFFFL, encodeZeroValue)
    }

    object SInt64 : Integer64("sint64") {
        override val wireType: WireType = WireType.VarInt

        override fun decode(wire: WireValue, onValue: (Long) -> Unit) = when (wire) {
            is WireValue.VarInt -> onValue(wire.value.decodeSInt64())
            is WireValue.Len -> while (wire.value.remaining > 0) onValue(wire.value.readVarIntAsLong().decodeSInt64())
            is WireValue.Fixed32 -> error("Cannot decode sint64 from fixed32")
            is WireValue.Fixed64 -> error("Cannot decode sint64 from fixed64")
        }

        override fun encode(value: Long, encodeZeroValue: Boolean): WireValue? =
            varInt(value.encodeSInt64(), encodeZeroValue)
    }

    object Fixed32 : Integer32("fixed32") {
        override val wireType: WireType = WireType.Fixed32

        override fun decode(wire: WireValue, onValue: (Int) -> Unit) = when (wire) {
            is WireValue.Fixed32 -> onValue(wire.value)
            is WireValue.Len -> while (wire.value.remaining > 0) onValue(wire.value.readFixed32())
            is WireValue.Fixed64 -> error("Cannot decode fixed32 from fixed64")
            is WireValue.VarInt -> error("Cannot decode fixed32 from varint")
        }

        override fun encode(value: Int, mask: Int, encodeZeroValue: Boolean): WireValue? =
            fixed32(value and mask, encodeZeroValue)

        override fun encode(value: Int, encodeZeroValue: Boolean): WireValue? = fixed32(value, encodeZeroValue)
    }

    object Fixed64 : Integer64("fixed64") {
        override val wireType: WireType = WireType.Fixed64

        override fun decode(wire: WireValue, onValue: (Long) -> Unit) = when (wire) {
            is WireValue.Fixed64 -> onValue(wire.value)
            is WireValue.Len -> while (wire.value.remaining > 0) onValue(wire.value.readFixed64())
            is WireValue.Fixed32 -> error("Cannot decode fixed64 from fixed32")
            is WireValue.VarInt -> error("Cannot decode fixed32 from varint")
        }

        override fun encode(value: Long, encodeZeroValue: Boolean): WireValue? = fixed64(value, encodeZeroValue)
    }

    object SFixed32 : Integer32("sfixed32") {
        override val wireType: WireType = WireType.Fixed32

        override fun decode(wire: WireValue, onValue: (Int) -> Unit) = when (wire) {
            is WireValue.Fixed32 -> onValue(wire.value)
            is WireValue.Len -> while (wire.value.remaining > 0) onValue(wire.value.readFixed32())
            is WireValue.Fixed64 -> error("Cannot decode fixed32 from fixed64")
            is WireValue.VarInt -> error("Cannot decode fixed32 from varint")
        }

        override fun encode(value: Int, mask: Int, encodeZeroValue: Boolean): WireValue? =
            fixed32(value and mask, encodeZeroValue)

        override fun encode(value: Int, encodeZeroValue: Boolean): WireValue? = fixed32(value, encodeZeroValue)
    }

    object SFixed64 : Integer64("sfixed64") {
        override val wireType: WireType = WireType.Fixed64

        override fun decode(wire: WireValue, onValue: (Long) -> Unit) = when (wire) {
            is WireValue.Fixed64 -> onValue(wire.value)
            is WireValue.Len -> while (wire.value.remaining > 0) onValue(wire.value.readFixed64())
            is WireValue.Fixed32 -> error("Cannot decode fixed64 from fixed32")
            is WireValue.VarInt -> error("Cannot decode fixed32 from varint")
        }

        override fun encode(value: Long, encodeZeroValue: Boolean): WireValue? = fixed64(value, encodeZeroValue)
    }

    object Bool : Scalar<Boolean>("bool") {
        override val wireType: WireType = WireType.VarInt
        override val isPackable = true

        override fun decode(wire: WireValue, onValue: (Boolean) -> Unit) = when (wire) {
            is WireValue.VarInt -> onValue(wire.value != 0L)
            is WireValue.Len -> while (wire.value.remaining > 0) onValue(wire.value.readVarIntAsInt() != 0)
            is WireValue.Fixed32 -> error("Cannot decode bool from fixed32")
            is WireValue.Fixed64 -> error("Cannot decode bool from fixed64")
        }

        override fun encode(value: Boolean, encodeZeroValue: Boolean): WireValue? =
            varInt(if (value) 1 else 0, encodeZeroValue)
    }

    object String : Scalar<kotlin.String>("string") {
        override val wireType: WireType = WireType.Len
        override val isPackable = false

        override fun decode(wire: WireValue, onValue: (kotlin.String) -> Unit) = when (wire) {
            is WireValue.Len -> onValue(wire.value.readBytes().decodeToString())
            is WireValue.VarInt -> error("Cannot decode string from varint")
            is WireValue.Fixed32 -> error("Cannot decode string from fixed32")
            is WireValue.Fixed64 -> error("Cannot decode string from fixed64")
        }

        override fun encode(value: kotlin.String, encodeZeroValue: Boolean): WireValue? = len(value, encodeZeroValue)
    }

    object Bytes : Scalar<ByteArray>("bytes") {
        override val wireType: WireType = WireType.Len
        override val isPackable = false

        override fun decode(wire: WireValue, onValue: (ByteArray) -> Unit) = when (wire) {
            is WireValue.Len -> onValue(wire.value.readBytes())
            is WireValue.VarInt -> error("Cannot decode string from varint")
            is WireValue.Fixed32 -> error("Cannot decode string from fixed32")
            is WireValue.Fixed64 -> error("Cannot decode string from fixed64")
        }

        override fun encode(value: ByteArray, encodeZeroValue: Boolean): WireValue? = len(value, encodeZeroValue)

        override fun encoder(
            serializersModule: SerializersModule,
            fieldNumber: FieldNumber?,
            output: WireBuffer,
            encodeZeroValue: Boolean
        ) = ByteArrayEncoder(serializersModule, output, requireNotNull(fieldNumber), encodeZeroValue)

        override fun decoder(serializersModule: SerializersModule, input: List<WireValue>) =
            ByteArrayDecoder(serializersModule, input)
    }

    sealed class Reference<T : Type> : FieldEncoding() {
        override val isPackable = false

        abstract val type: T

        val name: Identifier by lazy { type.name }

        override fun toString() = type.name.toString()
    }

    class MessageReference : Reference<Message>() {

        override val wireType: WireType = WireType.Len

        override lateinit var type: Message
            private set

        override fun encoder(
            serializersModule: SerializersModule,
            fieldNumber: FieldNumber?,
            output: WireBuffer,
            encodeZeroValue: Boolean
        ): Encoder = type.encoder(output, fieldNumber, encodeZeroValue)

        override fun decoder(serializersModule: SerializersModule, input: List<WireValue>): Decoder =
            type.decoder(input)

        companion object {
            fun to(type: Message): MessageReference {
                val ref = MessageReference()
                ref.type = type
                return ref
            }

            fun lazy(): Pair<MessageReference, (Message) -> Unit> {
                val ref = MessageReference()
                return ref to { ref.type = it }
            }
        }
    }

    class EnumReference(override val type: Enum) : Reference<Enum>() {

        override val wireType: WireType = WireType.VarInt

        override fun encoder(
            serializersModule: SerializersModule,
            fieldNumber: FieldNumber?,
            output: WireBuffer,
            encodeZeroValue: Boolean
        ): Encoder = PrimitiveEncoder(serializersModule, output, this, encodeZeroValue, fieldNumber)

        override fun decoder(serializersModule: SerializersModule, input: List<WireValue>): Decoder =
            PrimitiveDecoder(serializersModule, input, this)
    }
}

val FieldEncoding.isUnsigned get() = this is FieldEncoding.UInt32 || this is FieldEncoding.UInt64
