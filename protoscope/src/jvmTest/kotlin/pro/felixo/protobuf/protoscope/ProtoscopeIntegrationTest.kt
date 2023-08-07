package pro.felixo.protobuf.protoscope

import assertk.assertThat
import assertk.assertions.isEqualTo
import pro.felixo.protobuf.wire.WireBuffer
import kotlin.test.Test

/**
 * A test that tests [ProtoscopeTokenizer] and [ProtoscopeConverter] (as well as [WireBuffer]) in integration.
 */
class ProtoscopeIntegrationTest {
    private val converter = ProtoscopeConverter(ProtoscopeTokenizer())

    @Test
    fun `converts empty document to empty bytes`() = verify("", "")

    @Test
    fun `converts empty bytes literal to empty bytes`() = verify("``", "")

    @Test
    fun `converts bytes literal`() = verify("`0001FEFF`", "00 01 FE FF")

    @Test
    fun `ignores comments`() = verify(
        """
        # first comment
        `00`
        # second comment
        `01` # third comment `03`
        # fourth comment `02`
        """.trimIndent(),
        "00 01"
    )

    @Test
    fun `converts official protoscope specification document exactly as the original tool does`() = verify(
        PROTOSCOPE_SPECIFICATION,
        PROTOSCOPE_SPECIFICATION_HEX
    )

    private fun verify(protoscope: String, expected: String) =
            assertThat(converter.convert(protoscope)).isEqualTo(expected.bytes())

    private fun String.bytes() = replace(Regex("\\s"), "").chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
