package com.jtransc.util

import com.jtransc.error.noImpl
import java.util.*

// https://en.wikipedia.org/wiki/Topological_sorting
fun <T> List<T>.topologicalSort(getDependencies: (item:T) -> List<T>):List<T> {
	// Kahn's algorithm
	//val nodes = this
	//val edges = nodes.map { it to getDependencies(it) }.toMap()
	//val L = arrayListOf<T>()
	//val S = LinkedList<T>(edges.filter { it.value.isEmpty() }.map { it.key })
	//while (S.isNotEmpty()) {
	//	val n = S.removeFirst()
	//	L += n
	//	val ledges = edges[n]
	//	for (e in ledges)
	//	//ledges.
	//}
	noImpl("Not implemented topologicalSort!")
}
