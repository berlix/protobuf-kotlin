package pro.felixo.proto3.serialization.testutil

import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pro.felixo.proto3.serialization.IntegerType
import pro.felixo.proto3.serialization.ProtoDefaultEnumValue
import pro.felixo.proto3.serialization.ProtoIntegerType
import pro.felixo.proto3.serialization.ProtoMapEntry
import pro.felixo.proto3.serialization.ProtoNumber

@Serializable
class EmptyClass {
    override fun equals(other: Any?): Boolean = other != null && this::class == other::class
    override fun hashCode(): Int = 1
}

@Serializable
data class SimpleClass(val value: Int)

@Serializable
object Object {
    const val property: Int = 5
}

@Serializable
data class Scalars(
    val boolean: Boolean,
    val booleanNullable: Boolean?,
    val byte: Byte,
    val byteNullable: Byte?,
    val short: Short,
    val shortNullable: Short?,

    val int32: Int,
    val int32Nullable: Int?,
    @ProtoIntegerType(IntegerType.Default)
    val defaultInt32: Int,
    @ProtoIntegerType(IntegerType.Default)
    val defaultInt32Nullable: Int?,
    @ProtoIntegerType(IntegerType.Signed)
    val sint32: Int,
    @ProtoIntegerType(IntegerType.Signed)
    val sint32Nullable: Int?,
    @ProtoIntegerType(IntegerType.Unsigned)
    val uint32: Int,
    @ProtoIntegerType(IntegerType.Unsigned)
    val uint32Nullable: Int?,
    @ProtoIntegerType(IntegerType.Fixed)
    val fixedInt32: Int,
    @ProtoIntegerType(IntegerType.Fixed)
    val fixedInt32Nullable: Int?,
    @ProtoIntegerType(IntegerType.SignedFixed)
    val signedFixedInt32: Int,
    @ProtoIntegerType(IntegerType.SignedFixed)
    val signedFixedInt32Nullable: Int?,

    val int64: Long,
    val int64Nullable: Long?,
    @ProtoIntegerType(IntegerType.Default)
    val defaultInt64: Long,
    @ProtoIntegerType(IntegerType.Default)
    val defaultInt64Nullable: Long?,
    @ProtoIntegerType(IntegerType.Signed)
    val sint64: Long,
    @ProtoIntegerType(IntegerType.Signed)
    val sint64Nullable: Long?,
    @ProtoIntegerType(IntegerType.Unsigned)
    val uint64: Long,
    @ProtoIntegerType(IntegerType.Unsigned)
    val uint64Nullable: Long?,
    @ProtoIntegerType(IntegerType.Fixed)
    val fixedInt64: Long,
    @ProtoIntegerType(IntegerType.Fixed)
    val fixedInt64Nullable: Long?,
    @ProtoIntegerType(IntegerType.SignedFixed)
    val signedFixedInt64: Long,
    @ProtoIntegerType(IntegerType.SignedFixed)
    val signedFixedInt64Nullable: Long?,

    val float: Float,
    val floatNullable: Float?,
    val double: Double,
    val doubleNullable: Double?,
    val char: Char,
    val charNullable: Char?,
    val string: String,
    val stringNullable: String?,
    val bytes: ByteArray,
    val bytesNullable: ByteArray?,
) {
    @Suppress("CyclomaticComplexMethod")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Scalars

        if (boolean != other.boolean) return false
        if (booleanNullable != other.booleanNullable) return false
        if (byte != other.byte) return false
        if (byteNullable != other.byteNullable) return false
        if (short != other.short) return false
        if (shortNullable != other.shortNullable) return false
        if (int32 != other.int32) return false
        if (int32Nullable != other.int32Nullable) return false
        if (defaultInt32 != other.defaultInt32) return false
        if (defaultInt32Nullable != other.defaultInt32Nullable) return false
        if (sint32 != other.sint32) return false
        if (sint32Nullable != other.sint32Nullable) return false
        if (uint32 != other.uint32) return false
        if (uint32Nullable != other.uint32Nullable) return false
        if (fixedInt32 != other.fixedInt32) return false
        if (fixedInt32Nullable != other.fixedInt32Nullable) return false
        if (signedFixedInt32 != other.signedFixedInt32) return false
        if (signedFixedInt32Nullable != other.signedFixedInt32Nullable) return false
        if (int64 != other.int64) return false
        if (int64Nullable != other.int64Nullable) return false
        if (defaultInt64 != other.defaultInt64) return false
        if (defaultInt64Nullable != other.defaultInt64Nullable) return false
        if (sint64 != other.sint64) return false
        if (sint64Nullable != other.sint64Nullable) return false
        if (uint64 != other.uint64) return false
        if (uint64Nullable != other.uint64Nullable) return false
        if (fixedInt64 != other.fixedInt64) return false
        if (fixedInt64Nullable != other.fixedInt64Nullable) return false
        if (signedFixedInt64 != other.signedFixedInt64) return false
        if (signedFixedInt64Nullable != other.signedFixedInt64Nullable) return false
        if (float != other.float) return false
        if (floatNullable != other.floatNullable) return false
        if (double != other.double) return false
        if (doubleNullable != other.doubleNullable) return false
        if (char != other.char) return false
        if (charNullable != other.charNullable) return false
        if (string != other.string) return false
        if (stringNullable != other.stringNullable) return false
        if (!bytes.contentEquals(other.bytes)) return false
        if (bytesNullable != null) {
            if (other.bytesNullable == null) return false
            if (!bytesNullable.contentEquals(other.bytesNullable)) return false
        } else if (other.bytesNullable != null) return false

        return true
    }

    @Suppress("CyclomaticComplexMethod")
    override fun hashCode(): Int {
        var result = boolean.hashCode()
        result = 31 * result + (booleanNullable?.hashCode() ?: 0)
        result = 31 * result + byte
        result = 31 * result + (byteNullable ?: 0)
        result = 31 * result + short
        result = 31 * result + (shortNullable ?: 0)
        result = 31 * result + int32
        result = 31 * result + (int32Nullable ?: 0)
        result = 31 * result + defaultInt32
        result = 31 * result + (defaultInt32Nullable ?: 0)
        result = 31 * result + sint32
        result = 31 * result + (sint32Nullable ?: 0)
        result = 31 * result + uint32
        result = 31 * result + (uint32Nullable ?: 0)
        result = 31 * result + fixedInt32
        result = 31 * result + (fixedInt32Nullable ?: 0)
        result = 31 * result + signedFixedInt32
        result = 31 * result + (signedFixedInt32Nullable ?: 0)
        result = 31 * result + int64.hashCode()
        result = 31 * result + (int64Nullable?.hashCode() ?: 0)
        result = 31 * result + defaultInt64.hashCode()
        result = 31 * result + (defaultInt64Nullable?.hashCode() ?: 0)
        result = 31 * result + sint64.hashCode()
        result = 31 * result + (sint64Nullable?.hashCode() ?: 0)
        result = 31 * result + uint64.hashCode()
        result = 31 * result + (uint64Nullable?.hashCode() ?: 0)
        result = 31 * result + fixedInt64.hashCode()
        result = 31 * result + (fixedInt64Nullable?.hashCode() ?: 0)
        result = 31 * result + signedFixedInt64.hashCode()
        result = 31 * result + (signedFixedInt64Nullable?.hashCode() ?: 0)
        result = 31 * result + float.hashCode()
        result = 31 * result + (floatNullable?.hashCode() ?: 0)
        result = 31 * result + double.hashCode()
        result = 31 * result + (doubleNullable?.hashCode() ?: 0)
        result = 31 * result + char.hashCode()
        result = 31 * result + (charNullable?.hashCode() ?: 0)
        result = 31 * result + string.hashCode()
        result = 31 * result + (stringNullable?.hashCode() ?: 0)
        result = 31 * result + bytes.contentHashCode()
        result = 31 * result + (bytesNullable?.contentHashCode() ?: 0)
        return result
    }
}

