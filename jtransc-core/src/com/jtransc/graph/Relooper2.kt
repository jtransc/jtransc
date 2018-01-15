package com.jtransc.graph

import com.jtransc.ast.AstTypes

class Relooper2<TBody, TCond>(val types: AstTypes) {
	inner class Node(
		val body: TBody
	) {
		val incoming = arrayListOf<Edge>()
		val outgoing = arrayListOf<Edge>()
	}

	inner class Component(val nodes: Set<Node>)

	inner class Edge(val src: Node, val dst: Node, val cond: TCond? = null)

	interface Stm
	inner class Simple(val body: TBody) : Stm
	inner class If(val cond: TCond, val tbody: Stm, val fbody: Stm?) : Stm
	inner class While(val name: String, val cond: TCond, val tbody: Stm) : Stm
	inner class DoWhile(val name: String, val tbody: Stm, val cond: TCond) : Stm
	inner class Continue(val name: String) : Stm
	inner class Break(val name: String) : Stm

	fun node(body: TBody): Node {
		return Node(body)
	}

	fun edge(src: Node, dst: Node, cond: TCond? = null): Edge {
		return Edge(src, dst, cond).apply {
			dst.incoming += this
			src.outgoing += this
		}
	}

	fun render(node: Node): Stm {
		TODO()
	}
}