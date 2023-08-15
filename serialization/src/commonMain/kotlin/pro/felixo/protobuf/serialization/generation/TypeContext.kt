package pro.felixo.protobuf.serialization.generation

import kotlinx.serialization.descriptors.SerialDescriptor
import pro.felixo.protobuf.serialization.Enum
import pro.felixo.protobuf.serialization.Message
import pro.felixo.protobuf.serialization.Type
import pro.felixo.protobuf.serialization.encoding.FieldEncoding
import pro.felixo.protobuf.serialization.util.simpleTypeName

class TypeContext {
    private val referencesByName =
        mutableMapOf<String, Pair<FieldEncoding.Reference<*>, SerialDescriptor?>>()

    val localTypes: List<Type> get() = referencesByName.values.map { it.first.type }
    val localTypesByName: Map<String, Type> get() = referencesByName.mapValues { (_, v) -> v.first.type }

    fun putOrGetMessage(
        descriptor: SerialDescriptor? = null,
        name: String = requireNotNull(descriptor?.let { simpleTypeName(descriptor) }),
        createType: () -> Message
    ): FieldEncoding.MessageReference {
        val existingType = referencesByName[name]
        return if (existingType != null) {
            if (descriptor != null && existingType.second?.isCompatibleWith(descriptor) == true)
                existingType.first as FieldEncoding.MessageReference
            else
                error("Name conflict: encountered two incompatible types for type name $name")
        } else {
            val (reference, onCreateType) = FieldEncoding.MessageReference.lazy()
            referencesByName[name] = Pair(reference, descriptor)
            onCreateType(createType())
            reference
        }
    }

    fun putOrGetEnum(
        descriptor: SerialDescriptor? = null,
        name: String = requireNotNull(descriptor?.let { simpleTypeName(descriptor) }),
        createType: () -> Enum
    ): FieldEncoding.EnumReference {
        val existingType = referencesByName[name]
        return if (existingType != null) {
            if (descriptor != null && existingType.second?.isCompatibleWith(descriptor) == true)
                existingType.first as FieldEncoding.EnumReference
            else
                error("Name conflict: encountered two incompatible types for type name $name")
        } else {
            val reference = FieldEncoding.EnumReference(createType())
            referencesByName[name] = Pair(reference, descriptor)
            reference
        }
    }
}

fun <T> typeContext(block: TypeContext.() -> T): T = TypeContext().block()
