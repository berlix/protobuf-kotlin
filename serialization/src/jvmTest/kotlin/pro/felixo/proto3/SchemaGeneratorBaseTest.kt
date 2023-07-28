package pro.felixo.proto3

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
import pro.felixo.proto3.testutil.schemaOf

abstract class SchemaGeneratorBaseTest(
    module: SerializersModule = EmptySerializersModule()
) {
    protected open val generator = SchemaGenerator(module)
    protected val protoscopeConverter = ProtoscopeConverter(ProtoscopeTokenizer())
    protected open val proto3 = Proto3(module)

    protected fun verifyFailure(descriptor: SerialDescriptor) {
        assertFailure { generator.add(descriptor) }
    }

    protected fun verify(descriptor: SerialDescriptor, expectedSchema: String) =
        verify(listOf(descriptor), expectedSchema)

    protected fun verify(
        descriptors: List<SerialDescriptor>,
        expectedSchema: String
    ) {
        descriptors.forEach { generator.add(it) }
        assertThat(generator.schema()).isEqualTo(schemaOf(expectedSchema))
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
        assertThat(proto3.decodeFromByteArray(serializer, bytes)).isEqualTo(value)

    protected inline fun <reified T> verifyEncode(value: T, bytes: ByteArray, serializer: KSerializer<T>) {
        val encoded = proto3.encodeToByteArray(serializer, value)
        println("Encoded:    ${encoded.hex()}")
        assertThat(encoded).isEqualTo(bytes)
    }

    protected fun ByteArray.hex() = joinToString("") { "%02x".format(it) }
}
