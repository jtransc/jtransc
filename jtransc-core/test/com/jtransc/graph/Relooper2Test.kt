package com.jtransc.graph

import com.jtransc.ast.AstTypes
import com.jtransc.gen.TargetName
import org.junit.Test

class Relooper2Test {
	val types = AstTypes(TargetName("js"))
	val relooper = Relooper2<String, String>(types)

	/*
	@Test
	fun name() {
		// A -> B -> C --> D
		//     /|\___/
		val A = relooper.node("A")
		val B = relooper.node("B")
		val C = relooper.node("C")
		val D = relooper.node("D")
		relooper.edge(A, B, "loop")
	}
	*/

	@Test
	fun name2() {
		// START -> B -> C -> D -> END
		//         /|\   *    |
		//          |_________/
		val START = relooper.node("START")
		val B = relooper.node("B")
		val C = relooper.node("C")
		val D = relooper.node("D")
		val END = relooper.node("END")
		relooper.edge(START, B)
		relooper.edge(B, C)
		relooper.edge(C, D)
		relooper.edge(D, END)
		relooper.edge(D, B, "condLoopOut")
		relooper.edge(D, END, "condLoopOutExit")
		relooper.edge(C, C, "condLoopIn")
		relooper.edge(C, END, "condLoopExit")

		relooper.render(START)
	}
}