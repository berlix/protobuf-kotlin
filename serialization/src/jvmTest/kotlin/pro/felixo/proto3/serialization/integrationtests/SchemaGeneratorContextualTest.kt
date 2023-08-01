package pro.felixo.proto3.serialization.integrationtests

import kotlinx.serialization.modules.SerializersModule
import pro.felixo.proto3.serialization.testutil.ClassWithContextualAndSerializableProperty
import pro.felixo.proto3.serialization.testutil.ClassWithContextualProperty
import pro.felixo.proto3.serialization.testutil.ClassWithContextualSerializer
import pro.felixo.proto3.serialization.testutil.ClassWithContextualSerializerSerializer
import pro.felixo.proto3.serialization.testutil.SerializableClassWithContextualSerializer
import pro.felixo.proto3.serialization.testutil.SerializableClassWithContextualSerializerSerializer
import kotlin.test.Test

class SchemaGeneratorContextualTest : SchemaGeneratorBaseTest() {

    private val module = SerializersModule {
        contextual(ClassWithContextualSerializer::class, ClassWithContextualSerializerSerializer())
        contextual(
            SerializableClassWithContextualSerializer::class,
            SerializableClassWithContextualSerializerSerializer()
        )
    }

    @Test
    fun `creates message for class with contextual property`() {
        verifySchema(
            listOf(ClassWithContextualProperty.serializer().descriptor),
            serializersModule = module,
            expectedSchema = """
                message ClassWithContextualProperty {
                    string int = 1;
                }
                """
        )
        verifyConversion(
            ClassWithContextualProperty(ClassWithContextualSerializer(42)),
            """1: {"42"}"""
        )
    }

    @Test
    fun `creates message for class with contextual and serializable property`() {
        verifySchema(
            listOf(ClassWithContextualAndSerializableProperty.serializer().descriptor),
            serializersModule = module,
            expectedSchema = """
                message ClassWithContextualAndSerializableProperty {
                    string int = 1;
                }
                """
        )
        verifyConversion(
            ClassWithContextualAndSerializableProperty(
                SerializableClassWithContextualSerializer(42)
            ),
            """1: {"42"}"""
        )
    }
}
