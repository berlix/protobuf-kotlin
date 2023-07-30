package pro.felixo.proto3

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.serialization.modules.SerializersModule
import pro.felixo.proto3.schema.toSchemaDocument
import pro.felixo.proto3.testutil.NonSealedInterface
import pro.felixo.proto3.testutil.NonSealedLevel2Class
import pro.felixo.proto3.testutil.NonSealedLevel2LeafClassA
import pro.felixo.proto3.testutil.NonSealedLevel2LeafClassB
import pro.felixo.proto3.testutil.NonSealedLevel3LeafClass
import pro.felixo.proto3.testutil.SealedTopClass
import pro.felixo.proto3.testutil.SealedLevel2LeafClassA
import pro.felixo.proto3.testutil.SealedLevel2Class
import pro.felixo.proto3.testutil.SealedLevel2LeafClassB
import pro.felixo.proto3.testutil.SealedLevel3LeafClass
import pro.felixo.proto3.testutil.schemaOf
import kotlin.reflect.typeOf
import kotlin.test.Test

class SchemaGeneratorPolymorphyTest : SchemaGeneratorBaseTest(
    SerializersModule {
        polymorphic(NonSealedLevel2Class::class, NonSealedLevel3LeafClass::class, NonSealedLevel3LeafClass.serializer())
        polymorphic(NonSealedInterface::class, NonSealedLevel2LeafClassA::class, NonSealedLevel2LeafClassA.serializer())
        polymorphic(NonSealedInterface::class, NonSealedLevel2LeafClassB::class, NonSealedLevel2LeafClassB.serializer())
        polymorphic(NonSealedInterface::class, NonSealedLevel3LeafClass::class, NonSealedLevel3LeafClass.serializer())
    }
) {
    @Test
    fun `creates sealed class hierarchy`() {
        generator.add(SealedTopClass.serializer().descriptor)
        assertThat(generator.schema().toSchemaDocument()).isEqualTo(
            schemaOf(
                """
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
        generator.addFromSerializersModule(typeOf<NonSealedInterface>())
        assertThat(generator.schema().toSchemaDocument()).isEqualTo(
            schemaOf(
                """
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
