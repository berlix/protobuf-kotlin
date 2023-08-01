package pro.felixo.proto3.schema

import pro.felixo.proto3.FieldEncoding
import pro.felixo.proto3.schemadocument.FieldType
import pro.felixo.proto3.schemadocument.SchemaDocument

fun EncodingSchema.toSchemaDocument(): SchemaDocument =
    SchemaDocument(
        types.values.sortedBy { it.name }.map { it.toDocumentType() }
    )

fun Type.toDocumentType(): pro.felixo.proto3.schemadocument.Type = when (this) {
    is Message -> toDocumentMessage()
    is Enumeration -> toDocumentEnumeration()
}

fun Message.toDocumentMessage() = pro.felixo.proto3.schemadocument.Message(
    name,
    members.map { it.toDocumentMember() },
    nestedTypes.sortedBy { it.name }.map { it.toDocumentType() }
)

fun Enumeration.toDocumentEnumeration() = pro.felixo.proto3.schemadocument.Enumeration(
    name,
    values
)

fun Member.toDocumentMember(): pro.felixo.proto3.schemadocument.Member = when (this) {
    is OneOf -> toDocumentOneOf()
    is Field -> toDocumentField()
}

fun OneOf.toDocumentOneOf() = pro.felixo.proto3.schemadocument.OneOf(
    name,
    fields.sortedBy { it.number }.map { it.toDocumentField() }
)

fun Field.toDocumentField() = pro.felixo.proto3.schemadocument.Field(
    name,
    type.toDocumentFieldType(),
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
    is FieldEncoding.Reference -> FieldType.Reference(listOf(name))
}
