package pro.felixo.protobuf.serialization.integrationtests

import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import org.junit.Test
import pro.felixo.protobuf.serialization.testutil.EmptyClass
import pro.felixo.protobuf.serialization.testutil.EnumClass
import pro.felixo.protobuf.serialization.testutil.SealedLevel2LeafClassA
import pro.felixo.protobuf.serialization.testutil.SealedLevel3LeafClass
import pro.felixo.protobuf.serialization.testutil.SealedTopClass
import pro.felixo.protobuf.serialization.testutil.SimpleClass
import pro.felixo.protobuf.serialization.testutil.StringIntValueClass
import kotlin.reflect.typeOf

class ListIntegrationTest : BaseIntegrationTest() {

    @Test
    fun `creates message for class with list of int`() {
        givenSchema(
            ClassWithListOfInt.serializer().descriptor,
            encodeZeroValues = true
        )
        verifySchema(
            """
            message ClassWithListOfInt {
                repeated int32 list = 1;
            }
            """
        )
        verifyConversion(ClassWithListOfInt(emptyList()), "")
        verifyConversion(ClassWithListOfInt(listOf(0)), "1: { 0 }")
        verifyConversion(ClassWithListOfInt(listOf(-1)), "1: { -1 }")
        verifyConversion(ClassWithListOfInt(listOf(1, 2, 3)), "1: { 1 2 3 }")
        verifyDecode(ClassWithListOfInt(emptyList()), "1: {}")
        verifyDecode(ClassWithListOfInt(listOf(1, 2, 3)), "1: 1 1: 2 1: 3")
        verifyDecode(ClassWithListOfInt(listOf(1, 2, 3)), "1: { 1 2 } 1: { 3 }")

        givenSchema(
            ClassWithListOfInt.serializer().descriptor,
            encodeZeroValues = false
        )
        verifyEncode(ClassWithListOfInt(listOf(0)), "1: { 0 }")
    }

    @Test
    fun `creates message for class with list of UInt`() {
        givenSchema(
            ClassWithListOfUInt.serializer().descriptor
        )
        verifySchema(
            """
            message ClassWithListOfUInt {
                repeated int32 list = 1;
            }
            """
        )
        verifyConversion(ClassWithListOfUInt(emptyList()), "")
        verifyConversion(ClassWithListOfUInt(listOf(0u)), "1: { 0 }")
        verifyConversion(ClassWithListOfUInt(listOf(1u)), "1: { 1 }")
        verifyConversion(ClassWithListOfUInt(listOf(1u, 2u, 3u)), "1: { 1 2 3 }")
        verifyDecode(ClassWithListOfUInt(emptyList()), "1: {}")
        verifyDecode(ClassWithListOfUInt(listOf(1u, 2u, 3u)), "1: 1 1: 2 1: 3")
        verifyDecode(ClassWithListOfUInt(listOf(1u, 2u, 3u)), "1: { 1 2 } 1: { 3 }")

        givenSchema(
            ClassWithListOfUInt.serializer().descriptor,
            encodeZeroValues = false
        )
        verifyEncode(ClassWithListOfUInt(listOf(0u)), "1: { 0 }")
    }

    @Test
    fun `creates message for class with list of float`() {
        givenSchema(
            ClassWithListOfFloat.serializer().descriptor
        )
        verifySchema(
            """
            message ClassWithListOfFloat {
                repeated float list = 1;
            }
            """
        )
        verifyConversion(ClassWithListOfFloat(emptyList()), "")
        verifyConversion(ClassWithListOfFloat(listOf(0f)), "1: { 0.0i32 }")
        verifyConversion(ClassWithListOfFloat(listOf(-1.5f)), "1: { -1.5i32 }")
        verifyConversion(ClassWithListOfFloat(listOf(1.0f, 2.5f, 3.0f)), "1: { 1.0i32 2.5i32 3.0i32 }")
        verifyDecode(ClassWithListOfFloat(emptyList()), "1: {}")
        verifyDecode(ClassWithListOfFloat(listOf(1.0f, 2.5f, 3.0f)), "1: 1.0i32 1: 2.5i32 1: 3.0i32")
        verifyDecode(ClassWithListOfFloat(listOf(1.0f, 2.5f, 3.0f)), "1: { 1.0i32 2.5i32 } 1: { 3.0i32 }")

        givenSchema(
            ClassWithListOfFloat.serializer().descriptor,
            encodeZeroValues = false
        )
        verifyEncode(ClassWithListOfFloat(listOf(0f)), "1: { 0.0i32 }")
    }

