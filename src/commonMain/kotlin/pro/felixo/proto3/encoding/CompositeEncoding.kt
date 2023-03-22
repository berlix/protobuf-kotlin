package pro.felixo.proto3.encoding

import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeEncoder
import pro.felixo.proto3.wire.WireOutput
import pro.felixo.proto3.wire.WireValue

data class CompositeEncoding(
    val encoder: (output: WireOutput, isStandalone: Boolean) -> CompositeEncoder,
    val decoder: (value: List<WireValue>) -> CompositeDecoder
)
