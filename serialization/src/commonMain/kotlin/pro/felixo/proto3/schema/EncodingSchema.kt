package pro.felixo.proto3.schema

import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pro.felixo.proto3.EnumValue
import pro.felixo.proto3.FieldEncoding
import pro.felixo.proto3.FieldNumber
import pro.felixo.proto3.FieldRule
import pro.felixo.proto3.Identifier
import pro.felixo.proto3.encoding.HybridDecoder
import pro.felixo.proto3.encoding.HybridEncoder
import pro.felixo.proto3.wire.WireBuffer
import pro.felixo.proto3.wire.WireValue

data class EncodingSchema(
    val types: List<Type> = emptyList()
)

sealed class Type {
    abstract val name: Identifier
}

data class Message(
    override val name: Identifier,
    val members: List<Member> = emptyList(),
    val nestedTypes: List<Type> = emptyList(),
    val encoder: (output: WireBuffer, isStandalone: Boolean) -> HybridEncoder,
    val decoder: (value: List<WireValue>) -> HybridDecoder
) : Type() {
    val fields: List<Field> =
        members.filterIsInstance<Field>() + members.filterIsInstance<OneOf>().flatMap { it.fields }
}

sealed interface Member {
    val name: Identifier
}

data class Field(
    override val name: Identifier,
    val type: FieldEncoding,
    val number: FieldNumber,
    val rule: FieldRule = FieldRule.Singular,
    val encoder: ((WireBuffer) -> Encoder),
    val decoder: ((List<WireValue>) -> Decoder)
) : Member

data class OneOf(
    override val name: Identifier,
    val fields: List<Field>
) : Member

data class Enumeration(
    override val name: Identifier,
    val values: List<EnumValue>
) : Type() {
    private val numberByElementIndex: List<Int> by lazy { values.map { it.number } }
    private val elementIndexByNumber = numberByElementIndex.withIndex().associate { it.value to it.index }
    private val defaultElementIndex: Int = numberByElementIndex.indexOf(0)

    fun decode(number: Int): Int = elementIndexByNumber[number] ?: defaultElementIndex
    fun encode(elementIndex: Int): Int = numberByElementIndex[elementIndex]
}
