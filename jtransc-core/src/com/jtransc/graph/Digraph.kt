package com.jtransc.graph

internal const val UNDEFINED = -1

interface Digraph {
	val size: Int
	fun getIn(node: Int): List<Int>
	fun getOut(node: Int): List<Int>
}

interface DigraphWithNodes<T> : Digraph {
	val nodes: List<T>
	val nodeIndices: Map<T, Int>
}

fun <T> DigraphWithNodes<T>.toNodes(indices: Iterable<Int>): List<T> = indices.map { nodes[it] }
fun <T> DigraphWithNodes<T>.getInNodes(index: Int): List<T> = toNodes(this.getIn(index))
fun <T> DigraphWithNodes<T>.getOutNodes(index: Int): List<T> = toNodes(this.getOut(index))

fun <T> DigraphWithNodes<T>.getInNodes(node: T): List<T> = this.getInNodes(nodeIndices[node]!!)
fun <T> DigraphWithNodes<T>.getOutNodes(node: T): List<T> = this.getOutNodes(nodeIndices[node]!!)

fun <T> DigraphWithNodes<T>.dump() {
	println("DigraphWithNodes: SIZE($nodes)")
	for (n in 0 until size) {
		println("[${nodes[n]}] : IN[${getInNodes(n)}] : OUT[${getOutNodes(n)}]")
	}
	for (v in nodes) {
		for (w in getOutNodes(v)) {
			println("$v -> $w")
		}
	}
}

fun Digraph.dump() {
	println("Digraph: SIZE($size)")
	for (n in 0 until size) {
		println("[$n] : IN[${getIn(n)}] : OUT[${getOut(n)}]")
	}
}

open class DigraphImpl(override val size: Int, private val pairIn: IntArray, private val pairOut: IntArray) : Digraph {
	val input = (0 until size).map { arrayListOf<Int>() }
	val output = (0 until size).map { arrayListOf<Int>() }

	init {
		for (n in 0 until Math.min(pairIn.size, pairOut.size)) {
			input[pairIn[n]] += pairOut[n]
			output[pairOut[n]] += pairIn[n]
		}
	}

	override fun getIn(node: Int): List<Int> = input[node]
	override fun getOut(node: Int): List<Int> = output[node]
}

open class DigraphImplWithNodes<T>(override val nodes: List<T>, pairIn: IntArray, pairOut: IntArray) : DigraphImpl(nodes.size, pairIn, pairOut), DigraphWithNodes<T> {
	override val nodeIndices = (0 until size).map { nodes[it] to it }.toMap()
}

fun <T> graphList(vararg edges: Pair<T, List<T>>): DigraphImplWithNodes<T> {
	return graph(*edges.flatMap { pair -> pair.second.map { pair.first to it } }.toTypedArray())
}

fun <T> graph(vararg edges: Pair<T, T>): DigraphImplWithNodes<T> {
	var nodes = hashMapOf<T, Int>()
	var size = 0
	fun getIndex(node: T): Int {
		if (node !in nodes) nodes[node] = size++
		return nodes[node]!!
	}

	val pairIn = IntArray(edges.size)
	val pairOut = IntArray(edges.size)
	for (n in 0 until edges.size) {
		var edge = edges[n]
		pairOut[n] = getIndex(edge.first)
		pairIn[n] = getIndex(edge.second)
	}
	return DigraphImplWithNodes(nodes.keys.toList(), pairIn, pairOut)
}