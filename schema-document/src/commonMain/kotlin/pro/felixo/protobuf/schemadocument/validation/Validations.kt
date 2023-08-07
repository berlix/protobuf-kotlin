package pro.felixo.protobuf.schemadocument.validation

import pro.felixo.protobuf.EnumValue
import pro.felixo.protobuf.FieldNumber
import pro.felixo.protobuf.FieldRule
import pro.felixo.protobuf.Identifier
import pro.felixo.protobuf.schemadocument.Enum
import pro.felixo.protobuf.schemadocument.Field
import pro.felixo.protobuf.schemadocument.FieldType
import pro.felixo.protobuf.schemadocument.Member
import pro.felixo.protobuf.schemadocument.Message
import pro.felixo.protobuf.schemadocument.OneOf
import pro.felixo.protobuf.schemadocument.SchemaDocument
import pro.felixo.protobuf.schemadocument.Type
import pro.felixo.protobuf.schemadocument.fields

private val IDENTIFIER_REGEX = Regex("""[a-zA-Z_][a-zA-Z\d_]*""")

fun validate(schema: SchemaDocument): ValidationResult = rootValidationScope(schema) {
    validateNoDuplicates(schema.types, { it.name }) { ValidationError.DuplicateTypeName(this, it.name) } +
    schema.types.map { validate(it) }
}

fun ValidationScope.validate(type: Type): ValidationResult = validationScope(type) {
    validate(type.name) + when (type) {
        is Message -> validate(type)
        is Enum -> validate(type)
    }
}

private fun ValidationScope.validate(message: Message): ValidationResult {
    fun validateMembers() = message.members.validateAll { validate(it) }
    fun validateNestedTypes() = message.nestedTypes.validateAll { validate(it) }
    fun validateReservedNames() = message.reservedNames.validateAll { validate(it) }

    fun validateDistinctFieldNumbers() = validateNoDuplicates(message.fields, { it.number }) {
        ValidationError.DuplicateFieldNumber(this, it.number)
    }

    fun validateDistinctFieldNames() = validateNoDuplicates(message.fields, { it.name }) {
        ValidationError.DuplicateFieldName(this, it.name)
    }

    fun validateDistinctTypeNames() = validateNoDuplicates(message.nestedTypes, { it.name }) {
        ValidationError.DuplicateTypeName(this, it.name)
    }

    fun validateReservationsRespected() = message.fields.validateAll { field ->
        validate(!message.reservedNames.contains(field.name)) { ValidationError.ReservedFieldName(this, field.name) } +
        validate(!message.reservedNumbers.any { it.contains(field.number.value) }) {
            ValidationError.ReservedFieldNumber(this, field.number)
        }
    }

    return validateMembers() +
        validateNestedTypes() +
        validateReservedNames() +
        validateDistinctFieldNames() +
        validateDistinctFieldNumbers() +
        validateDistinctTypeNames() +
        validateReservationsRespected()
}

fun ValidationScope.validate(member: Member): ValidationResult = validationScope(member) {
    validate(member.name) + when (member) {
        is Field -> validate(member)
        is OneOf -> validate(member)
    }
}

private fun ValidationScope.validate(oneOf: OneOf): ValidationResult =
    validate(oneOf.fields.isNotEmpty()) { ValidationError.OneOfWithoutFields(this) } +
    oneOf.fields.validateAll { validate(it as Member) } +
    oneOf.fields.validateAll {
        validate(it.rule != FieldRule.Repeated) { ValidationError.RepeatedFieldInOneOf(this, it.name) }
    }

private fun ValidationScope.validate(field: Field): ValidationResult {
    fun ValidationScope.validate(type: FieldType) = when (type) {
        is FieldType.Reference -> {
            validate(type.components.isNotEmpty()) { ValidationError.EmptyReference(this) } +
            type.components.validateAll { validate(it) }
        }
        is FieldType.Scalar<*> -> ValidationResult.OK
    }

    return validate(field.type) + validate(field.number)
}

private fun ValidationScope.validate(enum: Enum) : ValidationResult {
    fun validateValuesPresent() = validate(enum.values.isNotEmpty()) {
        ValidationError.EnumContainsNoValues(this)
    }

    fun validateValues() = enum.values.validateAll { validate(it) }

    fun validateDefaultValue() =
        validate(enum.values.firstOrNull()?.number?.let { it == 0 } ?: true) {
            ValidationError.FirstEnumIsNotDefaultValue(this)
        }

    fun validateDistinctNames() =
        validateNoDuplicates(enum.values, { it.name }) { ValidationError.DuplicateEnumValueName(this, it.name) }

    fun validateDistinctNumbers() = if (enum.allowAlias)
        ValidationResult.OK
    else
        validateNoDuplicates(enum.values, { it.number }) { ValidationError.DuplicateEnumValueNumber(this, it.number) }

    fun validateReservedNames() =
        enum.reservedNames.validateAll { validate(it) }

    fun validateReservationsRespected() = enum.values.validateAll { value ->
        validate(!enum.reservedNames.contains(value.name)) { ValidationError.ReservedEnumValueName(this, value.name) } +
        validate(!enum.reservedNumbers.any { it.contains(value.number) }) {
            ValidationError.ReservedEnumValueNumber(this, value.number)
        }
    }

    return validateValuesPresent() +
        validateValues() +
        validateDistinctNames() +
        validateDefaultValue() +
        validateDistinctNumbers() +
        validateReservedNames() +
        validateReservationsRespected()
}

fun ValidationScope.validate(enumValue: EnumValue) = validate(enumValue.name)

fun ValidationScope.validate(fieldNumber: FieldNumber) = validate(
    fieldNumber.value in FieldNumber.MIN until FieldNumber.RESERVED_RANGE_START ||
    fieldNumber.value in FieldNumber.RESERVED_RANGE_END..FieldNumber.MAX
) { ValidationError.InvalidFieldNumber(this, fieldNumber) }

fun ValidationScope.validate(identifier: Identifier) = validate(
    IDENTIFIER_REGEX.matches(identifier.value)
) { ValidationError.InvalidIdentifier(this, identifier) }
