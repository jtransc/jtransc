package com.jtransc.graph

import com.jtransc.ast.AstExpr
import com.jtransc.ast.AstStm
import com.jtransc.ast.AstStmUtils
import com.jtransc.types.dump
import java.util.*

class Relooper {
	class Graph
	class Node(val body: AstStm) {
		var next: Node? = null
		val edges = arrayListOf<Edge>()
		val possibleNextNodes: List<Node> get() = listOf(next).filterNotNull() + edges.map { it.dst }

		override fun toString(): String = dump(body).toString().trim()
	}

	class Edge(val dst: Node, val cond: AstExpr) {
		override fun toString(): String = "IF ($cond) goto $dst;"
	}

	fun node(body: AstStm): Node = Node(body)

	fun edge(a: Node, b: Node) {
		a.next = b
	}

	fun edge(a: Node, b: Node, cond: AstExpr) {
		a.edges += Edge(b, cond)
	}

	private fun prepare(entry: Node): List<Node> {
		val exit = node(AstStm.NOP())
		val explored = LinkedHashSet<Node>()
		fun explore(node: Node) {
			if (node in explored) return
			explored += node
			if (node.next != null) {
				explore(node.next!!)
			} else {
				node.next = exit
			}
			for (edge in node.edges) explore(edge.dst)
		}
		explore(entry)
		explored += exit
		return explored.toList()
	}

	fun render(entry: Node): AstStm {
		val nodes = prepare(entry)
		val graph = graphList(nodes.map {
			//println("$it -> ${it.possibleNextNodes}")
			it to it.possibleNextNodes
		})
		//println("----------")
		//graph.dump()
		//println("----------")

		val graph2 = graph.tarjanStronglyConnectedComponentsAlgorithm()
		val entry2 = graph2.findComponentIndexWith(entry)

		//graph2.outputEdges

		return renderInternal(graph2, entry2)
		/*
		println(entry)
		println(entry2)
		*/

		//graph.dump()
		//println(graph)
		// @TODO: strong components
	}

	private fun renderInternal2(scgraph: Digraph<StrongComponent<Node>>, node: Int): AstStm {
		val node2 = scgraph.getNode(node)
		// @TODO: recursive strong components!
		return AstStmUtils.stms(node2.nodes.map { it.body })
		//return AstStm.NOP()
	}

	private fun getEdge(from: Node, to: Node): Edge? {
		return from.edges.firstOrNull() { it.dst == to }
	}

	private fun renderInternal(scgraph: Digraph<StrongComponent<Node>>, node: Int, lookup: AcyclicDigraphLookup<StrongComponent<Node>> = scgraph.assertAcyclic().createCommonDescendantLookup()): AstStm {
		val stms = arrayListOf<AstStm>()
		stms.add(renderInternal2(scgraph, node))

		//node.scgraph.dump()

		val nodeNode = scgraph.getNode(node)
		val targets = scgraph.getOut(node)

		val nodeSrc = nodeNode.nodes.last()
		val nodeDsts = targets.map { scgraph.getNode(it).nodes.first() }

		if (targets.size == 2) {
			val common = lookup.common(targets)
			val branches = targets.map { branch -> renderInternal(scgraph.subgraph(branch, common), branch, lookup) }

			val edge = nodeDsts.map { getEdge(nodeSrc, it) }.filterNotNull().first()
			val cond = edge.cond

			// IF
			if (common in targets) {
				stms.add(AstStm.IF(cond, branches[1]))
				stms.add(branches[0])
			}
			// IF-ELSE
			else {
				stms.add(AstStm.IF_ELSE(cond, branches[1], branches[0]))
				stms.add(renderInternal(scgraph, common, lookup))
			}
		} else if (targets.size >= 2) {
			val common = lookup.common(targets)
			val branches = targets.map { branch -> renderInternal(scgraph.subgraph(branch, common), branch, lookup) }

			println(branches)
			println("COMMON: $common")
			//println(outNode.next)
			//println(outNode.possibleNextNodes.size)

		}
		return AstStmUtils.stms(stms)
	}
}