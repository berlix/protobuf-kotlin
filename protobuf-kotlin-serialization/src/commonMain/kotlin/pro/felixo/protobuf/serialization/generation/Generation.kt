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

/**
 * Creates an [EncodingSchema] from the parameters:
 *
 * - [descriptors] is a list of [SerialDescriptor]s for all top-level messages that should be supported by the schema.
 *   Note that any referenced descriptors are discovered automatically.
 * - [typesFromSerializersModule] is a list of [KType]s for all top-level messages to be supported by the
 *   schema, which are not already included in [descriptors]. These types are looked up in the [serializersModule].
 * - [serializersModule] is the [SerializersModule] to use to support contextual serialization and open polymorphism.
 * - [encodeZeroValues] controls whether fields that have their Protobuf default values (zero/empty) are encoded or
 *   omitted. This can be useful for compatibility with Protobuf 2 consumers. Note that this only applies to
 *   singular fields: values for optional and repeatable fields are always encoded.
 */
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
