package pro.felixo.protobuf.schemadocument

fun areEquivalent(schema1: SchemaDocument, schema2: SchemaDocument): Boolean =
    compareUnordered(schema1.types, schema2.types) { type1, type2 -> areEquivalent(type1, type2) }

fun areEquivalent(type1: Type, type2: Type): Boolean =
    type1.name == type2.name && when {
        type1 is Message && type2 is Message -> areEquivalent(type1, type2)
        type1 is Enum && type2 is Enum -> areEquivalent(type1, type2)
        else -> false
    }

private fun areEquivalent(type1: Enum, type2: Enum): Boolean =
    compareUnordered(type1.values, type2.values) && type1.allowAlias == type2.allowAlias &&
    compareUnordered(type1.reservedNames, type2.reservedNames) &&
    normalize(type1.reservedNumbers) == normalize(type2.reservedNumbers)

private fun areEquivalent(type1: Message, type2: Message): Boolean =
    compareUnordered(type1.nestedTypes, type2.nestedTypes) {
        nestedType1, nestedType2 -> areEquivalent(nestedType1, nestedType2)
    } && compareUnordered(type1.members, type2.members) { member1, member2 -> areEquivalent(member1, member2) } &&
    compareUnordered(type1.reservedNames, type2.reservedNames) &&
    normalize(type1.reservedNumbers) == normalize(type2.reservedNumbers)

fun areEquivalent(member1: Member, member2: Member): Boolean =
    member1.name == member2.name && when {
        member1 is Field && member2 is Field -> areEquivalent(member1, member2)
        member1 is OneOf && member2 is OneOf -> areEquivalent(member1, member2)
        else -> false
    }

private fun areEquivalent(member1: OneOf, member2: OneOf): Boolean =
    compareUnordered(member1.fields, member2.fields) { field1, field2 -> areEquivalent(field1, field2) }

private fun areEquivalent(field1: Field, field2: Field): Boolean =
    field1.type == field2.type && field1.number == field2.number && field1.rule == field2.rule

internal fun <T> compareUnordered(
    list1: List<T>,
    list2: List<T>,
    areEqual: (T, T) -> Boolean = { item1, item2 -> item1 == item2 }
): Boolean =
    list1.size == list2.size && run {
        val visited = BooleanArray(list1.size) { false }

        list1.all { item1 ->
            var matchFound = false

            for (index in list2.indices)
                if (!visited[index] && areEqual(item1, list2[index])) {
                    visited[index] = true
                    matchFound = true
                    break
                }

            matchFound
        }
    }

internal fun normalize(ranges: List<IntRange>): List<IntRange> {
    val sortedRanges = ranges.filter { !it.isEmpty() }.sortedBy { it.first }
    if (sortedRanges.isEmpty()) return emptyList()

    val result = mutableListOf(sortedRanges.first())

    for (range in sortedRanges.drop(1)) {
        val lastRange = result.last()

        if (range.first <= lastRange.last + 1)
            result[result.size - 1] = lastRange.first..kotlin.math.max(lastRange.last, range.last)
        else
            result.add(range)
    }

    return result
}
