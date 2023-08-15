@file:OptIn(ExperimentalSerializationApi::class)

package pro.felixo.protobuf.serialization.generation.internal

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import pro.felixo.protobuf.EnumValue
import pro.felixo.protobuf.Identifier
import pro.felixo.protobuf.serialization.Enum
import pro.felixo.protobuf.serialization.ProtoDefaultEnumValue
import pro.felixo.protobuf.serialization.ProtoNumber
import pro.felixo.protobuf.serialization.encoding.FieldEncoding
import pro.felixo.protobuf.serialization.util.NumberIterator
import pro.felixo.protobuf.serialization.util.requireNoDuplicates
import pro.felixo.protobuf.serialization.util.simpleTypeName

fun TypeContext.enum(descriptor: SerialDescriptor): FieldEncoding.EnumReference =
    putOrGetEnum(descriptor) {
        val numberIterator = numberIteratorFromEnumElements(descriptor)

        val values = (0 until descriptor.elementsCount).map { index ->
            val annotations = descriptor.getElementAnnotations(index)
            val number =
                if (annotations.filterIsInstance<ProtoDefaultEnumValue>().any())
                    0
                else
                    annotations.filterIsInstance<ProtoNumber>().firstOrNull()?.number
                        ?.also { require(it > 0) { "Number of non-default enum value must be > 0." } }
                        ?: numberIterator.next()
            EnumValue(Identifier(descriptor.getElementName(index)), number)
        }

        Enum(
            Identifier(simpleTypeName(descriptor)),
            values
        )
    }

private fun numberIteratorFromEnumElements(descriptor: SerialDescriptor) = NumberIterator(
    1,
    reserved = (0 until descriptor.elementsCount).mapNotNull {
        descriptor.getElementAnnotations(it).filterIsInstance<ProtoNumber>().firstOrNull()?.number
    }.requireNoDuplicates { "Duplicate field number in descriptor ${descriptor.serialName}: $it" }
        .map { it..it }
)
