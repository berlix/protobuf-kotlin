package pro.felixo.protobuf.serialization.util

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import kotlin.test.Test

class TopologicalOrderingTest {
    @Test
    fun `returns empty for empty argument`() = assertOrdering(
        emptyMap(),
        emptyList()
    )

    @Test
    fun `returns node for graph of 1 node`() = assertOrdering(
        mapOf(0 to emptyList()),
        listOf(0),
        0
    )

    @Test
    fun `returns topological ordering for graph of 2 nodes`() {
        val graph = mapOf(
            0 to listOf(1),
            1 to emptyList()
        )
        assertOrdering(
            graph,
            listOf(0),
            0, 1
        )
        assertOrdering(
            graph,
            listOf(0, 1),
            0, 1
        )
        assertOrdering(
            graph,
            listOf(1, 0),
            0, 1
        )
    }

    @Test
    fun `returns stable ordering for 2 disconnected nodes`() {
        val graph = mapOf(
            0 to emptyList<Int>(),
            1 to emptyList()
        )
        assertOrdering(
            graph,
            listOf(0, 1),
            0, 1
        )
        assertOrdering(
            graph,
            listOf(1, 0),
            1, 0
        )
    }

    @Test
    fun `returns undefined ordering for cycle of 2 nodes`() {
        val graph = mapOf(
            0 to listOf(1),
            1 to listOf(0)
        )
        assertUndefinedOrdering(
            graph,
            listOf(0),
            0, 1
        )
        assertUndefinedOrdering(
            graph,
            listOf(1),
            1, 0
        )
        assertUndefinedOrdering(
            graph,
            listOf(1, 0),
            1, 0
        )
    }

    @Test
    fun `returns topological ordering for complex graph`() = assertOrdering(
        mapOf(
            0 to listOf(3, 2, 1),
            1 to listOf(2),
            2 to listOf(0, 3), // cycle between 0 and 2
            3 to emptyList()
        ),
        listOf(0),
        0, 1, 2, 3
    )

    private fun assertOrdering(
        graph: Map<Int, List<Int>>,
        startingNodes: List<Int>,
        vararg expectedTopologicalOrder: Int
    ) {
        assertThat(reverseTopologicalOrdering(startingNodes, graph::getValue))
            .containsExactly(*expectedTopologicalOrder.reversed().toTypedArray())
        assertThat(topologicalIndex(startingNodes, graph::getValue)).isEqualTo(
            expectedTopologicalOrder.mapIndexed { index, node -> node to index }.toMap()
        )
    }

    private fun assertUndefinedOrdering(
        graph: Map<Int, List<Int>>,
        startingNodes: List<Int>,
        vararg expectedTopologicalOrder: Int
    ) {
        assertThat(reverseTopologicalOrdering(startingNodes, graph::getValue))
            .containsExactlyInAnyOrder(*expectedTopologicalOrder.reversed().toTypedArray())
        assertThat(topologicalIndex(startingNodes, graph::getValue).keys).containsExactlyInAnyOrder(
            *expectedTopologicalOrder.toTypedArray()
        )
    }
}
