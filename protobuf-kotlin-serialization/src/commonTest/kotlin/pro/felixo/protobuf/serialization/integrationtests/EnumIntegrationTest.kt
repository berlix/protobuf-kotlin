package pro.felixo.protobuf.serialization.integrationtests

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.StringSpec
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pro.felixo.protobuf.serialization.ProtoDefaultEnumValue
import pro.felixo.protobuf.serialization.ProtoNumber
import pro.felixo.protobuf.serialization.testutil.ClassWithNullableEnumClassMember
import pro.felixo.protobuf.serialization.testutil.EnumClass
import pro.felixo.protobuf.serialization.testutil.IntegrationTestUtil

class EnumIntegrationTest : StringSpec({
    isolationMode = IsolationMode.InstancePerTest
    with (IntegrationTestUtil()) {
        "creates enum" {
            givenSchema(
                ClassWithEnumClassMember.serializer().descriptor,
                encodeZeroValues = true
            )
            verifySchema(
                """
            message ClassWithEnumClassMember {
              EnumClass enum = 1;
            }

            enum EnumClass {
              A = 0;
              B = 1;
              C = 2;
            }
            """
            )
            verifyConversion(ClassWithEnumClassMember(EnumClass.A), "1: 0")
            verifyConversion(ClassWithEnumClassMember(EnumClass.B), "1: 1")
            verifyConversion(ClassWithEnumClassMember(EnumClass.C), "1: 2")
            verifyDecode(ClassWithEnumClassMember(EnumClass.A), "1: 5")
            verifyDecode(ClassWithEnumClassMember(EnumClass.A), "")

            givenSchema(
                ClassWithEnumClassMember.serializer().descriptor,
                encodeZeroValues = false
            )
            verifyEncode(ClassWithEnumClassMember(EnumClass.A), "")
            verifyEncode(ClassWithEnumClassMember(EnumClass.B), "1: 1")
        }

        "creates nullable enum" {
            givenSchema(
                ClassWithNullableEnumClassMember.serializer().descriptor,
                encodeZeroValues = true
            )
            verifySchema(
                """
            message ClassWithNullableEnumClassMember {
              optional EnumClass enum = 1;
            }

            enum EnumClass {
              A = 0;
              B = 1;
              C = 2;
            }
            """
            )
            verifyConversion(ClassWithNullableEnumClassMember(null), "")
            verifyConversion(ClassWithNullableEnumClassMember(EnumClass.A), "1: 0")
            verifyConversion(ClassWithNullableEnumClassMember(EnumClass.B), "1: 1")
            verifyConversion(ClassWithNullableEnumClassMember(EnumClass.C), "1: 2")
            verifyDecode(ClassWithNullableEnumClassMember(EnumClass.A), "1: 5")

            givenSchema(
                ClassWithNullableEnumClassMember.serializer().descriptor,
                encodeZeroValues = false
            )
            verifyEncode(ClassWithNullableEnumClassMember(null), "")
            verifyEncode(ClassWithNullableEnumClassMember(EnumClass.A), "1: 0")
            verifyEncode(ClassWithNullableEnumClassMember(EnumClass.B), "1: 1")
        }

        "creates enum with custom numbers" {
            givenSchema(
                ClassWithEnumClassWithCustomNumbersMember.serializer().descriptor,
                encodeZeroValues = true
            )
            verifySchema(
                """
            message ClassWithEnumClassWithCustomNumbersMember {
                EnumClassWithCustomNumbers enum = 1;
            }

            enum EnumClassWithCustomNumbers {
              C = 0;
              A = 5;
              B = 1;
              D = 2;
            }
            """
            )
            verifyConversion(ClassWithEnumClassWithCustomNumbersMember(EnumClassWithCustomNumbers.A), "1: 5")
            verifyConversion(ClassWithEnumClassWithCustomNumbersMember(EnumClassWithCustomNumbers.B), "1: 1")
            verifyConversion(ClassWithEnumClassWithCustomNumbersMember(EnumClassWithCustomNumbers.C), "1: 0")
            verifyConversion(ClassWithEnumClassWithCustomNumbersMember(EnumClassWithCustomNumbers.D), "1: 2")
            verifyDecode(ClassWithEnumClassWithCustomNumbersMember(EnumClassWithCustomNumbers.C), "1: 7")
            verifyDecode(ClassWithEnumClassWithCustomNumbersMember(EnumClassWithCustomNumbers.C), "")

            givenSchema(
                ClassWithEnumClassWithCustomNumbersMember.serializer().descriptor,
                encodeZeroValues = false
            )
            verifyEncode(ClassWithEnumClassWithCustomNumbersMember(EnumClassWithCustomNumbers.B), "1: 1")
            verifyEncode(ClassWithEnumClassWithCustomNumbersMember(EnumClassWithCustomNumbers.C), "")
        }

        "creates enum with custom serial name" {
            givenSchema(
                ClassWithEnumClassWithCustomSerialNameMember.serializer().descriptor,
                encodeZeroValues = true
            )
            verifySchema(
                """
            message ClassWithEnumClassWithCustomSerialNameMember {
                CustomName enum = 1;
            }

            enum CustomName {
                A = 0;
            }
            """
            )
            verifyConversion(ClassWithEnumClassWithCustomSerialNameMember(EnumClassWithCustomSerialName.A), "1: 0")
            verifyDecode(ClassWithEnumClassWithCustomSerialNameMember(EnumClassWithCustomSerialName.A), "1: 7")
            verifyDecode(ClassWithEnumClassWithCustomSerialNameMember(EnumClassWithCustomSerialName.A), "")
        }

        "creates enum with value with custom serial name" {
            givenSchema(
                ClassWithEnumClassWithValueWithCustomSerialNameMember.serializer().descriptor,
                encodeZeroValues = true
            )
            verifySchema(
                """
            message ClassWithEnumClassWithValueWithCustomSerialNameMember {
                EnumClassWithValueWithCustomSerialName enum = 1;
            }

            enum EnumClassWithValueWithCustomSerialName {
                CUSTOM_NAME = 0;
            }
            """
            )
            verifyConversion(
                ClassWithEnumClassWithValueWithCustomSerialNameMember(EnumClassWithValueWithCustomSerialName.A),
                "1: 0"
            )
            verifyDecode(
                ClassWithEnumClassWithValueWithCustomSerialNameMember(EnumClassWithValueWithCustomSerialName.A),
                "1: 7"
            )
            verifyDecode(
                ClassWithEnumClassWithValueWithCustomSerialNameMember(EnumClassWithValueWithCustomSerialName.A),
                ""
            )
        }
    }
}) {
    @Serializable
    data class ClassWithEnumClassMember(val enum: EnumClass)

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
}