@Serializable
data class UnsignedInts(
    @ProtoIntegerType(IntegerType.Unsigned)
    val ubyte: UByte,
    @ProtoIntegerType(IntegerType.Unsigned)
    val ubyteNullable: UByte?,

    @ProtoIntegerType(IntegerType.Unsigned)
    val ushort: UShort,
    @ProtoIntegerType(IntegerType.Unsigned)
    val ushortNullable: UShort?,

    @ProtoIntegerType(IntegerType.Unsigned)
    val uint32: UInt,
    @ProtoIntegerType(IntegerType.Unsigned)
    val uint32Nullable: UInt?,

    @ProtoIntegerType(IntegerType.Fixed)
    val fixedUint32: UInt,
    @ProtoIntegerType(IntegerType.Fixed)
    val fixedUint32Nullable: UInt?,

    @ProtoIntegerType(IntegerType.Unsigned)
    val uint64: ULong,
    @ProtoIntegerType(IntegerType.Unsigned)
    val uint64Nullable: ULong?,

    @ProtoIntegerType(IntegerType.Fixed)
    val fixedUint64: ULong,
    @ProtoIntegerType(IntegerType.Fixed)
    val fixedUint64Nullable: ULong?,

    @ProtoIntegerType(IntegerType.Default)
    val int32: UInt,
)

