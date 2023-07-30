package pro.felixo.proto3

import pro.felixo.proto3.testutil.ClassWithEnumClassMember
import pro.felixo.proto3.testutil.ClassWithEnumClassWithCustomNumbersMember
import pro.felixo.proto3.testutil.ClassWithEnumClassWithCustomSerialNameMember
import pro.felixo.proto3.testutil.ClassWithEnumClassWithValueWithCustomSerialNameMember
import pro.felixo.proto3.testutil.ClassWithNullableEnumClassMember
import pro.felixo.proto3.testutil.EnumClass
import pro.felixo.proto3.testutil.EnumClassWithCustomNumbers
import pro.felixo.proto3.testutil.EnumClassWithCustomSerialName
import pro.felixo.proto3.testutil.EnumClassWithValueWithCustomSerialName
import kotlin.test.Test

class SchemaGeneratorEnumTest : SchemaGeneratorBaseTest() {
    @Test
    fun `creates enum`() {
        verify(
            ClassWithEnumClassMember.serializer().descriptor,
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
    }

    @Test
    fun `creates nullable enum`() {
        verify(
            ClassWithNullableEnumClassMember.serializer().descriptor,
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
    }

    @Test
    fun `creates enum with custom numbers`() {
        verify(
            ClassWithEnumClassWithCustomNumbersMember.serializer().descriptor,
            """
            message ClassWithEnumClassWithCustomNumbersMember {
                EnumClassWithCustomNumbers enum = 1;
            }

            enum EnumClassWithCustomNumbers {
              A = 5;
              B = 1;
              C = 0;
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
    }

    @Test
    fun `creates enum class with custom serial name`() {
        verify(
            ClassWithEnumClassWithCustomSerialNameMember.serializer().descriptor,
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

    @Test
    fun `creates enum class with value with custom serial name`() {
        verify(
            ClassWithEnumClassWithValueWithCustomSerialNameMember.serializer().descriptor,
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
