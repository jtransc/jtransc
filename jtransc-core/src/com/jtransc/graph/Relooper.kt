package com.jtransc.graph

import com.jtransc.ast.*
import com.jtransc.ds.Queue
import com.jtransc.error.invalidOp
import com.jtransc.text.INDENTS
import java.util.*
import kotlin.collections.LinkedHashSet

/**
 * Converts a digraph representing the control flow graph of a method into ifs and whiles.
 * If we fail doing so because the graph is irreductible or we have a bug, we will fallback to creating
 * a state machine as we are already doing.
 *
 * Fors:
 * - Separate the graph in Strong Components
 * - Each strong component represents a loop (with potentially other loops inside)
 * - Each strong component should have a single entry and a single exit (modulo breaking/continuing other loops) in a reductible graph
 * - That entry/exit delimits the loop
 * - Inside strong components, all edges should be internal, or external referencing the beginning/end of this or other loops.
 * - Internal links to that component represents ifs, while external links represents, break or continue to specific loops
 * - Each loop/strong component should be splitted into smaller strong components after removing links to the beginning of the loop to detect inner loops, each split is recursively handled.
 *
 * Ifs:
 * - For each 'if' we have two forward edges. One to enter the if, and other to skip the if. So the conditional edge is the negation of the if.
 * - To determine the end of the if, we have to compute the common successor of all the edges
 * - If the common successor is the same as the if-skipping edge, we have a plain if, and if not, we have an if-else combination.
 * - So we have three nodes that delimits if-elses: entering the condition, skipping the condition, and the common successor.
 *
 * Switch:
 * - TODO (probably we can just create if chains and then generate a switch from it)
 *
 * Try-Catch:
 * - TODO
 *
 * Irreductible CFGs:
 * - TODO
 * - Ideas: Since we need the strong components to have a single entry and a single exit (modulo continuing/exiting other loops),
 *   we can try to create synthetic edges to enter each strong component, from a single entry point and then skip some parts.
 *   That should work with custom gotos or await/async implementations.
 */
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
			val set = LinkedHashSet<Node>()
			val queue = Queue<Node>()
			queue.queue(a)
			while (queue.hasMore) {
				val item = queue.dequeue()
				if (item !in visited) {
					set += item
					visited += item
					if (item != exit) {
						for (edge in item.dstEdges) {
							queue.queue(edge.dst)
						}
					}
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

	fun renderComponents(g: StrongComponentGraph<Node>, entry: Node, exit: Node? = null, ctx: RenderContext = RenderContext(g.graph), level: Int = 0): AstStm {
		if (level > 5) {
			//throw RelooperException("Too much nesting levels!")
			invalidOp("ERROR When Relooping $name (TOO MUCH NESTING LEVELS)")
		}
		val indent by lazy { INDENTS[level] }
		val out = arrayListOf<AstStm>()
		var node: Node? = entry
		val explored = LinkedHashSet<Node>()
		loop@ while (node != null && node != exit) {
			if (node in explored) {
				//invalidOp("Already explored : $node")
				break
			}
			explored += node
			val prevNode = node
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

				val cond = true.lit


				out += AstStm.DO_WHILE(
					loopName,
					cond,
					if (isSingleNodeLoop) {
						trace { "$indent- render single node: renderNoLoops" }
						val out2 = arrayListOf<AstStm>()
						renderNoLoops(g, out2, node, exitNode, ctx, level)
						out2.stmsWoNops
					} else {
						trace { "$indent- render multi node: renderComponents (${entryNode.index} - ${exitNode.index})" }
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

			if (node == prevNode) invalidOp("Infinite loop detected")
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
				endOfIf.cond!!.not(), // @TODO: Negate a float comparison problem with NaNs
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
		val bodyValue = this.body.value
		if (bodyValue is AstStm.STMS) {
			val stms = bodyValue.stms
			if (stms.size >= 2) {
				val last = stms[stms.size - 1].value
				val plast = stms[stms.size - 2].value
				if (last is AstStm.BREAK && plast is AstStm.IF && plast.strue.value is AstStm.CONTINUE) {
					return AstStm.DO_WHILE(name, plast.cond.value, stms.map { it.value }.slice(0 until stms.size - 2).stms.box.value)
				}
			}
		}
		return this
		// var n = 0; do { if (n++ < 10) continue; } while (false); console.log(n); // NOT WORKING: 1
		// var n = 0; do { if (n++ < 10) continue; break; } while (true); console.log(n); // WORKING: 11
		// var n = 0; do { } while (n++ < 10); console.log(n); // WORKING: 11
	}
}

class RelooperException(message: String) : RuntimeException(message)