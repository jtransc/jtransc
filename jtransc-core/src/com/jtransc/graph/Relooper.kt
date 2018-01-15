package com.jtransc.graph

import com.jtransc.ast.*
import com.jtransc.ds.Queue
import com.jtransc.error.invalidOp
import com.jtransc.text.INDENTS
import java.util.*
import kotlin.collections.LinkedHashSet

class Relooper(val types: AstTypes, val name: String = "unknown", val debug: Boolean = false) {
	class Node(val types: AstTypes, val index: Int, val body: List<AstStm>) {
		//var next: Node? = null
		val srcEdges = arrayListOf<Edge>()
		val dstEdges = arrayListOf<Edge>()
		val dstEdgesButNext get() = dstEdges.filter { it.cond != null }
		val possibleNextNodes: List<Node> get() = dstEdges.map { it.dst }

		val next get() = dstEdges.firstOrNull { it.cond == null }?.dst
		val nextEdge get() = dstEdges.firstOrNull { it.cond == null }

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
		println("Relooping '$name'...")
		val result = renderComponents(g.tarjanStronglyConnectedComponentsAlgorithm(), entry)
		println("Relooping '$name'...OK")
		return result
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
			//val checkRendered = true
			val checkRendered = false
			val aSet = getNodeSuccessorsLinkedSet(a, exit, checkRendered)
			val bSet = getNodeSuccessorsLinkedSet(b, exit, checkRendered)
			//for (item in bSet) if (item in aSet) return item
			//for (item in aSet) if (item in bSet) return item

			val aIt = aSet.iterator()
			val bIt = bSet.iterator()
			do {
				var c = 0
				if (aIt.hasNext()) {
					val item = aIt.next()
					if (item in bSet) return item
					c++
				}
				if (bIt.hasNext()) {
					val item = bIt.next()
					if (item in aSet) return item
					c++
				}
			} while (c != 0)
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
				trace { "$indent- LOOP csize=${component.size} : $node" }
				val outs = component.getExternalOutputsNodes()
				val outsNotInContext = outs.filter { it !in ctx.loopStarts && it !in ctx.loopEnds }
				//val outsNotInContext = outs.filter { it !in ctx.loopStarts }
				if (outsNotInContext.size != 1) {
					trace { "$indent- ASSERTION FAILED! outsNotInContext.size != 1 (${outsNotInContext.size}) : $node" }
					invalidOp("ERROR When Relooping $name (ASSERTION FAILED)")
				}

				val entryNode = node
				val exitNode = outsNotInContext.first()

				val loopName = ctx.allocName()

				//trace { "$indent:: ${entryNode.index} - ${exitNode.index}" }
				//trace { "$indent:: ${component}" }

				ctx.loopStarts[entryNode] = loopName
				ctx.loopEnds[exitNode] = loopName

				val optimizedWhile = isSingleNodeLoop && (node.dstEdgesButNext.size == 1)
				//val cond: AstExpr = if (optimizedWhile) {
				//	node.dstEdgesButNext.first().cond!!
				//} else {
				//	true.lit
				//}
				val cond = true.lit


				out += AstStm.DO_WHILE(
					loopName,
					cond,
					if (isSingleNodeLoop) {
						trace { "$indent- render single node: renderNoLoops" }
						//if (optimizedWhile) {
						//	node.body.stms
						//} else {
							val out2 = arrayListOf<AstStm>()
							renderNoLoops(g, out2, node, exitNode, ctx, level)
							out2.stmsWoNops
						//}
					} else {
						trace { "$indent- render multi node: renderComponents" }
						renderComponents(component.split(entryNode, exitNode), entryNode, exitNode, ctx, level = level + 1)
					}
				).optimizeDoWhile()

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
		trace { "$indent- renderNoLoops: Detected no loop : $node" }
		out += node.body.stmsWoNops

		fun getNodeContinueOrBreak(node: Relooper.Node?): AstStm? {
			val loopStart = ctx.loopStarts[node]
			val loopEnd = ctx.loopEnds[node]
			return when {
				loopStart != null -> AstStm.CONTINUE(loopStart)
				loopEnd != null -> AstStm.BREAK(loopEnd)
				else -> null
			}

		}

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

			}
			else -> {
				//TODO()
			}
		}

		var ifsAdded = 0
		for (e in node.dstEdgesButNext) {
			val breakOrContinue = getNodeContinueOrBreak(e.dst) ?: break
			out += AstStm.IF(e.cond!!, breakOrContinue)
			ifsAdded++
		}

		if (ifsAdded == node.dstEdgesButNext.size) {
			val breakOrContinue = getNodeContinueOrBreak(node.next)
			if (breakOrContinue != null) {
				out += breakOrContinue
			}
			return node.next
		}

		if (node.dstEdges.size != 2) {
			TODO()
		}

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

	fun AstStm.DO_WHILE.optimizeDoWhile(): AstStm.DO_WHILE {
		this.body
		return this
	}
}

class RelooperException(message: String) : RuntimeException(message)