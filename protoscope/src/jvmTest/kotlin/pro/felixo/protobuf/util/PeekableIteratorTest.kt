package pro.felixo.protobuf.util

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import kotlin.test.Test

class PeekableIteratorTest {
    @Test
    fun `hasNext is false for empty iterator`() {
        val emptyIterator = PeekableIterator(emptyList<Int>().iterator())
        assertThat(emptyIterator.hasNext()).isFalse()
    }

    @Test
    fun `peek is null for empty iterator`() {
        val emptyIterator = PeekableIterator(emptyList<Int>().iterator())
        assertThat(emptyIterator.peek()).isNull()
    }

    @Test
    fun `next retrieves the next item`() {
        val iterator = PeekableIterator(listOf(1, 2, 3).iterator())
        assertThat(iterator.next()).isEqualTo(1)
        assertThat(iterator.next()).isEqualTo(2)
        assertThat(iterator.next()).isEqualTo(3)
        assertFailure { iterator.next() }.isInstanceOf<NoSuchElementException>()
    }

    @Test
    fun `peek retrieves the next item without advancing the iterator`() {
        val iterator = PeekableIterator(listOf(1, 2, 3, 4, 5).iterator())
        assertThat(iterator.peek()).isEqualTo(1)
        assertThat(iterator.next()).isEqualTo(1)
        assertThat(iterator.peek()).isEqualTo(2)
        assertThat(iterator.peek()).isEqualTo(3)
        assertThat(iterator.next()).isEqualTo(2)
        assertThat(iterator.next()).isEqualTo(3)
        assertThat(iterator.next()).isEqualTo(4)
        assertThat(iterator.peek()).isEqualTo(5)
        assertThat(iterator.peek()).isNull()
        assertThat(iterator.next()).isEqualTo(5)
        assertThat(iterator.peek()).isNull()
        assertFailure { iterator.next() }.isInstanceOf<NoSuchElementException>()
        assertThat(iterator.peek()).isNull()
    }
}
