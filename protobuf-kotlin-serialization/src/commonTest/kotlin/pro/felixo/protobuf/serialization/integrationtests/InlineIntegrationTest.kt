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
import pro.felixo.protobuf.serialization.testutil.IntegrationTestUtil
import kotlin.jvm.JvmInline

class InlineIntegrationTest : StringSpec({
    isolationMode = IsolationMode.InstancePerTest
    with (IntegrationTestUtil()) {

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
                        optional string value = 1;
                    }
                }
                """
            )
            verifyConversion(ClassWithListOfNullableValueClass(emptyList()), "")
            verifyConversion(ClassWithListOfNullableValueClass(listOf(null)), "1: {}")
            verifyConversion(ClassWithListOfNullableValueClass(listOf(StringIntValueClass(0))), """1: { 1: { "0" } }""")
            verifyConversion(
                ClassWithListOfNullableValueClass(
                    listOf(
                        StringIntValueClass(5),
                        StringIntValueClass(6)
                    )
                ),
                """1: { 1: { "5" } } 1: { 1: { "6" } }"""
            )
            verifyConversion(
                ClassWithListOfNullableValueClass(listOf(null, StringIntValueClass(7), null)),
                """1: {} 1: { 1: { "7" } } 1: {}"""
            )

            givenSchema(
                ClassWithListOfNullableValueClass.serializer().descriptor,
                encodeZeroValues = false
            )
            verifyEncode(ClassWithListOfNullableValueClass(listOf(null)), "1: {}")
            verifyEncode(ClassWithListOfNullableValueClass(listOf(StringIntValueClass(0))), """1: { 1: { "0" } }""")
        }

        "creates class with value class property" {
            givenSchema(
                ClassWithValueClassProperty.serializer().descriptor
            )
            verifySchema(
                """
            message ClassWithValueClassProperty {
                string stringInt = 1;
            }
            """
            )
            verifyConversion(
                ClassWithValueClassProperty(StringIntValueClass(5)),
                """
            1: {"5"}
            """
            )
        }

        "does not create synthetic top-level message from value class with custom serializer" {
            givenSchema(StringIntValueClass.serializer().descriptor)
            verifySchemaGenerationFails()
        }

    }
}) {
    @Serializable
    data class ClassWithListOfNullableValueClass(
        val list: List<StringIntValueClass?>
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
}
