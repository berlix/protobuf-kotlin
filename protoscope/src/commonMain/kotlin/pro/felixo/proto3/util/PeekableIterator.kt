package pro.felixo.proto3.util

internal class PeekableIterator<T : Any>(
    private val base: Iterator<T>
) : Iterator<T> {
    private var peeked = ArrayDeque<T>()

    override fun hasNext(): Boolean = peeked.isNotEmpty() || base.hasNext()

    override fun next(): T = peeked.removeFirstOrNull() ?: base.next()

    /**
     * Each call to this method peeks ahead one additional time without affecting the result of subsequent [next] calls.
     */
    fun peek(): T? = if (!base.hasNext()) null else base.next().also { peeked.add(it) }
}
