package pro.felixo.protobuf.wire

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.kotest.core.spec.style.StringSpec

class WireBufferTest : StringSpec({
    "creates zero-length buffer by default" {
        val buffer = WireBuffer()
        assertThat(buffer.length).isEqualTo(0)
        assertThat(buffer.remaining).isEqualTo(0)
        assertThat(buffer.getBytes().size).isEqualTo(0)
    }
})
