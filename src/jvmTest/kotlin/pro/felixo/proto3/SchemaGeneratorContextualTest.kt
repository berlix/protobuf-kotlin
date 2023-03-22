package pro.felixo.proto3

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.serialization.modules.SerializersModule
import pro.felixo.proto3.testutil.ClassWithContextualAndSerializableProperty
import pro.felixo.proto3.testutil.ClassWithContextualProperty
import pro.felixo.proto3.testutil.ClassWithContextualSerializer
import pro.felixo.proto3.testutil.ClassWithContextualSerializerSerializer
import pro.felixo.proto3.testutil.SerializableClassWithContextualSerializer
import pro.felixo.proto3.testutil.SerializableClassWithContextualSerializerSerializer
import pro.felixo.proto3.testutil.schemaOf
import kotlin.test.Test

class SchemaGeneratorContextualTest : SchemaGeneratorBaseTest(
    SerializersModule {
        contextual(ClassWithContextualSerializer::class, ClassWithContextualSerializerSerializer())
        contextual(
            SerializableClassWithContextualSerializer::class,
            SerializableClassWithContextualSerializerSerializer()
        )
    }
) {
    @Test
    fun `creates message for class with contextual property`() {
        generator.add(ClassWithContextualProperty.serializer().descriptor)
        assertThat(generator.schema()).isEqualTo(
            schemaOf(
                """
                message ClassWithContextualProperty {
                    string int = 1;
                }
                """
            )
        )
        verifyConversion(
            ClassWithContextualProperty(ClassWithContextualSerializer(42)),
            """1: {"42"}"""
        )
    }

    @Test
    fun `creates message for class with contextual and serializable property`() {
        generator.add(ClassWithContextualAndSerializableProperty.serializer().descriptor)
        assertThat(generator.schema()).isEqualTo(
            schemaOf(
                """
                message ClassWithContextualAndSerializableProperty {
                    string int = 1;
                }
                """
            )
        )
        verifyConversion(
            ClassWithContextualAndSerializableProperty(
                SerializableClassWithContextualSerializer(42)
            ),
            """1: {"42"}"""
        )
    }
}
