package pro.felixo.proto3.schemadocument.validation

import pro.felixo.proto3.FieldNumber
import pro.felixo.proto3.Identifier

sealed class ValidationError {
    abstract val message: String
    abstract val scope: ValidationScope

    override fun toString(): String = "$scope: $message"

    data class InvalidIdentifier(
        override val scope: ValidationScope,
        val identifier: Identifier
    ) : ValidationError() {
        override val message: String get() = "Invalid identifier: $identifier"
    }

    data class DuplicateTypeName(
        override val scope: ValidationScope,
        val name: Identifier
    ) : ValidationError() {
        override val message: String get() = "Duplicate type name: $name"
    }

    data class InvalidFieldNumber(
        override val scope: ValidationScope,
        val number: FieldNumber
    ) : ValidationError() {
        override val message: String get() = "Invalid field number: $number"
    }

    data class DuplicateFieldNumber(
        override val scope: ValidationScope,
        val number: FieldNumber
    ) : ValidationError() {
        override val message: String get() = "Duplicate field number: $number"
    }

    data class DuplicateFieldName(
        override val scope: ValidationScope,
        val name: Identifier
    ) : ValidationError() {
        override val message: String get() = "Duplicate field name: $name"
    }

    data class ReservedFieldNumber(
        override val scope: ValidationScope,
        val number: FieldNumber
    ) : ValidationError() {
        override val message: String get() = "Reserved field number: $number"
    }

    data class ReservedFieldName(
        override val scope: ValidationScope,
        val name: Identifier
    ) : ValidationError() {
        override val message: String get() = "Reserved field name: $name"
    }

    data class OneOfWithoutFields(
        override val scope: ValidationScope
    ) : ValidationError() {
        override val message: String get() = "oneof contains no fields"
    }

    data class RepeatedFieldInOneOf(
        override val scope: ValidationScope,
        val fieldName: Identifier
    ) : ValidationError() {
        override val message: String get() = "Repeated field in oneof: $fieldName"
    }

    data class EmptyReference(
        override val scope: ValidationScope
    ) : ValidationError() {
        override val message: String get() = "Empty reference"
    }

    data class EnumContainsNoValues(
        override val scope: ValidationScope
    ) : ValidationError() {
        override val message: String get() = "Enum contains no values"
    }

    data class FirstEnumIsNotDefaultValue(
        override val scope: ValidationScope
    ) : ValidationError() {
        override val message: String get() = "Fist enum value does not have number 0"
    }

    data class DuplicateEnumValueName(
        override val scope: ValidationScope,
        val name: Identifier
    ) : ValidationError() {
        override val message: String get() = "Duplicate enum value name: $name"
    }

    data class DuplicateEnumValueNumber(
        override val scope: ValidationScope,
        val number: Int
    ) : ValidationError() {
        override val message: String get() = "Duplicate enum value name: $number"
    }

    data class ReservedEnumValueNumber(
        override val scope: ValidationScope,
        val number: Int
    ) : ValidationError() {
        override val message: String get() = "Reserved enum value number: $number"
    }

    data class ReservedEnumValueName(
        override val scope: ValidationScope,
        val name: Identifier
    ) : ValidationError() {
        override val message: String get() = "Reserved enum value name: $name"
    }
}
