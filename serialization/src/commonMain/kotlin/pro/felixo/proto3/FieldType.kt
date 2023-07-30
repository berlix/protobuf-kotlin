@file:Suppress("MagicNumber")

package pro.felixo.proto3

import pro.felixo.proto3.wire.WireValue
import pro.felixo.proto3.schema.Identifier
import pro.felixo.proto3.wire.WireBuffer
import pro.felixo.proto3.wire.WireType
import pro.felixo.proto3.wire.decodeSInt32
import pro.felixo.proto3.wire.decodeSInt64
import pro.felixo.proto3.wire.encodeSInt32
import pro.felixo.proto3.wire.encodeSInt64

sealed class FieldType {
    abstract val isPackable: Boolean

    sealed class Scalar<DecodedType: Any>(val name: kotlin.String) : FieldType() {
        abstract val wireType: WireType
        abstract fun encode(value: DecodedType): WireValue
        abstract fun decode(wire: WireValue, onValue: (DecodedType) -> Unit)
        override fun toString() = name
    }

    sealed class Integer32(name: kotlin.String) : Scalar<Int>(name) {
        abstract fun encode(value: Int, mask: Int): WireValue
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

        override fun encode(value: kotlin.Double) = WireValue.Fixed64(value.toRawBits())
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

        override fun encode(value: kotlin.Float) = WireValue.Fixed32(value.toBits())
    }

    object Int32 : Integer32("int32") {
        override val wireType: WireType = WireType.VarInt

        override fun decode(wire: WireValue, onValue: (Int) -> Unit) = when (wire) {
            is WireValue.VarInt -> onValue(wire.value.toInt())
            is WireValue.Len -> while (wire.value.remaining > 0) onValue(wire.value.readVarIntAsInt())
            is WireValue.Fixed32 -> error("Cannot decode int32 from fixed32")
            is WireValue.Fixed64 -> error("Cannot decode int32 from fixed64")
        }

        override fun encode(value: Int, mask: Int): WireValue = WireValue.VarInt(
            value.toLong() and mask.toUInt().toLong()
        )

        override fun encode(value: Int) = WireValue.VarInt(value.toLong())
    }

    object Int64 : Integer64("int64") {
        override val wireType: WireType = WireType.VarInt

        override fun decode(wire: WireValue, onValue: (Long) -> Unit) = when (wire) {
            is WireValue.VarInt -> onValue(wire.value)
            is WireValue.Len -> while (wire.value.remaining > 0) onValue(wire.value.readVarIntAsLong())
            is WireValue.Fixed32 -> error("Cannot decode int64 from fixed32")
            is WireValue.Fixed64 -> error("Cannot decode int64 from fixed64")
        }

        override fun encode(value: Long) = WireValue.VarInt(value)
    }

    object UInt32 : Integer32("uint32") {
        override val wireType: WireType = WireType.VarInt

        override fun decode(wire: WireValue, onValue: (Int) -> Unit) = when (wire) {
            is WireValue.VarInt -> onValue(wire.value.toInt())
            is WireValue.Len -> while (wire.value.remaining > 0) onValue(wire.value.readVarIntAsInt())
            is WireValue.Fixed32 -> error("Cannot decode int32 from fixed32")
            is WireValue.Fixed64 -> error("Cannot decode int32 from fixed64")
        }

        override fun encode(value: Int, mask: Int): WireValue = WireValue.VarInt(
            value.toUInt().toLong() and mask.toUInt().toLong()
        )

        override fun encode(value: Int) = WireValue.VarInt(value.toUInt().toLong())
    }

    object UInt64 : Integer64("uint64") {
        override val wireType: WireType = WireType.VarInt

        override fun decode(wire: WireValue, onValue: (Long) -> Unit) = when (wire) {
            is WireValue.VarInt -> onValue(wire.value)
            is WireValue.Len -> while (wire.value.remaining > 0) onValue(wire.value.readVarIntAsLong())
            is WireValue.Fixed32 -> error("Cannot decode int64 from fixed32")
            is WireValue.Fixed64 -> error("Cannot decode int64 from fixed64")
        }

        override fun encode(value: Long) = WireValue.VarInt(value)
    }

    object SInt32 : Integer32("sint32") {
        override val wireType: WireType = WireType.VarInt

        override fun decode(wire: WireValue, onValue: (Int) -> Unit) = when (wire) {
            is WireValue.VarInt -> onValue(wire.value.toInt().decodeSInt32())
            is WireValue.Len -> while (wire.value.remaining > 0) onValue(wire.value.readVarIntAsInt().decodeSInt32())
            is WireValue.Fixed32 -> error("Cannot decode sint32 from fixed32")
            is WireValue.Fixed64 -> error("Cannot decode sint32 from fixed64")
        }

