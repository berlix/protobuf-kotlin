package pro.felixo.proto3.schemadocument

import pro.felixo.proto3.EnumValue
import pro.felixo.proto3.FieldNumber
import pro.felixo.proto3.FieldRule
import pro.felixo.proto3.Identifier

data class SchemaDocument(
    val types: List<Type> = emptyList()
) {
    fun validate(): ValidationResult =
        types.validateNoDuplicates({ it.name }) { ValidationError.DuplicateTypeName(it.name) } +
        types.map { it.validate() }

    override fun toString(): String {
        val out = StringBuilder()
        SchemaDocumentWriter(out).write(this)
        return out.toString()
    }
}

sealed class Type {
    abstract val name: Identifier

    fun validate(): ValidationResult = name.validate() + validateType()

    protected abstract fun validateType(): ValidationResult

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
    override fun validateType(): ValidationResult =
        validateMembers() +
                validateNestedTypes() +
                validateReservedNames() +
                validateDistinctFieldNames() +
                validateDistinctFieldNumbers() +
                validateDistinctTypeNames() +
                validateReservationsRespected()

    private fun validateMembers() = members.merge { it.validate() }
    private fun validateNestedTypes() = nestedTypes.merge { it.validate() }
    private fun validateReservedNames() = reservedNames.merge { it.validate() }

    private fun validateDistinctFieldNumbers() = fields.validateNoDuplicates({ it.number }) {
        ValidationError.DuplicateFieldNumber(it.number)
    }

    private fun validateDistinctFieldNames() = fields.validateNoDuplicates({ it.name }) {
        ValidationError.DuplicateFieldName(it.name)
    }

    private fun validateDistinctTypeNames() = nestedTypes.validateNoDuplicates({ it.name }) {
        ValidationError.DuplicateTypeName(it.name)
    }

    private fun validateReservationsRespected() = fields.merge { field ->
        validate(!reservedNames.contains(field.name)) { ValidationError.ReservedFieldName(field.name) } +
        validate(!reservedNumbers.any { it.contains(field.number.value) }) {
            ValidationError.ReservedFieldNumber(field.number)
        }
    }
}

val Message.fields: List<Field> get() =
    (members.filterIsInstance<Field>() + members.filterIsInstance<OneOf>().flatMap { it.fields })

sealed class Member {
    abstract val name: Identifier

    fun validate(): ValidationResult = name.validate() + validateMember()

    protected abstract fun validateMember(): ValidationResult
}

data class Field(
    override val name: Identifier,
    val type: FieldType,
    val number: FieldNumber,
    val rule: FieldRule = FieldRule.Singular
) : Member() {
    override fun validateMember(): ValidationResult = type.validate() + number.validate()
}

data class OneOf(
    override val name: Identifier,
    val fields: List<Field>
) : Member() {
    override fun validateMember(): ValidationResult =
        validate(fields.isNotEmpty()) { ValidationError.OneOfWithoutFields } +
        fields.merge { validate(it.rule != FieldRule.Repeated) { ValidationError.RepeatedFieldInOneOf(it.name) } } +
        fields.merge { it.validate() }
}

data class Enumeration(
    override val name: Identifier,
    val values: List<EnumValue>,
    val allowAlias: Boolean = false,
    val reservedNames: List<Identifier> = emptyList(),
    val reservedNumbers: List<IntRange> = emptyList()
) : Type() {
    override fun validateType() : ValidationResult =
        validateValuesPresent() +
        validateValues() +
        validateDistinctNames() +
        validateDefaultValue() +
        validateDistinctNumbers() +
        validateReservedNames() +
        validateReservationsRespected()

    private fun validateValuesPresent() = validate(values.isNotEmpty()) { ValidationError.EnumContainsNoValues }

    private fun validateValues() = values.merge { it.validate() }

    private fun validateDefaultValue() =
        validate(values.first().number == 0) { ValidationError.DefaultEnumValueNotFirstValue }

    private fun validateDistinctNames() =
        values.validateNoDuplicates({ it.name }) { ValidationError.DuplicateEnumValueName(it.name) }

    private fun validateDistinctNumbers() = if (allowAlias)
        ValidationResult.OK
    else
        values.validateNoDuplicates({ it.number }) { ValidationError.DuplicateEnumValueNumber(it.number) }

    private fun validateReservedNames() =
        reservedNames.merge { it.validate() }

    private fun validateReservationsRespected() = values.merge { value ->
        validate(!reservedNames.contains(value.name)) { ValidationError.ReservedEnumValueName(value.name) } +
        validate(!reservedNumbers.any { it.contains(value.number) }) {
            ValidationError.ReservedEnumValueNumber(value.number)
        }
    }
}

sealed class FieldType {
    abstract fun validate() : ValidationResult

    sealed class Scalar<DecodedType: Any>(val name: kotlin.String) : FieldType() {
        override fun toString() = name
        override fun validate() = ValidationResult.OK
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
        override fun validate() =
            validate(components.isNotEmpty()) { ValidationError.EmptyReference }

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
