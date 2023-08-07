package pro.felixo.protobuf.serialization.encoding

import pro.felixo.protobuf.wire.EMPTY_LEN
import pro.felixo.protobuf.wire.WireBuffer
import pro.felixo.protobuf.wire.WireValue

fun <TField : FieldEncoding.Scalar<TDecoded>, TDecoded: Any> decodeLast(
    wireValues: List<WireValue>?,
    type: TField
): TDecoded? {
    var ret: TDecoded? = null
    wireValues?.forEach { v -> type.decode(v) { ret = it } }
    return ret
}

fun concatLenValues(wireValues: List<WireValue.Len>): WireValue.Len = when (wireValues.size) {
    1 -> wireValues[0]
    0 -> EMPTY_LEN
    else -> {
        val out = WireBuffer()
        for (value in wireValues)
            out.writeAndConsume(value.value)
        WireValue.Len(out)
    }
}
