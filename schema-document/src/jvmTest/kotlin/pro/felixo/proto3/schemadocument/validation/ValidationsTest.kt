package pro.felixo.proto3.schemadocument.validation

import assertk.assertThat
import pro.felixo.proto3.EnumValue
import pro.felixo.proto3.FieldNumber
import pro.felixo.proto3.FieldRule
import pro.felixo.proto3.Identifier
import pro.felixo.proto3.schemadocument.Enumeration
import pro.felixo.proto3.schemadocument.Field
import pro.felixo.proto3.schemadocument.FieldType
import pro.felixo.proto3.schemadocument.Message
import pro.felixo.proto3.schemadocument.OneOf
import pro.felixo.proto3.schemadocument.SchemaDocument
import kotlin.test.Test

class ValidationsTest {
    @Test
    fun `validates identifiers`() = listOf(
        "" to false,
        "." to false,
        "a.b" to false,
        "1" to false,
        "1a" to false,
        "1_" to false,
        "_" to true,
        "_a" to true,
        "_1" to true,
        "a" to true,
        "a1" to true,
        "a_" to true,
        "_1a2b3c_" to true,
    ).forEach { (name, valid) ->
        val identifier = Identifier(name)
        assertThat(testScope.validate(identifier)).isEquivalentTo(
            resultOf(
                ValidationError.InvalidIdentifier(testScope, identifier).takeIf { !valid }
            )
        )
    }

    @Test
    fun `validates field numbers`() = listOf(
        Int.MIN_VALUE to false,
        -1_000_000_000 to false,
        -100_000_000 to false,
        -10_000_000 to false,
        -1_000_000 to false,
        -100_000 to false,
        -10_000 to false,
        -1_000 to false,
        -100 to false,
        -10 to false,
        -1 to false,
        0 to false,
        1 to true,
        10 to true,
        100 to true,
        1_000 to true,
        10_000 to true,
        18_999 to true,
        19_000 to false,
        19_999 to false,
        20_000 to true,
        100_000 to true,
        1_000_000 to true,
        10_000_000 to true,
        100_000_000 to true,
        536_870_911 to true,
        536_870_912 to false,
        1_000_000_000 to false,
        Int.MAX_VALUE to false,
    ).forEach { (int, valid) ->
        val number = FieldNumber(int)
        assertThat(testScope.validate(number)).isEquivalentTo(
            resultOf(
                ValidationError.InvalidFieldNumber(testScope, number).takeIf { !valid }
            )
        )
    }

    @Test
    fun `validates enum values`() = listOf(
        EnumValue(invalidIdentifier, 1) to ValidationError.InvalidIdentifier(testScope, invalidIdentifier),
        EnumValue(validIdentifier, Int.MIN_VALUE) to null,
        EnumValue(validIdentifier, -1) to null,
        EnumValue(validIdentifier, 0) to null,
        EnumValue(validIdentifier, 1) to null,
        EnumValue(validIdentifier, 19_500) to null,
        EnumValue(validIdentifier, Int.MAX_VALUE) to null,
    ).forEach { (value, error) ->
        assertThat(testScope.validate(value)).isEquivalentTo(resultOf(error))
    }

