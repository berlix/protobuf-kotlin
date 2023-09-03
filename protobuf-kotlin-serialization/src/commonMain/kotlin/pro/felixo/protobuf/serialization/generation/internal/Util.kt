@file:OptIn(ExperimentalSerializationApi::class)

package pro.felixo.protobuf.serialization.generation.internal

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pro.felixo.protobuf.FieldNumber
import pro.felixo.protobuf.serialization.encoding.FieldEncoding
import pro.felixo.protobuf.wire.WireBuffer
import pro.felixo.protobuf.wire.WireValue

fun SerialDescriptor.isCompatibleWith(other: SerialDescriptor): Boolean =
    other === this || (
        kind == other.kind &&
            (0 until elementsCount).all {
                getElementName(it) == other.getElementName(it) &&
                getElementDescriptor(it) == other.getElementDescriptor(it) &&
                getElementAnnotations(it) == other.getElementAnnotations(it)
            }
        )

val SerialDescriptor.actual: SerialDescriptor
    get() = actual(false)

private fun SerialDescriptor.actual(parentNullable: Boolean): SerialDescriptor =
    if (isInline)
        elementDescriptors.single().actual(parentNullable || isNullable)
    else if (parentNullable && !isNullable)
        this.nullable
    else
        this

fun TypeContext.fieldEncoder(
    type: FieldEncoding,
    number: FieldNumber,
    encodeZeroValue: Boolean
): (WireBuffer) -> Encoder = {
    type.encoder(serializersModule, number, it, encodeZeroValue || encodeZeroValues)
}

fun TypeContext.fieldDecoder(type: FieldEncoding): (List<WireValue>) -> Decoder = {
    type.decoder(serializersModule, it)
}
