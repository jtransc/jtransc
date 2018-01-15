package com.jtransc.graph

import com.jtransc.ast.*
import com.jtransc.gen.TargetName
import com.jtransc.text.Indenter
import org.junit.Assert.assertEquals
import org.junit.Test

class RelooperTest {
	val types = AstTypes(TargetName("js"))
	val relooper = Relooper(types)

	private fun stmt(name: String): AstStm = AstType.INT.local(name).setTo(1.lit)

	@Test fun testIf() {
		val A = relooper.node(stmt("a"))
		val B = relooper.node(stmt("b"))
		val C = relooper.node(stmt("c"))
		relooper.edge(A, C, AstType.INT.local("a") eq 1.lit)
		relooper.edge(A, B)
		relooper.edge(B, C)
		assertEquals("""
			a = 1;
			if ((!(a == 1))) {
				b = 1;
			}
			c = 1;
		""".normalizeMulti(), relooper.renderStr(A))
	}

	@Test fun testIfElseExplicitEnd() {
		val A = relooper.node(stmt("a"))
		val B = relooper.node(stmt("b"))
		val C = relooper.node(stmt("c"))
		val D = relooper.node(stmt("d"))
		relooper.edge(A, B, AstType.INT.local("a") eq 1.lit)
		relooper.edge(A, C)
		relooper.edge(B, D)
		relooper.edge(C, D)
		assertEquals("""
			a = 1;
			if ((a == 1)) {
				b = 1;
			}
			else {
				c = 1;
			}
			d = 1;
		""".normalizeMulti(), relooper.renderStr(A))
	}

	//@Test fun testDoubleIf() {
	//	val A = relooper.node(stmt("pre"))
	//	val B = relooper.node(stmt("if1"))
	//	val C = relooper.node(stmt("if2"))
	//	val D = relooper.node(stmt("end"))
	//	relooper.edge(A, B, AstType.INT.local("pre") eq 1.lit)
	//	relooper.edge(B, C, AstType.INT.local("if1") eq 1.lit)
	//	relooper.edge(C, D)
	//	relooper.edge(B, D)
	//	assertEquals("""
	//		pre = 1;
	//		if ((pre == 1)) {
	//			if1 = 1;
	//			if ((if1 == 1)) {
	//				if2 = 1;
	//			}
	//			else {
	//				NOP(empty stm)
	//			}
	//			end = 1;
	//		}
	//		else {
	//			NOP(empty stm)
	//		}
	//	""".normalizeMulti(), relooper.renderStr(A))
	//}

	/*
	@Test fun testIf2() {
		val A = relooper.node(stmt("a"))
		val B = relooper.node(stmt("b"))
		val C = relooper.node(stmt("c"))
		relooper.edge(A, B)
		relooper.edge(A, C, AstExpr.build { INT.local("a") eq 1.lit })
		relooper.edge(C, B)
		Assert.assertEquals("{ a = 1; if ((a == 1)) { b = 1; } NOP }", dump(relooper.render(A)!!.optimize()).toString(doIndent = false).trim())
	}
	*/

	/*
	@Test fun testIfElseNoExplicitEnd() {
		val A = relooper.node(stmt("a"))
		val B = relooper.node(stmt("b"))
		val C = relooper.node(stmt("c"))
		relooper.edge(A, B, AstExpr.build { INT.local("a") eq 1.lit })
		relooper.edge(A, C)
		Assert.assertEquals("{ a = 1; if ((a == 1)) { b = 1; } else { c = 1; } NOP }", dump(relooper.render(A)).toString(doIndent = false).trim())
	}

	*/

	@Test
	fun testDoubleWhile() {
		// A -> B -> C -> D -> E
		//     /|\   *    |
		//      |_________/
		val A = relooper.node(stmt("A"))
		val B = relooper.node(stmt("B"))
		val C = relooper.node(stmt("C"))
		val D = relooper.node(stmt("D"))
		val E = relooper.node(stmt("E"))
		relooper.edge(A, B)
		relooper.edge(B, C)
		relooper.edge(C, D)
		relooper.edge(D, E)
		relooper.edge(D, B, AstExpr.RAW(AstType.BOOL, "condLoopOutContinue"))
		relooper.edge(D, E, AstExpr.RAW(AstType.BOOL, "condLoopOutBreak"))
		relooper.edge(C, C, AstExpr.RAW(AstType.BOOL, "condLoopInContinue"))
		relooper.edge(C, E, AstExpr.RAW(AstType.BOOL, "condLoopOutBreak"))
		relooper.edge(A, E, AstExpr.RAW(AstType.BOOL, "condToAvoidLoop"))
		assertEquals("""
			A = 1;
			if ((!condToAvoidLoop)) {
				do {
					B = 1;
					do {
						C = 1;
						if (condLoopInContinue) {
							continue;
						}
						if (condLoopOutBreak) {
							break;
						}
					} while (true);
					D = 1;
					if (condLoopOutContinue) {
						continue;
					}
					if (condLoopOutBreak) {
						break;
					}
				} while (true);
			}
			E = 1;
		""".normalizeMulti(), relooper.renderStr(A))
	}

	fun String.normalizeMulti() = this.trimIndent().trim().lines().map { it.trimEnd() }.joinToString("\n")
	fun Indenter.normalizeMulti() = this.toString().normalizeMulti()
	fun Relooper.renderStr(node: Relooper.Node) = render(node).dumpCollapse(types).normalizeMulti()
}