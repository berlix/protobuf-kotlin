package pro.felixo.protobuf.serialization

import pro.felixo.protobuf.EnumValue
import pro.felixo.protobuf.schemadocument.FieldType
import pro.felixo.protobuf.schemadocument.SchemaDocument
import pro.felixo.protobuf.serialization.encoding.FieldEncoding
import pro.felixo.protobuf.serialization.util.topologicalIndex

fun EncodingSchema.toSchemaDocument(): SchemaDocument {
    val typeOrdering = topologicalIndex(types.values.sortedBy { it.name }) { typeDependencies(it) }
    return SchemaDocument(
        types.values.sortedBy { typeOrdering[it] }.map { it.toDocumentType(typeOrdering) }
    )
}

private fun typeDependencies(type: Type): List<Type> = when (type) {
    is Message -> (
        type.fields.sortedBy { it.number }.mapNotNull { fieldDependency(it) } +
        type.nestedTypes.sortedBy { it.name }.flatMap { typeDependencies(it) }
    )
    is Enum -> emptyList()
}

fun fieldDependency(field: Field): Type? = when (field.encoding) {
    is FieldEncoding.Reference<*> -> field.encoding.type
    else -> null
}

fun Type.toDocumentType(typeOrdering: Map<Type, Int>): pro.felixo.protobuf.schemadocument.Type = when (this) {
    is Message -> toDocumentMessage(typeOrdering)
    is Enum -> toDocumentEnum()
}

fun Message.toDocumentMessage(typeOrdering: Map<Type, Int>) = pro.felixo.protobuf.schemadocument.Message(
    name,
    members.sortedBy { member ->
        when (member) {
            is Field -> member.number
            is OneOf -> member.fields.minOf { it.number }
        }
    }.map { it.toDocumentMember() },
    nestedTypes.sortedBy { typeOrdering[it] }.map { it.toDocumentType(typeOrdering) }
)

fun Enum.toDocumentEnum() = pro.felixo.protobuf.schemadocument.Enum(
    name,
    values.sortedWith { a: EnumValue, b: EnumValue ->
        if (a.number == 0 && b.number != 0) -1
        else if (b.number == 0 && a.number != 0) 1
        else 0
    }
)

fun Member.toDocumentMember(): pro.felixo.protobuf.schemadocument.Member = when (this) {
    is OneOf -> toDocumentOneOf()
    is Field -> toDocumentField()
}

fun OneOf.toDocumentOneOf() = pro.felixo.protobuf.schemadocument.OneOf(
    name,
    fields.sortedBy { it.number }.map { it.toDocumentField() }
)

fun Field.toDocumentField() = pro.felixo.protobuf.schemadocument.Field(
    name,
    encoding.toDocumentFieldType(),
    number,
    rule
)

private fun FieldEncoding.toDocumentFieldType() = when (this) {
    FieldEncoding.Bool -> FieldType.Bool
    FieldEncoding.Bytes -> FieldType.Bytes
    FieldEncoding.Double -> FieldType.Double
    FieldEncoding.Float -> FieldType.Float
    FieldEncoding.Fixed32 -> FieldType.Fixed32
    FieldEncoding.Int32 -> FieldType.Int32
    FieldEncoding.SFixed32 -> FieldType.SFixed32
    FieldEncoding.SInt32 -> FieldType.SInt32
    FieldEncoding.UInt32 -> FieldType.UInt32
    FieldEncoding.Fixed64 -> FieldType.Fixed64
    FieldEncoding.Int64 -> FieldType.Int64
    FieldEncoding.SFixed64 -> FieldType.SFixed64
    FieldEncoding.SInt64 -> FieldType.SInt64
    FieldEncoding.UInt64 -> FieldType.UInt64
    FieldEncoding.String -> FieldType.String
    is FieldEncoding.Reference<*> -> FieldType.Reference(listOf(name))
}