        override fun encode(value: Int, mask: Int): WireValue = WireValue.VarInt(
            value.encodeSInt32().toLong() and mask.toUInt().toLong()
        )

        override fun encode(value: Int) = WireValue.VarInt(value.encodeSInt32().toLong() and 0xFFFFFFFFL)
    }

    object SInt64 : Integer64("sint64") {
        override val wireType: WireType = WireType.VarInt

        override fun decode(wire: WireValue, onValue: (Long) -> Unit) = when (wire) {
            is WireValue.VarInt -> onValue(wire.value.decodeSInt64())
            is WireValue.Len -> while (wire.value.remaining > 0) onValue(wire.value.readVarIntAsLong().decodeSInt64())
            is WireValue.Fixed32 -> error("Cannot decode sint64 from fixed32")
            is WireValue.Fixed64 -> error("Cannot decode sint64 from fixed64")
        }

        override fun encode(value: Long) = WireValue.VarInt(value.encodeSInt64())
    }

    object Fixed32 : Integer32("fixed32") {
        override val wireType: WireType = WireType.Fixed32

        override fun decode(wire: WireValue, onValue: (Int) -> Unit) = when (wire) {
            is WireValue.Fixed32 -> onValue(wire.value)
            is WireValue.Len -> while (wire.value.remaining > 0) onValue(wire.value.readFixed32())
            is WireValue.Fixed64 -> error("Cannot decode fixed32 from fixed64")
            is WireValue.VarInt -> error("Cannot decode fixed32 from varint")
        }

        override fun encode(value: Int, mask: Int): WireValue = WireValue.Fixed32(value and mask)
        override fun encode(value: Int) = WireValue.Fixed32(value)
    }

    object Fixed64 : Integer64("fixed64") {
        override val wireType: WireType = WireType.Fixed64

        override fun decode(wire: WireValue, onValue: (Long) -> Unit) = when (wire) {
            is WireValue.Fixed64 -> onValue(wire.value)
            is WireValue.Len -> while (wire.value.remaining > 0) onValue(wire.value.readFixed64())
            is WireValue.Fixed32 -> error("Cannot decode fixed64 from fixed32")
            is WireValue.VarInt -> error("Cannot decode fixed32 from varint")
        }

        override fun encode(value: Long) = WireValue.Fixed64(value)
    }

    object SFixed32 : Integer32("sfixed32") {
        override val wireType: WireType = WireType.Fixed32

        override fun decode(wire: WireValue, onValue: (Int) -> Unit) = when (wire) {
            is WireValue.Fixed32 -> onValue(wire.value)
            is WireValue.Len -> while (wire.value.remaining > 0) onValue(wire.value.readFixed32())
            is WireValue.Fixed64 -> error("Cannot decode fixed32 from fixed64")
            is WireValue.VarInt -> error("Cannot decode fixed32 from varint")
        }

        override fun encode(value: Int, mask: Int): WireValue = WireValue.Fixed32(value and mask)
        override fun encode(value: Int) = WireValue.Fixed32(value)
    }

    object SFixed64 : Integer64("sfixed64") {
        override val wireType: WireType = WireType.Fixed64

        override fun decode(wire: WireValue, onValue: (Long) -> Unit) = when (wire) {
            is WireValue.Fixed64 -> onValue(wire.value)
            is WireValue.Len -> while (wire.value.remaining > 0) onValue(wire.value.readFixed64())
            is WireValue.Fixed32 -> error("Cannot decode fixed64 from fixed32")
            is WireValue.VarInt -> error("Cannot decode fixed32 from varint")
        }

        override fun encode(value: Long) = WireValue.Fixed64(value)
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

        override fun encode(value: Boolean) = WireValue.VarInt(if (value) 1 else 0)
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

        override fun encode(value: kotlin.String) = WireValue.Len(WireBuffer(value.toByteArray()))
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

        override fun encode(value: ByteArray) = WireValue.Len(WireBuffer(value))
    }

    /**
     * A reference to a message or enum type.
     */
    data class Reference(val components: List<Identifier>) : FieldType() {
        override val isPackable = false

        init {
            require(components.isNotEmpty()) { "Type reference must not be empty" }
        }

        override fun toString() = components.joinToString(".")
    }
}

val FieldType.isUnsigned get() = this is FieldType.UInt32 || this is FieldType.UInt64
