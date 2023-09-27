package pro.felixo.protobuf.serialization.integrationtests

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.StringSpec
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pro.felixo.protobuf.serialization.ProtoNumber
import pro.felixo.protobuf.serialization.testutil.IntegrationTestUtil
import kotlin.jvm.JvmInline

class InlineIntegrationTest : StringSpec({
    isolationMode = IsolationMode.InstancePerTest
    with (IntegrationTestUtil()) {

        "creates message for class with list of value class" {
            givenSchema(
                ClassWithListOfValueClass.serializer().descriptor,
                encodeZeroValues = true
            )
            verifySchema(
                """
                message ClassWithListOfValueClass {
                    repeated int32 list = 1;
                }
                """
            )
            verifyConversion(ClassWithListOfValueClass(emptyList()), "")
            verifyConversion(ClassWithListOfValueClass(listOf(IntValueClass(0))), """1: { 0 }""")
            verifyConversion(
                ClassWithListOfValueClass(
                    listOf(
                        IntValueClass(5),
                        IntValueClass(6)
                    )
                ),
                """1: { 5 6 }"""
            )

            givenSchema(
                ClassWithListOfValueClass.serializer().descriptor,
                encodeZeroValues = false
            )
            verifyEncode(ClassWithListOfValueClass(emptyList()), "")
            verifyEncode(ClassWithListOfValueClass(listOf(IntValueClass(0))), """1: { 0 }""")
        }

        "creates message for class with list of nullable value class" {
            givenSchema(
                ClassWithListOfNullableValueClass.serializer().descriptor,
                encodeZeroValues = true
            )
            verifySchema(
                """
                message ClassWithListOfNullableValueClass {
                    repeated ListItem list = 1;
                    message ListItem {
                        optional int32 value = 1;
                    }
                }
                """
            )
            verifyConversion(ClassWithListOfNullableValueClass(emptyList()), "")
            verifyConversion(ClassWithListOfNullableValueClass(listOf(null)), "1: {}")
            verifyConversion(ClassWithListOfNullableValueClass(listOf(IntValueClass(0))), """1: { 1: 0 }""")
            verifyConversion(
                ClassWithListOfNullableValueClass(
                    listOf(
                        IntValueClass(5),
                        IntValueClass(6)
                    )
                ),
                """1: { 1: 5 } 1: { 1: 6 }"""
            )
            verifyConversion(
                ClassWithListOfNullableValueClass(listOf(null, IntValueClass(7), null)),
                """1: {} 1: { 1: 7 } 1: {}"""
            )

            givenSchema(
                ClassWithListOfNullableValueClass.serializer().descriptor,
                encodeZeroValues = false
            )
            verifyEncode(ClassWithListOfNullableValueClass(listOf(null)), "1: {}")
            verifyEncode(ClassWithListOfNullableValueClass(listOf(IntValueClass(0))), """1: { 1: 0 }""")
        }

        "creates class with value class property with custom serializer" {
            givenSchema(ClassWithStringIntValueClassProperty.serializer().descriptor)
            verifySchema(
                """
                message ClassWithStringIntValueClassProperty {
                    string stringInt = 5;
                }
                """
            )
            verifyConversion(
                ClassWithStringIntValueClassProperty(StringIntValueClass(5)),
                """
                5: {"5"}
                """
            )
        }

        "creates class with int value class property" {
            givenSchema(
                ClassWithIntValueClassProperty.serializer().descriptor,
                encodeZeroValues = true
            )
            verifySchema(
                """
                message ClassWithIntValueClassProperty {
                    int32 int = 5;
                }
                """
            )
            verifyConversion(
                ClassWithIntValueClassProperty(IntValueClass(0)),
                "5: 0"
            )
            verifyConversion(
                ClassWithIntValueClassProperty(IntValueClass(33)),
                """
                5: 33
                """
            )

            givenSchema(
                ClassWithIntValueClassProperty.serializer().descriptor,
                encodeZeroValues = false
            )
            verifyConversion(
                ClassWithIntValueClassProperty(IntValueClass(0)),
                ""
            )
        }

        "creates class with nullable int value class property" {
            givenSchema(
                ClassWithNullableIntValueClassProperty.serializer().descriptor,
                encodeZeroValues = true
            )
            verifySchema(
                """
                message ClassWithNullableIntValueClassProperty {
                    optional int32 int = 5;
                }
                """
            )
            verifyConversion(
                ClassWithNullableIntValueClassProperty(NullableIntValueClass(0)),
                "5: 0"
            )
            verifyConversion(
                ClassWithNullableIntValueClassProperty(NullableIntValueClass(33)),
                """
                5: 33
                """
            )

            givenSchema(
                ClassWithNullableIntValueClassProperty.serializer().descriptor,
                encodeZeroValues = false
            )
            verifyConversion(
                ClassWithNullableIntValueClassProperty(NullableIntValueClass(0)),
                "5: 0"
            )
        }

        "creates class with int value class nullable property" {
            givenSchema(
                ClassWithIntValueClassNullableProperty.serializer().descriptor,
                encodeZeroValues = true
            )
            verifySchema(
                """
                message ClassWithIntValueClassNullableProperty {
                    optional int32 int = 5;
                }
                """
            )
            verifyConversion(
                ClassWithIntValueClassNullableProperty(IntValueClass(0)),
                "5: 0"
            )
            verifyConversion(
                ClassWithIntValueClassNullableProperty(IntValueClass(33)),
                """
                5: 33
                """
            )

            givenSchema(
                ClassWithIntValueClassNullableProperty.serializer().descriptor,
                encodeZeroValues = false
            )
            verifyConversion(
                ClassWithIntValueClassNullableProperty(IntValueClass(0)),
                "5: 0"
            )
        }

        "creates class with bytes value class property" {
            givenSchema(
                ClassWithBytesValueClassProperty.serializer().descriptor,
                encodeZeroValues = true
            )
            verifySchema(
                """
                message ClassWithBytesValueClassProperty {
                    bytes bytes = 5;
                }
                """
            )
            verifyConversion(
                ClassWithBytesValueClassProperty(BytesValueClass(byteArrayOf())),
                "5: {}"
            )
            verifyConversion(
                ClassWithBytesValueClassProperty(BytesValueClass(byteArrayOf(1, 2, 0xff.toByte()))),
                """
                5: {`0102ff`}
                """
            )

            givenSchema(
                ClassWithBytesValueClassProperty.serializer().descriptor,
                encodeZeroValues = false
            )
            verifyConversion(
                ClassWithBytesValueClassProperty(BytesValueClass(byteArrayOf())),
                ""
            )
        }

        "creates class with nullable bytes value class property" {
            givenSchema(
                ClassWithNullableBytesValueClassProperty.serializer().descriptor,
                encodeZeroValues = true
            )
            verifySchema(
                """
                message ClassWithNullableBytesValueClassProperty {
                    optional bytes bytes = 5;
                }
                """
            )
            verifyConversion(
                ClassWithNullableBytesValueClassProperty(NullableBytesValueClass(null)),
                ""
            )
            verifyConversion(
                ClassWithNullableBytesValueClassProperty(NullableBytesValueClass(byteArrayOf())),
                "5: {}"
            )
            verifyConversion(
                ClassWithNullableBytesValueClassProperty(NullableBytesValueClass(byteArrayOf(1, 2, 0xff.toByte()))),
                """
                5: {`0102ff`}
                """
            )

            givenSchema(
                ClassWithNullableBytesValueClassProperty.serializer().descriptor,
                encodeZeroValues = false
            )
            verifyConversion(
                ClassWithNullableBytesValueClassProperty(NullableBytesValueClass(null)),
                ""
            )
            verifyConversion(
                ClassWithNullableBytesValueClassProperty(NullableBytesValueClass(byteArrayOf())),
                "5: {}"
            )
        }

        "creates class with bytes value class nullable property" {
            givenSchema(
                ClassWithBytesValueClassNullableProperty.serializer().descriptor,
                encodeZeroValues = true
            )
            verifySchema(
                """
                message ClassWithBytesValueClassNullableProperty {
                    optional bytes bytes = 5;
                }
                """
            )
            verifyConversion(
                ClassWithBytesValueClassNullableProperty(null),
                ""
            )
            verifyConversion(
                ClassWithBytesValueClassNullableProperty(BytesValueClass(byteArrayOf())),
                "5: {}"
            )
            verifyConversion(
                ClassWithBytesValueClassNullableProperty(BytesValueClass(byteArrayOf(1, 2, 0xff.toByte()))),
                """
                5: {`0102ff`}
                """
            )

            givenSchema(
                ClassWithBytesValueClassNullableProperty.serializer().descriptor,
                encodeZeroValues = false
            )
            verifyConversion(
                ClassWithBytesValueClassNullableProperty(null),
                ""
            )
            verifyConversion(
                ClassWithBytesValueClassNullableProperty(BytesValueClass(byteArrayOf())),
                "5: {}"
            )
        }

        "creates class hierarchy with value sub-class" {
            givenSchema(
                SuperClass.serializer().descriptor,
                encodeZeroValues = true
            )
            verifySchema(
                """
                message SuperClass {
                    oneof subtypes {
                        int32 subValueClassWithInt = 1;
                        string subValueClassWithString = 2;
                    }
                }
                """
            )
            verifyConversion<SuperClass>(
                SubValueClassWithInt(0),
                "1: 0"
            )
            verifyConversion<SuperClass>(
                SubValueClassWithInt(1),
                "1: 1"
            )
            verifyConversion<SuperClass>(
                SubValueClassWithString(""),
                "2: {}"
            )
            verifyConversion<SuperClass>(
                SubValueClassWithString("foo"),
                """2: { "foo" }"""
            )

            givenSchema(
                SuperClass.serializer().descriptor,
                encodeZeroValues = false
            )
            verifyConversion<SuperClass>(
                SubValueClassWithInt(0),
                "1: 0"
            )
            verifyConversion<SuperClass>(
                SubValueClassWithString(""),
                "2: {}"
            )
        }

        "does not create synthetic top-level message from value class with custom serializer" {
            givenSchema(StringIntValueClass.serializer().descriptor)
            verifySchemaGenerationFails()
        }
    }
}) {

    @Serializable
    data class ClassWithIntValueClassProperty(
        @ProtoNumber(5)
        val int: IntValueClass
    )

    @Serializable
    data class ClassWithIntValueClassNullableProperty(
        @ProtoNumber(5)
        val int: IntValueClass?
    )

    @Serializable
    data class ClassWithNullableIntValueClassProperty(
        @ProtoNumber(5)
        val int: NullableIntValueClass
    )

    @Serializable
    data class ClassWithListOfValueClass(
        val list: List<IntValueClass>
    )

    @Serializable
    data class ClassWithListOfNullableValueClass(
        val list: List<IntValueClass?>
    )

    @Serializable
    data class ClassWithStringIntValueClassProperty(
        @ProtoNumber(5)
        val stringInt: StringIntValueClass
    )

    @Serializable
    data class ClassWithBytesValueClassProperty(
        @ProtoNumber(5)
        val bytes: BytesValueClass
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as ClassWithBytesValueClassProperty

            return bytes.value.contentEquals(other.bytes.value)
        }

        override fun hashCode(): Int = bytes.value.contentHashCode()

        @OptIn(ExperimentalStdlibApi::class)
        override fun toString(): String = bytes.value.toHexString()
    }

    @Serializable
    data class ClassWithNullableBytesValueClassProperty(
        @ProtoNumber(5)
        val bytes: NullableBytesValueClass
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as ClassWithNullableBytesValueClassProperty

            return bytes.value.contentEquals(other.bytes.value)
        }

        override fun hashCode(): Int = bytes.value.contentHashCode()

        @OptIn(ExperimentalStdlibApi::class)
        override fun toString(): String = bytes.value?.toHexString() ?: "null"
    }

    @Serializable
    data class ClassWithBytesValueClassNullableProperty(
        @ProtoNumber(5)
        val bytes: BytesValueClass?
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as ClassWithBytesValueClassNullableProperty

            return bytes?.value.contentEquals(other.bytes?.value)
        }

        override fun hashCode(): Int = bytes?.value?.contentHashCode() ?: 0

        @OptIn(ExperimentalStdlibApi::class)
        override fun toString(): String = bytes?.value?.toHexString() ?: "null"
    }

    @Serializable
    @JvmInline
    value class BytesValueClass(
        val value: ByteArray
    )

    @Serializable
    @JvmInline
    value class NullableBytesValueClass(
        val value: ByteArray?
    )

    @JvmInline
    @Serializable
    value class IntValueClass(val value: Int)

    @JvmInline
    @Serializable
    value class NullableIntValueClass(val value: Int?)

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

    @Serializable
    sealed interface SuperClass

    @JvmInline
    @Serializable
    @ProtoNumber(1)
    value class SubValueClassWithInt(val int: Int) : SuperClass

    @JvmInline
    @Serializable
    @ProtoNumber(2)
    value class SubValueClassWithString(val string: String) : SuperClass
}
