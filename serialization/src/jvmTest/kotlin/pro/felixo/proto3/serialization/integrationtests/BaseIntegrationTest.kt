package pro.felixo.proto3.serialization.integrationtests

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import pro.felixo.proto3.protoscope.ProtoscopeConverter
import pro.felixo.proto3.protoscope.ProtoscopeTokenizer
import pro.felixo.proto3.schemadocument.validation.ValidationResult
import pro.felixo.proto3.schemadocument.validation.validate
import pro.felixo.proto3.serialization.EncodingSchema
import pro.felixo.proto3.serialization.testutil.schemaOf
import pro.felixo.proto3.serialization.toSchemaDocument
import kotlin.reflect.KType

abstract class BaseIntegrationTest {
    protected val protoscopeConverter = ProtoscopeConverter(ProtoscopeTokenizer())
    protected lateinit var schema: EncodingSchema

    protected fun verifyFailure(descriptor: SerialDescriptor) {
        assertFailure { EncodingSchema.of(listOf(descriptor)) }
    }

    protected fun verify(descriptor: SerialDescriptor, expectedSchema: String) =
        verify(listOf(descriptor), expectedSchema = expectedSchema)

    protected fun verify(descriptors: List<SerialDescriptor>, expectedSchema: String) =
        verifySchema(descriptors, expectedSchema = expectedSchema)

    protected fun verifySchema(
        descriptors: List<SerialDescriptor> = emptyList(),
        typesFromSerializersModule: List<KType> = emptyList(),
        serializersModule: SerializersModule = EmptySerializersModule(),
        expectedSchema: String
    ) {
        val expectedSchemaDocument = schemaOf(expectedSchema)
        schema = EncodingSchema.of(
            descriptors,
            typesFromSerializersModule,
            serializersModule
        )

        val schemaDocument = schema.toSchemaDocument()

        println("Expected schema:\n$expectedSchemaDocument\n")
        println("Actual schema:\n$schemaDocument")

        assertThat(validate(schemaDocument)).isEqualTo(ValidationResult.OK)
        assertThat(schemaDocument).isEqualTo(expectedSchemaDocument)
    }

    protected inline fun <reified T> verifyConversion(value: T, protoscope: String) =
        verifyConversion(value, protoscope, serializer())

    protected inline fun <reified T> verifyConversion(value: T, protoscope: String, serializer: KSerializer<T>) {
        val bytes = protoscopeConverter.convert(protoscope)
        println("Protoscope: ${bytes.hex()}")
        verifyEncode(value, bytes, serializer)
        verifyDecode(value, bytes, serializer)
    }

    protected inline fun <reified T> verifyDecode(value: T, protoscope: String) =
        verifyDecode(value, protoscope, serializer())

    protected inline fun <reified T> verifyDecode(value: T, protoscope: String, serializer: KSerializer<T>) =
        verifyDecode(value, protoscopeConverter.convert(protoscope), serializer)

    protected inline fun <reified T> verifyDecode(value: T, bytes: ByteArray, serializer: KSerializer<T>) =
        assertThat(schema.decodeFromByteArray(serializer, bytes)).isEqualTo(value)

    protected inline fun <reified T> verifyEncode(value: T, bytes: ByteArray, serializer: KSerializer<T>) {
        val encoded = schema.encodeToByteArray(serializer, value)
        println("Encoded:    ${encoded.hex()}")
        assertThat(encoded).isEqualTo(bytes)
    }

    protected fun ByteArray.hex() = joinToString("") { "%02x".format(it) }
}
