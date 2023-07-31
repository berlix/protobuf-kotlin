package pro.felixo.proto3.internal

import kotlinx.serialization.descriptors.SerialDescriptor
import pro.felixo.proto3.FieldEncoding
import pro.felixo.proto3.schema.Type

class TypeContext {
    private val localTypesByName =
        mutableMapOf<String, Pair<FieldEncoding.Reference, SerialDescriptor?>>()

    val localTypes: List<Type> get() = localTypesByName.values.map { it.first.type }

    fun putOrGet(
        descriptor: SerialDescriptor? = null,
        name: String = requireNotNull(descriptor?.let { simpleTypeName(descriptor) }),
        createType: () -> Type
    ): FieldEncoding.Reference {
        val existingType = localTypesByName[name]
        return if (existingType != null) {
            if (descriptor != null && existingType.second?.isCompatibleWith(descriptor) == true)
                existingType.first
            else
                error("Name conflict: encountered two incompatible types for type name $name")
        } else {
            val (reference, onCreateType) = FieldEncoding.Reference.lazy()
            localTypesByName[name] = Pair(reference, descriptor)
            onCreateType(createType())
            reference
        }
    }
}

fun <T> typeContext(block: TypeContext.() -> T): T = TypeContext().block()
