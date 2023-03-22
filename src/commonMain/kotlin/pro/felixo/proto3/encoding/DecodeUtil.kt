package pro.felixo.proto3.encoding

import pro.felixo.proto3.FieldType
import pro.felixo.proto3.wire.WireInput
import pro.felixo.proto3.wire.WireOutput
import pro.felixo.proto3.wire.WireValue

fun <TField : FieldType.Scalar<TDecoded>, TDecoded: Any> decodeLast(
    wireValues: List<WireValue>?,
    type: TField
): TDecoded? {
    var ret: TDecoded? = null
    wireValues?.forEach { v -> type.decode(v) { ret = it } }
    return ret
}

fun <TField : FieldType.Scalar<TDecoded>, TDecoded: Any> decode(
    wireValue: WireValue,
    type: TField
): TDecoded? {
    var ret: TDecoded? = null
    type.decode(wireValue) { ret = it }
    return ret
}

fun concatLenValues(wireValues: List<WireValue.Len>): WireValue.Len = when (wireValues.size) {
    1 -> wireValues[0]
    0 -> WireValue.Len(WireInput(ByteArray(0)))
    else -> {
        val out = WireOutput()
        for (value in wireValues)
            out.write(value.value)
        WireValue.Len(WireInput(out.getBytes()))
    }
}
