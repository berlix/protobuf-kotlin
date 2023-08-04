package pro.felixo.proto3.serialization.testutil

import kotlinx.serialization.Serializable
import pro.felixo.proto3.serialization.ProtoDefaultEnumValue
import pro.felixo.proto3.serialization.ProtoNumber

@Serializable
class EmptyClass {
    override fun equals(other: Any?): Boolean = other != null && this::class == other::class
    override fun hashCode(): Int = 1
}

@Serializable
data class SimpleClass(val value: Int)

@Serializable
enum class EnumClass {
    @ProtoDefaultEnumValue
    A,
    B,
    C
}

@Serializable
data class ClassWithNullableEnumClassMember(val enum: EnumClass?)

@Serializable
sealed class SealedTopClass

@Serializable
@ProtoNumber(2)
data class SealedLevel2LeafClassA(
    val int: Int
) : SealedTopClass()

@Serializable
@ProtoNumber(3)
data class SealedLevel2LeafClassB(
    val intermediate: SealedLevel2Class
) : SealedTopClass()

@Serializable
sealed class SealedLevel2Class: SealedTopClass()

@Serializable
@ProtoNumber(4)
data class SealedLevel3LeafClass(
    val top: SealedTopClass
) : SealedLevel2Class()
