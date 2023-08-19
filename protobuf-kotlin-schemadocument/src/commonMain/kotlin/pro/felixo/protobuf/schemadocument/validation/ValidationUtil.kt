package pro.felixo.protobuf.schemadocument.validation

fun <T> Iterable<T>.validateAll(validate: (T) -> ValidationResult): ValidationResult =
    ValidationResult(map(validate).flatMap { it.errors })

fun <T, U> ValidationScope.validateNoDuplicates(
    items: Iterable<T>,
    transform: (T) -> U,
    error: ValidationScope.(duplicate: T) -> ValidationError
): ValidationResult {
    val seen = mutableSetOf<U>()
    return ValidationResult(
        items.mapNotNull {
            if (!seen.add(transform(it)))
                error(it)
            else
                null
        }
    )
}

fun ValidationScope.validate(condition: Boolean, error: ValidationScope.() -> ValidationError): ValidationResult =
    if (condition)
        ValidationResult.OK
    else
        ValidationResult(listOf(error()))
