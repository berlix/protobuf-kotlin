package pro.felixo.protobuf.serialization.integrationtests

import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import pro.felixo.protobuf.serialization.ProtoDefaultEnumValue
import pro.felixo.protobuf.serialization.ProtoNumber
import pro.felixo.protobuf.serialization.testutil.SealedLevel2Class
import pro.felixo.protobuf.serialization.testutil.SealedLevel2LeafClassA
import pro.felixo.protobuf.serialization.testutil.SealedLevel2LeafClassB
import pro.felixo.protobuf.serialization.testutil.SealedLevel3LeafClass
import pro.felixo.protobuf.serialization.testutil.SealedTopClass
import kotlin.reflect.typeOf
import kotlin.test.Test

class PolymorphicIntegrationTest : BaseIntegrationTest() {
    private val module = SerializersModule {
        polymorphic(NonSealedLevel2Class::class, NonSealedLevel3LeafClass::class, NonSealedLevel3LeafClass.serializer())
        polymorphic(NonSealedInterface::class, NonSealedLevel2LeafClassA::class, NonSealedLevel2LeafClassA.serializer())
        polymorphic(NonSealedInterface::class, NonSealedLevel2LeafClassB::class, NonSealedLevel2LeafClassB.serializer())
        polymorphic(NonSealedInterface::class, NonSealedLevel3LeafClass::class, NonSealedLevel3LeafClass.serializer())
    }

    @Test
    fun `creates messages for sealed class hierarchy`() {
        givenSchema(
            listOf(SealedTopClass.serializer().descriptor),
            encodeZeroValues = true
        )
        verifySchema(
            """
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
        verifyConversion(
            SealedLevel2LeafClassA(42),
            """1: 42"""
        )
        verifyConversion<SealedTopClass>(
            SealedLevel2LeafClassA(0),
            """2: {1: 0}"""
        )
        verifyConversion<SealedTopClass>(
            SealedLevel2LeafClassB(SealedLevel3LeafClass(SealedLevel2LeafClassA(41))),
            """3: {1: {4: {1: {2: {1: 41}}}}}"""
        )
        verifyConversion<SealedLevel2Class>(
            SealedLevel3LeafClass(SealedLevel2LeafClassA(40)),
            """4: {1: {2: {1: 40}}}"""
        )
        verifyConversion<SealedTopClass>(
            SealedLevel3LeafClass(SealedLevel2LeafClassA(0)),
            """4: {1: {2: {1: 0}}}"""
        )

        givenSchema(
            listOf(SealedTopClass.serializer().descriptor),
            encodeZeroValues = false
        )
        verifyEncode<SealedTopClass>(
            SealedLevel2LeafClassA(0),
            """2: {}"""
        )
    }

    @Test
    fun `creates messages for non-sealed polymorphic class hierarchy`() {
        givenSchema(
            typesFromSerializersModule = listOf(typeOf<NonSealedInterface>()),
            serializersModule = module,
            encodeZeroValues = true
        )
        verifySchema(
            """
            message NonSealedLevel3LeafClass {
              NonSealedInterface top = 1;
            }
            
            message NonSealedInterface {
              oneof subtypes {
                NonSealedLevel2LeafClassA nonSealedLevel2LeafClassA = 2;
                NonSealedLevel2LeafClassB nonSealedLevel2LeafClassB = 3;
                NonSealedLevel3LeafClass nonSealedLevel3LeafClass = 4;
              }
            }
            
            message NonSealedLevel2LeafClassA {
              int32 int = 1;
            }
            
            message NonSealedLevel2LeafClassB {
              NonSealedLevel2Class intermediate = 1;
            }
            
            message NonSealedLevel2Class {
              oneof subtypes {
                NonSealedLevel3LeafClass nonSealedLevel3LeafClass = 4;
              }
            }
            """
        )
        verifyConversion(
            NonSealedLevel2LeafClassA(42),
            """1: 42"""
        )
        verifyConversion<NonSealedInterface>(
            NonSealedLevel2LeafClassA(0),
            """2: {1: 0}"""
        )
        verifyConversion<NonSealedInterface>(
            NonSealedLevel2LeafClassB(NonSealedLevel3LeafClass(NonSealedLevel2LeafClassA(41))),
            """3: {1: {4: {1: {2: {1: 41}}}}}"""
        )
        verifyConversion<NonSealedLevel2Class>(
            NonSealedLevel3LeafClass(NonSealedLevel2LeafClassA(40)),
            """4: {1: {2: {1: 40}}}"""
        )
        verifyConversion<NonSealedInterface>(
            NonSealedLevel3LeafClass(NonSealedLevel2LeafClassA(40)),
            """4: {1: {2: {1: 40}}}"""
        )

        givenSchema(
            typesFromSerializersModule = listOf(typeOf<NonSealedInterface>()),
            serializersModule = module,
            encodeZeroValues = false
        )
        verifyEncode<NonSealedInterface>(
            NonSealedLevel2LeafClassA(0),
            """2: {}"""
        )
    }

    @Test
    fun `creates messages for sealed class hierarchy with diverse sub-types`() {
        givenSchema(
            listOf(DiverseInterface.serializer().descriptor),
            encodeZeroValues = true
        )
        verifySchema(
            """
            message DiverseInterface {
              oneof subtypes {
                ConcreteClass concreteClass = 1;
                ConcreteObject concreteObject = 2;
                ConcreteEnumClass concreteEnumClass = 3;
              }
            }
            
            message ConcreteClass {
              int32 int = 1;
            }
            
            message ConcreteObject {
            }
            
            enum ConcreteEnumClass {
              A = 0;
              B = 1;
            }
            """
        )

        verifyConversion<DiverseInterface>(
            ConcreteClass(0),
            """1: { 1: 0 }"""
        )
        verifyConversion<DiverseInterface>(
            ConcreteObject,
            """2: {}"""
        )
        verifyConversion<DiverseInterface>(
            ConcreteEnumClass.A,
            """3: 0"""
        )
        verifyConversion<DiverseInterface>(
            ConcreteEnumClass.B,
            """3: 1"""
        )

        givenSchema(
            listOf(DiverseInterface.serializer().descriptor),
            encodeZeroValues = false
        )

        verifyConversion<DiverseInterface>(
            ConcreteClass(0),
            """1: {}"""
        )
        verifyEncode<DiverseInterface>(
            ConcreteEnumClass.A,
            """3: 0"""
        )
    }

    interface NonSealedInterface

    @Serializable
    @ProtoNumber(2)
    data class NonSealedLevel2LeafClassA(
        val int: Int
    ) : NonSealedInterface

    @Serializable
    @ProtoNumber(3)
    data class NonSealedLevel2LeafClassB(
        val intermediate: NonSealedLevel2Class
    ) : NonSealedInterface

    @Serializable
    abstract class NonSealedLevel2Class: NonSealedInterface

    @Serializable
    @ProtoNumber(4)
    data class NonSealedLevel3LeafClass(
        val top: NonSealedInterface
    ) : NonSealedLevel2Class()

    @Serializable
    sealed interface DiverseInterface

    @Serializable
    @ProtoNumber(1)
    data class ConcreteClass(val int: Int) : DiverseInterface

    @Serializable
    @ProtoNumber(2)
    data object ConcreteObject : DiverseInterface

    @Serializable
    @ProtoNumber(3)
    enum class ConcreteEnumClass : DiverseInterface {
        @ProtoDefaultEnumValue A,
        B
    }
}