    @Test
    fun `creates message for class with list of double`() {
        givenSchema(
            ClassWithListOfDouble.serializer().descriptor
        )
        verifySchema(
            """
            message ClassWithListOfDouble {
                repeated double list = 1;
            }
            """
        )
        verifyConversion(ClassWithListOfDouble(emptyList()), "")
        verifyConversion(ClassWithListOfDouble(listOf(0.0)), "1: { 0.0 }")
        verifyConversion(ClassWithListOfDouble(listOf(-1.5)), "1: { -1.5 }")
        verifyConversion(ClassWithListOfDouble(listOf(1.0, 2.5, 3.0)), "1: { 1.0 2.5 3.0 }")
        verifyDecode(ClassWithListOfDouble(emptyList()), "1: {}")
        verifyDecode(ClassWithListOfDouble(listOf(1.0, 2.5, 3.0)), "1: 1.0 1: 2.5 1: 3.0")
        verifyDecode(ClassWithListOfDouble(listOf(1.0, 2.5, 3.0)), "1: { 1.0 2.5 } 1: { 3.0 }")

        givenSchema(
            ClassWithListOfDouble.serializer().descriptor,
            encodeZeroValues = false
        )
        verifyEncode(ClassWithListOfDouble(listOf(0.0)), "1: { 0.0 }")
    }

    @Test
    fun `creates message for class with list of string`() {
        givenSchema(
            ClassWithListOfString.serializer().descriptor,
            encodeZeroValues = true
        )
        verifySchema(
            """
            message ClassWithListOfString {
                repeated string list = 1;
            }
            """
        )
        verifyConversion(ClassWithListOfString(emptyList()), "")
        verifyConversion(ClassWithListOfString(listOf("")), """1: {}""")
        verifyConversion(ClassWithListOfString(listOf("a")), """1: { "a" }""")
        verifyConversion(ClassWithListOfString(listOf("a", "b", "c")), """1: { "a" } 1: { "b" } 1: { "c" }""")

        givenSchema(
            ClassWithListOfString.serializer().descriptor,
            encodeZeroValues = false
        )
        verifyEncode(ClassWithListOfString(listOf("")), """1: {}""")
    }

    @Test
    fun `creates message for class with list of bytes`() {
        givenSchema(
            ClassWithListOfBytes.serializer().descriptor,
            encodeZeroValues = true
        )
        verifySchema(
            """
            message ClassWithListOfBytes {
                repeated bytes list = 1;
            }
            """
        )
        verifyConversion(ClassWithListOfBytes(emptyList()), "")
        verifyConversion(ClassWithListOfBytes(listOf(byteArrayOf())), """1: {}""")
        verifyConversion(ClassWithListOfBytes(listOf(byteArrayOf(1))), """1: { `01` }""")
        verifyConversion(
            ClassWithListOfBytes(listOf(byteArrayOf(1), byteArrayOf(2), byteArrayOf(3))),
            """1: { `01` } 1: { `02` } 1: { `03` }"""
        )

        givenSchema(
            ClassWithListOfBytes.serializer().descriptor,
            encodeZeroValues = false
        )
        verifyEncode(ClassWithListOfBytes(listOf(byteArrayOf())), """1: {}""")
    }

    @Test
    fun `creates message for class with list of enum`() {
        givenSchema(
            ClassWithListOfEnum.serializer().descriptor,
            encodeZeroValues = true
        )
        verifySchema(
            """
            message ClassWithListOfEnum {
                repeated EnumClass list = 1;
            }
            
            enum EnumClass {
                A = 0;
                B = 1;
                C = 2;
            }
            """
        )
        verifyConversion(ClassWithListOfEnum(emptyList()), "")
        verifyConversion(ClassWithListOfEnum(listOf(EnumClass.A)), """1: 0""")
        verifyConversion(ClassWithListOfEnum(listOf(EnumClass.A, EnumClass.B, EnumClass.C)), """1: 0 1: 1 1: 2""")

        givenSchema(
            ClassWithListOfEnum.serializer().descriptor,
            encodeZeroValues = false
        )
        verifyEncode(ClassWithListOfEnum(emptyList()), "")
        verifyEncode(ClassWithListOfEnum(listOf(EnumClass.A)), """1: 0""")
        verifyEncode(ClassWithListOfEnum(listOf(EnumClass.A, EnumClass.B, EnumClass.C)), """1: 0 1: 1 1: 2""")
    }

