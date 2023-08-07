package pro.felixo.proto3.schemadocument

import pro.felixo.proto3.EnumValue
import pro.felixo.proto3.FieldNumber
import pro.felixo.proto3.FieldRule
import pro.felixo.proto3.Identifier

interface SchemaElement {
    val elementType: String
    val elementName: String
}

data class SchemaDocument(
    val types: List<Type> = emptyList()
) : SchemaElement {
    override val elementType = "schema"
    override val elementName: String = "root"

    override fun toString(): String {
        val out = StringBuilder()
        SchemaDocumentWriter(out).write(this)
        return out.toString()
    }
}

sealed class Type : SchemaElement {
    abstract val name: Identifier

    override val elementName: String get() = name.toString()

    override fun toString(): String {
        val out = StringBuilder()
        SchemaDocumentWriter(out).write(this)
        return out.toString()
    }
}

data class Message(
    override val name: Identifier,
    val members: List<Member> = emptyList(),
    val nestedTypes: List<Type> = emptyList(),
    val reservedNames: List<Identifier> = emptyList(),
    val reservedNumbers: List<IntRange> = emptyList()
) : Type() {
    override val elementType = "message"
}

val Message.fields: List<Field> get() =
    (members.filterIsInstance<Field>() + members.filterIsInstance<OneOf>().flatMap { it.fields })

sealed class Member : SchemaElement {
    abstract val name: Identifier
    override val elementName: String get() = name.toString()
}

data class Field(
    override val name: Identifier,
    val type: FieldType,
    val number: FieldNumber,
    val rule: FieldRule = FieldRule.Singular
) : Member() {
    override val elementType = "field"
}

data class OneOf(
    override val name: Identifier,
    val fields: List<Field>
) : Member() {
    override val elementType = "oneof"
}

data class Enum(
    override val name: Identifier,
    val values: List<EnumValue>,
    val allowAlias: Boolean = false,
    val reservedNames: List<Identifier> = emptyList(),
    val reservedNumbers: List<IntRange> = emptyList()
) : Type() {
    override val elementType = "enum"
}

sealed class FieldType {
    sealed class Scalar<DecodedType: Any>(val name: kotlin.String) : FieldType() {
        override fun toString() = name
    }

    sealed class Integer32(name: kotlin.String) : Scalar<Int>(name)
    sealed class Integer64(name: kotlin.String) : Scalar<Long>(name)

    object Double : Scalar<kotlin.Double>("double")
    object Float : Scalar<kotlin.Float>("float")
    object Int32 : Integer32("int32")
    object Int64 : Integer64("int64")
    object UInt32 : Integer32("uint32")
    object UInt64 : Integer64("uint64")
    object SInt32 : Integer32("sint32")
    object SInt64 : Integer64("sint64")
    object Fixed32 : Integer32("fixed32")
    object Fixed64 : Integer64("fixed64")
    object SFixed32 : Integer32("sfixed32")
    object SFixed64 : Integer64("sfixed64")
    object Bool : Scalar<Boolean>("bool")
    object String : Scalar<kotlin.String>("string")
    object Bytes : Scalar<ByteArray>("bytes")

    /**
     * A reference to a message or enum type.
     */
    data class Reference(val components: List<Identifier>) : FieldType() {
        override fun toString() = components.joinToString(".")
    }
}

val SCALARS: List<FieldType.Scalar<*>> = listOf(
    FieldType.Double,
    FieldType.Float,
    FieldType.Int32,
    FieldType.Int64,
    FieldType.UInt32,
    FieldType.UInt64,
    FieldType.SInt32,
    FieldType.SInt64,
    FieldType.Fixed32,
    FieldType.Fixed64,
    FieldType.SFixed32,
    FieldType.SFixed64,
    FieldType.Bool,
    FieldType.String,
    FieldType.Bytes
)
