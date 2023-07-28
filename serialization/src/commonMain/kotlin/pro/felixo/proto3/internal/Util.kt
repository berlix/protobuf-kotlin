package pro.felixo.proto3.internal

import pro.felixo.proto3.ProtoNumber
import pro.felixo.proto3.schema.FieldRule
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import pro.felixo.proto3.FieldNumber

fun <T, U, C : Iterable<T>> C.requireNoDuplicates(transform: (T) -> U, message: (duplicate: T) -> String): C {
    val seen = mutableSetOf<U>()
    forEach {
        require(seen.add(transform(it))) { message(it) }
    }
    return this
}

fun <T, C : Iterable<T>> C.requireNoDuplicates(message: (duplicate: T) -> String): C {
    val seen = mutableSetOf<T>()
    forEach {
        require(seen.add(it)) { message(it) }
    }
    return this
}

fun <T> Iterator<T>.nextWhere(predicate: (T) -> Boolean): T {
    while (hasNext()) {
        val value = next()
        if (predicate(value))
            return value
    }
    error("Iterator had no more elements that satisfy the predicate.")
}

class NumberIterator(first: Int, last: Int = Int.MAX_VALUE, private val reserved: List<IntRange>) {
    private val iterator = IntRange(first, last).iterator()

    fun next(): Int = iterator.nextWhere { n -> reserved.all { n !in it } }

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        fun fromEnumElements(descriptor: SerialDescriptor) = NumberIterator(
            1,
            reserved = (0 until descriptor.elementsCount).mapNotNull {
                descriptor.getElementAnnotations(it).filterIsInstance<ProtoNumber>().firstOrNull()?.number
            }.requireNoDuplicates { "Duplicate field number in descriptor ${descriptor.serialName}: $it" }
                .map { it..it }
        )
    }
}

class FieldNumberIterator(reserved: List<Int>) {
    private val intIterator = NumberIterator(1, FieldNumber.MAX, reserved.map { it..it }
        .plusElement(FieldNumber.RESERVED_RANGE_START until FieldNumber.RESERVED_RANGE_END))

    fun next(): Int = intIterator.next()

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        fun fromClassElements(descriptor: SerialDescriptor) =
            FieldNumberIterator(
                (0 until descriptor.elementsCount).mapNotNull {
                    descriptor.getElementAnnotations(it).filterIsInstance<ProtoNumber>().firstOrNull()?.number
                }.requireNoDuplicates { "Duplicate field number in descriptor ${descriptor.serialName}: $it" }
            )

        @OptIn(ExperimentalSerializationApi::class)
        fun fromSubTypes(descriptors: Iterable<SerialDescriptor>): FieldNumberIterator {
            return FieldNumberIterator(
                descriptors.mapNotNull {
                    it.annotations.filterIsInstance<ProtoNumber>().firstOrNull()?.number
                }.requireNoDuplicates { duplicatedNumber ->
                    "Duplicate field number $duplicatedNumber in sub-types: ${descriptors.map { it.serialName }}"
                }
            )
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
fun SerialDescriptor.nullableToOptional() = if (isNullable) FieldRule.Optional else FieldRule.Singular

private val polymorphicNamePattern = Regex("""^kotlinx\.serialization\.Polymorphic<(.*)>\??""")

fun simpleTypeName(descriptor: SerialDescriptor): String =
    fullTypeName(descriptor).substringAfterLast('.')

@OptIn(ExperimentalSerializationApi::class)
fun fullTypeName(descriptor: SerialDescriptor): String =
    (polymorphicNamePattern.find(descriptor.serialName)?.groupValues?.get(1)
        ?: descriptor.serialName).removeSuffix("?")

@OptIn(ExperimentalSerializationApi::class)
fun SerialDescriptor.isCompatibleWith(other: SerialDescriptor): Boolean =
    other === this || (
        kind == other.kind &&
            (0 until elementsCount).all {
                getElementName(it) == other.getElementName(it) &&
                getElementDescriptor(it) == other.getElementDescriptor(it) &&
                getElementAnnotations(it) == other.getElementAnnotations(it)
            }
        )

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> List<*>.castItems(): List<T> {
    require(all { it is T })
    return this as List<T>
}
