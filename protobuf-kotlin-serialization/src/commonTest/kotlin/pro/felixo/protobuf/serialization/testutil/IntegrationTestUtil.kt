package pro.felixo.protobuf.serialization.testutil

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import pro.felixo.protobuf.protoscope.ProtoscopeConverter
import pro.felixo.protobuf.schemadocument.areEquivalent
import pro.felixo.protobuf.schemadocument.validation.ValidationResult
import pro.felixo.protobuf.schemadocument.validation.validate
import pro.felixo.protobuf.serialization.EncodingSchema
import pro.felixo.protobuf.serialization.generation.encodingSchema
import pro.felixo.protobuf.serialization.toSchemaDocument
import kotlin.reflect.KType

class IntegrationTestUtil {
    val protoscopeConverter = ProtoscopeConverter()
    private var schemaResult: Result<EncodingSchema>? = null
    val schema get() = checkNotNull(schemaResult).getOrThrow()

    fun givenSchema(descriptor: SerialDescriptor, encodeZeroValues: Boolean = false) =
        givenSchema(listOf(descriptor), encodeZeroValues = encodeZeroValues)

    fun givenSchema(
        descriptors: List<SerialDescriptor> = emptyList(),
        typesFromSerializersModule: List<KType> = emptyList(),
        serializersModule: SerializersModule = EmptySerializersModule(),
        encodeZeroValues: Boolean = false
    ) {
        schemaResult = runCatching {
            encodingSchema(
                descriptors,
                typesFromSerializersModule,
                serializersModule,
                encodeZeroValues
            )
        }
    }

    fun verifySchema(expectedSchema: String) {
        val expectedSchemaDocument = schemaOf(expectedSchema)
        println("Expected schema:\n$expectedSchemaDocument\n")

        val schemaDocument = schema.toSchemaDocument()
        println("Actual schema:\n$schemaDocument")

        assertThat(validate(schemaDocument)).isEqualTo(ValidationResult.OK)
        assertThat(areEquivalent(schemaDocument, expectedSchemaDocument)).isTrue()
    }

    fun verifySchemaGenerationFails() {
        assertThat(schemaResult).isNotNull().isFailure()
    }

    inline fun <reified T> verifyConversion(value: T, protoscope: String) =
        verifyConversion(value, protoscope, serializer())

    inline fun <reified T> verifyConversion(value: T, protoscope: String, serializer: KSerializer<T>) {
        val bytes = protoscopeConverter.convert(protoscope)
        verifyEncode(value, bytes, serializer)
        verifyDecode(value, bytes, serializer)
    }

    inline fun <reified T> verifyDecode(value: T, protoscope: String) =
        verifyDecode(value, protoscope, serializer())

    inline fun <reified T> verifyDecode(value: T, protoscope: String, serializer: KSerializer<T>) =
        verifyDecode(value, protoscopeConverter.convert(protoscope), serializer)

    inline fun <reified T> verifyDecode(value: T, bytes: ByteArray, serializer: KSerializer<T>) =
        assertThat(schema.decodeFromByteArray(serializer, bytes)).isEqualTo(value)

    inline fun <reified T> verifyEncode(value: T, protoscope: String) =
        verifyEncode(value, protoscope, serializer())

    inline fun <reified T> verifyEncode(value: T, protoscope: String, serializer: KSerializer<T>) =
        verifyEncode(value, protoscopeConverter.convert(protoscope), serializer)

    inline fun <reified T> verifyEncode(value: T, bytes: ByteArray, serializer: KSerializer<T>) {
        val encoded = schema.encodeToByteArray(serializer, value)
        println("Protoscope: ${bytes.hex()}")
        println("Encoded:    ${encoded.hex()}")
        assertThat(encoded).isEqualTo(bytes)
    }

    fun ByteArray.hex(): String {
        val characters = CharArray(size * 2)
        for (i in indices) {
            val b = get(i).toInt() and 0xFF
            characters[i shl 1] = HEX_CHARS[b shr 4]
            characters[(i shl 1) + 1] = HEX_CHARS[b and 0x0F]
        }
        return characters.concatToString()
    }

    companion object {
        private val HEX_CHARS = "0123456789abcdef".toCharArray()
    }
}
