package pro.felixo.proto3.wire

sealed interface WireValue {
    @JvmInline
    value class VarInt(val value: Long) : WireValue
    @JvmInline
    value class Fixed64(val value: Long) : WireValue
    @JvmInline
    value class Len(val value: WireInput) : WireValue
    @JvmInline
    value class Fixed32(val value: Int) : WireValue
}
