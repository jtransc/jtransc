package com.jtransc.graph

import java.util.*

// https://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm
// G = Graph
// V = Vertices (Nodes)
// E = Edges (E)
fun Digraph.tarjanStronglyConnectedComponentsAlgorithm() = TarjanStronglyConnectedComponentsAlgorithm(this).calculate()

data class StrongComponent(val items: List<Int>)

fun <T> List<StrongComponent>.toNodes(graph: DigraphWithNodes<T>): List<List<T>> {
	return this.map { graph.toNodes(it.items) }
}

private class TarjanStronglyConnectedComponentsAlgorithm(val graph: Digraph) {
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
	val output = arrayListOf<StrongComponent>()

	fun calculate(): List<StrongComponent> {
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
			val strongComponent = arrayListOf<Int>()
			do {
				val w = S.pop()
				w.onStack = false
				strongComponent += w
				//println("add w to current strongly connected component")
			} while (w != v)
			output += StrongComponent(strongComponent)
			//println("output the current strongly connected component")
		}
	}
}