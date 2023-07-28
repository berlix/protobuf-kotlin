package pro.felixo.proto3

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

@SerialInfo
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
@OptIn(ExperimentalSerializationApi::class)
annotation class ProtoNumber(val number: Int)

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
@OptIn(ExperimentalSerializationApi::class)
annotation class ProtoMapEntry(val keyName: String = "key", val valueName: String = "value")

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
@OptIn(ExperimentalSerializationApi::class)
annotation class ProtoDefaultEnumValue

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
@OptIn(ExperimentalSerializationApi::class)
annotation class ProtoIntegerType(val type: IntegerType)

enum class IntegerType {
    /**
     * Uses variable-length encoding. Inefficient for encoding negative numbers – if your field is likely to have
     * negative values, use [Signed] instead.
     */
    Default,

    /**
     * Uses variable-length encoding.
     */
    Unsigned,

    /**
     * Uses variable-length encoding. Signed int value. This more efficiently encodes negative numbers than [Default].
     */
    Signed,

    /**
     * Uses fixed-length encoding.
     *
     * In the case of 32-bit numbers: Always four bytes. More efficient than [Unsigned] if values are often greater
     * than 2^28.
     *
     * In the case of 64-bit numbers: Always eight bytes. More efficient than [Unsigned] if values are often greater
     * than 2^56.
     */
    Fixed,

    /**
     * Uses fixed-length encoding.
     *
     * In the case of 32-bit numbers: Always four bytes.
     *
     * In the case of 64-bit numbers: Always eight bytes.
     */
    SignedFixed
}
