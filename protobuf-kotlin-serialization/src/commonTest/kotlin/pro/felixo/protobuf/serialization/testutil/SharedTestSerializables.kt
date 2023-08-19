package pro.felixo.protobuf.serialization.testutil

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pro.felixo.protobuf.serialization.ProtoDefaultEnumValue
import pro.felixo.protobuf.serialization.ProtoNumber
import kotlin.jvm.JvmInline

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

@JvmInline
@Serializable(with = StringIntValueClassSerializer::class)
value class StringIntValueClass(val value: Int)

class StringIntValueClassSerializer : KSerializer<StringIntValueClass> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("StringIntValueClass", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): StringIntValueClass =
        StringIntValueClass(decoder.decodeString().toInt())

    override fun serialize(encoder: Encoder, value: StringIntValueClass) = encoder.encodeString("${value.value}")
}
