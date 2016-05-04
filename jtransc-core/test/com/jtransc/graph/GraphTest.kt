package com.jtransc.graph

import com.jtransc.ast.*
import com.jtransc.types.dump
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
		val strongComponents = graph.tarjanStronglyConnectedComponentsAlgorithm()

		Assert.assertEquals("[[C, B, A], [G, F], [E, D], [H]] : [(F -> C), (E -> F), (D -> B), (D -> C), (H -> E), (H -> G)]", "$strongComponents")
	}

	@Test fun testAcyclic() {
		Assert.assertEquals(true, graph(A to C, B to C).isAcyclic())
		Assert.assertEquals(true, graph(A to B, A to C, B to D, C to D).isAcyclic())
	}

	@Test fun testCommon() {
		graph(A to C, B to C).assertAcyclic()
		Assert.assertEquals(
			D,
			graph(
				A to B,
				A to C,
				B to D,
				C to D
			).assertAcyclic().createCommonDescendantLookup().common(B, C)
		)
	}

	@Test fun testRelooper() {
		val relooper = Relooper()
		val A = relooper.node(AstStm.build { INT.local("a") assignTo 1.lit })
		val B = relooper.node(AstStm.build { INT.local("b") assignTo 1.lit })
		relooper.edge(A, B, AstExpr.build { INT.local("a") eq 1.lit })
		Assert.assertEquals("{ a = 1; if ((a == 1)) { b = 1; } NOP }", dump(relooper.render(A)).toString(doIndent = false).trim())
	}

	@Test fun testRelooper2() {
		val relooper = Relooper()
		val A = relooper.node(AstStm.build { INT.local("a") assignTo 1.lit })
		val B = relooper.node(AstStm.build { INT.local("b") assignTo 1.lit })
		val C = relooper.node(AstStm.build { INT.local("c") assignTo 1.lit })
		relooper.edge(A, B, AstExpr.build { INT.local("a") eq 1.lit })
		relooper.edge(A, C)
		Assert.assertEquals("{ a = 1; if ((a == 1)) { b = 1; } else { c = 1; } NOP }", dump(relooper.render(A)).toString(doIndent = false).trim())
	}

	@Test fun testRelooper3() {
		val relooper = Relooper()
		val A = relooper.node(AstStm.build { INT.local("a") assignTo 1.lit })
		val B = relooper.node(AstStm.build { INT.local("b") assignTo 1.lit })
		val C = relooper.node(AstStm.build { INT.local("c") assignTo 1.lit })
		val D = relooper.node(AstStm.build { INT.local("d") assignTo 1.lit })
		relooper.edge(A, B, AstExpr.build { INT.local("a") eq 1.lit })
		relooper.edge(A, C)
		relooper.edge(B, D)
		relooper.edge(C, D)
		Assert.assertEquals("{ a = 1; if ((a == 1)) { b = 1; } else { c = 1; } d = 1; }", dump(relooper.render(A)).toString(doIndent = false).trim())
	}
}