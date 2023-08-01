package pro.felixo.proto3.serialization.generation

import kotlinx.serialization.descriptors.SerialDescriptor
import pro.felixo.proto3.serialization.Type
import pro.felixo.proto3.serialization.encoding.FieldEncoding
import pro.felixo.proto3.serialization.util.simpleTypeName

class TypeContext {
    private val referencesByName =
        mutableMapOf<String, Pair<FieldEncoding.Reference, SerialDescriptor?>>()

    val localTypes: List<Type> get() = referencesByName.values.map { it.first.type }
    val localTypesByName: Map<String, Type> get() = referencesByName.mapValues { (_, v) -> v.first.type }

    fun putOrGet(
        descriptor: SerialDescriptor? = null,
        name: String = requireNotNull(descriptor?.let { simpleTypeName(descriptor) }),
        createType: () -> Type
    ): FieldEncoding.Reference {
        val existingType = referencesByName[name]
        return if (existingType != null) {
            if (descriptor != null && existingType.second?.isCompatibleWith(descriptor) == true)
                existingType.first
            else
                error("Name conflict: encountered two incompatible types for type name $name")
        } else {
            val (reference, onCreateType) = FieldEncoding.Reference.lazy()
            referencesByName[name] = Pair(reference, descriptor)
            onCreateType(createType())
            reference
        }
    }
}

fun <T> typeContext(block: TypeContext.() -> T): T = TypeContext().block()
