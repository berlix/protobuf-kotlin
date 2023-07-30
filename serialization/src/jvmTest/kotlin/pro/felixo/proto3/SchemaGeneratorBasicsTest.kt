package pro.felixo.proto3

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.serializer
import pro.felixo.proto3.testutil.ClassAWithCycle
import pro.felixo.proto3.testutil.ClassAWithReference
import pro.felixo.proto3.testutil.ClassBWithCycle
import pro.felixo.proto3.testutil.ClassBWithReference
import pro.felixo.proto3.testutil.ClassWithCustomFieldNumbers
import pro.felixo.proto3.testutil.ClassWithCustomSerialName
import pro.felixo.proto3.testutil.ClassWithPropertyWithCustomByteArraySerializer
import pro.felixo.proto3.testutil.ClassWithPropertyWithCustomSerialName
import pro.felixo.proto3.testutil.ClassWithPropertyWithCustomSerializer
import pro.felixo.proto3.testutil.ClassWithSelfReference
import pro.felixo.proto3.testutil.ClassWithValueClassProperty
import pro.felixo.proto3.testutil.EmptyClass
import pro.felixo.proto3.testutil.Object
import pro.felixo.proto3.testutil.Scalars
import pro.felixo.proto3.testutil.StringIntValueClass
import pro.felixo.proto3.testutil.UnsignedInts
import kotlin.reflect.typeOf
import kotlin.test.Test

class SchemaGeneratorBasicsTest : SchemaGeneratorBaseTest() {
    @Test
    fun `creates empty schema`() {
        verify(emptyList(), "")
    }

    @Test
    fun `creates message for empty class`() {
        verify(
            EmptyClass.serializer().descriptor,
            """
                message EmptyClass {}
                """
        )
        verifyConversion(EmptyClass(), "")
    }

    @Test
    fun `creates message for object`() {
        verify(
            Object.serializer().descriptor,
            """
            message Object {}
            """
        )
        verifyConversion(Object, "")
    }

    @Test
    fun `deduplicates duplicate types`() {
        verify(
            listOf(
                EmptyClass.serializer().descriptor,
                EmptyClass.serializer().descriptor
            ),
            """
            message EmptyClass {}
            """
        )
        verifyConversion(EmptyClass(), "")
    }

    @Test
    fun `creates multiple root messages`() {
        verify(
            listOf(
                EmptyClass.serializer().descriptor,
                Object.serializer().descriptor
            ),
            """
                message EmptyClass {}
                message Object {}
                """
        )
        verifyConversion(EmptyClass(), "")
        verifyConversion(Object, "")
    }

    @Test
    fun `creates message with custom field numbers`() {
        verify(
            ClassWithCustomFieldNumbers.serializer().descriptor,
            """
            message ClassWithCustomFieldNumbers {
                int32 int = 5;
                string string = 1;
                bool bool = 8;
                optional int64 long = 2;
            } 
            """
        )
        verifyConversion(ClassWithCustomFieldNumbers(17, "strong", true, 99L), """5: 17 1: {"strong"} 8: true 2: 99""")
    }

