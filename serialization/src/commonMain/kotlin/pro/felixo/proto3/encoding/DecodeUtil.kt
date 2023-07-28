package pro.felixo.proto3.encoding

import pro.felixo.proto3.FieldType
import pro.felixo.proto3.wire.WireBuffer
import pro.felixo.proto3.wire.WireValue

fun <TField : FieldType.Scalar<TDecoded>, TDecoded: Any> decodeLast(
    wireValues: List<WireValue>?,
    type: TField
): TDecoded? {
    var ret: TDecoded? = null
    wireValues?.forEach { v -> type.decode(v) { ret = it } }
    return ret
}

fun concatLenValues(wireValues: List<WireValue.Len>): WireValue.Len = when (wireValues.size) {
    1 -> wireValues[0]
    0 -> WireValue.Len(WireBuffer(ByteArray(0))) // TODO performance: make special EmptyLen value
    else -> {
        val out = WireBuffer()
        for (value in wireValues)
            out.write(value.value)
        WireValue.Len(out)
    }
}
