package pro.felixo.protobuf.wire

/**
 * Represents a protobuf wire type.
 */
@Suppress("MagicNumber")
enum class WireType(
    /**
     * The numeric value of the wire type, as it is encoded in the protobuf binary format.
     */
    val value: Int
) {
    VarInt(0),
    Fixed64(1),
    Len(2),
    Fixed32(5),

    /**
     * Deprecated in protobuf spec and generally ignored by this library.
     */
    SGroup(3),
    /**
     * Deprecated in protobuf spec and generally ignored by this library.
     */
    EGroup(4);

    companion object {
        /**
         * Returns the [WireType] that corresponds to the given numeric [value], or throws an exception if the value
         * does not correspond to any known wire type.
         */
        fun of(value: Int) = when (value) {
            VarInt.value -> VarInt
            Fixed64.value -> Fixed64
            Len.value -> Len
            Fixed32.value -> Fixed32
            SGroup.value -> SGroup
            EGroup.value -> EGroup
            else -> throw IllegalArgumentException("Invalid wire type: $value")
        }
    }
}
