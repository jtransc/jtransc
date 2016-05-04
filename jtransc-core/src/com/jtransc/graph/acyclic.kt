package com.jtransc.graph

import com.jtransc.error.invalidOp
import java.util.*

interface AcyclicDigraph<T> : Digraph<T>

fun <T> Digraph<T>.assertAcyclic(): AcyclicDigraph<T> = if (this is AcyclicDigraph<T>) this else AcyclicDigraphImpl(this)

fun <T> Digraph<T>.isAcyclic(): Boolean = try {
	AcyclicDigraphImpl(this); true
} catch (t: Throwable) {
	false
}

class AcyclicDigraphImpl<T>(val graph: Digraph<T>) : AcyclicDigraph<T> {
	override val nodes: List<T> = graph.nodes
	override val nodeIndices: Map<T, Int> = graph.nodeIndices
	override fun getIn(node: Int): List<Int> = graph.getIn(node)
	override fun getOut(node: Int): List<Int> = graph.getOut(node)

	init {
		for (c in graph.tarjanStronglyConnectedComponentsAlgorithm().components) {
			if (c.indices.size != 1) invalidOp("Cyclic graph")
		}
	}
}

class AcyclicDigraphLookup<T>(val graph: AcyclicDigraph<T>, val lookup: List<Set<Int>>) {
	fun common(items: Iterable<T>): T = items.reduce { a, b -> common(a, b) }
	fun common(a: T, b: T): T = graph.getNode(this.common(graph.getIndex(a), graph.getIndex(b)))

	fun common(items: Iterable<Int>): Int = items.reduce { a, b -> common(a, b) }
	fun common(a: Int, b: Int): Int {
		val aset = lookup[a]
		val bset = lookup[b]
		for (i in aset) if (i in bset) return i
		return UNDEFINED
	}
}

fun <T> AcyclicDigraph<T>.createCommonDescendantLookup(): AcyclicDigraphLookup<T> {
	val explored = BooleanArray(size)
	val descendants = (0 until size).map { LinkedHashSet<Int>() }
	fun explore(node: Int) {
		if (explored[node]) return
		explored[node] = true
		descendants[node] += node
		for (child in getOut(node)) {
			explore(child)
			descendants[node] += descendants[child]
		}
	}
	for (node in 0 until size) explore(node)
	return AcyclicDigraphLookup(this, descendants)
}