    @Test
    fun `creates message with scalar properties`() {
        verify(
            Scalars.serializer().descriptor,
            """
                message Scalars {
                    bool boolean = 1;
                    optional bool booleanNullable = 2;   
                    int32 byte = 3;
                    optional int32 byteNullable = 4;   
                    int32 short = 5;
                    optional int32 shortNullable = 6;   
                    int32 int32 = 7;
                    optional int32 int32Nullable = 8;   
                    int32 defaultInt32 = 9;
                    optional int32 defaultInt32Nullable = 10;   
                    sint32 sint32 = 11;
                    optional sint32 sint32Nullable = 12;   
                    uint32 uint32 = 13;
                    optional uint32 uint32Nullable = 14;   
                    fixed32 fixedInt32 = 15;
                    optional fixed32 fixedInt32Nullable = 16;   
                    sfixed32 signedFixedInt32 = 17;
                    optional sfixed32 signedFixedInt32Nullable = 18;   
                    int64 int64 = 19;
                    optional int64 int64Nullable = 20;   
                    int64 defaultInt64 = 21;
                    optional int64 defaultInt64Nullable = 22;   
                    sint64 sint64 = 23;
                    optional sint64 sint64Nullable = 24;   
                    uint64 uint64 = 25;
                    optional uint64 uint64Nullable = 26;   
                    fixed64 fixedInt64 = 27;
                    optional fixed64 fixedInt64Nullable = 28;   
                    sfixed64 signedFixedInt64 = 29;
                    optional sfixed64 signedFixedInt64Nullable = 30;
                    float float = 31;
                    optional float floatNullable = 32;
                    double double = 33;
                    optional double doubleNullable = 34;
                    int32 char = 35;
                    optional int32 charNullable = 36;
                    string string = 37;
                    optional string stringNullable = 38;
                    bytes bytes = 39;
                    optional bytes bytesNullable = 40;
                }
                """
        )
        verifyConversion(
            Scalars(
                boolean = false,
                booleanNullable = true,
                byte = -1,
                byteNullable = 2,
                short = -3,
                shortNullable = 4,
                int32 = -5,
                int32Nullable = 6,
                defaultInt32 = -7,
                defaultInt32Nullable = 8,
                sint32 = -9,
                sint32Nullable = 10,
                uint32 = -11,
                uint32Nullable = 12,
                fixedInt32 = -13,
                fixedInt32Nullable = 14,
                signedFixedInt32 = -15,
                signedFixedInt32Nullable = 16,
                int64 = -17,
                int64Nullable = 18,
                defaultInt64 = -19,
                defaultInt64Nullable = 20,
                sint64 = -21,
                sint64Nullable = 22,
                uint64 = -23,
                uint64Nullable = 24,
                fixedInt64 = -25,
                fixedInt64Nullable = 26,
                signedFixedInt64 = -27,
                signedFixedInt64Nullable = 28,
                float = -29f,
                floatNullable = 30f,
                double = -31.0,
                doubleNullable = 32.0,
                char = 'x',
                charNullable = 'ï',
                string = "strong",
                stringNullable = "strang",
                bytes = byteArrayOf(1, 2, 3),
                bytesNullable = byteArrayOf(4, 5, 6)
            ),
            """
                1: false
                2: true
                3: -1
                4: 2
                5: -3
                6: 4
                7: -5
                8: 6
                9: -7
                10: 8
                11: -9z
                12: 10z
                13: 4294967285 # encoding a negative number as uint32 will force it to be non-negative
                14: 12
                15: -13i32
                16: 14i32
                17: -15i32
                18: 16i32
                19: -17
                20: 18
                21: -19
                22: 20
                23: -21z
                24: 22z
                25: -23
                26: 24
                27: -25i64
                28: 26i64
                29: -27i64
                30: 28i64
                31: -29.0i32
                32: 30.0i32
                33: -31.0
                34: 32.0
                35: 120
                36: 239
                37: {"strong"}
                38: {"strang"}
                39: {`010203`}
                40: {`040506`}
                """
        )
        verifyConversion(
            Scalars(
                true,
                null,
                Byte.MIN_VALUE,
                null,
                Short.MIN_VALUE,
                null,
                Int.MIN_VALUE,
                null,
                Int.MIN_VALUE,
                null,
                Int.MIN_VALUE,
                null,
                Int.MIN_VALUE,
                null,
                Int.MIN_VALUE,
                null,
                Int.MIN_VALUE,
                null,
                Long.MIN_VALUE,
                null,
                Long.MIN_VALUE,
                null,
                Long.MIN_VALUE,
                null,
                Long.MIN_VALUE,
                null,
                Long.MIN_VALUE,
                null,
                Long.MIN_VALUE,
                null,
                Float.NEGATIVE_INFINITY,
                null,
                Double.NEGATIVE_INFINITY,
                null,
                'x',
                null,
                "strong",
                null,
                byteArrayOf(),
                null
            ),
            """
                1: true
                3: -128
                5: -32768
                7: -2147483648
                9: -2147483648
                11: -2147483648z
                13: 2147483648 # encoding a negative number as uint32 will force it to be non-negative
                15: -2147483648i32
                17: -2147483648i32
                19: -9223372036854775808
                21: -9223372036854775808
                23: -9223372036854775808z
                25: -9223372036854775808
                27: -9223372036854775808i64
                29: -9223372036854775808i64
                31: -inf32
                33: -inf64
                35: 120
                37: {"strong"}
                39: {}
                """
        )
        verifyConversion(
            Scalars(
                boolean = false,
                booleanNullable = false,
                byte = 0,
                byteNullable = 0,
                short = 0,
                shortNullable = 0,
                int32 = 0,
                int32Nullable = 0,
                defaultInt32 = 0,
                defaultInt32Nullable = 0,
                sint32 = 0,
                sint32Nullable = 0,
                uint32 = 0,
                uint32Nullable = 0,
                fixedInt32 = 0,
                fixedInt32Nullable = 0,
                signedFixedInt32 = 0,
                signedFixedInt32Nullable = 0,
                int64 = 0,
                int64Nullable = 0,
                defaultInt64 = 0,
                defaultInt64Nullable = 0,
                sint64 = 0,
                sint64Nullable = 0,
                uint64 = 0,
                uint64Nullable = 0,
                fixedInt64 = 0,
                fixedInt64Nullable = 0,
                signedFixedInt64 = 0,
                signedFixedInt64Nullable = 0,
                float = 0f,
                floatNullable = 0f,
                double = 0.0,
                doubleNullable = 0.0,
                char = '\u0000',
                charNullable = '\u0000',
                string = "",
                stringNullable = "",
                bytes = byteArrayOf(),
                bytesNullable = byteArrayOf()
            ),
            """
                1: false
                2: false
                3: 0
                4: 0
                5: 0
                6: 0
                7: 0
                8: 0
                9: 0
                10: 0
                11: 0z
                12: 0z
                13: 0
                14: 0
                15: 0i32
                16: 0i32
                17: 0i32
                18: 0i32
                19: 0
                20: 0
                21: 0
                22: 0
                23: 0z
                24: 0z
                25: 0
                26: 0
                27: 0i64
                28: 0i64
                29: 0i64
                30: 0i64
                31: 0.0i32
                32: 0.0i32
                33: 0.0
                34: 0.0
                35: 0
                36: 0
                37: {}
                38: {}
                39: {}
                40: {}
                """
        )
        verifyDecode(
            Scalars(
                false,
                null,
                0,
                null,
                0,
                null,
                0,
                null,
                0,
                null,
                0,
                null,
                0,
                null,
                0,
                null,
                0,
                null,
                0,
                null,
                0,
                null,
                0,
                null,
                0,
                null,
                0,
                null,
                0,
                null,
                0f,
                null,
                0.0,
                null,
                '\u0000',
                null,
                "",
                null,
                byteArrayOf(),
                null
            ),
            ""
        )
    }

