package com.jtransc.graph

import java.util.*

internal const val UNDEFINED = -1

interface Digraph<T> {
	val nodes: List<T>
	val nodeIndices: Map<T, Int>
	fun getIn(node: Int): List<Int>
	fun getOut(node: Int): List<Int>
}

val <T> Digraph<T>.size: Int get() = nodes.size

fun <T> Digraph<T>.descendants(entry: Int = 0): List<Int> {
	val explored = BooleanArray(size)
	val out = arrayListOf(entry)
	explored[entry] = true
	fun explore(node: Int) {
		val childsToExplored = arrayListOf<Int>()
		for (child in getOut(node)) {
			if (!explored[child]) {
				explored[child] = true
				out += child
				childsToExplored += child
			}
		}
		for (child in childsToExplored) explore(child)
	}
	explore(entry)
	return out
}

fun <T> Digraph<T>.calcDepths(entryIndex: Int = 0): IntArray {
	val depths = IntArray(size) { UNDEFINED }
	fun explore(index: Int, depth: Int = 0) {
		depths[index] = depth
		for (out in getOut(index)) {
			if (depths[out] != UNDEFINED) explore(out, depth + 1)
		}
	}
	explore(entryIndex)
	return depths
}

// LCA and LCA tree
fun <T> Digraph<T>.locateFirstConverge(nodes: Iterable<T>): T? {
	val result = locateFirstConverge(this.toIndices(nodes))
	return if (result != UNDEFINED) getNode(result) else null
}

fun <T> Digraph<T>.locateFirstConverge(indices: Iterable<Int>): Int {
	val depths = this.calcDepths()
	return indices.reduce { a, b -> this.locateFirstConverge(a, b, depths) }
}

fun <T> Digraph<T>.locateFirstConverge(a: Int, b: Int, depths: IntArray): Int {
	var l = if (depths[a] > depths[b]) a else b
	var r = if (depths[a] > depths[b]) b else a
	assert(depths[l] <= depths[r])
	while (l != r) {

	}
	return l
}


fun <T> Digraph<T>.getIndex(node: T): Int = nodeIndices[node]!!
fun <T> Digraph<T>.getNode(index: Int): T = nodes[index]
fun <T> Digraph<T>.toNodes(indices: Iterable<Int>): List<T> = indices.map { nodes[it] }
fun <T> Digraph<T>.toIndices(nodes: Iterable<T>): List<Int> = nodes.map { nodeIndices[it]!! }
fun <T> Digraph<T>.getInNodes(index: Int): List<T> = toNodes(this.getIn(index))
fun <T> Digraph<T>.getOutNodes(index: Int): List<T> = toNodes(this.getOut(index))

fun <T> Digraph<T>.getInNodes(node: T): List<T> = this.getInNodes(nodeIndices[node]!!)
fun <T> Digraph<T>.getOutNodes(node: T): List<T> = this.getOutNodes(nodeIndices[node]!!)

fun <T> Digraph<T>.dump() {
	println("DigraphWithNodes: SIZE($size)")
	for (n in 0 until size) {
		println("NODE: [${nodes[n]}] : IN[${getInNodes(n)}] : OUT[${getOutNodes(n)}]")
	}
	for (v in nodes) {
		for (w in getOutNodes(v)) {
			println("EDGE: $v -> $w")
		}
	}
}

fun <T> Digraph<T>.dumpSimple() {
	println("Digraph: SIZE($size)")
	for (n in 0 until size) {
		println("[$n] : IN[${getIn(n)}] : OUT[${getOut(n)}]")
	}
}

open class DigraphImpl<T>(override val nodes: List<T>, private val pairFrom: IntArray, private val pairTo: IntArray) : Digraph<T> {
	override val nodeIndices = (0 until size).map { nodes[it] to it }.toMap()
	override fun getIn(node: Int): List<Int> = input[node]
	override fun getOut(node: Int): List<Int> = output[node]

	val input = (0 until size).map { arrayListOf<Int>() }
	val output = (0 until size).map { arrayListOf<Int>() }

	init {
		for (n in 0 until Math.min(pairFrom.size, pairTo.size)) {
			input[pairTo[n]] += pairFrom[n]
			output[pairFrom[n]] += pairTo[n]
		}
	}
}

fun <T> graphList(edges: Iterable<Pair<T, List<T>>>): Digraph<T> {
	return graphList(*edges.toList().toTypedArray())
}

fun <T> graphList(vararg edges: Pair<T, List<T>>): Digraph<T> {
	return graph(*edges.flatMap { pair -> pair.second.map { pair.first to it } }.toTypedArray())
}

fun <T> graph(edges: Iterable<Pair<T, T>>): Digraph<T> {
	return graph(*edges.toList().toTypedArray())
}

fun <T> graph(vararg edges: Pair<T, T>): Digraph<T> {
	var nodes = LinkedHashMap<T, Int>()
	var size = 0
	fun getIndex(node: T): Int {
		if (node !in nodes) nodes[node] = size++
		return nodes[node]!!
	}

	val pairIn = IntArray(edges.size)
	val pairOut = IntArray(edges.size)
	for (n in 0 until edges.size) {
		var edge = edges[n]
		//println("build EDGE: ${edge.first} -> ${edge.second}")
		pairIn[n] = getIndex(edge.first)
		pairOut[n] = getIndex(edge.second)
	}
	//for (node in nodes.keys) println("build NODE: $node")
	return DigraphImpl(nodes.keys.toList(), pairIn, pairOut)
}