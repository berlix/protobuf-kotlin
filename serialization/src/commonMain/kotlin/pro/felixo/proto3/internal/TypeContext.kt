package pro.felixo.proto3.internal

import kotlinx.serialization.descriptors.SerialDescriptor
import pro.felixo.proto3.FieldType
import pro.felixo.proto3.schema.Identifier
import pro.felixo.proto3.schema.Type

class TypeContext {
    private val typesInCreation = mutableMapOf<String, SerialDescriptor?>()
    private val localTypesByName = mutableMapOf<String, Pair<SerialDescriptor?, Type>>()
    val localTypes get() = localTypesByName.values.map { it.second }.toSet()

    fun putOrGet(
        descriptor: SerialDescriptor? = null,
        name: String = requireNotNull(descriptor?.let { simpleTypeName(descriptor) }),
        createType: () -> Type
    ): FieldType.Reference {
        val reference = FieldType.Reference(listOf(Identifier(name)))
        val existingType = localTypesByName[name]
        return if (existingType != null) {
            if (descriptor != null && existingType.first?.isCompatibleWith(descriptor) == true)
                reference
            else
                error("Name conflict: encountered two incompatible types for type name $name")
        } else {
            if (typesInCreation.containsKey(name)) {
                if (descriptor != null && typesInCreation[name]?.isCompatibleWith(descriptor) == true)
                    reference
                else
                    error("Name conflict: encountered two incompatible types for type name $name")
            } else try {
                typesInCreation[name] = descriptor
                localTypesByName[name] = descriptor to createType()
                reference
            } finally {
                typesInCreation.remove(name)
            }
        }
    }
}

fun <T> typeContext(block: TypeContext.() -> T): T = TypeContext().block()
