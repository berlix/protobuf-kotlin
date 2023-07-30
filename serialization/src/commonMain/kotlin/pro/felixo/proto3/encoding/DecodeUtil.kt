package pro.felixo.proto3.encoding

import pro.felixo.proto3.FieldEncoding
import pro.felixo.proto3.wire.EMPTY_LEN
import pro.felixo.proto3.wire.WireBuffer
import pro.felixo.proto3.wire.WireValue

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
