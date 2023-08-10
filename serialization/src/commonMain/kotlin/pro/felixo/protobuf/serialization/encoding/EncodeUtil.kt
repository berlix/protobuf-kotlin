package pro.felixo.protobuf.serialization.encoding

import pro.felixo.protobuf.serialization.util.then
import pro.felixo.protobuf.wire.WireBuffer
import pro.felixo.protobuf.wire.WireValue

fun varInt(value: Int, encodeZeroValue: Boolean) = (encodeZeroValue || value != 0)
    .then { WireValue.VarInt(value.toLong()) }

fun varInt(value: Long, encodeZeroValue: Boolean) = (encodeZeroValue || value != 0L).then { WireValue.VarInt(value) }
fun fixed32(value: Int, encodeZeroValue: Boolean) = (encodeZeroValue || value != 0).then { WireValue.Fixed32(value) }
fun fixed64(value: Long, encodeZeroValue: Boolean) = (encodeZeroValue || value != 0L).then { WireValue.Fixed64(value) }

fun len(value: ByteArray, encodeZeroValue: Boolean) = (encodeZeroValue || value.isNotEmpty())
    .then { WireValue.Len(WireBuffer(value)) }

fun len(value: String, encodeZeroValue: Boolean) = (encodeZeroValue || value.isNotEmpty())
    .then { WireValue.Len(WireBuffer(value.toByteArray())) }
