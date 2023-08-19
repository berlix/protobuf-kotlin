package pro.felixo.protobuf.schemadocument.validation

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.kotest.core.spec.style.StringSpec

class ValidationUtilTest : StringSpec({
    "validate(true) returns OK result" {
        assertThat(testScope.validate(true) { testError() })
            .isEqualTo(ValidationResult.OK)
    }

    "validate(false) returns error result" {
        assertThat(testScope.validate(false) { testError() })
            .isEqualTo(resultOf(testErrorInTestScope()))
    }

    "validateAll returns OK for empty receiver" {
        assertThat(emptyList<Any>().validateAll { resultOf(testErrorInTestScope()) })
            .isEqualTo(ValidationResult.OK)
    }

    "validateAll returns concatenated errors" {
        assertThat(
            listOf(
                arrayOf(
                    testErrorInTestScope(1),
                    testErrorInTestScope(2)
                ),
                emptyArray(),
                arrayOf(
                    testErrorInTestScope(3)
                )
            ).validateAll { resultOf(*it) }
        ).isEqualTo(
            resultOf(
                testErrorInTestScope(1),
                testErrorInTestScope(2),
                testErrorInTestScope(3)
            )
        )
    }

    "validateNoDuplicates returns OK for empty argument" {
        assertThat(
            testScope.validateNoDuplicates(
                emptyList<Int>(),
                { it },
                { testError(it) }
            )
        ).isEqualTo(ValidationResult.OK)
    }

    "validateNoDuplicates returns OK for argument without duplicates" {
        assertThat(
            testScope.validateNoDuplicates(
                listOf(0, 1, 2, 3, 4, 5),
                { it },
                { testError(it) }
            )
        ).isEqualTo(ValidationResult.OK)
    }

    "validateNoDuplicates returns errors for argument with duplicates" {
        assertThat(
            testScope.validateNoDuplicates(
                listOf(0, 1, 2, 2, 4, 0, 0),
                { it },
                { testError(it) }
            )
        ).isEqualTo(
            resultOf(
                testErrorInTestScope(2),
                testErrorInTestScope(0),
                testErrorInTestScope(0)
            )
        )
    }
})
