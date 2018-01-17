package com.jtransc.graph

import com.jtransc.ast.*
import com.jtransc.ds.Queue
import com.jtransc.error.invalidOp
import com.jtransc.text.INDENTS
import com.jtransc.text.quote
import java.util.*
import kotlin.collections.ArrayList
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
	inner class Node(val index: Int, val body: ArrayList<AstStm>) {
		var exitNode = false
		var tag = ""
		val name = "L$index"
		//var next: Node? = null
		val srcEdges = arrayListOf<Edge>()
		val dstEdges = arrayListOf<Edge>()
		val dstEdgesButNext get() = dstEdges.filter { it.cond != null }
		val possibleNextNodes: List<Node> get() = dstEdges.map { it.dst }

		val nextEdge get() = dstEdges.firstOrNull { it.cond == null }
		var next: Node?
			get() = nextEdge?.dst
			set(value) {
				nextEdge?.remove()
				if (value != null) edgeTo(value)
			}

		init {
			trace { "node: ${this.name}" }
		}

		fun tryGetEdgeTo(dst: Node) = this.dstEdges.firstOrNull { it.dst == dst }

		fun edgeTo(dst: Node, cond: AstExpr? = null): Node {
			trace { "edge: ${this.name} -> ${dst.name}" }
			val src = this
			val edge = Edge(src, dst, cond)
			src.dstEdges += edge
			dst.srcEdges += edge
			return this
		}

		override fun toString(): String = "L$index: " + dump(types, body.stm()).toString().replace('\n', ' ').trim() + " EDGES: $dstEdges. SRC_EDGES: ${srcEdges.size}"
		fun isEmpty(): Boolean = body.isEmpty()
	}

	inner class Edge(val src: Node, val dst: Node, val cond: AstExpr? = null) {
		//override fun toString(): String = "IF (${cond.dump(types)}) goto L${dst.index}; else goto L${current.next?.index};"

		val condOrTrue get() = cond ?: true.lit

		fun remove() {
			src.dstEdges -= this
			dst.srcEdges -= this
		}

		override fun toString(): String = if (cond != null) "IF (${cond.dump(types)}) goto L${dst.index};" else "goto L${dst.index};"
	}

	fun Node.locateExitNode(): Node {
		val processed = hashSetOf<Node>()
		val queue = Queue<Node>()
		queue(this)
		while (queue.hasMore) {
			val node = queue.dequeue()
			if (node in processed) continue
			processed += node
			if (node.dstEdges.isEmpty()) {
				return node
			}
			for (e in node.dstEdges) queue.queue(e.dst)
		}
		invalidOp("Can't find exit node")
	}

	var lastIndex = 0
	//fun node(body: List<AstStm>): Node = Node(types, lastIndex, body.normalize(lastIndex)).apply { lastIndex++ }
	//fun node(body: AstStm): Node = Node(types, lastIndex, listOf(body).normalize(lastIndex)).apply { lastIndex++ }

	fun node(body: ArrayList<AstStm>): Node = Node(lastIndex, body).apply { lastIndex++ }
	fun node(body: AstStm): Node = node(ArrayList(listOf(body).normalize(lastIndex)))

	fun List<AstStm>.normalize(index: Int): List<AstStm> {
		val out = arrayListOf<AstStm>()
		//if (debug && index == 6) println("test")
		for (stm in this) {
			when (stm) {
				is AstStm.STMS -> out += stm.stmsUnboxed.normalize(index)
				is AstStm.NOP -> Unit
				else -> out += stm
			}
		}
		return out
	}

	fun edge(a: Node, b: Node, cond: AstExpr? = null) = a.edgeTo(b, cond)

	data class Prepare(val nodes: List<Node>, val entry: Node, val exit: Node)

	private fun Node.removeEmptyNodes(): Node {
		var nentry = this
		val processed = LinkedHashSet<Node>()
		val queue = Queue<Node>()
		queue.queue(nentry)
		loop@while (queue.hasMore) {
			val node = queue.dequeue()
			if (node in processed) continue
			processed += node

			// Combine an empty node that just links to another node
			if (node.body.isEmpty() && node.dstEdges.size == 1 && node.dstEdgesButNext.isEmpty()) {
				val dstNode = node.dstEdges.first().dst
				for (e in node.srcEdges.toList()) {
					e.remove()
					e.src.edgeTo(dstNode, e.cond)
				}
				if (nentry == node) {
					nentry = dstNode
				}
				queue.queue(dstNode)
				continue@loop
			}

			// Combine a non-empty node that references just a node and that node is just referenced by that one node
			//while (node.dstEdgesButNext.isEmpty() && node.next != null && node.next!!.srcEdges.size == 1 && node.next!!.srcEdges.first().src == node) {
			//	val next = node.next!!
			//	node.body += next.body
			//	node.next = next.next
			//	for (e in next.dstEdges.toList()) {
			//		e.remove()
			//		node.edgeTo(e.dst, e.cond)
			//	}
			//}

			for (edge in node.dstEdges) {
				queue.queue(edge.dst)
			}
		}
		return nentry
	}

	private fun Node.combineBooleanOpsEdges(): Node {
		val entry = this
		val processed = LinkedHashSet<Node>()
		val queue = Queue<Node>()
		queue.queue(entry)
		while (queue.hasMore) {
			val node = queue.dequeue()
			if (node in processed) continue
			processed += node

			// This node may be a || or a &&
			if (node.body.isEmpty() && node.srcEdges.size == 1 && node.srcEdges[0].src.next == node && node.dstEdges.size == 2) {
				val prev = node.srcEdges[0].src

				if (prev.dstEdges.size == 2) {
					val prevNext = prev.next!!; assert(prevNext == node)
					val currNext = node.next!!

					val prevCond = prev.dstEdgesButNext.first()
					val currCond = node.dstEdgesButNext.first()

					val prevCondNode = prevCond.dst
					val currCondNode = currCond.dst

					// && in the original code
					//* L0:  EDGES: [goto L1;, IF ((p0 >= p1)) goto L2;]. SRC_EDGES: 0
					//* L1: NOP(empty stm) EDGES: [goto L3;, IF ((p0 < 0)) goto L2;]. SRC_EDGES: 1
					if (prevCondNode == currCondNode) {
						prevCond.remove()
						prev.edgeTo(currCond.dst, prevCond.condOrTrue bor currCond.condOrTrue)
						prev.next = currNext
						//println("-------")
						queue(currCond.dst)
						queue(currNext)
						continue
					}
					// || in the original code
					//* L0:  EDGES: [goto L1;, IF ((p0 < p1)) goto L2;]. SRC_EDGES: 0
					//* L1: NOP(empty stm) EDGES: [IF ((p0 < 0)) goto L4;, goto L2;]. SRC_EDGES: 1
					else if (prevCondNode == currNext) {
						prevCond.remove()
						prev.edgeTo(currNext, prevCond.condOrTrue bor currCond.condOrTrue.not())
						prev.next = currCond.dst
						//println("-------")
						queue(currCond.dst)
						queue(currNext)
						continue
					}
					// None
					else {

					}
				}
			}

			for (edge in node.dstEdges) {
				queue(edge.dst)
			}
		}
		return entry
	}

	/**
	 * - Create a single artificial exit node
	 * - Creates a list of nodes for the graph
	 */
	private fun prepare(entry: Node): Prepare {
		val exit = node(arrayListOf())
		val processed = LinkedHashSet<Node>()
		exit.exitNode = true
		processed += exit
		val result = LinkedHashSet<Node>()

		val queue = Queue<Node>()
		queue.queue(entry)
		while (queue.hasMore) {
			val node = queue.dequeue()
			if (node in processed) continue
			processed += node

			result += node
			if (node.next == null) {
				node.exitNode = true
				node.edgeTo(exit)
			}
			if (node.next != null) queue(node.next!!)
			for (edge in node.dstEdges) queue(edge.dst)
		}
		result += exit
		exit.tag = "exit"
		return Prepare(result.toList(), entry, exit)
	}

	inline private fun trace(msg: () -> String) {
		if (debug) println(msg())
	}

	fun render(rentry: Node): AstStm {
		val entry = rentry
			.removeEmptyNodes()
			.combineBooleanOpsEdges()

		val gresult = prepare(entry)

		val g = graphList(gresult.nodes.map { it to it.possibleNextNodes })
		if (debug) {
			trace { "Rendering $name" }
			for (n in g.nodes) {
				trace { "* $n" }
			}
			trace { "// STRUCTURE CODE FOR TESTS START" }
			for (n in g.nodes) {
				val line = "val L${n.index} = node(\"L${n.index}\")"
				val bodyStr = n.body.filter { it !is AstStm.NOP }.dumpCollapse(types).toString(false).replace('\n', ' ').trim()
				trace { if (bodyStr.isNotEmpty()) "$line // $bodyStr" else line }
			}
			for (n in g.nodes) {
				if (n.dstEdges.size != 0) {
					var out = "L${n.index}"
					val conds = arrayListOf<String>()
					if (n.next != null) {
						out += ".edgeTo(L${n.next!!.index})"
					}
					for (e in n.dstEdgesButNext) {
						out += ".edgeTo(L${e.dst.index}, \"l${e.src.index}_l${e.dst.index}\")"
						conds += e.cond?.dump(types) ?: ""
					}
					trace {
						if (conds.isNotEmpty()) "$out // $conds" else out
					}
				}
			}
			trace { "// STRUCTURE CODE FOR TESTS END" }

			trace { "# GRAPHVIZ START - http://viz-js.com/" }
			trace { "digraph G {" }
			for (n in g.nodes) {
				val label = n.body.dumpCollapse(types).toString()
				val tag = n.tag
				trace { "L${n.index} [label = ${"$label$tag".quote()}]" }
			}
			for (n in g.nodes) {
				if (n.dstEdges.size != 0) {
					for (e in n.dstEdges) {
						val label = if (e.cond != null) {
							//"l${e.src.index}_l${e.dst.index}"
							e.cond.dump(types)
						} else {
							""
						}
						trace { "L${e.src.index} -> L${e.dst.index} [label = ${label.quote()}]" }
					}
				}
			}
			trace { "}" }
			trace { "# GRAPHVIZ END" }
		}
		//println("Relooping '$name'...")
		val result = renderComponents(null, g.tarjanStronglyConnectedComponentsAlgorithm(), gresult.entry, gresult.exit)
		//println("Relooping '$name'...OK")
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
		// @TODO: We should use the parent Strong Component to determine reachable nodes.
		fun findCommonSuccessorNotRendered(a: Node?, b: Node?, exit: Node?): Node? {
			if (a == null) return b
			if (b == null) return a
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

		fun findCommonSuccessorNotRendered(nodes: List<Node?>, exit: Node?): Node? {
			return nodes.reduce { acc, node -> findCommonSuccessorNotRendered(acc!!, node!!, exit) }
		}
	}

	companion object {
		fun AstExpr.dump() = this.dump(AstTypes(com.jtransc.gen.TargetName("js")))
		fun AstStm.dump() = this.dump(AstTypes(com.jtransc.gen.TargetName("js")))
	}

	fun renderComponents(pg: StrongComponentGraph<Node>? = null, g: StrongComponentGraph<Node>, entry: Node, exit: Node?, ctx: RenderContext = RenderContext(g.graph), level: Int = 0): AstStm {
		if (level > 6) {
			//throw RelooperException("Too much nesting levels!")
			invalidOp("ERROR When Relooping $name (TOO MUCH NESTING LEVELS)")
		}

		val indent by lazy { INDENTS[level] }
		val out = arrayListOf<AstStm>()
		var node: Node? = entry
		val locallyExplored = LinkedHashSet<Node>()

		trace { "$indent- renderComponents: start: L${entry.index}, end: L${exit?.index}, parentStrong=${pg?.components?.map { it.nodes.size }}, strong=${g.components.map { it.nodes.size }}" }

		fun List<Node>.toLString() = this.map { "L${it.index}" }.toString()

		loop@ while (node != null && node != exit) {
			if (node in locallyExplored) {
				//invalidOp("Already explored locally : $node")
				break
			}
			if (node in ctx.rendered) {
				//invalidOp("Already explored globally : $node")
			}
			trace { "$indent- Processing L${node?.index}" }
			locallyExplored += node
			val prevNode = node
			ctx.rendered += node
			val component = g.findComponentWith(node)
			val isMultiNodeLoop = component.isMultiNodeLoop()
			val isSingleNodeLoop = component.isSingleNodeLoop()

			// Loop
			if (isMultiNodeLoop || isSingleNodeLoop) {
				trace { "$indent- LOOP csize=${component.size} : $node" }
				val outs = component.getExternalOutputsNodes()
				var outsNotInContext = outs.filter { it !in ctx.loopStarts && it !in ctx.loopEnds }
				val outsNotInContext2 = outsNotInContext.filter { !it.exitNode }
				//val outsNotInContext = outs.filter { it !in ctx.loopStarts }
				if (outsNotInContext.size != 1) {
					if (outsNotInContext2.isEmpty()) {
						outsNotInContext = listOf(node.locateExitNode())
					} else {
						trace { "$indent- ASSERTION FAILED! outsNotInContext.size != 1 (${outsNotInContext.size}) : $node" }
						invalidOp("ERROR When Relooping '$name' MULTIPLE EXITS :: NODES${component.nodes.toLString()}, EXITS:${outsNotInContext.toLString()}")
					}
				}

				val entryNode = node
				val exitNode = outsNotInContext.first()

				trace { "$indent- LOOP --> entry: ${entryNode.name}, exit: ${exitNode.name}" }

				val loopName = ctx.allocName()

				//trace { "$indent:: ${entryNode.index} - ${exitNode.index}" }
				//trace { "$indent:: ${component}" }

				ctx.loopStarts[entryNode] = loopName
				ctx.loopEnds[exitNode] = loopName

				val cond = true.lit


				out += AstStm.WHILE(
					loopName,
					cond,
					if (isSingleNodeLoop) {
						trace { "$indent- render single node: renderNoLoops" }
						val out2 = arrayListOf<AstStm>()
						renderNoLoops(pg, g, out2, node, exitNode, ctx, level)
						out2.stmsWithoutNops
					} else {
						trace { "$indent- render multi node: renderComponents (${entryNode.index} - ${exitNode.index})" }
						val splitComponent = component.split(entryNode, exitNode)
						if (splitComponent == g) {
							invalidOp("Couldn't split strong component for some reason")
						}
						renderComponents(g, splitComponent, entryNode, exitNode, ctx, level = level + 1)
					}
				).optimizeWhile()

				ctx.loopEnds -= exitNode
				ctx.loopStarts -= entryNode

				node = exitNode
			}
			// Not a loop
			else {
				node = renderNoLoops(pg, g, out, node, exit, ctx, level = level)
			}

			if (node == prevNode) invalidOp("Infinite loop detected")
		}
		return out.stmsWithoutNops
	}

	val Iterable<AstStm>.stmsWithoutNopsAndLineList: List<AstStm> get() = this.filter { !it.isNopOrLine() }
	val Iterable<AstStm>.stmsWithoutNops: AstStm get() = this.stmsWithoutNopsAndLineList.stm()

	fun renderNoLoops(pg: StrongComponentGraph<Node>?, g: StrongComponentGraph<Node>, out: ArrayList<AstStm>, node: Node, exit: Node?, ctx: RenderContext, level: Int): Node? {
		val indent = INDENTS[level]
		trace { "$indent- renderNoLoops: Detected no loop : $node" }
		out += node.body.stmsWithoutNops

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

		val firstCondEdge = node.dstEdgesButNext.firstOrNull()
		val firstCondNode = firstCondEdge?.dst

		// Guard clause (either return or throw)
		if (node.dstEdgesButNext.size == 1 && node.next != null && node.next!!.exitNode) {
			out += AstStm.IF(firstCondEdge!!.cond!!.not(), node.next!!.body.stm())
			return node.dstEdgesButNext.first().dst
		}

		// Guard clause (either return or throw)
		if (node.dstEdgesButNext.size == 1 && node.next != null && firstCondNode?.exitNode == true) {
			out += AstStm.IF(firstCondEdge.cond!!, firstCondNode.body.stm())
			return node.next
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
			val common = ctx.findCommonSuccessorNotRendered(node.dstEdges.map { it.dst }, exit)
			//out += AstStm.SWITCH()

			var base = if (node.next != null) {
				renderComponents(pg, g, node.next!!, common, ctx, level = level + 1)
			} else {
				AstStm.NOP("")
			}

			for (e in node.dstEdgesButNext) {
				base = AstStm.IF_ELSE(
					e.condOrTrue,
					renderComponents(pg, g, e.dst, common, ctx, level = level + 1),
					base
				)
			}

			out += base

			return common
		}

		trace { "$indent- Node IF (and else?)" }
		val ifBody = node.next!!
		val endOfIfEdge = node.dstEdgesButNext.firstOrNull() ?: invalidOp("Expected conditional!")
		val endOfIfNode = endOfIfEdge.dst
		val common = ctx.findCommonSuccessorNotRendered(ifBody, endOfIfNode, exit = exit)
		//?: invalidOp("Not found common node for ${ifBody.name} and ${endOfIfNode.name}!")


		when (common) {
		// IF
			endOfIfNode -> out += AstStm.IF(
				endOfIfEdge.cond!!.not(), // @TODO: Negate a float comparison problem with NaNs
				renderComponents(pg, g, ifBody, endOfIfNode, ctx, level = level + 1)
			)
		// IF
			null -> {
				val ifBodyCB = getNodeContinueOrBreak(ifBody)
				val endOfIfCB = getNodeContinueOrBreak(endOfIfNode)
				when {
					ifBodyCB != null && endOfIfCB == null -> {
						out += AstStm.IF(endOfIfEdge.condOrTrue.not(), ifBodyCB)
						return endOfIfNode
					}
					ifBodyCB == null && endOfIfCB != null -> {
						TODO("ifBodyCB null!")
					}
					else -> {
						//TODO("Both null!")

						// Maybe an if-chain that was not optimized? And this will generate repeated branches!
						trace { "$indent- WARNING: Maybe an if-chain that was not optimized? And this will generate repeated branches!" }

						out += AstStm.IF_ELSE(
							endOfIfEdge.cond!!,
							renderComponents(pg, g, endOfIfNode, common, ctx, level = level + 1),
							renderComponents(pg, g, ifBody, common, ctx, level = level + 1)
						).optimizeIfElse()
					}
				}

			}
		// IF+ELSE
			else -> out += AstStm.IF_ELSE(
				endOfIfEdge.cond!!,
				renderComponents(pg, g, endOfIfNode, common, ctx, level = level + 1),
				renderComponents(pg, g, ifBody, common, ctx, level = level + 1)
			).optimizeIfElse()
		}
		return common


	}

	fun StrongComponent<Node>.isMultiNodeLoop(): Boolean = (size > 1)
	fun StrongComponent<Node>.isSingleNodeLoop(): Boolean = (size == 1 && nodes[0].dstEdges.any { it.dst == nodes[0] })
	//fun StrongComponent<Node>.isLoop(): Boolean = isMultiNodeLoop() || isSingleNodeLoop()

	fun StrongComponent<Node>.split(entry: Node, exit: Node): StrongComponentGraph<Node> {
		val parent = this
		val splitted = parent.graph.tarjanStronglyConnectedComponentsAlgorithm { src, dst -> dst != entry }
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

	fun AstStm.DO_WHILE.optimizeWhile(): AstStm {
		if (this.cond.isLiteral(true)) {
			return AstStm.WHILE(this.name, this.cond.value, this.body.value)
		} else {
			return this
		}
	}

	fun AstStm.WHILE.optimizeWhile(): AstStm {
		if (!this.cond.isLiteral(true)) return this // Can't optimize!

		val loopName = this.name
		val bodyValue = this.body.value
		@Suppress("FoldInitializerAndIfToElvis")
		if (bodyValue !is AstStm.STMS) return this

		val stms = bodyValue.stms.unboxed.stmsWithoutNopsAndLineList.map { it.box }

		val last = stms.lastOrNull()
		if (last != null) {
			val lastValue = last.value
			if (lastValue.isContinue(loopName)) {
				//lastValue.box.replaceWith(AstStm.NOP("optimized"))
				return AstStm.WHILE(loopName, cond.value, AstStm.STMS(stms.dropLast(1).unboxed, true)).optimizeWhile()
			}
		}

		val first = stms.firstOrNull()
		if (first != null) {
			val firstValue = first.value
			if (firstValue is AstStm.IF && firstValue.strue.value.isBreak(loopName)) {
				//firstValue.box.replaceWith(AstStm.NOP("optimized"))
				return AstStm.WHILE(loopName, firstValue.cond.value.not(), AstStm.STMS(stms.drop(1).unboxed, true)).optimizeWhile()
			}
		}

		if (stms.size >= 2) {
			val last = stms[stms.size - 1].value
			val plast = stms[stms.size - 2].value
			if (last is AstStm.BREAK && plast is AstStm.IF && plast.strue.value.isContinue(loopName)) {
				return AstStm.DO_WHILE(loopName, plast.cond.value, stms.map { it.value }.slice(0 until stms.size - 2).stms.box.value)
			}
		}

		return this
		// var n = 0; do { if (n++ < 10) continue; } while (false); console.log(n); // NOT WORKING: 1
		// var n = 0; do { if (n++ < 10) continue; break; } while (true); console.log(n); // WORKING: 11
		// var n = 0; do { } while (n++ < 10); console.log(n); // WORKING: 11
	}

	fun AstStm.IF_ELSE.optimizeIfElse(): AstStm {
		val cond = this.cond
		val st = this.strue.value.normalizeWithoutNopsOrLines()
		val sf = this.sfalse.value.normalizeWithoutNopsOrLines()
		if ((st is AstStm.RETURN) && (sf is AstStm.RETURN)) {
			return AstStm.RETURN(AstExpr.TERNARY(this.cond.value, st.retval.value, sf.retval.value, types))
		}
		// Guard clause!
		if ((st is AstStm.RETURN) || (st is AstStm.THROW)) {
			return listOf(
				AstStm.IF(cond.value, st),
				sf
			).stms
		}
		// Guard clause!
		if ((sf is AstStm.RETURN) || (sf is AstStm.THROW)) {
			return listOf(
				AstStm.IF(cond.value.not(), sf),
				st
			).stms
		}
		return this
	}
}

class RelooperException(message: String) : RuntimeException(message)