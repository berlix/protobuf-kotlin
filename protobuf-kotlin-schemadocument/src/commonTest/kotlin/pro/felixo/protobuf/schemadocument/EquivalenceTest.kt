package pro.felixo.protobuf.schemadocument

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import io.kotest.core.spec.style.StringSpec
import pro.felixo.protobuf.EnumValue
import pro.felixo.protobuf.FieldNumber
import pro.felixo.protobuf.Identifier

class EquivalenceTest : StringSpec({
    fun normalize(vararg ranges: IntRange): List<IntRange> =
        normalize(ranges.toList())

    "normalize normalizes int ranges" {
        assertThat(normalize()).isEqualTo(emptyList())
        assertThat(normalize(IntRange.EMPTY)).isEqualTo(emptyList())
        assertThat(normalize(0..0)).isEqualTo(listOf(0..0))
        assertThat(normalize(0..1, 0..1)).isEqualTo(listOf(0..1))
        assertThat(normalize(5..10, 3..8, 8..9, 2..4, 0..0, 11..12, 14..16)).isEqualTo(
            listOf(0..0, 2..12, 14..16)
        )
    }

    "compareUnordered compares unordered" {
        assertThat(compareUnordered(emptyList<Int>(), emptyList())).isTrue()
        assertThat(compareUnordered(emptyList(), listOf(0))).isFalse()
        assertThat(compareUnordered(listOf(0), emptyList())).isFalse()
        assertThat(compareUnordered(listOf(0), listOf(0))).isTrue()
        assertThat(compareUnordered(listOf(0, 1, 2), listOf(0, 1, 2))).isTrue()
        assertThat(compareUnordered(listOf(0, 1, 2), listOf(2, 1, 0))).isTrue()
        assertThat(compareUnordered(listOf(0, 1, 2), listOf(0, 2, 1))).isTrue()
        assertThat(compareUnordered(listOf(0, 1, 2), listOf(0, 0, 1))).isFalse()
        assertThat(compareUnordered(listOf(0, 1, 2), listOf(1, 2, 2))).isFalse()
        assertThat(compareUnordered(listOf(0, 1, 2), listOf(1, 2))).isFalse()
        assertThat(compareUnordered(listOf(0, 1, 2), listOf(0))).isFalse()
        assertThat(compareUnordered(listOf(1, 1, 1), listOf(1, 1, 1))).isTrue()
        assertThat(compareUnordered(listOf(1, 1, 1), listOf(1, 1))).isFalse()
        assertThat(compareUnordered(listOf(1, 1), listOf(1, 1, 1))).isFalse()
    }

    "compares enums" {
        assertThat(
            areEquivalent(
                Enum(
                    Identifier("Enum"),
                    listOf(EnumValue(Identifier("A"), 1), EnumValue(Identifier("B"), 2)),
                    allowAlias = true,
                    reservedNames = listOf(Identifier("C"), Identifier("D")),
                    reservedNumbers = listOf(0..2, 3..5)
                ),
                Enum(
                    Identifier("Enum"),
                    listOf(EnumValue(Identifier("B"), 2), EnumValue(Identifier("A"), 1)),
                    allowAlias = true,
                    reservedNames = listOf(Identifier("D"), Identifier("C")),
                    reservedNumbers = listOf(0..5)
                )
            )
        ).isTrue()

        assertThat(
            areEquivalent(
                Enum(
                    Identifier("EnumA"),
                    emptyList()
                ),
                Enum(
                    Identifier("EnumB"),
                    emptyList()
                )
            )
        ).isFalse()

        assertThat(
            areEquivalent(
                Enum(
                    Identifier("Enum"),
                    listOf(EnumValue(Identifier("A"), 1))
                ),
                Enum(
                    Identifier("Enum"),
                    listOf(EnumValue(Identifier("A"), 2))
                )
            )
        ).isFalse()

        assertThat(
            areEquivalent(
                Enum(
                    Identifier("Enum"),
                    listOf(EnumValue(Identifier("A"), 1))
                ),
                Enum(
                    Identifier("Enum"),
                    listOf(EnumValue(Identifier("B"), 1))
                )
            )
        ).isFalse()

        assertThat(
            areEquivalent(
                Enum(
                    Identifier("Enum"),
                    emptyList(),
                    allowAlias = false
                ),
                Enum(
                    Identifier("Enum"),
                    emptyList(),
                    allowAlias = true
                )
            )
        ).isFalse()

        assertThat(
            areEquivalent(
                Enum(
                    Identifier("Enum"),
                    emptyList(),
                    reservedNames = listOf(Identifier("A"))
                ),
                Enum(
                    Identifier("Enum"),
                    emptyList(),
                    reservedNames = listOf(Identifier("B"))
                )
            )
        ).isFalse()

        assertThat(
            areEquivalent(
                Enum(
                    Identifier("Enum"),
                    emptyList(),
                    reservedNumbers = listOf(0..1)
                ),
                Enum(
                    Identifier("Enum"),
                    emptyList(),
                    reservedNumbers = listOf(0..2)
                )
            )
        ).isFalse()
    }

    "compares messages" {
        assertThat(
            areEquivalent(
                Message(
                    Identifier("Message"),
                    members = listOf(
                        Field(
                            Identifier("field1"),
                            FieldType.Int32,
                            FieldNumber(1)
                        ),
                        Field(
                            Identifier("field2"),
                            FieldType.Int64,
                            FieldNumber(2)
                        )
                    ),
                    nestedTypes = listOf(
                        Message(
                            Identifier("NestedMessage1"),
                            emptyList()
                        ),
                        Message(
                            Identifier("NestedMessage2"),
                            emptyList()
                        )

                    ),
                    reservedNames = listOf(Identifier("C"), Identifier("D")),
                    reservedNumbers = listOf(0..2, 3..5)
                ),
                Message(
                    Identifier("Message"),
                    members = listOf(
                        Field(
                            Identifier("field2"),
                            FieldType.Int64,
                            FieldNumber(2)
                        ),
                        Field(
                            Identifier("field1"),
                            FieldType.Int32,
                            FieldNumber(1)
                        )
                    ),
                    nestedTypes = listOf(
                        Message(
                            Identifier("NestedMessage2"),
                            emptyList()
                        ),
                        Message(
                            Identifier("NestedMessage1"),
                            emptyList()
                        )
                    ),
                    reservedNames = listOf(Identifier("D"), Identifier("C")),
                    reservedNumbers = listOf(0..5)
                )
            )
        ).isTrue()

        assertThat(
            areEquivalent(
                Message(
                    Identifier("MessageA"),
                    emptyList()
                ),
                Message(
                    Identifier("MessageB"),
                    emptyList()
                )
            )
        ).isFalse()

        assertThat(
            areEquivalent(
                Message(
                    Identifier("Message"),
                    members = listOf(
                        Field(
                            Identifier("field1"),
                            FieldType.Int32,
                            FieldNumber(1)
                        )
                    )
                ),
                Message(
                    Identifier("Message"),
                    members = listOf(
                        Field(
                            Identifier("field2"),
                            FieldType.Int64,
                            FieldNumber(2)
                        )
                    )
                )
            )
        ).isFalse()

        assertThat(
            areEquivalent(
                Message(
                    Identifier("Message"),
                    nestedTypes = listOf(
                        Message(
                            Identifier("NestedMessage1"),
                            emptyList()
                        )
                    )
                ),
                Message(
                    Identifier("Message"),
                    nestedTypes = listOf(
                        Message(
                            Identifier("NestedMessage2"),
                            emptyList()
                        )
                    )
                )
            )
        ).isFalse()

        assertThat(
            areEquivalent(
                Message(
                    Identifier("Message"),
                    reservedNames = listOf(Identifier("A"))
                ),
                Message(
                    Identifier("Message"),
                    reservedNames = listOf(Identifier("B"))
                )
            )
        ).isFalse()

        assertThat(
            areEquivalent(
                Message(
                    Identifier("Message"),
                    reservedNumbers = listOf(0..1)
                ),
                Message(
                    Identifier("Message"),
                    reservedNumbers = listOf(0..2)
                )
            )
        ).isFalse()
    }

    "compares schemas" {
        assertThat(
            areEquivalent(
                SchemaDocument(
                    listOf(
                        Message(Identifier("MessageA")),
                        Message(Identifier("MessageB")),
                    )
                ),
                SchemaDocument(
                    listOf(
                        Message(Identifier("MessageB")),
                        Message(Identifier("MessageA")),
                    )
                )
            )
        ).isTrue()

        assertThat(
            areEquivalent(
                SchemaDocument(
                    listOf(
                        Message(Identifier("MessageA"))
                    )
                ),
                SchemaDocument(
                    listOf(
                        Message(Identifier("MessageB"))
                    )
                )
            )
        ).isFalse()
    }
})
