package com.jtransc.graph

import org.junit.Assert
import org.junit.Test

// http://character-code.com/arrows-html-codes.php
// ←↑→↓↔↕↖↗↘↙↺↻⇄⇅⇆⇇⇈⇉⇊
class GraphTest {
	val A = 'A'
	val B = 'B'
	val C = 'C'
	val D = 'D'
	val E = 'E'
	val F = 'F'
	val G = 'G'
	val H = 'H'

	fun <T> l(vararg items: T): List<T> = listOf(*items)

	@Test fun testGraph() {
		graph(A to B, B to A).dump()
	}

	// Graph from example: https://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm
	// A ← C ← F ⇆ G
	// ↓ ↗ ↑    ↑    ↑
	// B ← D ⇆ E ← H↺
	@Test fun testStronglyConnectedComponentsAlgorithm() {
		val graph = graphList(
			A to l(B),
			B to l(C),
			C to l(A),
			D to l(B, C, E),
			E to l(D, F),
			F to l(C, G),
			G to l(F),
			H to l(E, G, H)
		)
		val strongComponents = graph.tarjanStronglyConnectedComponentsAlgorithm().toNodes(graph)
		Assert.assertEquals("[[C, B, A], [G, F], [E, D], [H]]", "$strongComponents")
	}
}