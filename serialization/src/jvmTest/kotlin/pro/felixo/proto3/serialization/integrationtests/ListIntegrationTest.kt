package pro.felixo.proto3.serialization.integrationtests

import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import org.junit.Test
import pro.felixo.proto3.serialization.testutil.EmptyClass
import pro.felixo.proto3.serialization.testutil.SealedLevel2LeafClassA
import pro.felixo.proto3.serialization.testutil.SealedLevel3LeafClass
import pro.felixo.proto3.serialization.testutil.SealedTopClass
import pro.felixo.proto3.serialization.testutil.SimpleClass
import kotlin.reflect.typeOf

class ListIntegrationTest : BaseIntegrationTest() {

    @Test
    fun `creates message for class with list of int`() {
        verify(
            ClassWithListOfInt.serializer().descriptor,
            """
                message ClassWithListOfInt {
                    repeated int32 list = 1;
                }
                """
        )
        verifyConversion(ClassWithListOfInt(emptyList()), "")
        verifyConversion(ClassWithListOfInt(listOf(-1)), "1: { -1 }")
        verifyConversion(ClassWithListOfInt(listOf(1, 2, 3)), "1: { 1 2 3 }")
        verifyDecode(ClassWithListOfInt(listOf(1, 2, 3)), "1: 1 1: 2 1: 3")
        verifyDecode(ClassWithListOfInt(listOf(1, 2, 3)), "1: { 1 2 } 1: { 3 }")
    }

    @Test
    fun `creates message for class with list of UInt`() {
        verify(
            ClassWithListOfUInt.serializer().descriptor,
            """
                message ClassWithListOfUInt {
                    repeated int32 list = 1;
                }
                """
        )
        verifyConversion(ClassWithListOfUInt(emptyList()), "")
        verifyConversion(ClassWithListOfUInt(listOf(1u)), "1: { 1 }")
        verifyConversion(ClassWithListOfUInt(listOf(1u, 2u, 3u)), "1: { 1 2 3 }")
        verifyDecode(ClassWithListOfUInt(listOf(1u, 2u, 3u)), "1: 1 1: 2 1: 3")
        verifyDecode(ClassWithListOfUInt(listOf(1u, 2u, 3u)), "1: { 1 2 } 1: { 3 }")
    }

    @Test
    fun `creates message for class with list of float`() {
        verify(
            ClassWithListOfFloat.serializer().descriptor,
            """
                message ClassWithListOfFloat {
                    repeated float list = 1;
                }
                """
        )
        verifyConversion(ClassWithListOfFloat(emptyList()), "")
        verifyConversion(ClassWithListOfFloat(listOf(-1.5f)), "1: { -1.5i32 }")
        verifyConversion(ClassWithListOfFloat(listOf(1.0f, 2.5f, 3.0f)), "1: { 1.0i32 2.5i32 3.0i32 }")
        verifyDecode(ClassWithListOfFloat(listOf(1.0f, 2.5f, 3.0f)), "1: 1.0i32 1: 2.5i32 1: 3.0i32")
        verifyDecode(ClassWithListOfFloat(listOf(1.0f, 2.5f, 3.0f)), "1: { 1.0i32 2.5i32 } 1: { 3.0i32 }")
    }

    @Test
    fun `creates message for class with list of double`() {
        verify(
            ClassWithListOfDouble.serializer().descriptor,
            """
                message ClassWithListOfDouble {
                    repeated double list = 1;
                }
                """
        )
        verifyConversion(ClassWithListOfDouble(emptyList()), "")
        verifyConversion(ClassWithListOfDouble(listOf(-1.5)), "1: { -1.5 }")
        verifyConversion(ClassWithListOfDouble(listOf(1.0, 2.5, 3.0)), "1: { 1.0 2.5 3.0 }")
        verifyDecode(ClassWithListOfDouble(listOf(1.0, 2.5, 3.0)), "1: 1.0 1: 2.5 1: 3.0")
        verifyDecode(ClassWithListOfDouble(listOf(1.0, 2.5, 3.0)), "1: { 1.0 2.5 } 1: { 3.0 }")
    }

    @Test
    fun `creates message for class with list of string`() {
        verify(
            ClassWithListOfString.serializer().descriptor,
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
    }

    @Test
    fun `creates message for class with list of bytes`() {
        verify(
            ClassWithListOfBytes.serializer().descriptor,
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
    }

    @Test
    fun `creates message for class with list of empty class`() {
        verify(
            ClassWithListOfEmptyClass.serializer().descriptor,
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
    }

    @Test
    fun `creates message for class with list of simple class`() {
        verify(
            ClassWithListOfSimpleClass.serializer().descriptor,
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
        verifyConversion(ClassWithListOfSimpleClass(listOf(SimpleClass(5))), "1: { 1: 5 }")
        verifyConversion(
            ClassWithListOfSimpleClass(listOf(SimpleClass(1), SimpleClass(2), SimpleClass(3))),
            "1: { 1: 1 } 1: { 1: 2 } 1: { 1: 3 }"
        )
    }

    @Test
    fun `creates message for class with list of sealed class`() {
        verify(
            ClassWithListOfSealedClass.serializer().descriptor,
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
        verify(
            ClassWithNullableList.serializer().descriptor,
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
    }

    @Test
    fun `creates message for class with list of nullables`() {
        verify(
            ClassWithListOfNullableScalar.serializer().descriptor,
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
        verifyConversion(ClassWithListOfNullableScalar(listOf(5, 6)), "1: { 1: 5 } 1: { 1: 6 }")
        verifyConversion(ClassWithListOfNullableScalar(listOf(null, 7, null)), "1: {} 1: { 1: 7 } 1: {}")
    }

    @Test
    fun `creates message for class with nested lists`() {
        verify(
            ClassWithNestedLists.serializer().descriptor,
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
    }

    @Test
    fun `does not create schema with synthetic top-level message for list`() =
        verifyFailure(serializer(typeOf<List<String>>()).descriptor)

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

    companion object {
        private fun List<ByteArray>.contentEquals(other: List<ByteArray>) =
            contentEquals(other) { l, r -> l.contentEquals(r) }

        private fun List<ByteArray>.contentHashCode() =
            fold(0) { acc, e -> acc * 31 + e.contentHashCode() }

        private fun <T> List<T>.contentEquals(other: List<T>, elementEquals: (T, T) -> Boolean) =
            size == other.size && zip(other).all { (l, r) -> elementEquals(l, r) }

        private fun <T> List<T>.contentHashCode(elementHashCode: (T) -> Int) =
            fold(0) { acc, e -> acc * 31 + elementHashCode(e) }
    }
}