    @Test
    fun `creates message with unsigned ints`() {
        verify(
            UnsignedInts.serializer().descriptor,
            """
            message UnsignedInts {
                uint32 ubyte = 1;
                optional uint32 ubyteNullable = 2;
                uint32 ushort = 3;
                optional uint32 ushortNullable = 4;
                uint32 uint32 = 5;
                optional uint32 uint32Nullable = 6;
                fixed32 fixedUint32 = 7;
                optional fixed32 fixedUint32Nullable = 8;
                uint64 uint64 = 9;
                optional uint64 uint64Nullable = 10;
                fixed64 fixedUint64 = 11;
                optional fixed64 fixedUint64Nullable = 12;
                int32 int32 = 13;
            }
            """
        )
        verifyConversion(
            UnsignedInts(
                UByte.MIN_VALUE,
                null,
                UShort.MIN_VALUE,
                null,
                UInt.MIN_VALUE,
                null,
                UInt.MIN_VALUE,
                null,
                ULong.MIN_VALUE,
                null,
                ULong.MIN_VALUE,
                null,
                UInt.MIN_VALUE
            ),
            """
                1: 0
                3: 0
                5: 0
                7: 0i32
                9: 0
                11: 0i64
                13: 0
                """
        )
        verifyConversion(
            UnsignedInts(
                UByte.MAX_VALUE,
                UByte.MIN_VALUE,
                UShort.MAX_VALUE,
                UShort.MIN_VALUE,
                UInt.MAX_VALUE,
                UInt.MIN_VALUE,
                UInt.MAX_VALUE,
                UInt.MIN_VALUE,
                ULong.MAX_VALUE,
                ULong.MIN_VALUE,
                ULong.MAX_VALUE,
                ULong.MIN_VALUE,
                UInt.MAX_VALUE
            ),
            """
                1: 0xff
                2: 0
                3: 0xffff
                4: 0
                5: 0xffffffff
                6: 0
                7: 0xffffffffi32
                8: 0i32
                9: 0xffffffffffffffff
                10: 0
                11: 0xffffffffffffffffi64
                12: 0i64
                13: -1 # UInt 0xffffffff encoded as signed int32
                """
        )
    }

