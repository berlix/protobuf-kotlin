package pro.felixo.protobuf.wire

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSameAs
import io.kotest.core.spec.style.StringSpec

class WireTypeTest : StringSpec({
    "creates valid wire types" {
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

    "throws on invalid value" {
        assertFailure { WireType.of(-1) }
        assertFailure { WireType.of(6) }
        assertFailure { WireType.of(7) }
        assertFailure { WireType.of(8) }
    }
})
