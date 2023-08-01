package pro.felixo.proto3.schemadocument

import pro.felixo.proto3.FieldNumber
import pro.felixo.proto3.Identifier

data class ValidationResult(
    val errors: List<ValidationError>
) {
    val isValid: Boolean get() = errors.isEmpty()

    operator fun plus(other: ValidationResult) = ValidationResult(errors + other.errors)
    operator fun plus(other: List<ValidationResult>) = ValidationResult(errors + other.flatMap { it.errors })

    companion object {
        val OK = ValidationResult(emptyList())
    }
}

sealed class ValidationError {
    abstract val message: String

    data class InvalidIdentifier(val identifier: Identifier) : ValidationError() {
        override val message: String get() = "Invalid identifier: $identifier"
    }

    data class DuplicateTypeName(val name: Identifier) : ValidationError() {
        override val message: String get() = "Duplicate type name: $name"
    }

    data class InvalidFieldNumber(val number: FieldNumber) : ValidationError() {
        override val message: String get() = "Invalid field number: $number"
    }

    data class DuplicateFieldNumber(val number: FieldNumber) : ValidationError() {
        override val message: String get() = "Duplicate field number: $number"
    }

    data class DuplicateFieldName(val name: Identifier) : ValidationError() {
        override val message: String get() = "Duplicate field name: $name"
    }

    data class ReservedFieldNumber(val number: FieldNumber) : ValidationError() {
        override val message: String get() = "Reserved field number: $number"
    }

    data class ReservedFieldName(val name: Identifier) : ValidationError() {
        override val message: String get() = "Reserved field name: $name"
    }

    data object OneOfWithoutFields : ValidationError() {
        override val message: String get() = "oneOf contains no fields"
    }

    data class RepeatedFieldInOneOf(val fieldName: Identifier) : ValidationError() {
        override val message: String get() = "Repeated field in oneOf: $fieldName"
    }

    data object EmptyReference : ValidationError() {
        override val message: String get() = "Empty reference"
    }

    data object EnumContainsNoValues : ValidationError() {
        override val message: String get() = "Enum contains no values"
    }

    data object DefaultEnumValueNotFirstValue : ValidationError() {
        override val message: String get() = "Fist enum value does not have number 0"
    }

    data class DuplicateEnumValueName(val name: Identifier) : ValidationError() {
        override val message: String get() = "Duplicate enum value name: $name"
    }

    data class DuplicateEnumValueNumber(val number: Int) : ValidationError() {
        override val message: String get() = "Duplicate enum value name: $number"
    }

    data class ReservedEnumValueNumber(val number: Int) : ValidationError() {
        override val message: String get() = "Reserved enum value number: $number"
    }

    data class ReservedEnumValueName(val name: Identifier) : ValidationError() {
        override val message: String get() = "Reserved enum value name: $name"
    }
}
