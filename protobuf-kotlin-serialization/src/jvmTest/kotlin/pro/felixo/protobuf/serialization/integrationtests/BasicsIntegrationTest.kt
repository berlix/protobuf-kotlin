package pro.felixo.protobuf.serialization.integrationtests

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import pro.felixo.protobuf.serialization.IntegerType
import pro.felixo.protobuf.serialization.ProtoIntegerType
import pro.felixo.protobuf.serialization.ProtoNumber
import pro.felixo.protobuf.serialization.testutil.EmptyClass
import pro.felixo.protobuf.serialization.testutil.ListDescriptor
import pro.felixo.protobuf.serialization.testutil.StringIntValueClass
import kotlin.reflect.typeOf
import kotlin.test.Test

typealias StringInt = @Serializable(with = BasicsIntegrationTest.StringIntSerializer::class) Int

class BasicsIntegrationTest : BaseIntegrationTest() {
    @Test
    fun `creates empty schema`() {
        givenSchema(emptyList())
        verifySchema("")
    }

    @Test
    fun `creates message for empty class`() {
        givenSchema(
            EmptyClass.serializer().descriptor
        )
        verifySchema(
            """
            message EmptyClass {}
            """
        )
        verifyConversion(EmptyClass(), "")
    }

    @Test
    fun `creates message for object`() {
        givenSchema(
            Object.serializer().descriptor
        )
        verifySchema(
            """
            message Object {}
            """
        )
        verifyConversion(Object, "")
    }

    @Test
    fun `deduplicates duplicate types`() {
        givenSchema(
            listOf(
                EmptyClass.serializer().descriptor,
                EmptyClass.serializer().descriptor
            )
        )
        verifySchema(
            """
            message EmptyClass {}
            """

        )
        verifyConversion(EmptyClass(), "")
    }

    @Test
    fun `creates multiple root messages`() {
        givenSchema(
            listOf(
                EmptyClass.serializer().descriptor,
                Object.serializer().descriptor
            )
        )
        verifySchema(
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
        givenSchema(
            ClassWithCustomFieldNumbers.serializer().descriptor
        )
        verifySchema(
            """
            message ClassWithCustomFieldNumbers {
                string string = 1;
                optional int64 long = 2;
                int32 int = 5;
                bool bool = 8;
            } 
            """
        )
        verifyConversion(ClassWithCustomFieldNumbers(17, "strong", true, 99L), """5: 17 1: {"strong"} 8: true 2: 99""")
    }

    @Test
    fun `creates message with scalar properties`() {
        givenSchema(Scalars.serializer().descriptor, encodeZeroValues = true)
        verifySchema(
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

        givenSchema(Scalars.serializer().descriptor, encodeZeroValues = false)
        verifyConversion(
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
        verifyEncode(
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
                2: false
                4: 0
                6: 0
                8: 0
                10: 0
                12: 0z
                14: 0
                16: 0i32
                18: 0i32
                20: 0
                22: 0
                24: 0z
                26: 0
                28: 0i64
                30: 0i64
                32: 0.0i32
                34: 0.0
                36: 0
                38: {}
                40: {}
            """
        )
    }

    @Test
    fun `creates message with unsigned ints`() {
        givenSchema(UnsignedInts.serializer().descriptor, encodeZeroValues = true)
        verifySchema(
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

        givenSchema(UnsignedInts.serializer().descriptor, encodeZeroValues = false)
        verifyEncode(
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
            ""
        )
        verifyEncode(
            UnsignedInts(
                UByte.MIN_VALUE,
                UByte.MIN_VALUE,
                UShort.MIN_VALUE,
                UShort.MIN_VALUE,
                UInt.MIN_VALUE,
                UInt.MIN_VALUE,
                UInt.MIN_VALUE,
                UInt.MIN_VALUE,
                ULong.MIN_VALUE,
                ULong.MIN_VALUE,
                ULong.MIN_VALUE,
                ULong.MIN_VALUE,
                UInt.MIN_VALUE
            ),
            """
                2: 0
                4: 0
                6: 0
                8: 0i32
                10: 0
                12: 0i64
                """
        )
    }

    @Test
    fun `creates message referring to other message`() {
        givenSchema(ClassAWithReference.serializer().descriptor, encodeZeroValues = true)
        verifySchema(
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
        givenSchema(ClassAWithReference.serializer().descriptor, encodeZeroValues = false)
        verifyEncode(
            ClassAWithReference(EmptyClass()),
            ""
        )
    }

    @Test
    fun `creates messages referring to same other message`() {
        givenSchema(
            listOf(
                ClassAWithReference.serializer().descriptor,
                ClassBWithReference.serializer().descriptor
            ),
            encodeZeroValues = true
        )
        verifySchema(
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
        givenSchema(
            ClassWithSelfReference.serializer().descriptor,
            encodeZeroValues = true
        )
        verifySchema(
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
        givenSchema(
            ClassAWithCycle.serializer().descriptor,
            encodeZeroValues = true
        )
        verifySchema(
            """
            message ClassBWithCycle {
                optional ClassAWithCycle a = 1;
            }
            message ClassAWithCycle {
                optional ClassBWithCycle b = 1;
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
        givenSchema(
            ClassWithCustomSerialName.serializer().descriptor
        )
        verifySchema(
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
        givenSchema(
            ClassWithPropertyWithCustomSerialName.serializer().descriptor
        )
        verifySchema(
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
        givenSchema(
            ClassWithPropertyWithCustomSerializer.serializer().descriptor
        )
        verifySchema(
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
        givenSchema(
            ClassWithPropertyWithCustomByteArraySerializer.serializer().descriptor
        )
        verifySchema(
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

    @Test
    fun `does not create synthetic top-level message from value class with custom serializer`() {
        givenSchema(StringIntValueClass.serializer().descriptor)
        verifySchemaGenerationFails()
    }

    @Test
    fun `does not create synthetic top-level message for Int`() {
        givenSchema(Int.serializer().descriptor)
        verifySchemaGenerationFails()
    }

    @Test
    fun `does not create synthetic top-level message for nullable ByteArray`() {
        givenSchema(serializer(typeOf<ByteArray?>()).descriptor)
        verifySchemaGenerationFails()
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
    object Object {
        const val property: Int = 5
    }

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
    data class ClassWithPropertyWithCustomSerializer(
        @Serializable(StringIntSerializer::class)
        val int: Int
    )

    @Serializable
    data class ClassWithPropertyWithCustomByteArraySerializer(
        @Serializable(BooleanAsByteArraySerializer::class)
        val boolBytes: Boolean
    )

    @Serializable
    data class ClassWithValueClassProperty(
        val stringInt: StringIntValueClass
    )

    class StringIntSerializer : KSerializer<StringInt> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("StringInt", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder): Int = decoder.decodeString().toInt()
        override fun serialize(encoder: Encoder, value: Int) = encoder.encodeString("$value")
    }

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
}