    @Test
    fun `validates enums`() {
        assertValidation(
            Enumeration(
                invalidIdentifier,
                emptyList(),
                reservedNames = listOf(invalidIdentifier2)
            )
        ) { scope: ValidationScope ->
            arrayOf(
                ValidationError.InvalidIdentifier(scope, invalidIdentifier),
                ValidationError.EnumContainsNoValues(scope),
                ValidationError.InvalidIdentifier(scope, invalidIdentifier2),
            )
        }

        assertValidation(
            Enumeration(
                validIdentifier,
                listOf(
                    EnumValue(invalidIdentifier, 1),
                    EnumValue(validIdentifier, 1),
                    EnumValue(validIdentifier, 2),
                    EnumValue(validIdentifier2, 3),
                    EnumValue(validIdentifier3, 3),
                ),
                allowAlias = false,
                reservedNames = listOf(validIdentifier2, validIdentifier3),
                reservedNumbers = listOf(1..1, 2..3)
            )
        ) { scope: ValidationScope ->
            arrayOf(
                ValidationError.InvalidIdentifier(scope, invalidIdentifier),
                ValidationError.FirstEnumIsNotDefaultValue(scope),
                ValidationError.DuplicateEnumValueName(scope, validIdentifier),
                ValidationError.DuplicateEnumValueNumber(scope, 1),
                ValidationError.DuplicateEnumValueNumber(scope, 3),
                ValidationError.ReservedEnumValueName(scope, validIdentifier2),
                ValidationError.ReservedEnumValueName(scope, validIdentifier3),
                ValidationError.ReservedEnumValueNumber(scope, 1),
                ValidationError.ReservedEnumValueNumber(scope, 1),
                ValidationError.ReservedEnumValueNumber(scope, 2),
                ValidationError.ReservedEnumValueNumber(scope, 3),
                ValidationError.ReservedEnumValueNumber(scope, 3),
            )
        }

        assertValidation(
            Enumeration(
                validIdentifier,
                listOf(
                    EnumValue(validIdentifier, 0),
                    EnumValue(validIdentifier2, 1),
                    EnumValue(validIdentifier3, 1),
                ),
                allowAlias = true,
                reservedNames = listOf(validIdentifier4),
                reservedNumbers = listOf(2..3)
            )
        ) { emptyArray() }

        assertValidation(
            Enumeration(
                validIdentifier,
                listOf(EnumValue(validIdentifier, 0))
            )
        ) { emptyArray() }
    }

    private fun assertValidation(
        enum: Enumeration,
        errors: (ValidationScope) -> Array<ValidationError>
    ) = assertThat(testScope.validate(enum)).isEquivalentTo(resultOf(*errors(testScope.sub(enum))))

    @Test
    fun `validates fields`() {
        assertValidation(
            Field(
                validIdentifier,
                FieldType.Int32,
                FieldNumber(1),
                FieldRule.Singular
            )
        ) { emptyArray() }

        assertValidation(
            Field(
                invalidIdentifier,
                FieldType.Reference(listOf(invalidIdentifier2)),
                FieldNumber(0),
                FieldRule.Optional
            )
        ) { scope ->
            arrayOf(
                ValidationError.InvalidIdentifier(scope, invalidIdentifier),
                ValidationError.InvalidIdentifier(scope, invalidIdentifier2),
                ValidationError.InvalidFieldNumber(scope, FieldNumber(0)),
            )
        }

        assertValidation(
            Field(
                validIdentifier,
                FieldType.Reference(emptyList()),
                FieldNumber(1),
                FieldRule.Repeated
            )
        ) { scope ->
            arrayOf(
                ValidationError.EmptyReference(scope)
            )
        }
    }

    private fun assertValidation(
        field: Field,
        errors: (ValidationScope) -> Array<ValidationError>
    ) {
        val scope = testScope.sub(field)
        assertThat(testScope.validate(field)).isEquivalentTo(resultOf(*errors(scope)))
    }

    @Test
    fun `validates oneofs`() {
        assertValidation(
            OneOf(
                validIdentifier,
                listOf(
                    Field(
                        validIdentifier2,
                        FieldType.Int32,
                        FieldNumber(1),
                        FieldRule.Singular
                    ),
                    Field(
                        validIdentifier3,
                        FieldType.UInt64,
                        // Note it is not the responsibility of the OneOf validation to prevent duplicate field numbers,
                        // since this is covered by the validation of Message.
                        FieldNumber(1),
                        FieldRule.Singular
                    ),
                )
            )
        ) { emptyArray() }

        assertValidation(
            OneOf(validIdentifier, emptyList())
        ) { scope ->
            arrayOf(ValidationError.OneOfWithoutFields(scope))
        }

        val invalidField = Field(
            invalidIdentifier2,
            FieldType.Int32,
            FieldNumber(2),
            FieldRule.Singular
        )
        assertValidation(
            OneOf(
                invalidIdentifier,
                listOf(
                    Field(
                        validIdentifier,
                        FieldType.Int32,
                        FieldNumber(1),
                        FieldRule.Repeated
                    ),
                    invalidField
                )
            )
        ) { scope ->
            arrayOf(
                ValidationError.InvalidIdentifier(scope, invalidIdentifier),
                ValidationError.InvalidIdentifier(scope.sub(invalidField), invalidIdentifier2),
                ValidationError.RepeatedFieldInOneOf(scope, validIdentifier),
            )
        }
    }

