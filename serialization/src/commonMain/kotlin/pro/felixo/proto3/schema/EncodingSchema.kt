package pro.felixo.proto3.schema

import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pro.felixo.proto3.FieldNumber
import pro.felixo.proto3.FieldEncoding
import pro.felixo.proto3.wire.WireBuffer
import pro.felixo.proto3.wire.WireValue

data class EncodingSchema(
    val types: Set<Type> = emptySet()
)

sealed class Type {
    abstract val name: Identifier
}

data class Message(
    override val name: Identifier,
    val members: Set<Member>,
    val nestedTypes: Set<Type> = emptySet(),
    val reservedNames: Set<Identifier> = emptySet(),
    val reservedNumbers: Set<IntRange> = emptySet()
) : Type() {
    val fields: Set<Field> =
        (members.filterIsInstance<Field>() + members.filterIsInstance<OneOf>().flatMap { it.fields }).toSet()
}

sealed interface Member {
    val name: Identifier
}

class Field(
    override val name: Identifier,
    val type: FieldEncoding,
    val number: FieldNumber,
    val rule: FieldRule = FieldRule.Singular,
    val encoder: ((WireBuffer) -> Encoder),
    val decoder: ((List<WireValue>) -> Decoder)
) : Member {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Field

        if (name != other.name) return false
        if (type != other.type) return false
        if (number != other.number) return false
        return rule == other.rule
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + number.hashCode()
        result = 31 * result + rule.hashCode()
        return result
    }

    override fun toString(): String {
        return "Field(name=$name, type=$type, number=$number, rule=$rule)"
    }
}

data class OneOf(
    override val name: Identifier,
    val fields: Set<Field>
) : Member

enum class FieldRule {
    Singular,
    Optional,
    Repeated
}

data class Enumeration(
    override val name: Identifier,
    val values: Set<EnumValue>,
    val allowAlias: Boolean = false,
    val reservedNames: Set<Identifier> = emptySet(),
    val reservedNumbers: Set<IntRange> = emptySet()
) : Type()

data class EnumValue(
    val name: Identifier,
    val number: Int
) : Comparable<EnumValue> {
    override fun compareTo(other: EnumValue): Int = number.compareTo(other.number)
}

@JvmInline
value class Identifier(val value: String) : Comparable<Identifier> {
    override fun compareTo(other: Identifier): Int = value.compareTo(other.value)

    override fun toString() = value
}