    @Test
    fun `creates message for class with list of empty class`() {
        givenSchema(
            ClassWithListOfEmptyClass.serializer().descriptor,
            encodeZeroValues = true
        )
        verifySchema(
            """
            message ClassWithListOfEmptyClass {
                repeated EmptyClass list = 1;
            }
            message EmptyClass {}
            """
        )
        verifyConversion(ClassWithListOfEmptyClass(emptyList()), "")
        verifyConversion(ClassWithListOfEmptyClass(listOf(EmptyClass())), "1: {}")
        verifyConversion(
            ClassWithListOfEmptyClass(listOf(EmptyClass(), EmptyClass(), EmptyClass())),
            "1: {} 1: {} 1: {}"
        )

        givenSchema(
            ClassWithListOfEmptyClass.serializer().descriptor,
            encodeZeroValues = false
        )
        verifyEncode(ClassWithListOfEmptyClass(listOf(EmptyClass())), "1: {}")
    }

    @Test
    fun `creates message for class with list of simple class`() {
        givenSchema(
            ClassWithListOfSimpleClass.serializer().descriptor,
            encodeZeroValues = true
        )
        verifySchema(
            """
            message ClassWithListOfSimpleClass {
                repeated SimpleClass list = 1;
            }
            message SimpleClass {
                int32 value = 1;
            }
            """
        )
        verifyConversion(ClassWithListOfSimpleClass(emptyList()), "")
        verifyConversion(ClassWithListOfSimpleClass(listOf(SimpleClass(0))), "1: { 1: 0 }")
        verifyConversion(ClassWithListOfSimpleClass(listOf(SimpleClass(5))), "1: { 1: 5 }")
        verifyConversion(
            ClassWithListOfSimpleClass(listOf(SimpleClass(1), SimpleClass(2), SimpleClass(3))),
            "1: { 1: 1 } 1: { 1: 2 } 1: { 1: 3 }"
        )

        givenSchema(
            ClassWithListOfSimpleClass.serializer().descriptor,
            encodeZeroValues = false
        )
        verifyEncode(ClassWithListOfSimpleClass(listOf(SimpleClass(0))), "1: {}")
    }

    @Test
    fun `creates message for class with list of sealed class`() {
        givenSchema(
            ClassWithListOfSealedClass.serializer().descriptor
        )
        verifySchema(
            """
            message ClassWithListOfSealedClass {
              repeated SealedTopClass list = 1;
            }
            
            message SealedTopClass {
              oneof subtypes {
                SealedLevel2LeafClassA sealedLevel2LeafClassA = 2;
                SealedLevel2LeafClassB sealedLevel2LeafClassB = 3;
                SealedLevel3LeafClass sealedLevel3LeafClass = 4;
              }
            }
            
            message SealedLevel2LeafClassA {
              int32 int = 1;
            }
            
            message SealedLevel2LeafClassB {
              SealedLevel2Class intermediate = 1;
            }
            
            message SealedLevel2Class {
              oneof subtypes {
                SealedLevel3LeafClass sealedLevel3LeafClass = 4;
              }
            }
            
            message SealedLevel3LeafClass {
              SealedTopClass top = 1;
            }
            """
        )
        verifyConversion(ClassWithListOfSealedClass(emptyList()), "")
        verifyConversion(
            ClassWithListOfSealedClass(
                listOf(
                    SealedLevel2LeafClassA(5),
                    SealedLevel2LeafClassA(6),
                    SealedLevel3LeafClass(SealedLevel2LeafClassA(7))
                )
            ),
            """
            1: { 2: { 1: 5 } }
            1: { 2: { 1: 6 } }
            1: { 4: { 1: { 2: { 1: 7 } } } }
            """
        )
    }

