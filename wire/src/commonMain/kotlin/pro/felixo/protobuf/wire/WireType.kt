package pro.felixo.protobuf.wire

@Suppress("MagicNumber")
enum class WireType(val value: Int) {
    VarInt(0),
    Fixed64(1),
    Len(2),
    Fixed32(5),

    /**
     * Deprecated in protobuf spec and ignored.
     */
    SGroup(3),
    /**
     * Deprecated in protobuf spec and ignored.
     */
    EGroup(4);

    companion object {
        fun of(value: Int) = when (value) {
            VarInt.value -> VarInt
            Fixed64.value -> Fixed64
            Len.value -> Len
            Fixed32.value -> Fixed32
            SGroup.value -> SGroup
            EGroup.value -> EGroup
            else -> error("Invalid wire type: $value")
        }
    }
}
