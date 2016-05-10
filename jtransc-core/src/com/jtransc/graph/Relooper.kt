package com.jtransc.graph

import com.jtransc.ast.AstExpr
import com.jtransc.ast.AstStm
import com.jtransc.ast.AstStmUtils
import com.jtransc.ast.not
import com.jtransc.error.noImpl
import com.jtransc.types.dump
import java.util.*

class Relooper {
	class Graph
	class Node(val body: List<AstStm>) {
		var next: Node? = null
		val edges = arrayListOf<Edge>()
		val possibleNextNodes: List<Node> get() = listOf(next).filterNotNull() + edges.map { it.dst }

		override fun toString(): String = dump(AstStmUtils.stms(body)).toString().trim()
	}

	class Edge(val dst: Node, val cond: AstExpr) {
		override fun toString(): String = "IF ($cond) goto $dst;"
	}

	fun node(body: List<AstStm>): Node = Node(body)
	fun node(body: AstStm): Node = Node(listOf(body))

	fun edge(a: Node, b: Node) {
		a.next = b
	}

	fun edge(a: Node, b: Node, cond: AstExpr) {
		a.edges += Edge(b, cond)
	}

	private fun prepare(entry: Node): List<Node> {
		val exit = node(listOf())
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

	fun render(entry: Node): AstStm? {
		val nodes = prepare(entry)
		val graph = graphList(nodes.map {
			//println("$it -> ${it.possibleNextNodes}")
			it to it.possibleNextNodes
		})
		//println("----------")
		//graph.dump()
		//println("----------")

		if (graph.hasCycles()) {
			//noImpl("acyclic!")
			//println("cyclic!")
			return null
		}


		val graph2 = graph.tarjanStronglyConnectedComponentsAlgorithm()
		val entry2 = graph2.findComponentIndexWith(entry)

		//graph2.outputEdges

		return AstStmUtils.stms(renderInternal(graph2, entry2, -1))
		/*
		println(entry)
		println(entry2)
		*/

		//graph.dump()
		//println(graph)
		// @TODO: strong components
	}

	private fun renderInternal2(scgraph: Digraph<StrongComponent<Node>>, node: Int): List<AstStm> {
		val node2 = scgraph.getNode(node)
		// @TODO: recursive strong components!
		return node2.nodes.flatMap { it.body }
		//return AstStm.NOP()
	}

	private fun getEdge(from: Node, to: Node): Edge? {
		return from.edges.firstOrNull() { it.dst == to }
	}

	private fun renderInternal(
		scgraph: Digraph<StrongComponent<Node>>,
		node: Int,
		endnode: Int,
		lookup: AcyclicDigraphLookup<StrongComponent<Node>> = scgraph.assertAcyclic().createCommonDescendantLookup()
	): List<AstStm> {
		if (node == endnode) return listOf()

		val stms = arrayListOf<AstStm>()
		stms += renderInternal2(scgraph, node)

		//node.scgraph.dump()

		val nodeNode = scgraph.getNode(node)
		val targets = scgraph.getOut(node)

		val nodeSrc = nodeNode.nodes.last()
		val nodeDsts = targets.map { scgraph.getNode(it).nodes.first() }

		when (targets.size) {
			0 -> Unit
			1 -> stms += renderInternal(scgraph, targets.first(), endnode, lookup)
			2 -> {
				val common = lookup.common(targets)
				val branches = targets.map { branch -> renderInternal(scgraph, branch, common, lookup) }

				val edge = nodeDsts.map { getEdge(nodeSrc, it) }.filterNotNull().first()
				val cond = edge.cond

				// IF
				if (common in targets) {
					//val type1 = targets.indexOfFirst { it != common }
					//stms += AstStm.IF(cond.not(), AstStmUtils.stms(branches[type1]))
					stms += AstStm.IF(cond.not(), AstStmUtils.stms(branches[0]))
				}
				// IF-ELSE
				else {
					stms += AstStm.IF_ELSE(cond.not(), AstStmUtils.stms(branches[0]), AstStmUtils.stms(branches[1]))
				}
				stms += renderInternal(scgraph, common, endnode, lookup)
			}
			else -> {
				val common = lookup.common(targets)
				val branches = targets.map { branch -> renderInternal(scgraph, branch, common, lookup) }

				println(branches)
				println("COMMON: $common")
				//println(outNode.next)
				//println(outNode.possibleNextNodes.size)
			}
		}
		return stms
	}
}