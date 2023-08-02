package pro.felixo.proto3.schemadocument.validation

import assertk.Assert
import assertk.assertions.containsExactlyInAnyOrder
import pro.felixo.proto3.Identifier
import pro.felixo.proto3.schemadocument.FieldType
import pro.felixo.proto3.schemadocument.SchemaElement

val testScope = ValidationScope(
    object : SchemaElement {
        override val elementType: String = "root"
        override val elementName: String = "root"
    },
    null
)

fun resultOf(vararg errors: ValidationError) = ValidationResult(errors.toList())
fun resultOf(error: ValidationError?) = ValidationResult(listOfNotNull(error))

fun ValidationScope.testError(n: Int = 1) = ValidationError.ReservedEnumValueNumber(this, n)
fun testErrorInTestScope(n: Int = 1) = ValidationError.ReservedEnumValueNumber(testScope, n)

val invalidIdentifier = Identifier("%invalid")
val invalidIdentifier2 = Identifier("%invalid2")
val invalidIdentifier3 = Identifier("%invalid3")
val invalidIdentifier4 = Identifier("%invalid4")
val invalidIdentifier5 = Identifier("%invalid5")
val validIdentifier = Identifier("valid")
val validIdentifier2 = Identifier("valid2")
val validIdentifier3 = Identifier("valid3")
val validIdentifier4 = Identifier("valid4")

fun Assert<ValidationResult>.isEquivalentTo(expected: ValidationResult) = given { actual ->
    assertThat(actual.errors).containsExactlyInAnyOrder(*expected.errors.toTypedArray())
}
