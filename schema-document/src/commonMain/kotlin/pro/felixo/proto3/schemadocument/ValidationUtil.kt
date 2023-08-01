package pro.felixo.proto3.schemadocument

import pro.felixo.proto3.EnumValue
import pro.felixo.proto3.FieldNumber
import pro.felixo.proto3.Identifier

private val IDENTIFIER_REGEX = Regex("""[a-zA-Z_][a-zA-Z\d_]*""")

fun merge(results: Iterable<ValidationResult>): ValidationResult =
    ValidationResult(results.flatMap { it.errors })

fun <T> Iterable<T>.merge(validate: (T) -> ValidationResult): ValidationResult = merge(map(validate))

fun <T, U> Iterable<T>.validateNoDuplicates(
    transform: (T) -> U,
    error: (duplicate: T) -> ValidationError
): ValidationResult {
    val seen = mutableSetOf<U>()
    return ValidationResult(
        mapNotNull {
            if (!seen.add(transform(it)))
                error(it)
            else
                null
        }
    )
}

fun validate(condition: Boolean, error: () -> ValidationError): ValidationResult =
    if (condition)
        ValidationResult.OK
    else
        ValidationResult(listOf(error()))

fun EnumValue.validate() = name.validate()

fun FieldNumber.validate() = validate(
    value !in FieldNumber.MIN until FieldNumber.RESERVED_RANGE_START &&
    value !in FieldNumber.RESERVED_RANGE_END..FieldNumber.MAX
) { ValidationError.InvalidFieldNumber(this) }

fun Identifier.validate() = validate(
    IDENTIFIER_REGEX.matches(value)
) { ValidationError.InvalidIdentifier(this) }
