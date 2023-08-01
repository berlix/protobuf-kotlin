package pro.felixo.proto3.util

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
}

class FieldNumberIterator(reserved: List<Int>) {
    private val intIterator = NumberIterator(1, FieldNumber.MAX, reserved.map { it..it }
        .plusElement(FieldNumber.RESERVED_RANGE_START until FieldNumber.RESERVED_RANGE_END))

    fun next(): Int = intIterator.next()
}
