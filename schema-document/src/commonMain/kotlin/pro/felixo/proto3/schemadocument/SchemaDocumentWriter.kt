package pro.felixo.proto3.schemadocument

class SchemaDocumentWriter(
    private val out: StringBuilder,
    private val indentSpaces: Int = 2
) {
    private var currentIndent = 0

    fun write(schema: SchemaDocument) {
        line("""syntax = "proto3";""")
        line()
        schema.types.sortedBy { it.name }.forEach {
            write(it)
            line()
        }
    }

    fun write(type: Type) {
        when (type) {
            is Message -> writeMessage(type)
            is Enumeration -> writeEnum(type)
        }
    }

    private fun writeMessage(message: Message) {
        line("message ${message.name} {")
        indent {
            message.nestedTypes.sortedBy { it.name }.forEach { write(it) }
            writeMessageReservedNumbers(message.reservedNumbers)
            writeReservedNames(message.reservedNames)
            message.members.sortedBy { member ->
                when (member) {
                    is Field -> member.number
                    is OneOf -> member.fields.minOf { it.number }
                }
            }.forEach { writeMember(it) }
        }
        line("}")
    }

    private fun writeMessageReservedNumbers(numbers: List<IntRange>) {
        if (numbers.isNotEmpty())
            line("reserved ${numbers.joinToString(", ", transform = ::fieldRangeToString)};")
    }

    private fun writeMember(member: Member) {
        when (member) {
            is Field -> writeField(member)
            is OneOf -> writeOneOf(member)
        }
    }

    private fun writeOneOf(oneOf: OneOf) {
        line("oneof ${oneOf.name} {")
        indent {
            oneOf.fields.sortedBy { it.number }.forEach { writeField(it) }
        }
        line("}")
    }

    private fun writeField(field: Field) {
        line("${rulePrefix(field.rule)}${field.type} ${field.name} = ${field.number};")
    }

    private fun writeEnum(enum: Enumeration) {
        line("enum ${enum.name} {")
        indent {
            if (enum.allowAlias)
                line("option allow_alias = true;")
            writeEnumReservedNumbers(enum.reservedNumbers)
            writeReservedNames(enum.reservedNames)
            enum.values.forEach { writeEnumValue(it) }
        }
        line("}")
    }

    private fun writeEnumReservedNumbers(numbers: List<IntRange>) {
        if (numbers.isNotEmpty())
            line("reserved ${numbers.joinToString(", ", transform = ::enumRangeToString)};")
    }

    private fun writeReservedNames(names: List<Identifier>) {
        if (names.isNotEmpty())
            line("reserved ${names.joinToString(", ", transform = { "\"$it\"" })};")
    }

    private fun writeEnumValue(value: EnumValue) {
        line("${value.name} = ${value.number};")
    }

    private fun enumRangeToString(intRange: IntRange): String =
        if (intRange.first == intRange.last)
            "${intRange.first}"
        else
            "${intRange.first} to ${if (intRange.last == Int.MAX_VALUE) "max" else intRange.last}"

    private fun fieldRangeToString(intRange: IntRange): String =
        if (intRange.first == intRange.last)
            "${intRange.first}"
        else
            "${intRange.first} to ${intRange.last}"

    private fun rulePrefix(rule: FieldRule): String = when(rule) {
        FieldRule.Singular -> ""
        FieldRule.Optional -> "optional "
        FieldRule.Repeated -> "repeated "
    }

    private fun line(s: String = "") {
        if (s.isEmpty())
            out.appendLine()
        else
            out.appendLine(" ".repeat(currentIndent * indentSpaces) + s)
    }

    private fun indent(block: () -> Unit) {
        currentIndent++
        try {
            block()
        } finally {
            currentIndent--
        }
    }
}
