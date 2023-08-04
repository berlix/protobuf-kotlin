package pro.felixo.proto3.serialization.integrationtests

import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.Test

class ContextualIntegrationTest : BaseIntegrationTest() {

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

    data class ClassWithContextualSerializer(val int: Int)

    @Serializable
    data class SerializableClassWithContextualSerializer(val int: Int)

    @Serializable
    data class ClassWithContextualProperty(
        @Contextual
        val int: ClassWithContextualSerializer
    )

    @Serializable
    data class ClassWithContextualAndSerializableProperty(
        @Contextual
        val int: SerializableClassWithContextualSerializer
    )

    class ClassWithContextualSerializerSerializer : KSerializer<ClassWithContextualSerializer> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("ClassWithContextualSerializer", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): ClassWithContextualSerializer =
            ClassWithContextualSerializer(decoder.decodeString().toInt())

        override fun serialize(encoder: Encoder, value: ClassWithContextualSerializer) =
            encoder.encodeString("${value.int}")
    }

    class SerializableClassWithContextualSerializerSerializer : KSerializer<SerializableClassWithContextualSerializer> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("SerializableClassWithContextualSerializer", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): SerializableClassWithContextualSerializer =
            SerializableClassWithContextualSerializer(decoder.decodeString().toInt())

        override fun serialize(encoder: Encoder, value: SerializableClassWithContextualSerializer) =
            encoder.encodeString("${value.int}")
    }
}
