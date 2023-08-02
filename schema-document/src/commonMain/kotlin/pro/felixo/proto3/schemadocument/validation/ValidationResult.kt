package pro.felixo.proto3.schemadocument.validation

data class ValidationResult(
    val errors: List<ValidationError>
) {
    val isValid: Boolean get() = errors.isEmpty()

    override fun toString(): String = if (isValid)
        "Schema is valid."
    else
        errors.joinToString("\n")

    operator fun plus(other: ValidationResult) = ValidationResult(errors + other.errors)
    operator fun plus(other: List<ValidationResult>) = ValidationResult(errors + other.flatMap { it.errors })

    companion object {
        val OK = ValidationResult(emptyList())
    }
}
