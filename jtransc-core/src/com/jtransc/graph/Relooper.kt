package com.jtransc.graph

import com.jtransc.ast.*
import com.jtransc.error.invalidOp
import com.jtransc.text.INDENTS
import java.util.*

class Relooper(val types: AstTypes, val name: String = "unknown", val debug: Boolean = false) {
	class Graph
	class Node(val types: AstTypes, val index: Int, val body: List<AstStm>) {
		//var next: Node? = null
		val srcEdges = arrayListOf<Edge>()
		val dstEdges = arrayListOf<Edge>()
		val dstEdgesButNext get() = dstEdges.filter { it.cond != null }
		val possibleNextNodes: List<Node> get() = dstEdges.map { it.dst }

		val next get() = dstEdges.firstOrNull { it.cond == null }?.dst

		override fun toString(): String = "L$index: " + dump(types, body.stm()).toString().trim() + "EDGES: $dstEdges. SRC_EDGES: ${srcEdges.size}"
	}

	class Edge(val types: AstTypes, val src: Node, val dst: Node, val cond: AstExpr? = null) {
		//override fun toString(): String = "IF (${cond.dump(types)}) goto L${dst.index}; else goto L${current.next?.index};"
		override fun toString(): String = if (cond != null) "IF (${cond.dump(types)}) goto L${dst.index};" else "goto L${dst.index};"
	}

	var lastIndex = 0
	fun node(body: List<AstStm>): Node = Node(types, lastIndex++, body)
	fun node(body: AstStm): Node = Node(types, lastIndex++, listOf(body))

