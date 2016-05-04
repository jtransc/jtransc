package com.jtransc.graph

fun <T> Digraph<T>.subgraph(from: T, to: T): Digraph<T> {
	return FilterDigraph(this) { graph, index, node ->
		node != to
	}
}

fun <T> Digraph<T>.subgraph(from: Int, to: Int): Digraph<T> {
	return FilterDigraph(this) { graph, index, node ->
		index != to
	}
}

class FilterDigraph<T>(val graph: Digraph<T>, val filter: (graph: Digraph<T>, index:Int, node: T) -> Boolean) : Digraph<T> {
	override val nodes: List<T> = graph.nodes
	override val nodeIndices: Map<T, Int> = graph.nodeIndices

	override fun getIn(node: Int): List<Int> = graph.getIn(node).filter { filter(graph, it, nodes[it]) }
	override fun getOut(node: Int): List<Int> = graph.getOut(node).filter { filter(graph, it, nodes[it]) }
}
