package pro.felixo.proto3.serialization.integrationtests

import kotlinx.serialization.modules.SerializersModule
import pro.felixo.proto3.serialization.testutil.NonSealedInterface
import pro.felixo.proto3.serialization.testutil.NonSealedLevel2Class
import pro.felixo.proto3.serialization.testutil.NonSealedLevel2LeafClassA
import pro.felixo.proto3.serialization.testutil.NonSealedLevel2LeafClassB
import pro.felixo.proto3.serialization.testutil.NonSealedLevel3LeafClass
import pro.felixo.proto3.serialization.testutil.SealedLevel2Class
import pro.felixo.proto3.serialization.testutil.SealedLevel2LeafClassA
import pro.felixo.proto3.serialization.testutil.SealedLevel2LeafClassB
import pro.felixo.proto3.serialization.testutil.SealedLevel3LeafClass
import pro.felixo.proto3.serialization.testutil.SealedTopClass
import kotlin.reflect.typeOf
import kotlin.test.Test

class SchemaGeneratorPolymorphyTest : SchemaGeneratorBaseTest() {
    private val module = SerializersModule {
        polymorphic(NonSealedLevel2Class::class, NonSealedLevel3LeafClass::class, NonSealedLevel3LeafClass.serializer())
        polymorphic(NonSealedInterface::class, NonSealedLevel2LeafClassA::class, NonSealedLevel2LeafClassA.serializer())
        polymorphic(NonSealedInterface::class, NonSealedLevel2LeafClassB::class, NonSealedLevel2LeafClassB.serializer())
        polymorphic(NonSealedInterface::class, NonSealedLevel3LeafClass::class, NonSealedLevel3LeafClass.serializer())
    }

    @Test
    fun `creates sealed class hierarchy`() {
        verifySchema(
            listOf(SealedTopClass.serializer().descriptor),
            serializersModule = module,
            expectedSchema = """
                message SealedLevel2Class {
                  oneof subtypes {
                    SealedLevel3LeafClass sealedLevel3LeafClass = 4;
                  }
                }
                
                message SealedLevel2LeafClassA {
                  int32 int = 1;
                }
                
                message SealedLevel2LeafClassB {
                  SealedLevel2Class intermediate = 1;
                }
                
                message SealedLevel3LeafClass {
                  SealedTopClass top = 1;
                }
                
                message SealedTopClass {
                  oneof subtypes {
                    SealedLevel2LeafClassA sealedLevel2LeafClassA = 2;
                    SealedLevel2LeafClassB sealedLevel2LeafClassB = 3;
                    SealedLevel3LeafClass sealedLevel3LeafClass = 4;
                  }
                }
                """
        )
        verifyConversion(
            SealedLevel2LeafClassA(42),
            """1: 42"""
        )
        verifyConversion<SealedTopClass>(
            SealedLevel2LeafClassA(42),
            """2: {1: 42}"""
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
            SealedLevel3LeafClass(SealedLevel2LeafClassA(40)),
            """4: {1: {2: {1: 40}}}"""
        )
    }

    @Test
    fun `creates non-sealed polymorphic class hierarchy`() {
        verifySchema(
            typesFromSerializersModule = listOf(typeOf<NonSealedInterface>()),
            serializersModule = module,
            expectedSchema = """
                message NonSealedInterface {
                  oneof subtypes {
                    NonSealedLevel2LeafClassA nonSealedLevel2LeafClassA = 2;
                    NonSealedLevel2LeafClassB nonSealedLevel2LeafClassB = 3;
                    NonSealedLevel3LeafClass nonSealedLevel3LeafClass = 4;
                  }
                }

                message NonSealedLevel2Class {
                  oneof subtypes {
                    NonSealedLevel3LeafClass nonSealedLevel3LeafClass = 4;
                  }
                }
                
                message NonSealedLevel2LeafClassA {
                  int32 int = 1;
                }
                
                message NonSealedLevel2LeafClassB {
                  NonSealedLevel2Class intermediate = 1;
                }
                
                message NonSealedLevel3LeafClass {
                  NonSealedInterface top = 1;
                }                
                """
        )
        verifyConversion(
            NonSealedLevel2LeafClassA(42),
            """1: 42"""
        )
        verifyConversion<NonSealedInterface>(
            NonSealedLevel2LeafClassA(42),
            """2: {1: 42}"""
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
    }
}