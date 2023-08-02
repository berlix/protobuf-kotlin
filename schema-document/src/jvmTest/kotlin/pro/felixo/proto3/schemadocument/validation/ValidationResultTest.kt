package pro.felixo.proto3.schemadocument.validation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test

class ValidationResultTest {
    @Test
    fun `OK is empty`() = assertThat(ValidationResult.OK).isEqualTo(resultOf())

    @Test
    fun `empty ValidationResult is valid`() = assertThat(ValidationResult(emptyList()).isValid).isTrue()

    @Test
    fun `non-empty ValidationResult is invalid`() =
        assertThat(resultOf(testErrorInTestScope()).isValid).isFalse()

    @Test
    fun `toString of empty ValidationResult says it is valid`() =
        assertThat(resultOf().toString()).isEqualTo("Schema is valid.")

    @Test
    fun `toString of non-empty ValidationResult lists errors`() =
        assertThat(resultOf(testErrorInTestScope(1), testErrorInTestScope(2)).toString())
            .isEqualTo("${testErrorInTestScope(1)}\n${testErrorInTestScope(2)}")

    @Test
    fun `plus concatenates with single other ValidationResult`() =
        assertThat(
            resultOf(testErrorInTestScope(1), testErrorInTestScope(2)) + resultOf(testErrorInTestScope(3))
        ).isEqualTo(
            resultOf(testErrorInTestScope(1), testErrorInTestScope(2), testErrorInTestScope(3))
        )

    @Test
    fun `plus concatenates with list of other ValidationResults`() =
        assertThat(
            resultOf(testErrorInTestScope(1), testErrorInTestScope(2)) + listOf(
                resultOf(testErrorInTestScope(3)),
                resultOf(testErrorInTestScope(4), testErrorInTestScope(5))
            )
        ).isEqualTo(
            resultOf(
                testErrorInTestScope(1),
                testErrorInTestScope(2),
                testErrorInTestScope(3),
                testErrorInTestScope(4),
                testErrorInTestScope(5)
            )
        )
}