    private fun assertValidation(
        oneOf: OneOf,
        errors: (ValidationScope) -> Array<ValidationError>
    ) = assertThat(testScope.validate(oneOf)).isEquivalentTo(resultOf(*errors(testScope.sub(oneOf))))

    @Test
    fun `validates messages`() {
        assertValidation(
            Message(validIdentifier)
        ) { emptyArray() }

        assertValidation(
            Message(
                validIdentifier,
                listOf(
                    Field(
                        validIdentifier2,
                        FieldType.Int32,
                        FieldNumber(1),
                        FieldRule.Singular
                    ),
                    OneOf(
                        validIdentifier3,
                        listOf(
                            Field(
                                validIdentifier3,
                                FieldType.Int32,
                                FieldNumber(2),
                                FieldRule.Singular
                            )
                        )
                    ),
                ),
                listOf(
                    Message(
                        validIdentifier4,
                        listOf(
                            Field(
                                validIdentifier4,
                                FieldType.Int32,
                                FieldNumber(1),
                                FieldRule.Singular
                            )
                        )
                    )
                ),
                listOf(validIdentifier4),
                listOf(
                    3..8,
                )
            )
        ) { emptyArray() }

        val invalidField1 = Field(
            invalidIdentifier2,
            FieldType.Int32,
            FieldNumber(2),
            FieldRule.Singular
        )
        val invalidField2 = Field(
            invalidIdentifier4,
            FieldType.Int32,
            FieldNumber(1),
            FieldRule.Singular
        )
        val invalidOneOf = OneOf(
            invalidIdentifier3,
            listOf(invalidField2)
        )
        val invalidSubType = Message(invalidIdentifier5, emptyList())

        assertValidation(
            Message(
                invalidIdentifier,
                listOf(
                    Field(
                        validIdentifier,
                        FieldType.Int32,
                        FieldNumber(1),
                        FieldRule.Singular
                    ),
                    invalidField1,
                    invalidOneOf,
                ),
                listOf(invalidSubType),
                listOf(validIdentifier, invalidIdentifier5),
                listOf(2..2)
            )
        ) { scope ->
            arrayOf(
                ValidationError.InvalidIdentifier(scope, invalidIdentifier),
                ValidationError.InvalidIdentifier(scope.sub(invalidField1), invalidIdentifier2),
                ValidationError.InvalidIdentifier(scope.sub(invalidOneOf).sub(invalidField2), invalidIdentifier4),
                ValidationError.InvalidIdentifier(scope.sub(invalidOneOf), invalidIdentifier3),
                ValidationError.InvalidIdentifier(scope.sub(invalidSubType), invalidIdentifier5),
                ValidationError.DuplicateFieldNumber(scope, FieldNumber(1)),
                ValidationError.InvalidIdentifier(scope, invalidIdentifier5),
                ValidationError.ReservedFieldName(scope, validIdentifier),
                ValidationError.ReservedFieldNumber(scope, FieldNumber(2)),
            )
        }
    }

    private fun assertValidation(
        message: Message,
        errors: (ValidationScope) -> Array<ValidationError>
    ) = assertThat(testScope.validate(message)).isEquivalentTo(resultOf(*errors(testScope.sub(message))))

    @Test
    fun `validates schemas`() {
        assertValidation(
            SchemaDocument()
        ) { emptyArray() }

        assertValidation(
            SchemaDocument(
                listOf(
                    Message(validIdentifier)
                )
            )
        ) { emptyArray() }

        val invalidMessage1 = Message(invalidIdentifier)
        val invalidMessage2 = Message(invalidIdentifier2)
        assertValidation(
            SchemaDocument(
                listOf(
                    invalidMessage1,
                    invalidMessage2,
                )
            )
        ) { scope ->
            arrayOf(
                ValidationError.InvalidIdentifier(scope.sub(invalidMessage1), invalidIdentifier),
                ValidationError.InvalidIdentifier(scope.sub(invalidMessage2), invalidIdentifier2)
            )
        }
    }

    private fun assertValidation(
        schema: SchemaDocument,
        errors: (ValidationScope) -> Array<ValidationError>
    ) = assertThat(validate(schema)).isEquivalentTo(resultOf(*errors(ValidationScope(schema, null))))
}
