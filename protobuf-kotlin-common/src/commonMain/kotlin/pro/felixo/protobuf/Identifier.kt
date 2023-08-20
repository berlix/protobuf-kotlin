package pro.felixo.protobuf

import kotlin.jvm.JvmInline

/**
 * Represents a protobuf identifier, which is a string consisting of letters, numbers, and underscores, starting with a
 * letter or underscore.
 *
 * This class does not perform validation of identifiers.
 */
@JvmInline
value class Identifier(val value: String) : Comparable<Identifier> {
    override fun compareTo(other: Identifier): Int = value.compareTo(other.value)
    override fun toString() = value
}