@Serializable
data class ClassWithCustomFieldNumbers(
    @ProtoNumber(5)
    val int: Int,
    val string: String,
    @ProtoNumber(8)
    val bool: Boolean,
    val long: Long?
)

@Serializable
data class ClassAWithReference(
    val ref: EmptyClass
)

@Serializable
data class ClassBWithReference(
    val ref: EmptyClass
)

@Serializable
data class ClassWithListOfInt(
    val list: List<Int>
)

@Serializable
data class ClassWithListOfUInt(
    val list: List<UInt>
)

@Serializable
data class ClassWithListOfFloat(
    val list: List<Float>
)

@Serializable
data class ClassWithListOfDouble(
    val list: List<Double>
)

@Serializable
data class ClassWithListOfString(
    val list: List<String>
)

@Serializable
data class ClassWithListOfBytes(
    val list: List<ByteArray>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClassWithListOfBytes

        return list.contentEquals(other.list)
    }

    override fun hashCode(): Int = list.contentHashCode()
}

@Serializable
data class ClassWithNullableList(
    val list: List<Int>?
)

@Serializable
data class ClassWithListOfNullableScalar(
    val list: List<Int?>
)

@Serializable
data class ClassWithNestedLists(
    val list: List<List<List<ByteArray>>>
) {
    override fun equals(other: Any?): Boolean =
        other is ClassWithNestedLists && list.contentEquals(other.list) { l1, r1 ->
            l1.contentEquals(r1) { l2, r2 ->
                l2.contentEquals(r2)
            }
        }

    override fun hashCode(): Int = list.contentHashCode { outer-> outer.contentHashCode { it.contentHashCode() } }
}

@Serializable
data class ClassWithListOfEmptyClass(
    val list: List<EmptyClass>
)

@Serializable
data class ClassWithListOfSimpleClass(
    val list: List<SimpleClass>
)

@Serializable
data class ClassWithListOfSealedClass(
    val list: List<SealedTopClass>
)

@Serializable
data class ClassWithMapOfScalars(
    val map: Map<String, Int>
)

@Serializable
data class ClassWithNullableMap(
    val map: Map<String, Int>?
)

@Serializable
data class ClassWithNestedMaps(
    val map: Map<Map<String, Int>, Map<String, Int>>
)

@Serializable
data class ClassWithMapWithCustomEntryPropertyNames(
    @ProtoMapEntry("customKey", "customValue")
    val map: Map<Int, Int>
)

@Serializable
data class ClassWithMapOfReferences(
    val map: Map<ClassWithEnum, SimpleClass>
)

@Serializable
data class ClassWithSelfReference(
    val ref: ClassWithSelfReference?
)

@Serializable
data class ClassAWithCycle(
    val b: ClassBWithCycle?
)

@Serializable
data class ClassBWithCycle(
    val a: ClassAWithCycle?
)

@Serializable
data class ClassWithEnum(
    val enum: EnumClass?
)

@Serializable
enum class EnumClass {
    @ProtoDefaultEnumValue
    A,
    B,
    C
}

@Serializable
data class ClassWithEnumClassMember(val enum: EnumClass)

@Serializable
data class ClassWithNullableEnumClassMember(val enum: EnumClass?)

@Serializable
enum class EnumClassWithCustomNumbers {
    @ProtoNumber(5)
    A,
    B,
    @ProtoDefaultEnumValue
    C,
    D
}

@Serializable
data class ClassWithEnumClassWithCustomNumbersMember(val enum: EnumClassWithCustomNumbers)

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

interface NonSealedInterface

@Serializable
@ProtoNumber(2)
data class NonSealedLevel2LeafClassA(
    val int: Int
) : NonSealedInterface

@Serializable
@ProtoNumber(3)
data class NonSealedLevel2LeafClassB(
    val intermediate: NonSealedLevel2Class
) : NonSealedInterface

