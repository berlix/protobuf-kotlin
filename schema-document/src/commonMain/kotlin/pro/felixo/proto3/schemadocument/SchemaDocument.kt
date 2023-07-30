package pro.felixo.proto3.schemadocument

import pro.felixo.proto3.EnumValue
import pro.felixo.proto3.FieldNumber
import pro.felixo.proto3.Identifier
import pro.felixo.proto3.util.requireNoDuplicates

data class SchemaDocument(
    val types: List<Type> = emptyList()
) {
    fun validate() {
        types.requireNoDuplicates({ it.name }) { "Duplicate type name: $it" }
        types.forEach { it.validate() }
    }

    override fun toString(): String {
        val out = StringBuilder()
        SchemaDocumentWriter(out).write(this)
        return out.toString()
    }
}

sealed class Type {
    abstract val name: Identifier

    fun validate() {
        name.validate()
        validateType()
    }

    protected abstract fun validateType()

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
    override fun validateType() {
        validateMembers()
        validateNestedTypes()
        validateReservedNames()
        requireDistinctFieldNames()
        requireDistinctFieldNumbers()
        requireDistinctTypeNames()
        requireReservationsRespected()
    }

    private fun validateMembers() {
        members.forEach { it.validate() }
    }

    private fun validateNestedTypes() {
        nestedTypes.forEach { it.validate() }
    }

    private fun validateReservedNames() {
        reservedNames.forEach { it.validate() }
    }

    private fun requireDistinctFieldNumbers() {
        fields.requireNoDuplicates({ it.number }) { "Duplicate field number in message $name: $it" }
    }

    private fun requireDistinctFieldNames() {
        fields.requireNoDuplicates({ it.name }) { "Duplicate field name in message $name: $it" }
    }

    private fun requireDistinctTypeNames() {
        nestedTypes.requireNoDuplicates({ it.name }) { "Duplicate nested type name in message $name: $it" }
    }

    private fun requireReservationsRespected() {
        fields.forEach { field ->
            require(!reservedNames.contains(field.name)) { "Field name ${field.name} is reserved in message $name" }
            require(!reservedNumbers.any { it.contains(field.number.value) }) {
                "Field number ${field.number.value} is reserved in message $name"
            }
        }
    }
}

val Message.fields: List<Field> get() =
    (members.filterIsInstance<Field>() + members.filterIsInstance<OneOf>().flatMap { it.fields })


sealed class Member {
    abstract val name: Identifier

    fun validate() {
        name.validate()
        validateMember()
    }

    protected abstract fun validateMember()
}

data class Field(
    override val name: Identifier,
    val type: FieldType,
    val number: FieldNumber,
    val rule: FieldRule = FieldRule.Singular
) : Member() {
    override fun validateMember() {
        type.validate()
        number.validate()
    }
}

data class OneOf(
    override val name: Identifier,
    val fields: List<Field>
) : Member() {
    override fun validateMember() {
        require(fields.isNotEmpty()) { "OneOf must have at least one field" }
        require(fields.all { it.rule != FieldRule.Repeated }) { "OneOf fields may not be repeated" }
        fields.forEach { it.validate() }
    }
}

enum class FieldRule {
    Singular,
    Optional,
    Repeated
}

data class Enumeration(
    override val name: Identifier,
    val values: List<EnumValue>,
    val allowAlias: Boolean = false,
    val reservedNames: List<Identifier> = emptyList(),
    val reservedNumbers: List<IntRange> = emptyList()
) : Type() {
    override fun validateType() {
        requireValues()
        validateValues()
        requireDistinctNames()
        requireDefaultValue()
        if (!allowAlias)
            requireDistinctNumbers()
        validateReservedNames()
        requireReservationsRespected()
    }

    private fun requireValues() {
        require(values.isNotEmpty()) { "Enum $name must have at least one value" }
    }

    private fun validateValues() {
        values.forEach { it.validate() }
    }

    private fun requireDefaultValue() {
        require(values.first().number == 0) { "Fist value of enum $name must have number 0" }
    }

    private fun requireDistinctNames() {
        values.requireNoDuplicates({ it.name }) { "Duplicate value name in enum $name: $it" }
    }

    private fun requireDistinctNumbers() {
        values.requireNoDuplicates({ it.number }) { "Duplicate value number in enum $name: $it" }
    }

    private fun validateReservedNames() {
        reservedNames.forEach { it.validate() }
    }

    private fun requireReservationsRespected() {
        values.forEach { value ->
            require(!reservedNames.contains(value.name)) { "Field name ${value.name} is reserved in message $name" }
            require(!reservedNumbers.any { it.contains(value.number) }) {
                "Field number ${value.number} is reserved in message $name"
            }
        }
    }
}

sealed class FieldType {
    abstract fun validate()

    sealed class Scalar<DecodedType: Any>(val name: kotlin.String) : FieldType() {
        override fun toString() = name
        override fun validate() {}
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
        override fun validate() {
            require(components.isNotEmpty()) { "Type reference must not be empty" }
        }

        override fun toString() = components.joinToString(".")
    }

    companion object {
        val SCALARS: Set<Scalar<*>> = setOf(
            Double,
            Float,
            Int32,
            Int64,
            UInt32,
            UInt64,
            SInt32,
            SInt64,
            Fixed32,
            Fixed64,
            SFixed32,
            SFixed64,
            Bool,
            String,
            Bytes
        )
    }
}
