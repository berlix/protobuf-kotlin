package pro.felixo.protobuf.serialization.integrationtests

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isNotNull
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import pro.felixo.protobuf.protoscope.ProtoscopeConverter
import pro.felixo.protobuf.protoscope.ProtoscopeTokenizer
import pro.felixo.protobuf.schemadocument.validation.ValidationResult
import pro.felixo.protobuf.schemadocument.validation.validate
import pro.felixo.protobuf.serialization.EncodingSchema
import pro.felixo.protobuf.serialization.testutil.schemaOf
import pro.felixo.protobuf.serialization.toSchemaDocument
import kotlin.reflect.KType

abstract class BaseIntegrationTest {
    protected val protoscopeConverter = ProtoscopeConverter(ProtoscopeTokenizer())
    private var schemaResult: Result<EncodingSchema>? = null
    protected val schema get() = checkNotNull(schemaResult).getOrThrow()

    protected fun givenSchema(descriptor: SerialDescriptor, encodeZeroValues: Boolean = false) =
        givenSchema(listOf(descriptor), encodeZeroValues = encodeZeroValues)

    protected fun givenSchema(
        descriptors: List<SerialDescriptor> = emptyList(),
        typesFromSerializersModule: List<KType> = emptyList(),
        serializersModule: SerializersModule = EmptySerializersModule(),
        encodeZeroValues: Boolean = false
    ) {
        schemaResult = runCatching {
            EncodingSchema.of(
                descriptors,
                typesFromSerializersModule,
                serializersModule,
                encodeZeroValues
            )
        }
    }

    protected fun verifySchema(expectedSchema: String) {
        val expectedSchemaDocument = schemaOf(expectedSchema)
        println("Expected schema:\n$expectedSchemaDocument\n")

        val schemaDocument = schema.toSchemaDocument()
        println("Actual schema:\n$schemaDocument")

        assertThat(validate(schemaDocument)).isEqualTo(ValidationResult.OK)
        assertThat(schemaDocument).isEqualTo(expectedSchemaDocument)
    }

    protected fun verifySchemaGenerationFails() {
        assertThat(schemaResult).isNotNull().isFailure()
    }

    protected inline fun <reified T> verifyConversion(value: T, protoscope: String) =
        verifyConversion(value, protoscope, serializer())

    protected inline fun <reified T> verifyConversion(value: T, protoscope: String, serializer: KSerializer<T>) {
        val bytes = protoscopeConverter.convert(protoscope)
        verifyEncode(value, bytes, serializer)
        verifyDecode(value, bytes, serializer)
    }

    protected inline fun <reified T> verifyDecode(value: T, protoscope: String) =
        verifyDecode(value, protoscope, serializer())

    protected inline fun <reified T> verifyDecode(value: T, protoscope: String, serializer: KSerializer<T>) =
        verifyDecode(value, protoscopeConverter.convert(protoscope), serializer)

    protected inline fun <reified T> verifyDecode(value: T, bytes: ByteArray, serializer: KSerializer<T>) =
        assertThat(schema.decodeFromByteArray(serializer, bytes)).isEqualTo(value)

    protected inline fun <reified T> verifyEncode(value: T, protoscope: String) =
        verifyEncode(value, protoscope, serializer())

    protected inline fun <reified T> verifyEncode(value: T, protoscope: String, serializer: KSerializer<T>) =
        verifyEncode(value, protoscopeConverter.convert(protoscope), serializer)

    protected inline fun <reified T> verifyEncode(value: T, bytes: ByteArray, serializer: KSerializer<T>) {
        val encoded = schema.encodeToByteArray(serializer, value)
        println("Protoscope: ${bytes.hex()}")
        println("Encoded:    ${encoded.hex()}")
        assertThat(encoded).isEqualTo(bytes)
    }

    protected fun ByteArray.hex() = joinToString("") { "%02x".format(it) }
}
