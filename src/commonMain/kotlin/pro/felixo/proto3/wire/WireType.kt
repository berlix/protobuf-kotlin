package pro.felixo.proto3.wire

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
            0 -> VarInt
            1 -> Fixed64
            2 -> Len
            5 -> Fixed32
            3 -> SGroup
            4 -> EGroup
            else -> error("Invalid wire type: $value")
        }
    }
}
