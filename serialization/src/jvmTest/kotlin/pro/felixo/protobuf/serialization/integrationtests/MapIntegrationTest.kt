package pro.felixo.protobuf.serialization.integrationtests

import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import org.junit.Test
import pro.felixo.protobuf.serialization.IntegerType
import pro.felixo.protobuf.serialization.ProtoMapEntry
import pro.felixo.protobuf.serialization.testutil.ClassWithNullableEnumClassMember
import pro.felixo.protobuf.serialization.testutil.EnumClass
import pro.felixo.protobuf.serialization.testutil.SimpleClass
import kotlin.reflect.typeOf

class MapIntegrationTest : BaseIntegrationTest() {

    @Test
    fun `creates message for class with map of scalars`() {
        givenSchema(
            ClassWithMapOfScalars.serializer().descriptor,
            encodeZeroValues = true
        )
        verifySchema(
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
        verifyConversion(ClassWithMapOfScalars(mapOf("" to 0)), """1: { 1: { "" } 2: 0 }""")
        verifyConversion(ClassWithMapOfScalars(mapOf("key" to 5)), """1: { 1: { "key" } 2: 5 }""")
        verifyConversion(
            ClassWithMapOfScalars(mapOf("key" to 5, "key2" to 6)),
            """1: { 1: { "key" } 2: 5 } 1: { 1: { "key2" } 2: 6 }"""
        )

        givenSchema(
            ClassWithMapOfScalars.serializer().descriptor,
            encodeZeroValues = false
        )
        verifyEncode(ClassWithMapOfScalars(mapOf("" to 0)), """1: {}""")
    }

    @Test
    fun `creates message for class with nullable map`() {
        givenSchema(
            ClassWithNullableMap.serializer().descriptor,
            encodeZeroValues = true
        )
        verifySchema(
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
        verifyConversion(ClassWithNullableMap(mapOf("" to 0)), """1: { 1: { 1: { "" } 2: 0 } }""")
        verifyConversion(ClassWithNullableMap(mapOf("key" to 5)), """1: { 1: { 1: { "key" } 2: 5 } }""")
        verifyConversion(
            ClassWithNullableMap(mapOf("key" to 5, "key2" to 6)),
            """1: { 1: { 1: { "key" } 2: 5 } 1: { 1: { "key2" } 2: 6 } }"""
        )

        givenSchema(
            ClassWithNullableMap.serializer().descriptor,
            encodeZeroValues = false
        )
        verifyEncode(ClassWithNullableMap(emptyMap()), "1: {}")
        verifyEncode(ClassWithNullableMap(mapOf("" to 0)), """1: { 1: {} }""")
    }

    @Test
    fun `creates message for class with nested maps`() {
        givenSchema(
            ClassWithNestedMaps.serializer().descriptor
        )
        verifySchema(
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
    fun `creates message for class with map with custom entry property names`() {
        givenSchema(
            ClassWithMapWithCustomEntrySettings.serializer().descriptor
        )
        verifySchema(
            """
            message ClassWithMapWithCustomEntrySettings {
                repeated CustomMapEntry map = 1;
                message CustomMapEntry {
                    fixed32 customKey = 3;
                    sint32 customValue = 4;
                }
            }
            """
        )
        verifyConversion(ClassWithMapWithCustomEntrySettings(emptyMap()), "")
        verifyConversion(
            ClassWithMapWithCustomEntrySettings(mapOf(1 to 5, 2 to -6)),
            """1: { 3: 1i32 4: 5z } 1: { 3: 2i32 4: -6z }"""
        )
    }

    @Test
    fun `creates message for class with map of references`() {
        givenSchema(
            ClassWithMapOfReferences.serializer().descriptor,
            encodeZeroValues = true
        )
        verifySchema(
            """
            message ClassWithMapOfReferences {
              message MapEntry {
                ClassWithNullableEnumClassMember key = 1;
                SimpleClass value = 2;
              }
              repeated MapEntry map = 1;
            }
            
            message ClassWithNullableEnumClassMember {
              optional EnumClass enum = 1;
            }
            
            enum EnumClass {
              A = 0;
              B = 1;
              C = 2;
            }
            
            message SimpleClass {
              int32 value = 1;
            }
            """
        )
        verifyConversion(ClassWithMapOfReferences(emptyMap()), "")
        verifyConversion(
            ClassWithMapOfReferences(mapOf(ClassWithNullableEnumClassMember(EnumClass.A) to SimpleClass(0))),
            """1: { 1: { 1: 0 } 2: { 1: 0 } }"""
        )
        verifyConversion(
            ClassWithMapOfReferences(mapOf(ClassWithNullableEnumClassMember(EnumClass.B) to SimpleClass(5))),
            """1: { 1: { 1: 1 } 2: { 1: 5 } }"""
        )
        verifyConversion(
            ClassWithMapOfReferences(
                mapOf(
                    ClassWithNullableEnumClassMember(EnumClass.A) to SimpleClass(5),
                    ClassWithNullableEnumClassMember(EnumClass.B) to SimpleClass(-1),
                )
            ),
            """1: { 1: { 1: 0 } 2: { 1: 5 } } 1: { 1: { 1: 1 } 2: { 1: -1 } }"""
        )

        givenSchema(
            ClassWithMapOfReferences.serializer().descriptor,
            encodeZeroValues = false
        )
        verifyEncode(
            ClassWithMapOfReferences(mapOf(ClassWithNullableEnumClassMember(null) to SimpleClass(0))),
            """1: {}"""
        )
    }

    @Test
    fun `does not create schema with synthetic top-level message for map`() {
        givenSchema(serializer(typeOf<Map<String, Int>>()).descriptor)
        verifySchemaGenerationFails()
    }

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
    data class ClassWithMapWithCustomEntrySettings(
        @ProtoMapEntry(
            messageName = "CustomMapEntry",
            keyName = "customKey",
            keyNumber = 3,
            keyIntegerType = IntegerType.Fixed,
            valueName = "customValue",
            valueNumber = 4,
            valueIntegerType = IntegerType.Signed
        )
        val map: Map<Int, Int>
    )

    @Serializable
    data class ClassWithMapOfReferences(
        val map: Map<ClassWithNullableEnumClassMember, SimpleClass>
    )
}
