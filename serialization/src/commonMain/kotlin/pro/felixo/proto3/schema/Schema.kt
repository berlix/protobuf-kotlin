package pro.felixo.proto3.schema

import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pro.felixo.proto3.FieldType
import pro.felixo.proto3.internal.FIELD_NUMBER_RESERVED_RANGE_END
import pro.felixo.proto3.internal.FIELD_NUMBER_RESERVED_RANGE_START
import pro.felixo.proto3.internal.MAX_FIELD_NUMBER
import pro.felixo.proto3.internal.MIN_FIELD_NUMBER
import pro.felixo.proto3.internal.requireNoDuplicates
import pro.felixo.proto3.wire.WireOutput
import pro.felixo.proto3.wire.WireValue

data class Schema(
    val types: Set<Type> = emptySet()
) {
    init {
        requireDistinctTypeNames()
    }

    private fun requireDistinctTypeNames() {
        types.requireNoDuplicates({ it.name }) { "Duplicate type name: $it" }
    }

    override fun toString(): String {
        val out = StringBuilder()
        SchemaWriter(out).write(this)
        return out.toString()
    }
}

sealed class Type {
    abstract val name: Identifier

    override fun toString(): String {
        val out = StringBuilder()
        SchemaWriter(out).write(this)
        return out.toString()
    }
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

    init {
        requireDistinctFieldNumbers()
        requireDistinctTypeNames()
        requireReservationsRespected()
    }

    private fun requireDistinctFieldNumbers() {
        fields.requireNoDuplicates({ it.number }) { "Duplicate field number in message $name: $it" }
    }

    private fun requireDistinctTypeNames() {
        nestedTypes.requireNoDuplicates({ it.name }) { "Duplicate nested type name in message $name: $it" }
    }

    private fun requireReservationsRespected() {
        fields.forEach { field ->
            require(!reservedNames.contains(field.name)) { "Field name ${field.name} is reserved in message $name" }
            require(!reservedNumbers.any { it.contains(field.number.value) }) {
                "Field number ${field.number.value} is reserved in message $name"
            }
        }
    }
}

sealed interface Member {
    val name: Identifier
}

class Field(
    override val name: Identifier,
    val type: FieldType,
    val number: FieldNumber,
    val rule: FieldRule = FieldRule.Singular,
    val encoder: ((WireOutput) -> Encoder),
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
) : Member {
    init {
        require(fields.isNotEmpty()) { "OneOf must have at least one field" }
        require(fields.all { it.rule != FieldRule.Repeated }) { "OneOf fields may not be repeated" }
    }
}


@JvmInline
value class FieldNumber(val value: Int) : Comparable<FieldNumber> {
    init {
        require(
            value in MIN_FIELD_NUMBER until FIELD_NUMBER_RESERVED_RANGE_START ||
                value in FIELD_NUMBER_RESERVED_RANGE_END..MAX_FIELD_NUMBER
        ) { "Invalid field number $value" }
    }

    override fun compareTo(other: FieldNumber): Int = value.compareTo(other.value)

    override fun toString(): String = value.toString()
}

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
) : Type() {
    init {
        requireValues()
        requireDistinctNames()
        requireDefaultValue()
        if (!allowAlias)
            requireDistinctNumbers()
        requireReservationsRespected()
    }

    private fun requireValues() {
        require(values.isNotEmpty()) { "Enum $name must have at least one value" }
    }

    private fun requireDefaultValue() {
        require(values.any { it.number == 0 }) { "Enum $name must have a value with number 0" }
    }

    private fun requireDistinctNames() {
        values.requireNoDuplicates({ it.name }) { "Duplicate value name in enum $name: $it" }
    }

    private fun requireDistinctNumbers() {
        values.requireNoDuplicates({ it.number }) { "Duplicate value number in enum $name: $it" }
    }

    private fun requireReservationsRespected() {
        values.forEach { value ->
            require(!reservedNames.contains(value.name)) { "Field name ${value.name} is reserved in message $name" }
            require(!reservedNumbers.any { it.contains(value.number) }) {
                "Field number ${value.number} is reserved in message $name"
            }
        }
    }}

data class EnumValue(
    val name: Identifier,
    val number: Int
) : Comparable<EnumValue> {
    override fun compareTo(other: EnumValue): Int = number.compareTo(other.number)
}

@JvmInline
value class Identifier(val value: String) : Comparable<Identifier> {
    init {
        require(value.isNotEmpty()) { "Identifier must not be empty" }
        require(value.first().let { it.isLetter() || it == '_' }) {
            "Invalid identifier: $value"
        }
        require(value.all { it.isLetter() || it.isDigit() || it == '_' }) { "Invalid identifier: $value" }
    }

    override fun compareTo(other: Identifier): Int = value.compareTo(other.value)

    override fun toString() = value
}
