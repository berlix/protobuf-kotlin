package pro.felixo.proto3

import kotlinx.serialization.serializer
import org.junit.Test
import pro.felixo.proto3.testutil.ClassWithEnum
import pro.felixo.proto3.testutil.ClassWithMapOfReferences
import pro.felixo.proto3.testutil.ClassWithMapOfScalars
import pro.felixo.proto3.testutil.ClassWithMapWithCustomEntryPropertyNames
import pro.felixo.proto3.testutil.ClassWithNestedMaps
import pro.felixo.proto3.testutil.ClassWithNullableMap
import pro.felixo.proto3.testutil.EnumClass
import pro.felixo.proto3.testutil.SimpleClass
import kotlin.reflect.typeOf

class SchemaGeneratorMapTest : SchemaGeneratorBaseTest() {

    @Test
    fun `creates schema for class with map of scalars`() {
        verify(
            ClassWithMapOfScalars.serializer().descriptor,
            """
            message ClassWithMapOfScalars {
                repeated MapEntry map = 1;
                message MapEntry {
                    string key = 1;
                    int32 value = 2;
                }
            }
            """
        )
        verifyConversion(ClassWithMapOfScalars(emptyMap()), "")
        verifyConversion(ClassWithMapOfScalars(mapOf("key" to 5)), """1: { 1: { "key" } 2: 5 }""")
        verifyConversion(
            ClassWithMapOfScalars(mapOf("key" to 5, "key2" to 6)),
            """1: { 1: { "key" } 2: 5 } 1: { 1: { "key2" } 2: 6 }"""
        )
    }

    @Test
    fun `creates schema for class with nullable map`() {
        verify(
            ClassWithNullableMap.serializer().descriptor,
            """
            message ClassWithNullableMap {
                optional MapValue map = 1;
                message MapValue {
                    repeated MapEntry map = 1;
                    message MapEntry {
                        string key = 1;
                        int32 value = 2;
                    }
                }
            }
            """
        )
        verifyConversion(ClassWithNullableMap(null), "")
        verifyConversion(ClassWithNullableMap(emptyMap()), "1: {}")
        verifyConversion(ClassWithNullableMap(mapOf("key" to 5)), """1: { 1: { 1: { "key" } 2: 5 } }""")
        verifyConversion(
            ClassWithNullableMap(mapOf("key" to 5, "key2" to 6)),
            """1: { 1: { 1: { "key" } 2: 5 } 1: { 1: { "key2" } 2: 6 } }"""
        )
    }

    @Test
    fun `creates schema for class with nested maps`() {
        verify(
            ClassWithNestedMaps.serializer().descriptor,
            """
            message ClassWithNestedMaps {
                repeated MapEntry map = 1;
                message MapEntry {
                    message KeyEntry {
                        string key = 1;
                        int32 value = 2;
                    }
                    message ValueEntry {
                        string key = 1;
                        int32 value = 2;
                    }
                    repeated KeyEntry key = 1;
                    repeated ValueEntry value = 2;
                }
            }
            """
        )
        verifyConversion(ClassWithNestedMaps(emptyMap()), "")
        verifyConversion(
            ClassWithNestedMaps(
                mapOf(
                    mapOf("k1" to 1) to mapOf("v1" to 11),
                    mapOf("k2" to 2) to mapOf("v2" to 12)
                )
            ),
            """1: { 1: { 1: { "k1" } 2: 1 } 2: { 1: { "v1" } 2: 11 } }
               1: { 1: { 1: { "k2" } 2: 2 } 2: { 1: { "v2" } 2: 12 } }"""
        )
    }

    @Test
    fun `creates schema for class with map with custom entry property names`() {
        verify(
            ClassWithMapWithCustomEntryPropertyNames.serializer().descriptor,
            """
            message ClassWithMapWithCustomEntryPropertyNames {
                repeated MapEntry map = 1;
                message MapEntry {
                    int32 customKey = 1;
                    int32 customValue = 2;
                }
            }
            """
        )
        verifyConversion(ClassWithMapWithCustomEntryPropertyNames(emptyMap()), "")
        verifyConversion(ClassWithMapWithCustomEntryPropertyNames(mapOf(1 to 5)), """1: { 1: 1 2: 5 }""")
        verifyConversion(
            ClassWithMapWithCustomEntryPropertyNames(mapOf(1 to 5, 2 to 6)),
            """1: { 1: 1 2: 5 } 1: { 1: 2 2: 6 }"""
        )
    }

    @Test
    fun `creates schema for class with map of references`() {
        verify(
            ClassWithMapOfReferences.serializer().descriptor,
            """
            message ClassWithMapOfReferences {
                repeated MapEntry map = 1;
                message MapEntry {
                    ClassWithEnum key = 1;
                    SimpleClass value = 2;
                }
            }
            message SimpleClass {
                int32 value = 1;
            }
            message ClassWithEnum {
              optional EnumClass enum = 1;
            }
            enum EnumClass {
              A = 0;
              B = 1;
              C = 2;
            }
            """
        )
        verifyConversion(ClassWithMapOfReferences(emptyMap()), "")
        verifyConversion(
            ClassWithMapOfReferences(mapOf(ClassWithEnum(EnumClass.A) to SimpleClass(5))),
            """1: { 1: { 1: 0 } 2: { 1: 5 } }"""
        )
        verifyConversion(
            ClassWithMapOfReferences(
                mapOf(
                    ClassWithEnum(EnumClass.A) to SimpleClass(5),
                    ClassWithEnum(EnumClass.B) to SimpleClass(-1),
                )
            ),
            """1: { 1: { 1: 0 } 2: { 1: 5 } } 1: { 1: { 1: 1 } 2: { 1: -1 } }"""
        )
    }

    @Test
    fun `does not create schema with synthetic top-level message for map`() =
        verifyFailure(serializer(typeOf<Map<String, Int>>()).descriptor)
}
