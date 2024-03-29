package pro.felixo.protobuf.wire

import kotlin.jvm.JvmInline

sealed interface WireValue {
    @JvmInline
    value class VarInt(val value: Long) : WireValue
    @JvmInline
    value class Fixed64(val value: Long) : WireValue
    @JvmInline
    value class Len(val value: WireBuffer) : WireValue
    @JvmInline
    value class Fixed32(val value: Int) : WireValue
}
