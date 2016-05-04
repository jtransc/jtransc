package com.jtransc.graph

import java.util.*

// https://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm
// G = Graph
// V = Vertices (Nodes)
// E = Edges (E)
fun <T> Digraph<T>.tarjanStronglyConnectedComponentsAlgorithm() = StrongComponentGraph(this, TarjanStronglyConnectedComponentsAlgorithm<T>(this).calculate())

// A strong component list is a disjoint set : https://en.wikipedia.org/wiki/Disjoint-set_data_structure
class StrongComponent<T>(val scgraph: StrongComponentGraph<T>, val indices: LinkedHashSet<Int>) {
	val graph: Digraph<T> = scgraph.graph
	val nodes: List<T> = graph.toNodes(indices)
	val inp: List<StrongComponent<T>> get() = scgraph.getInNodes(this)
	val out: List<StrongComponent<T>> get() = scgraph.getOutNodes(this)
	override fun toString() = graph.toNodes(indices).toString()
}

data class StrongComponentEdge<T>(val scgraph: StrongComponentGraph<T>, val fromNode:Int, val fromSC:StrongComponent<T>, val toNode:Int, val toSC:StrongComponent<T>) {
	val graph: Digraph<T> = scgraph.graph
	val fromNodeNode = graph.nodes[fromNode]
	val toNodeNode = graph.nodes[toNode]
	override fun toString() = "$fromNodeNode -> $toNodeNode"
}

class StrongComponentGraph<T>(val graph: Digraph<T>, componentsData: List<LinkedHashSet<Int>>) : AcyclicDigraph<StrongComponent<T>> {
	val components = componentsData.map { StrongComponent(this, it) }
	override val nodes: List<StrongComponent<T>> = components
	override val nodeIndices = (0 until size).map { nodes[it] to it }.toMap()
	override fun getIn(node: Int): List<Int> = input[node]
	override fun getOut(node: Int): List<Int> = output[node]

	private val input = (0 until size).map { arrayListOf<Int>() }
	private val output = (0 until size).map { arrayListOf<Int>() }
	val outputEdges = (0 until size).map { arrayListOf<StrongComponentEdge<T>>() }
	private val nodeIndexToComponents = IntArray(graph.size).let {
		for (c in 0 until size) for (n in components[c].indices) it[n] = c
		it
	}

	private fun addEdge(edge: StrongComponentEdge<T>) {
		val fromSCIndex = nodeIndices[edge.fromSC]!!
		val toSCIndex = nodeIndices[edge.toSC]!!
		input[toSCIndex] += fromSCIndex
		output[fromSCIndex] += toSCIndex
		outputEdges[fromSCIndex] += edge
	}

	init {
		// @TODO: SLOW! This information probably could be obtained directly from the algorithm!
		for (inpComponent in components) {
			val graph = inpComponent.graph
			val set = inpComponent.indices
			for (inp in set) {
				for (out in graph.getOut(inp)) {
					if (out !in set) {
						// external link
						val outComponent = findComponentWith(out)
						addEdge(StrongComponentEdge(this, inp, inpComponent, out, outComponent))
					}
				}
			}
		}
	}

	override fun toString() = "$components : " + outputEdges.flatMap { it }.map { "($it)" }.toString()

	fun findComponentWith(nodeIndex: Int): StrongComponent<T> {
		return components[nodeIndexToComponents[nodeIndex]]
	}

	fun findComponentWith(node: T): StrongComponent<T> {
		return components[nodeIndexToComponents[graph.getIndex(node)]]
	}

	fun findComponentIndexWith(node: T): Int {
		return nodeIndexToComponents[graph.getIndex(node)]
	}
}

fun <T> List<StrongComponent<T>>.toNodes(graph: Digraph<T>): List<List<T>> {
	return this.map { graph.toNodes(it.indices) }
}

private class TarjanStronglyConnectedComponentsAlgorithm<T>(val graph: Digraph<T>) {
	val indices = IntArray(graph.size) { UNDEFINED }
	val lowlinks = IntArray(graph.size) { UNDEFINED }
	val onStackList = BooleanArray(graph.size) { false }

	// @TODO: Use type aliases when kotlin accept them!
	var Int.index: Int get() = indices[this]; set(value) { indices[this] = value }
	var Int.lowlink: Int get() = lowlinks[this]; set(value) { lowlinks[this] = value }
	var Int.onStack: Boolean get() = onStackList[this]; set(value) { onStackList[this] = value }
	fun Int.successors(): List<Int> = graph.getOut(this)

	var index = 0
	var S = Stack<Int>()
	val output = arrayListOf<LinkedHashSet<Int>>()

	fun calculate(): List<LinkedHashSet<Int>> {
		for (v in 0 until graph.size) {
			if (v.index == UNDEFINED) strongconnect(v)
		}
		return output
	}

	fun strongconnect(v: Int) {
		// Set the depth index for v to the smallest unused index
		v.index = index
		v.lowlink = index
		index++
		S.push(v)
		v.onStack = true

		// Consider successors of v
		for (w in v.successors()) {
			if (w.index == UNDEFINED) {
				// Successor w has not yet been visited; recurse on it
				strongconnect(w)
				v.lowlink = Math.min(v.lowlink, w.lowlink)
			} else if (w.onStack) {
				// Successor w is in stack S and hence in the current SCC
				v.lowlink = Math.min(v.lowlink, w.index)
			}
		}

		// If v is a root node, pop the stack and generate an SCC
		if (v.lowlink == v.index) {
			//start a new strongly connected component
			//println("start a new strongly connected component")
			val strongComponent = LinkedHashSet<Int>()
			do {
				val w = S.pop()
				w.onStack = false
				strongComponent += w
				//println("add w to current strongly connected component")
			} while (w != v)
			output += strongComponent
			//println("output the current strongly connected component")
		}
	}
}