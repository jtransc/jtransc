package com.jtransc.graph

import com.jtransc.ast.*
import com.jtransc.ds.Queue
import com.jtransc.error.invalidOp
import com.jtransc.text.INDENTS
import java.util.*
import kotlin.collections.LinkedHashSet

class Relooper(val types: AstTypes, val name: String = "unknown", val debug: Boolean = false) {
	class Graph
	class Node(val types: AstTypes, val index: Int, val body: List<AstStm>) {
		//var next: Node? = null
		val srcEdges = arrayListOf<Edge>()
		val dstEdges = arrayListOf<Edge>()
		val dstEdgesButNext get() = dstEdges.filter { it.cond != null }
		val possibleNextNodes: List<Node> get() = dstEdges.map { it.dst }

		val next get() = dstEdges.firstOrNull { it.cond == null }?.dst

		override fun toString(): String = "L$index: " + dump(types, body.stm()).toString().replace('\n', ' ').trim() + " EDGES: $dstEdges. SRC_EDGES: ${srcEdges.size}"
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

	fun render(entry: Node): AstStm {
		val g = graphList(prepare(entry).map { it to it.possibleNextNodes })
		trace { "Rendering $name" }
		for (n in g.nodes) {
			trace { "* $n" }
		}
		return renderComponents(g.tarjanStronglyConnectedComponentsAlgorithm(), entry)
	}

	class RenderContext(val graph: Digraph<Node>) {
		var lastId = 0
		val loopStarts = hashMapOf<Node, String>()
		val loopEnds = hashMapOf<Node, String>()
		val rendered = LinkedHashSet<Node>()
		fun allocName() = "loop${lastId++}"

		// @TODO: Optimize performance! And maybe cache?
		fun getNodeSuccessorsLinkedSet(a: Node, exit: Node?, checkRendered: Boolean = false): Set<Node> {
			val visited = LinkedHashSet<Node>()
			if (!checkRendered) visited += rendered
			if (exit != null) visited += exit
			val set = LinkedHashSet<Node>()
			val queue = Queue<Node>()
			queue.queue(a)
			while (queue.hasMore) {
				val item = queue.dequeue()
				if (item in visited) continue
				visited += item
				set += item
				for (edge in item.dstEdges) {
					queue.queue(edge.dst)
				}
			}
			return set
		}

		// @TODO: Optimize performance!
		fun findCommonSuccessorNotRendered(a: Node, b: Node, exit: Node?): Node? {
			val checkRendered = true
			//val checkRendered = false
			val aSet = getNodeSuccessorsLinkedSet(a, exit, checkRendered)
			val bSet = getNodeSuccessorsLinkedSet(b, exit, checkRendered)
			for (item in bSet) {
				if (item in aSet) return item
			}
			return null
		}
	}

