package com.jtransc.graph

import com.jtransc.ast.*
import com.jtransc.gen.TargetName
import com.jtransc.text.Indenter
import org.junit.Assert.assertEquals
import org.junit.Test

class RelooperTest {
	val types = AstTypes(TargetName("js"))
	val relooper = Relooper(types, debug = true)

	@Test
	fun testIf() = relooperTest {
		val A = node("a")
		val B = node("b")
		val C = node("c")
		edge(A, C, AstType.INT.local("a") eq 1.lit)
		edge(A, B)
		edge(B, C)
		A.assertDump("""
			a = 1;
			if ((!(a == 1))) {
				b = 1;
			}
			c = 1;
		""")
	}

	@Test
	fun testIfElseExplicitEnd() = relooperTest {
		val A = node("a")
		val B = node("b")
		val C = node("c")
		val D = node("d")
		edge(A, B, AstType.INT.local("a") eq 1.lit)
		edge(A, C)
		edge(B, D)
		edge(C, D)
		A.assertDump("""
			a = 1;
			if ((a == 1)) {
				b = 1;
			}
			else {
				c = 1;
			}
			d = 1;
		""")
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
	fun testDoubleWhile() = relooperTest {
		// A -> B -> C -> D -> E
		//     /|\   *    |
		//      |_________/
		val A = node(stmt("A"))
		val B = node(stmt("B"))
		val C = node(stmt("C"))
		val D = node(stmt("D"))
		val E = node(stmt("E"))
		edge(A, B)
		edge(B, C)
		edge(C, D)
		edge(D, E)
		edge(D, B, AstExpr.RAW(AstType.BOOL, "condLoopOutContinue"))
		edge(D, E, AstExpr.RAW(AstType.BOOL, "condLoopOutBreak"))
		edge(C, C, AstExpr.RAW(AstType.BOOL, "condLoopInContinue"))
		edge(C, E, AstExpr.RAW(AstType.BOOL, "condLoopOutBreak"))
		edge(A, E, AstExpr.RAW(AstType.BOOL, "condToAvoidLoop"))
		A.assertDump("""
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
						break;
					} while (true);
					D = 1;
					if (condLoopOutContinue) {
						continue;
					}
					if (condLoopOutBreak) {
						break;
					}
					break;
				} while (true);
			}
			E = 1;
		""")
	}

	@Test
	fun testSmallFunctionWithWhileAndIf() = relooperTest {
		val L0 = node("L0")
		val L1 = node("L1")
		val L2 = node("L2")
		val L3 = node("L3")
		val L4 = node("L4")
		val L5 = node("L5")
		val L6 = node("L6")
		val L8 = node("L8")
		L0.edgeTo(L1)
		L1.edgeTo(L2).edgeTo(L3, cond("l1_l3"))
		L2.edgeTo(L4)
		L3.edgeTo(L6).edgeTo(L1, "l3_l1")
		L4.edgeTo(L5).edgeTo(L4, cond("l4_l4"))
		L5.edgeTo(L3)
		L6.edgeTo(L8)

		//* L0: { 	lI0 = p0; 	lI1 = p1; 	lI1 = (lI1 + 1); }EDGES: [goto L1;]. SRC_EDGES: 0
		//* L1: EDGES: [goto L2;, IF (((lI0 % 2) != 0)) goto L3;]. SRC_EDGES: 2
		//* L2: NOP(empty stm)EDGES: [goto L4;]. SRC_EDGES: 1
		//* L3: { 	lI0 = (lI0 + 1); }EDGES: [goto L6;, IF ((lI0 < lI1)) goto L1;]. SRC_EDGES: 2
		//* L4: { 	com.jtransc.io.JTranscConsole.log(lI0); 	lI0 = (lI0 + 1); }EDGES: [goto L5;, IF ((lI0 < lI1)) goto L4;]. SRC_EDGES: 2
		//* L5: NOP(empty stm)EDGES: [goto L3;]. SRC_EDGES: 1
		//* L6: { 	return lI1; }EDGES: [goto L8;]. SRC_EDGES: 1
		//* L8: NOP(empty stm)EDGES: []. SRC_EDGES: 1


		//@JTranscRelooper(value = true, debug = true)
		//static public int simpleDoWhile(int a, int b) {
		//	b++;
		//
		//	do {
		//		if (a % 2 == 0) {
		//			do {
		//				JTranscConsole.log(a);
		//				a++;
		//			} while (a < b);
		//		}
		//		a++;
		//	} while (a < b);
		//
		//	return b;
		//}

		L0.assertDump("""
			L0 = 1;
			do {
				L1 = 1;
				if ((!l1_l3)) {
					{
						L2 = 1;
						do {
							L4 = 1;
						} while (l4_l4);
						L5 = 1;
					}
				}
				L3 = 1;
				if (l3_l1) {
					continue;
				}
				break;
			} while (true);
			L6 = 1;
			L8 = 1;
    	""")
	}

	@Test
	fun testSimpleDoWhile() = relooperTest {
		val L0 = node("L0")
		val L1 = node("L1")
		val L2 = node("L2")
		val L4 = node("L4")

		L0.edgeTo(L1)
		L1.edgeTo(L2).edgeTo(L1, "lI0 < lI1")
		L2.edgeTo(L4)

		L0.assertDump("""
			L0 = 1;
			do {
				L1 = 1;
			} while (lI0 < lI1);
			L2 = 1;
			L4 = 1;
    	""")

		//* L0: { 	lI0 = p0; 	lI1 = p1; 	lI1 = (lI1 + 1); } EDGES: [goto L1;]. SRC_EDGES: 0
		//* L1: { 	lI0 = (lI0 + 1); } EDGES: [goto L2;, IF ((lI0 < lI1)) goto L1;]. SRC_EDGES: 2
		//* L2: { 	return lI1; } EDGES: [goto L4;]. SRC_EDGES: 1
		//* L4: NOP(empty stm) EDGES: []. SRC_EDGES: 1

		//@JTranscRelooper(value = true, debug = true)
		//static public int simpleDoWhile(int a, int b) {
		//	b++;
		//
		//	do {
		//		a++;
		//	} while (a < b);
		//
		//	return b;
		//}
	}

	@Test
	fun testSimpleWhile() = relooperTest {
		val L0 = node("L0")
		val L1 = node("L1")
		val L2 = node("L2")
		val L3 = node("L3")
		val L6 = node("L6")

		L0.edgeTo(L1)
		L1.edgeTo(L2).edgeTo(L3, "l2_l3")
		L2.edgeTo(L1)
		L3.edgeTo(L6)

		L0.assertDump("""
			-
    	""")

		// * L0: { 	lI0 = p0; 	lI1 = p1; 	lI1 = (lI1 + 1); } EDGES: [goto L1;]. SRC_EDGES: 0
		// * L1:  EDGES: [goto L2;, IF ((lI0 >= lI1)) goto L3;]. SRC_EDGES: 2
		// * L2: { 	com.jtransc.io.JTranscConsole.log(lI0); 	lI0 = (lI0 + 1); } EDGES: [goto L1;]. SRC_EDGES: 1
		// * L3: { 	com.jtransc.io.JTranscConsole.log(lI0); 	com.jtransc.io.JTranscConsole.log(lI1); 	return lI1; } EDGES: [goto L6;]. SRC_EDGES: 2
		// * L6: L6: NOP(empty stm) EDGES: []. SRC_EDGES: 1
		//@JTranscRelooper(debug = true)
		//static public int simpleWhile(int a, int b) {
		//	b++;
		//	while (a < b) {
		//		JTranscConsole.log(a);
		//		a++;
		//	}
		//	JTranscConsole.log(a);
		//	JTranscConsole.log(b);
		//	return b;
		//}
	}

	fun Relooper.Node.assertDump(msg: String) {
		assertEquals(msg.normalizeMulti(), relooper.renderStr(this))
	}

	inline fun relooperTest(callback: Relooper.() -> Unit): Unit = callback(relooper)

	fun Relooper.Node.edgeTo(other: Relooper.Node, cond: AstExpr? = null): Relooper.Node = this.apply { relooper.edge(this, other, cond) }
	fun Relooper.Node.edgeTo(other: Relooper.Node, cond: String): Relooper.Node = this.edgeTo(other, cond.let { cond(it) })
	fun String.normalizeMulti() = this.trimIndent().trim().lines().map { it.trimEnd() }.joinToString("\n")
	fun Indenter.normalizeMulti() = this.toString().normalizeMulti()
	fun Relooper.renderStr(node: Relooper.Node) = render(node).dumpCollapse(types).normalizeMulti()
	fun Relooper.node(name: String) = node(stmt(name))
	private fun stmt(name: String): AstStm = AstType.INT.local(name).setTo(1.lit)
	private fun cond(name: String) = AstExpr.RAW(AstType.BOOL, name)
}