@file:OptIn(ExperimentalSerializationApi::class)

package pro.felixo.protobuf.serialization.generation

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializerOrNull
import pro.felixo.protobuf.serialization.EncodingSchema
import pro.felixo.protobuf.serialization.generation.internal.TypeContext
import pro.felixo.protobuf.serialization.generation.internal.namedType
import kotlin.reflect.KType

fun encodingSchema(
    descriptors: List<SerialDescriptor> = emptyList(),
    typesFromSerializersModule: List<KType> = emptyList(),
    serializersModule: SerializersModule = EmptySerializersModule(),
    encodeZeroValues: Boolean = false
) = with (TypeContext(serializersModule, encodeZeroValues)) {
    descriptors.forEach { namedType(it) }

    typesFromSerializersModule.forEach { type ->
        namedType(
            serializersModule.serializerOrNull(type)?.descriptor ?: error("No serializer in module found for $type")
        )
    }

    EncodingSchema(serializersModule, localTypesByName)
}