    @Test
    fun `creates message for class with nullable list`() {
        givenSchema(
            ClassWithNullableList.serializer().descriptor,
            encodeZeroValues = true
        )
        verifySchema(
            """
            message ClassWithNullableList {
                optional ListValue list = 1;
                message ListValue {
                    repeated int32 list = 1;
                }
            }
            """
        )
        verifyConversion(ClassWithNullableList(null), "")
        verifyConversion(ClassWithNullableList(emptyList()), "1: {}")
        verifyConversion(ClassWithNullableList(listOf(5, 6)), "1: { 1: { 5 6 } }")

        givenSchema(
            ClassWithNullableList.serializer().descriptor,
            encodeZeroValues = false
        )
        verifyEncode(ClassWithNullableList(emptyList()), "1: {}")
    }

    @Test
    fun `creates message for class with list of nullable scalar`() {
        givenSchema(
            ClassWithListOfNullableScalar.serializer().descriptor,
            encodeZeroValues = true
        )
        verifySchema(
            """
            message ClassWithListOfNullableScalar {
                repeated ListItem list = 1;
                message ListItem {
                    optional int32 value = 1;
                }
            }
            """
        )
        verifyConversion(ClassWithListOfNullableScalar(emptyList()), "")
        verifyConversion(ClassWithListOfNullableScalar(listOf(null)), "1: {}")
        verifyConversion(ClassWithListOfNullableScalar(listOf(0)), "1: { 1: 0 }")
        verifyConversion(ClassWithListOfNullableScalar(listOf(5, 6)), "1: { 1: 5 } 1: { 1: 6 }")
        verifyConversion(ClassWithListOfNullableScalar(listOf(null, 7, null)), "1: {} 1: { 1: 7 } 1: {}")

        givenSchema(
            ClassWithListOfNullableScalar.serializer().descriptor,
            encodeZeroValues = false
        )
        verifyEncode(ClassWithListOfNullableScalar(listOf(null)), "1: {}")
        verifyEncode(ClassWithListOfNullableScalar(listOf(0)), "1: { 1: 0 }")
    }

    @Test
    fun `creates message for class with list of nullable bytes`() {
        givenSchema(
            ClassWithListOfNullableBytes.serializer().descriptor,
            encodeZeroValues = true
        )
        verifySchema(
            """
            message ClassWithListOfNullableBytes {
                repeated ListItem list = 1;
                message ListItem {
                    optional bytes value = 1;
                }
            }
            """
        )
        verifyConversion(ClassWithListOfNullableBytes(emptyList()), "")
        verifyConversion(ClassWithListOfNullableBytes(listOf(null)), "1: {}")
        verifyConversion(ClassWithListOfNullableBytes(listOf(byteArrayOf())), "1: { 1: {} }")
        verifyConversion(
            ClassWithListOfNullableBytes(listOf(byteArrayOf(5), byteArrayOf(6))),
            "1: { 1: { `05` } } 1: { 1: { `06` } }"
        )
        verifyConversion(
            ClassWithListOfNullableBytes(listOf(null, byteArrayOf(7), null)),
            "1: {} 1: { 1: { `07` } } 1: {}"
        )

        givenSchema(
            ClassWithListOfNullableBytes.serializer().descriptor,
            encodeZeroValues = false
        )
        verifyEncode(ClassWithListOfNullableBytes(listOf(null)), "1: {}")
        verifyEncode(ClassWithListOfNullableBytes(listOf(byteArrayOf())), "1: { 1: {} }")
    }

    @Test
    fun `creates message for class with list of nullable message`() {
        givenSchema(
            ClassWithListOfNullableMessage.serializer().descriptor,
            encodeZeroValues = true
        )
        verifySchema(
            """
            message ClassWithListOfNullableMessage {
                repeated ListItem list = 1;
                message ListItem {
                    optional SimpleClass value = 1;
                }
            }
            
            message SimpleClass {
                int32 value = 1;
            }
            """
        )
        verifyConversion(ClassWithListOfNullableMessage(emptyList()), "")
        verifyConversion(ClassWithListOfNullableMessage(listOf(null)), "1: {}")
        verifyConversion(ClassWithListOfNullableMessage(listOf(SimpleClass(0))), "1: { 1: { 1: 0 } }")
        verifyConversion(
            ClassWithListOfNullableMessage(listOf(SimpleClass(5), SimpleClass(6))),
            "1: { 1: { 1: 5 } } 1: { 1: { 1: 6 } }"
        )
        verifyConversion(
            ClassWithListOfNullableMessage(listOf(null, SimpleClass(7), null)),
            "1: {} 1: { 1: { 1: 7 } } 1: {}"
        )

        givenSchema(
            ClassWithListOfNullableMessage.serializer().descriptor,
            encodeZeroValues = false
        )
        verifyEncode(ClassWithListOfNullableMessage(listOf(null)), "1: {}")
        verifyEncode(ClassWithListOfNullableMessage(listOf(SimpleClass(0))), "1: { 1: {} }")
    }

