package pro.felixo.protobuf.serialization.encoding

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import pro.felixo.protobuf.FieldNumber
import pro.felixo.protobuf.serialization.Field
import pro.felixo.protobuf.wire.Tag
import pro.felixo.protobuf.wire.WireBuffer
import pro.felixo.protobuf.wire.WireType
import pro.felixo.protobuf.wire.WireValue
import pro.felixo.protobuf.wire.encodeField

@OptIn(ExperimentalSerializationApi::class)
class PolymorphicEncoder(
    override val serializersModule: SerializersModule,
    private val fieldByDescriptor: Map<SerialDescriptor, Field>,
    private val fieldNumber: FieldNumber?,
    private val output: WireBuffer,
) : HybridEncoder() {
    private val buffer = if (fieldNumber != null) WireBuffer() else output

    override fun endStructure(descriptor: SerialDescriptor) {
        if (fieldNumber != null)
            output.encodeField(Tag.of(fieldNumber, WireType.Len), WireValue.Len(buffer))
    }

    @ExperimentalSerializationApi
    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?
    ) = error("Polymorphic encoding does not support nullable elements")

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) {
        require(index == 1) { "Polymorphic encoding called with invalid index: $index" }

        val field = fieldByDescriptor[serializer.descriptor]
            ?: error("Descriptor ${serializer.descriptor.serialName} not a known subtype of ${descriptor.serialName}")
        serializer.serialize(
            field.encoder(buffer),
            value
        )
    }

    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder =
        error("Polymorphic encoding does not support inline elements")

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) =
        error("Polymorphic encoding does not support boolean elements")

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) =
        error("Polymorphic encoding does not support byte elements")

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) =
        error("Polymorphic encoding does not support char elements")

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) =
        error("Polymorphic encoding does not support double elements")

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) =
        error("Polymorphic encoding does not support float elements")

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) =
        error("Polymorphic encoding does not support int elements")

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) =
        error("Polymorphic encoding does not support long elements")

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) =
        error("Polymorphic encoding does not support short elements")

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        // This is called by the serializer to let us know the subtype, but we don't need this information here.
    }
}
