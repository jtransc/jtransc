package com.jtransc.graph

import com.jtransc.ast.*
import java.util.*

class Relooper(val types: AstTypes) {
	class Graph
	class Node(val types: AstTypes, val body: List<AstStm>) {
		var next: Node? = null
		val edges = arrayListOf<Edge>()
		val possibleNextNodes: List<Node> get() = listOf(next).filterNotNull() + edges.map { it.dst }

		override fun toString(): String = dump(types, body.stm()).toString().trim()
	}

	class Edge(val dst: Node, val cond: AstExpr) {
		override fun toString(): String = "IF ($cond) goto $dst;"
	}

	fun node(body: List<AstStm>): Node = Node(types, body)
	fun node(body: AstStm): Node = Node(types, listOf(body))

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

		scgraph = graph2
		lookup = scgraph.assertAcyclic().createCommonDescendantLookup()
		processedCount = IntArray(scgraph.size)

		return renderInternal(entry2, -1).stm()
		/*
		println(entry)
		println(entry2)
		*/

		//graph.dump()
		//println(graph)
		// @TODO: strong components
	}

	lateinit var processedCount: IntArray
	lateinit var scgraph: Digraph<StrongComponent<Node>>
	lateinit var lookup: AcyclicDigraphLookup<StrongComponent<Node>>

	private fun getEdge(from: Node, to: Node): Edge? {
		return from.edges.firstOrNull() { it.dst == to }
	}

	private fun renderInternal(node: Int, endnode: Int): List<AstStm> {
		processedCount[node]++
		if (processedCount[node] > 4) {
			//println("Processed several!")
			throw RelooperException("Node processed several times!")
		}
		if (node == endnode) return listOf()

		val stms = arrayListOf<AstStm>()
		stms += scgraph.getNode(node).nodes.flatMap { it.body }

		//node.scgraph.dump()

		val nodeNode = scgraph.getNode(node)
		val targets = scgraph.getOut(node)

		val nodeSrc = nodeNode.nodes.last()
		val nodeDsts = targets.map { scgraph.getNode(it).nodes.first() }

		when (targets.size) {
			0 -> Unit
			1 -> stms += renderInternal(targets.first(), endnode)
			2 -> {
				val common = lookup.common(targets)
				val branches = targets.map { branch -> renderInternal(branch, common) }

				val edge = nodeDsts.map { getEdge(nodeSrc, it) }.filterNotNull().first()
				val cond = edge.cond

				// IF
				if (common in targets) {
					//val type1 = targets.indexOfFirst { it != common }
					//stms += AstStm.IF(cond.not(), AstStmUtils.stms(branches[type1]))
					stms += AstStm.IF(cond.not(), branches[0].stm())
				}
				// IF-ELSE
				else {
					stms += AstStm.IF_ELSE(cond.not(), branches[0].stm(), branches[1].stm())
				}
				stms += renderInternal(common, endnode)
			}
			else -> {
				val common = lookup.common(targets)
				val branches = targets.map { branch -> renderInternal(branch, common) }

				println(branches)
				println("COMMON: $common")
				//println(outNode.next)
				//println(outNode.possibleNextNodes.size)
			}
		}
		return stms
	}
}

class RelooperException(message: String) : RuntimeException(message)