    @Test
    fun `creates message for class with list of nullable value class`() {
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
            ClassWithListOfNullableValueClass(listOf(StringIntValueClass(5), StringIntValueClass(6))),
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

    @Test
    fun `creates message for class with nested lists`() {
        givenSchema(
            ClassWithNestedLists.serializer().descriptor,
            encodeZeroValues = true
        )
        verifySchema(
            """
            message ClassWithNestedLists {
                repeated ListItem list = 1;
                message ListItem {
                    repeated ValueItem value = 1;
                    message ValueItem {
                        repeated bytes value = 1;
                    }
                }
            }
            """
        )
        verifyConversion(ClassWithNestedLists(emptyList()), "")
        verifyConversion(ClassWithNestedLists(listOf(emptyList())), "1: {}")
        verifyConversion(ClassWithNestedLists(listOf(listOf(emptyList()))), "1: { 1: {} }")
        verifyConversion(ClassWithNestedLists(listOf(listOf(listOf(byteArrayOf())))), "1: { 1: { 1: {} } }")
        verifyConversion(
            ClassWithNestedLists(listOf(listOf(listOf(byteArrayOf(9, 8, 9))))),
            "1: { 1: { 1: { `090809` } } }"
        )
        verifyConversion(
            ClassWithNestedLists(
                listOf(
                    listOf(
                        listOf(byteArrayOf(9, 8, 9))
                    ),
                    listOf(
                        listOf(byteArrayOf(7, 6), byteArrayOf(5)),
                        listOf(byteArrayOf(2))
                    )
                )
            ),
            "1: { 1: { 1: { `090809` } } } 1: { 1: { 1: { `0706` } 1: { `05` } } 1: { 1: { `02` } } }"
        )

        givenSchema(
            ClassWithNestedLists.serializer().descriptor,
            encodeZeroValues = false
        )
        verifyConversion(ClassWithNestedLists(emptyList()), "")
        verifyConversion(ClassWithNestedLists(listOf(emptyList())), "1: {}")
        verifyConversion(ClassWithNestedLists(listOf(listOf(emptyList()))), "1: { 1: {} }")
        verifyConversion(ClassWithNestedLists(listOf(listOf(listOf(byteArrayOf())))), "1: { 1: { 1: {} } }")
        verifyConversion(
            ClassWithNestedLists(listOf(listOf(listOf(byteArrayOf(0))))),
            "1: { 1: { 1: { `00` } } }"
        )
    }

    @Test
    fun `does not create schema with synthetic top-level message for list`() {
        givenSchema(serializer(typeOf<List<String>>()).descriptor)
        verifySchemaGenerationFails()
    }

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
    data class ClassWithListOfNullableBytes(
        val list: List<ByteArray?>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ClassWithListOfNullableBytes

            return list.contentEquals(other.list)
        }

        override fun hashCode(): Int = list.contentHashCode()
    }

    @Serializable
    data class ClassWithListOfEnum(
        val list: List<EnumClass>
    )

    @Serializable
    data class ClassWithNullableList(
        val list: List<Int>?
    )

    @Serializable
    data class ClassWithListOfNullableScalar(
        val list: List<Int?>
    )

    @Serializable
    data class ClassWithListOfNullableValueClass(
        val list: List<StringIntValueClass?>
    )

    @Serializable
    data class ClassWithListOfNullableMessage(
        val list: List<SimpleClass?>
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

    companion object {
        private fun List<ByteArray?>.contentEquals(other: List<ByteArray?>) =
            contentEquals(other) { l, r -> l.contentEquals(r) }

        private fun List<ByteArray?>.contentHashCode() =
            fold(0) { acc, e -> acc * 31 + e.contentHashCode() }

        private fun <T> List<T>.contentEquals(other: List<T>, elementEquals: (T, T) -> Boolean) =
            size == other.size && zip(other).all { (l, r) -> elementEquals(l, r) }

        private fun <T> List<T>.contentHashCode(elementHashCode: (T) -> Int) =
            fold(0) { acc, e -> acc * 31 + elementHashCode(e) }
    }
}
