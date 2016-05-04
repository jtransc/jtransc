package com.jtransc.graph

// https://en.wikipedia.org/wiki/Lowest_common_ancestor
class LCATree(val capacity: Int) {
	val parents: IntArray = IntArray(capacity)
	val depths: IntArray = IntArray(capacity)
	private var current: Int = 1

	val Int.parent:Int get() = parents[this]
	val Int.depth:Int get() = depths[this]

	fun create(parent: Int): Int {
		parents[current] = parent
		depths[current] = depths[parent] + 1
		return current++
	}

	fun lca(items: Iterable<Int>): Int = items.reduce { a, b -> lca(a, b) }

	fun lca(a: Int, b: Int): Int {
		var (l, r) = if (a.depth < b.depth) Pair(a, b) else Pair(b, a)
		while (true) {
			if (l == r) return a
			while (l.depth > r.depth) l = l.parent
			while (r.depth > l.depth) r = r.parent
		}
	}
}

// https://en.wikipedia.org/wiki/Lowest_common_ancestor#Extension_to_directed_acyclic_graphs

/*
fun <T> AcyclicDigraph<T>.lca(items:Iterable<Int>):Int = items.reduce { a, b -> lca(a, b) }

fun <T> AcyclicDigraph<T>.lca(a:Int, b:Int):Int {

}
*/