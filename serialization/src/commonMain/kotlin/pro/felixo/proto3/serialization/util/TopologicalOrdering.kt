package pro.felixo.proto3.serialization.util

/**
 * Creates a reverse-topologically sorted list of nodes by means of performing a depth-first search in the graph.
 * In case an edge is encountered that creates a cycle in the graph, that edge is ignored.
 *
 * Exploration is started from the given [startingNodes]. The [targets] function is used to find the outgoing edges of
 * each node. [startingNodes] does not need to (but may) contain all nodes of the graph. [targets] is guaranteed to be
 * called exactly once for each node.
 *
 * The generic type T must have correct implementations of [equals] and [hashCode], such that they consider two nodes
 * equal if and only if they represent the same node in the graph.
 */
fun <T : Any> reverseTopologicalOrdering(startingNodes: List<T>, targets: (T) -> List<T>): List<T> {
    val sorted = mutableListOf<T>()
    val visited = mutableSetOf<T>()

    fun visit(node: T) {
        if (node !in visited) {
            visited.add(node)
            targets(node).asReversed().forEach(::visit)
            sorted.add(node)
        }
    }

    startingNodes.asReversed().forEach(::visit)

    return sorted
}

/**
 * Creates a topological ordering of nodes by means of performing a depth-first search in the graph.
 * In case an edge is encountered that creates a cycle in the graph, that edge is ignored. The ordering is returned as
 * a map from each node to its index in the ordering.
 *
 * Exploration is started from the given [startingNodes]. The [targets] function is used to find the outgoing edges of
 * each node. [startingNodes] does not need to (but may) contain all nodes of the graph. [targets] is guaranteed to be
 * called exactly once for each node.
 *
 * The generic type T must have correct implementations of [equals] and [hashCode], such that they consider two nodes
 * equal if and only if they represent the same node in the graph.
 */
fun <T : Any> topologicalIndex(startingNodes: List<T>, targets: (T) -> List<T>): Map<T, Int> {
    val reverse = reverseTopologicalOrdering(startingNodes, targets)
    return reverse.mapIndexed { index, node -> node to reverse.size - 1 - index }.toMap()
}
