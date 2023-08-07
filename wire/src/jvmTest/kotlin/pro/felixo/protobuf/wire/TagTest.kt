package pro.felixo.protobuf.wire

import assertk.assertThat
import assertk.assertions.isEqualTo
import pro.felixo.protobuf.FieldNumber
import kotlin.test.Test

class TagTest {
    @Test
    fun `creates valid tags`() {
        listOf(
            Triple(FieldNumber(1), WireType.VarInt, 0b00001000),
            Triple(FieldNumber(2), WireType.Fixed64, 0b00010001),
            Triple(FieldNumber(3), WireType.Len, 0b00011010),
            Triple(FieldNumber(4), WireType.SGroup, 0b00100011),
            Triple(FieldNumber(5), WireType.EGroup, 0b00101100),
            Triple(FieldNumber(6), WireType.Fixed32, 0b00110101),
        ).forEach { (testFieldNumber, testWireType, testValue) ->
            listOf(
                Tag(testValue),
                Tag.of(testFieldNumber, testWireType),
                Tag.of(testFieldNumber.value, testWireType.value),
            ).forEach {
                assertThat(it.fieldNumber).isEqualTo(testFieldNumber)
                assertThat(it.wireType).isEqualTo(testWireType)
                assertThat(it.value).isEqualTo(testValue)
            }
        }
    }
}
