package pro.felixo.proto3.wire

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSameAs
import kotlin.test.Test

class WireTypeTest {
    @Test
    fun `creates valid wire types`() {
        listOf(
            WireType.VarInt to 0,
            WireType.Fixed64 to 1,
            WireType.Len to 2,
            WireType.SGroup to 3,
            WireType.EGroup to 4,
            WireType.Fixed32 to 5
        ).forEach { (type, value) ->
            assertThat(type.value).isEqualTo(value)
            assertThat(WireType.of(value)).isSameAs(type)
        }
    }

    @Test
    fun `throws on invalid value`() {
        assertFailure { WireType.of(-1) }
        assertFailure { WireType.of(6) }
        assertFailure { WireType.of(7) }
        assertFailure { WireType.of(8) }
    }
}