	companion object {
		fun AstExpr.dump() = this.dump(AstTypes(com.jtransc.gen.TargetName("js")))
		fun AstStm.dump() = this.dump(AstTypes(com.jtransc.gen.TargetName("js")))
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
	fun renderComponents(g: StrongComponentGraph<Node>, entry: Node, exit: Node? = null, ctx: RenderContext = RenderContext(g.graph), level: Int = 0): AstStm {
		if (level > 5) {
			//throw RelooperException("Too much nesting levels!")
			invalidOp("ERROR When Relooping $name (TOO MUCH NESTING LEVELS)")
		}
		val indent by lazy { INDENTS[level] }
		val out = arrayListOf<AstStm>()
		var node: Node? = entry
		loop@ while (node != null && node != exit) {
			ctx.rendered += node
			val component = g.findComponentWith(node)
			val isMultiNodeLoop = component.isMultiNodeLoop()
			val isSingleNodeLoop = component.isSingleNodeLoop()

			// Loop
			if (isMultiNodeLoop || isSingleNodeLoop) {
				trace { "$indent- Detected node loop csize=${component.size} : $node" }
				val outs = component.getExternalOutputsNodes()
				val outsNotInContext = outs.filter { it !in ctx.loopStarts && it !in ctx.loopEnds }
				if (outsNotInContext.size != 1) {
					trace { "ASSERTION FAILED! outsNotInContext.size != 1 (${outsNotInContext.size}) : $node" }
					invalidOp("ERROR When Relooping $name (ASSERTION FAILED)")
				}

				val entryNode = node
				val exitNode = outsNotInContext.first()

				val loopName = ctx.allocName()

				trace { "$indent:: ${entryNode.index} - ${exitNode.index}" }
				trace { "$indent:: ${component}" }

				ctx.loopStarts[entryNode] = loopName
				ctx.loopEnds[exitNode] = loopName

				out += AstStm.DO_WHILE(
					loopName,
					if (isSingleNodeLoop) {
						val out2 = arrayListOf<AstStm>()
						renderNoLoops(g, out2, node, exitNode, ctx, level)
						//Stm(node.body) // @TODO: Here we should add ifs with breaks, and then convert put the condition there if possible
						out2.stmsWoNops
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
				node = renderNoLoops(g, out, node, exit, ctx, level = level)
			}
		}
		return out.stmsWoNops
	}

	val Iterable<AstStm>.stmsWoNops: AstStm get() = this.toList().filter { it !is AstStm.NOP }.stm()

	fun renderNoLoops(g: StrongComponentGraph<Node>, out: ArrayList<AstStm>, node: Node, exit: Node?, ctx: RenderContext, level: Int): Node? {
		val indent = INDENTS[level]
		trace { "$indent- Detected no loop : $node" }
		out += node.body.stmsWoNops
		when (node.dstEdges.size) {
			0, 1 -> {
				if (node.dstEdges.size == 0) {
					trace { "$indent- Last node" }
				} else {
					trace { "$indent- Node continuing" }
				}
				if (node.dstEdges.size == 0) return null
			}
			2 -> {
				trace { "$indent- Node IF (and else?)" }
				val ifBody = node.next!!
				val endOfIf = node.dstEdgesButNext.firstOrNull() ?: invalidOp("Expected conditional!")
				val endOfIfNode = endOfIf.dst
				val common = ctx.findCommonSuccessorNotRendered(ifBody, endOfIfNode, exit)

				// IF
				if (common == endOfIfNode) {
					out += AstStm.IF(
						endOfIf.cond!!.not(),
						renderComponents(g, ifBody, endOfIfNode, ctx, level = level + 1)
					)
				}
				// IF+ELSE
				else {
					out += AstStm.IF_ELSE(
						endOfIf.cond!!,
						renderComponents(g, endOfIfNode, common, ctx, level = level + 1),
						renderComponents(g, ifBody, common, ctx, level = level + 1)
					)
				}
				return common
			}
			else -> {
				//TODO()
			}
		}

		for (e in node.dstEdgesButNext) {
			val loopStart = ctx.loopStarts[e.dst]
			val loopEnd = ctx.loopEnds[e.dst]
			when {
				loopStart != null -> out += AstStm.IF(e.cond!!, AstStm.CONTINUE(loopStart))
				loopEnd != null -> out += AstStm.IF(e.cond!!, AstStm.BREAK(loopEnd))
				else -> TODO()
			}
		}
		return node.next
	}

	fun StrongComponent<Node>.isMultiNodeLoop(): Boolean = (size > 1)
	fun StrongComponent<Node>.isSingleNodeLoop(): Boolean = (size == 1 && nodes[0].dstEdges.any { it.dst == nodes[0] })
	//fun StrongComponent<Node>.isLoop(): Boolean = isMultiNodeLoop() || isSingleNodeLoop()

	fun StrongComponent<Node>.split(entry: Node, exit: Node): StrongComponentGraph<Node> {
		val parent = this
		val splitted = parent.graph.tarjanStronglyConnectedComponentsAlgorithm { src, dst -> dst != entry.index }
		return splitted
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