	fun edge(a: Node, b: Node, cond: AstExpr? = null) {
		a.dstEdges += Edge(types, a, b, cond)
		b.srcEdges += Edge(types, a, b, cond)
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
				if (node != exit) edge(node, exit)
			}
			for (edge in node.dstEdges) explore(edge.dst)
		}
		explore(entry)
		explored += exit
		return explored.toList()
	}

	inline private fun trace(msg: () -> String) {
		if (debug) println(msg())
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

		trace { "Rendering $name" }

		//if (graph.hasCycles()) {
		//	//noImpl("acyclic!")
		//	//println("cyclic!")
		//	trace { "Do not render $name" }
		//	return null
		//}

		val graph2 = graph.tarjanStronglyConnectedComponentsAlgorithm()

		val result = renderComponents(graph2, entry, null, RenderContext(), level = 0)

		println(result)
		println("render!")

		val entry2 = graph2.findComponentIndexWith(entry)

		//val inputs0 = graph2.components[0].getExternalInputs()
		val entries = graph2.components[1].getEntryPoints()
		val inputs1 = graph2.components[1].getExternalInputsEdges()
		val outputs1 = graph2.components[1].getExternalOutputsEdges()
		//val inputs2 = graph2.components[2].getExternalInputs()

		if (debug) {
			println("--")
		}

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

	class RenderContext {
		var lastId = 0
		val loopStarts = hashMapOf<Node, String>()
		val loopEnds = hashMapOf<Node, String>()
		fun allocName() = "loop${lastId++}"
	}

	companion object {
		fun AstExpr.dump() = this.dump(AstTypes(com.jtransc.gen.TargetName("js")))
		fun AstStm.dump() = this.dump(AstTypes(com.jtransc.gen.TargetName("js")))
	}

	interface Res
	data class Stm(val body: List<AstStm>) : Res {
		override fun toString(): String = body.map { it.dump() }.joinToString("")
	}

	data class Stms(val stms: List<Res>) : Res {
		override fun toString(): String = "{ ${stms.joinToString(" ")}}"
	}

	data class DoWhile(val name: String, val body: Res, val cond: AstExpr) : Res {
		override fun toString(): String = "$name: do { $body } while(${cond.dump()});"
	}

	data class If(val cond: AstExpr, val tbody: Res, val fbody: Res? = null) : Res {
		override fun toString(): String {
			return if (fbody != null) {
				"if (${cond.dump()}) { $tbody } else { $fbody }"
			} else {
				"if (${cond.dump()}) { $tbody }"
			}
		}
	}

	data class Continue(val name: String) : Res {
		override fun toString(): String = "continue $name;"
	}

	data class Break(val name: String) : Res {
		override fun toString(): String = "break $name;"
	}

	/**
	 * The process consists in:
	 * - Separate the graph in Strong Components
	 * - Each strong component represents a loop
	 * - Each strong component should have a single entry and a single exit (modulo breaking/continuing other loops) in a reductible graph
	 * - That entry/exit delimits the loop
	 * - Inside strong components, all links should be internal, or external referencing the beginning/end of this or other loops.
	 * - Internal links to that component represents ifs, while external links represents, break or continue to specific loops
	 * - Each loop/strong component should be splitted into smaller strong components after removing links to the beginning of the loop to detect inner loops
	 */
	fun renderComponents(g: StrongComponentGraph<Node>, entry: Node, exit: Node?, ctx: RenderContext, level: Int): Res {
		val indent = INDENTS[level]
		val out = arrayListOf<Res>()
		var node: Node? = entry
		loop@ while (node != null && node != exit) {
			var component = g.findComponentWith(node)
			val isMultiNodeLoop = component.isMultiNodeLoop()
			val isSingleNodeLoop = component.isSingleNodeLoop()

			// Loop
			if (isMultiNodeLoop || isSingleNodeLoop) {
				println("$indent- Detected node loop csize=${component.size} : $node")
				val outs = component.getExternalOutputsNodes()
				val outsNotInContext = outs.filter { it !in ctx.loopStarts && it !in ctx.loopEnds }
				if (outsNotInContext.size != 1) {
					println("ASSERTION FAILED! outsNotInContext.size != ${outsNotInContext.size} : $node")
					invalidOp
				}

				val entryNode = node
				val exitNode = outsNotInContext.first()

				val loopName = ctx.allocName()

				println("$indent:: ${entryNode.index} - ${exitNode.index}")
				println("$indent:: ${component}")

				ctx.loopStarts[entryNode] = loopName
				ctx.loopEnds[exitNode] = loopName

				out += DoWhile(
					loopName,
					if (isSingleNodeLoop) {
						val out2 = arrayListOf<Res>()
						renderNoLoops(g, out2, node, ctx, level)
						//Stm(node.body) // @TODO: Here we should add ifs with breaks, and then convert put the condition there if possible
						Stms(out2)
					} else {
						renderComponents(component.split(entryNode, exitNode), entryNode, exitNode, ctx, level = level + 1)
					}
					,
					true.lit
				)

				ctx.loopEnds -= exitNode
				ctx.loopStarts -= entryNode

				node = exitNode
			}
			// Not a loop
			else {
				node = renderNoLoops(g, out, node, ctx, level = level)
			}
		}
		return if (out.size == 1) out.first() else Stms(out)
	}

	fun renderNoLoops(g: StrongComponentGraph<Node>, out: ArrayList<Res>, node: Node, ctx: RenderContext, level: Int): Node? {
		val indent = INDENTS[level]
		println("$indent- Detected no loop : $node")
		when (node.dstEdges.size) {
			0, 1 -> {
				if (node.dstEdges.size == 0) {
					println("$indent- Last node")
				} else {
					println("$indent- Node continuing")
				}
				out += Stm(node.body)
				if (node.dstEdges.size == 0) return null
			}
			2 -> {
				println("$indent- Node IF (and else?)")
				val ifBody = node.next
				val endOfIf = node.dstEdgesButNext.firstOrNull() ?: invalidOp("Expected conditional!")
				val endOfIdNode = endOfIf.dst
				out += If(
					endOfIf.cond!!,
					renderComponents(g, ifBody!!, endOfIdNode, ctx, level = level + 1)
				)
				return endOfIdNode
			}
			else -> {
				//TODO()
			}
		}

		for (e in node.dstEdgesButNext) {
			val loopStart = ctx.loopStarts[e.dst]
			val loopEnd = ctx.loopEnds[e.dst]
			when {
				loopStart != null -> out += If(e.cond!!, Continue(loopStart))
				loopEnd != null -> out += If(e.cond!!, Break(loopEnd))
				else -> TODO()
			}
		}
		return node.next
	}

	fun StrongComponent<Node>.isMultiNodeLoop(): Boolean {
		return (size > 1)
	}

	fun StrongComponent<Node>.isSingleNodeLoop(): Boolean {
		return (size == 1 && nodes[0].dstEdges.any { it.dst == nodes[0] })
	}

	fun StrongComponent<Node>.isLoop(): Boolean {
		return isMultiNodeLoop() || isSingleNodeLoop()
	}

	fun StrongComponent<Node>.split(entry: Node, exit: Node): StrongComponentGraph<Node> {
		val parent = this
		val splitted = parent.graph.tarjanStronglyConnectedComponentsAlgorithm { src, dst -> dst != entry.index }
		return splitted
	}

	lateinit var processedCount: IntArray
	lateinit var scgraph: Digraph<StrongComponent<Node>>
	lateinit var lookup: AcyclicDigraphLookup<StrongComponent<Node>>

	private fun getEdge(from: Node, to: Node): Edge? {
		return from.dstEdges.firstOrNull() { it.dst == to }
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

				val edge = nodeDsts.map { getEdge(nodeSrc, it) }.filterNotNull().filter { it?.cond != null }.first()
				val cond = edge.cond!!

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

	fun StrongComponent<Relooper.Node>.getExternalInputsEdges(): List<Relooper.Edge> {
		val edges = arrayListOf<Relooper.Edge>()
		for (node in this.nodes) {
			for (srcEdge in node.srcEdges) {
				if (srcEdge.src !in this) edges += srcEdge
			}
		}
		return edges
	}

	fun StrongComponent<Relooper.Node>.getExternalOutputsEdges(): List<Relooper.Edge> {
		val edges = arrayListOf<Relooper.Edge>()
		for (node in this.nodes) {
			for (dstEdge in node.dstEdges) {
				if (dstEdge.dst !in this) edges += dstEdge
			}
		}
		return edges
	}

	fun StrongComponent<Relooper.Node>.getExternalInputsNodes(): List<Relooper.Node> {
		return this.getExternalInputsEdges().map { it.src }.distinct()
	}

	fun StrongComponent<Relooper.Node>.getExternalOutputsNodes(): List<Relooper.Node> {
		return this.getExternalOutputsEdges().map { it.dst }.distinct()
	}

	fun StrongComponent<Relooper.Node>.getEntryPoints(): List<Relooper.Node> {
		return this.getExternalInputsEdges().map { it.dst }.distinct()
	}
}

class RelooperException(message: String) : RuntimeException(message)