    @Test
    fun `creates message referring to other message`() {
        verify(
            ClassAWithReference.serializer().descriptor,
            """
            message ClassAWithReference {
                EmptyClass ref = 1;
            }
            message EmptyClass {}
            """
        )
        verifyConversion(
            ClassAWithReference(EmptyClass()),
            """
            1: {}
            """
        )
    }

    @Test
    fun `creates messages referring to same other message`() {
        verify(
            listOf(
                ClassAWithReference.serializer().descriptor,
                ClassBWithReference.serializer().descriptor
            ),
            """
            message ClassAWithReference {
                EmptyClass ref = 1;
            }
            message ClassBWithReference {
                EmptyClass ref = 1;
            }
            message EmptyClass {}
            """
        )
        verifyConversion(
            ClassAWithReference(EmptyClass()),
            """
            1: {}
            """
        )
        verifyConversion(
            ClassBWithReference(EmptyClass()),
            """
            1: {}
            """
        )
    }

    @Test
    fun `creates self-referential message`() {
        verify(
            ClassWithSelfReference.serializer().descriptor,
            """
            message ClassWithSelfReference {
                optional ClassWithSelfReference ref = 1;
            }
            """
        )
        verifyConversion(
            ClassWithSelfReference(ClassWithSelfReference(ClassWithSelfReference(null))),
            """
            1: {
                1: {}
            }
            """
        )
    }

    @Test
    fun `creates messages with cyclical references`() {
        verify(
            ClassAWithCycle.serializer().descriptor,
            """
            message ClassAWithCycle {
                optional ClassBWithCycle b = 1;
            }
            message ClassBWithCycle {
                optional ClassAWithCycle a = 1;
            }
            """
        )
        verifyConversion(
            ClassAWithCycle(ClassBWithCycle(ClassAWithCycle(ClassBWithCycle(null)))),
            """
            1: {
                1: {
                    1: {}
                }
            }
            """
        )
    }

    @Test
    fun `creates class with custom serial name`() {
        verify(
            ClassWithCustomSerialName.serializer().descriptor,
            """
            message CustomName {}
            """
        )
        verifyConversion(
            ClassWithCustomSerialName(),
            ""
        )
    }

    @Test
    fun `creates class with property with custom serial name`() {
        verify(
            ClassWithPropertyWithCustomSerialName.serializer().descriptor,
            """
            message ClassWithPropertyWithCustomSerialName {
                int32 customName = 1;
            } 
            """
        )
        verifyConversion(
            ClassWithPropertyWithCustomSerialName(5),
            """
            1: 5
            """
        )
    }

    @Test
    fun `creates class with property with custom serializer`() {
        verify(
            ClassWithPropertyWithCustomSerializer.serializer().descriptor,
            """
            message ClassWithPropertyWithCustomSerializer {
                string int = 1;
            }
            """
        )
        verifyConversion(
            ClassWithPropertyWithCustomSerializer(5),
            """
            1: {"5"}
            """
        )
    }

    @Test
    fun `creates class with custom serializer that encodes bytes`() {
        verify(
            ClassWithPropertyWithCustomByteArraySerializer.serializer().descriptor,
            """
            message ClassWithPropertyWithCustomByteArraySerializer {
                bytes boolBytes = 1;
            }
            """
        )
        verifyConversion(
            ClassWithPropertyWithCustomByteArraySerializer(false),
            """
            1: {`00`}
            """
        )
        verifyConversion(
            ClassWithPropertyWithCustomByteArraySerializer(true),
            """
            1: {`ff`}
            """
        )
    }

    @Test
    fun `creates class with value class property`() {
        verify(
            ClassWithValueClassProperty.serializer().descriptor,
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

    @Test
    fun `does not create synthetic top-level message from value class with custom serializer`() =
        verifyFailure(StringIntValueClass.serializer().descriptor)

    @Test
    fun `does not create synthetic top-level message for Int`() =
        verifyFailure(Int.serializer().descriptor)

    @Test
    fun `does not create synthetic top-level message for nullable ByteArray`() =
        verifyFailure(serializer(typeOf<ByteArray?>()).descriptor)
}