@Serializable
abstract class NonSealedLevel2Class: NonSealedInterface

@Serializable
@ProtoNumber(4)
data class NonSealedLevel3LeafClass(
    val top: NonSealedInterface
) : NonSealedLevel2Class()

@Serializable
@SerialName("CustomName")
class ClassWithCustomSerialName {
    override fun equals(other: Any?): Boolean = other != null && this::class == other::class
    override fun hashCode(): Int = 1
}

@Serializable
data class ClassWithPropertyWithCustomSerialName(
    @SerialName("customName")
    val int: Int
)

@Serializable
@SerialName("CustomName")
enum class EnumClassWithCustomSerialName {
    @ProtoDefaultEnumValue
    A
}

@Serializable
data class ClassWithEnumClassWithCustomSerialNameMember(val enum: EnumClassWithCustomSerialName)

@Serializable
enum class EnumClassWithValueWithCustomSerialName {
    @ProtoDefaultEnumValue
    @SerialName("CUSTOM_NAME")
    A
}

@Serializable
data class ClassWithEnumClassWithValueWithCustomSerialNameMember(val enum: EnumClassWithValueWithCustomSerialName)

@Serializable
data class ClassWithPropertyWithCustomSerializer(
    @Serializable(StringIntSerializer::class)
    val int: Int
)

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class ClassWithPropertyWithCustomByteArraySerializer(
    @Serializable(BooleanAsByteArraySerializer::class)
    val boolBytes: Boolean
)

@Serializable
data class ClassWithValueClassProperty(
    val stringInt: StringIntValueClass
)

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

typealias StringInt = @Serializable(with = StringIntSerializer::class) Int

class StringIntSerializer : KSerializer<StringInt> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("StringInt", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): Int = decoder.decodeString().toInt()
    override fun serialize(encoder: Encoder, value: Int) = encoder.encodeString("$value")
}

data class ClassWithContextualSerializer(val int: Int)

@Serializable
data class SerializableClassWithContextualSerializer(val int: Int)

@Serializable
data class ClassWithContextualProperty(
    @Contextual
    val int: ClassWithContextualSerializer
)

@Serializable
data class ClassWithContextualAndSerializableProperty(
    @Contextual
    val int: SerializableClassWithContextualSerializer
)

class ClassWithContextualSerializerSerializer : KSerializer<ClassWithContextualSerializer> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ClassWithContextualSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ClassWithContextualSerializer =
        ClassWithContextualSerializer(decoder.decodeString().toInt())

    override fun serialize(encoder: Encoder, value: ClassWithContextualSerializer) =
        encoder.encodeString("${value.int}")
}

class SerializableClassWithContextualSerializerSerializer : KSerializer<SerializableClassWithContextualSerializer> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("SerializableClassWithContextualSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): SerializableClassWithContextualSerializer =
        SerializableClassWithContextualSerializer(decoder.decodeString().toInt())

    override fun serialize(encoder: Encoder, value: SerializableClassWithContextualSerializer) =
        encoder.encodeString("${value.int}")
}

@ExperimentalSerializationApi
class BooleanAsByteArraySerializer : KSerializer<Boolean> {
    private val elementDescriptor = Byte.serializer().descriptor

    override val descriptor: SerialDescriptor =
        ListDescriptor("BooleanAsByteArraySerializer", elementDescriptor)

    override fun deserialize(decoder: Decoder): Boolean = decoder.beginStructure(descriptor).run {
        require(decodeElementIndex(descriptor) == 0)
        val value = decodeByteElement(elementDescriptor, 0)
        endStructure(descriptor)
        value
    } == (-1).toByte()

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.beginStructure(descriptor).apply {
            encodeByteElement(elementDescriptor, 0, if (value) -1 else 0)
            endStructure(descriptor)
        }
    }
}

private fun List<ByteArray>.contentEquals(other: List<ByteArray>) =
    contentEquals(other) { l, r -> l.contentEquals(r) }

private fun List<ByteArray>.contentHashCode() =
    fold(0) { acc, e -> acc * 31 + e.contentHashCode() }

private fun <T> List<T>.contentEquals(other: List<T>, elementEquals: (T, T) -> Boolean) =
    size == other.size && zip(other).all { (l, r) -> elementEquals(l, r) }

private fun <T> List<T>.contentHashCode(elementHashCode: (T) -> Int) =
    fold(0) { acc, e -> acc * 31 + elementHashCode(e) }
