/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jtransc.ast.feature.method

import com.jtransc.ast.*
import com.jtransc.ast.optimize.optimize
import com.jtransc.graph.Relooper
import com.jtransc.graph.RelooperException

@Suppress("UNUSED_PARAMETER", "LoopToCallChain")
// @TODO: Use AstBuilder to make it more readable
class GotosFeature : AstMethodFeature() {
	override fun remove(method: AstMethod, body: AstBody, settings: AstBuildSettings, types: AstTypes): AstBody {
		//if (false) {
		if (method.relooperEnabled ?: settings.relooper) {
			//if (method.relooperEnabled ?: false) {
			try {
				return removeRelooper(method, body, settings, types) ?: removeMachineState(body, types)
			} catch (t: Throwable) {
				System.err.println("Not relooping $method because of exception!:")
				t.printStackTrace()
				return removeMachineState(body, types)
			}
		} else {
			return removeMachineState(body, types)
		}
	}

	private fun removeRelooper(method: AstMethod, body: AstBody, settings: AstBuildSettings, types: AstTypes): AstBody? {
		//return removeRelooper1(method, body, settings, types)
		return removeRelooper2(method, body, settings, types)
	}

	class BasicBlock(var index: Int) {
		lateinit var node: Relooper.Node
		var isSwitch = false
		val stms = arrayListOf<AstStm>()
		var next: BasicBlock? = null
		val edges = arrayListOf<BBEdge>()
		//var condExpr: AstExpr? = null
		//var ifNext: BasicBlock? = null

		//val targets by lazy { (listOf(next, ifNext) + (switchNext?.values ?: listOf())).filterNotNull() }

		override fun toString(): String = "BasicBlock($index)"
	}

	class BBEdge(val cond: AstExpr, val next: BasicBlock)

	private fun removeRelooper1(method: AstMethod, body: AstBody, settings: AstBuildSettings, types: AstTypes): AstBody? {

		val entryStm = body.stm as? AstStm.STMS ?: return null
		// Not relooping single statements
		if (body.traps.isNotEmpty()) return null // Not relooping functions with traps by the moment

		val stms = entryStm.stms
		val bblist = arrayListOf<BasicBlock>()
		val bbs = hashMapOf<AstLabel, BasicBlock>()
		fun createBB(): BasicBlock {
			val bb = BasicBlock(bblist.size)
			bblist += bb
			return bb
		}

		fun getBBForLabel(label: AstLabel): BasicBlock {
			return bbs.getOrPut(label) { createBB() }
		}

		val entry = createBB()
		var current = entry
		var switchId = 0
		var prev: BasicBlock? = current
		for (stmBox in stms) {
			val stm = stmBox.value

			fun setPrevNextTo(that: BasicBlock?) {
				//if (!prev.isSwitch) {
				prev?.next = that
				//}
			}

			when (stm) {
				is AstStm.STM_LABEL -> {
					current = getBBForLabel(stm.label)
					setPrevNextTo(current)
					prev = current
				}
				is AstStm.GOTO -> {
					current = createBB()
					setPrevNextTo(getBBForLabel(stm.label))
					prev = null
				}
				is AstStm.IF_GOTO -> {
					current = createBB()
					prev?.edges?.add(BBEdge(stm.cond.value, getBBForLabel(stm.label)))
					setPrevNextTo(current)
					prev = current
				}
				is AstStm.SWITCH_GOTO -> {
					return null
					current = createBB()

					//val endNode = createBB()

					val switchLocal = if (stm.subject.value is AstExpr.LOCAL) {
						val switchLocal = AstType.INT.local("switch${switchId++}")
						current.stms += switchLocal.setTo(stm.subject.value)
						switchLocal
					} else {
						stm.subject.value
					}
					current.isSwitch = true
					current.next = getBBForLabel(stm.default)
					//current.next!!.next = endNode
					for ((keys, label) in stm.cases) {
						val caseLabel = getBBForLabel(label)
						//caseLabel.next = endNode
						for (key in keys) {
							current.edges += BBEdge(switchLocal eq key.lit, caseLabel)
						}
					}
					setPrevNextTo(current)
					//prev = endNode
					//prev = current
					prev = null
				}
				is AstStm.RETURN, is AstStm.THROW, is AstStm.RETHROW -> {
					current.stms += stm
					current = createBB()
					setPrevNextTo(null)
					prev = current
				}
				else -> {
					current.stms += stm
					prev = current
				}
			}
		}

		val relooper = Relooper(types, "$method", method.relooperDebug)
		for (n in bblist) {
			n.node = relooper.node(n.stms)
			//println("NODE(${n.index}): ${n.stms}")
			//if (n.next != null) println("   -> ${n.next}")
			//if (n.ifNext != null) println("   -> ${n.ifNext} [${n.condExpr}]")
		}
		for (n in bblist) {
			val next = n.next
			if (next != null) n.node.edgeTo(next.node)
			for (edge in n.edges) {
				n.node.edgeTo(edge.next.node, edge.cond)
			}
		}

		try {
			val render = relooper.render(bblist[0].node)
			val bodyGotos = if (settings.optimize) {
				render.optimize(body.flags)
			} else {
				render
			}
			return body.copy(
				stm = bodyGotos ?: return null
			)
		} catch (e: RelooperException) {
			//println("RelooperException: ${e.message}")
			return null
		}
		//return AstBody(relooper.render(bblist[0].node!!) ?: return null, body.locals, body.traps)
	}

	private fun removeRelooper2(method: AstMethod, body: AstBody, settings: AstBuildSettings, types: AstTypes): AstBody? {
		val entryStm = body.stm as? AstStm.STMS ?: return null
		// Not relooping single statements
		if (body.traps.isNotEmpty()) return null // Not relooping functions with traps by the moment
		val stms = entryStm.stmsUnboxed
		val labelToIndex = stms.withIndex().filter { it.value is AstStm.STM_LABEL }.map { (it.value as AstStm.STM_LABEL).label to it.index }.toMap()
		val relooper = Relooper(types, "$method", method.relooperDebug)
		val tswitchLocal = AstType.INT.local("_switchId")

		fun AstLabel.index(): Int = labelToIndex[this]!!

		val nodesByIndex = hashMapOf<Int, Relooper.Node>()

		fun Relooper.Node.edgeToOrMerge(that: Relooper.Node, cond: AstExpr? = null) {
			val edge = this.tryGetEdgeTo(that)
			if (edge != null) {
				edge.remove()
				this.edgeTo(that, if (cond != null) (edge.condOrTrue bor cond) else edge.cond)
			} else {
				this.edgeTo(that, cond)
			}
		}

		fun render(index: Int): Relooper.Node {
			//println("$index: ${stms[index]}")
			if (index in nodesByIndex) return nodesByIndex[index]!!
			val out = arrayListOf<AstStm>()
			val node = relooper.node(out).apply {
				nodesByIndex[index] = this
			}
			loop@ for (i in index until stms.size) {
				val stm = stms[i]
				//if (out.isEmpty()) nodesByIndex[i] = node

				when (stm) {
					is AstStm.LINE, is AstStm.NOP -> Unit
					is AstStm.STM_LABEL -> {
						if (i != index) {
							node.edgeTo(render(i))
							break@loop
						}
					}
					is AstStm.RETURN, is AstStm.THROW -> {
						out += stm
						break@loop
					}
					is AstStm.GOTO -> {
						node.edgeTo(render(stm.label.index()))
						break@loop
					}
					is AstStm.IF_GOTO -> {
						node.edgeTo(render(i + 1))
						node.edgeTo(render(stm.label.index()), stm.cond.value)
						break@loop
					}
				//is AstStm.IF_GOTO, is AstStm.GOTO -> {
				//	for (n in i until stms.size) {
				//		val stm = stms[n]
				//		if (stm is AstStm.IF_GOTO) {
				//			node.edgeToOrMerge(render(stm.label.index()), stm.cond.value)
				//		} else if (stm is AstStm.GOTO) {
				//			node.edgeToOrMerge(render(stm.label.index()))
				//			break@loop
				//		} else {
				//			node.edgeTo(render(n))
				//			break@loop
				//		}
				//	}
				//	break@loop
				//}
					is AstStm.SWITCH_GOTO -> {
						val switchLocal = if (stm.subject.value is AstExpr.LOCAL) {
							out += tswitchLocal.setTo(stm.subject.value)
							tswitchLocal
						} else {
							stm.subject.value
						}

						node.edgeTo(render(stm.default.index()))
						for ((keys, label) in stm.cases) {
							val branch = render(label.index())
							for (key in keys) {
								node.edgeTo(branch, switchLocal eq key.lit)
							}
						}
						break@loop
					}
					else -> {
						out += stm
					}
				}
			}
			return node
		}

		try {
			val render = relooper.render(render(0))
			val bodyGotos = if (settings.optimize) render.optimize(body.flags) else render
			return body.copy(stm = bodyGotos)
		} catch (e: RelooperException) {
			println("RelooperException: ${e.message}")
			return null
		}
	}

	fun removeMachineState(body: AstBody, types: AstTypes): AstBody {
		// @TODO: this should create simple blocks and do analysis like that, instead of creating a gigantic switch
		var stm = body.stm
		//val locals = body.locals.toCollection(arrayListOf<AstLocal>())
		val traps = body.traps.toCollection(arrayListOf<AstTrap>())

		//val gotostate = AstLocal(-1, "_gotostate", AstType.INT)
		val gotostate = AstExpr.LOCAL(AstLocal(-1, "G", AstType.INT))
		var hasLabels = false

		var stateIndex = 0
		val labelsToState = hashMapOf<AstLabel, Int>()

		fun getStateFromLabel(label: AstLabel): Int {
			if (label !in labelsToState) {
				labelsToState[label] = ++stateIndex
			}
			return labelsToState[label]!!
		}

		fun strip(stm: AstStm): AstStm = when (stm) {
			is AstStm.STMS -> {
				// Without labels/gotos
				if (!stm.stms.any { it.value is AstStm.STM_LABEL }) {
					stm
				}
				// With labels/gotos
				else {
					hasLabels = true
					val stms = stm.stms
					var stateIndex2 = 0
					var stateStms = arrayListOf<AstStm>()
					val cases = arrayListOf<Pair<List<Int>, AstStm>>()

					fun flush() {
						cases.add(Pair(listOf(stateIndex2), stateStms.stm()))
						stateIndex2 = -1
						stateStms = arrayListOf<AstStm>()
					}

					fun simulateGotoLabel(index: Int, insideCatch: Boolean = false) = listOf(
						gotostate.setTo(index.lit),
						AstStm.CONTINUE(if (insideCatch) "tryLoop" else "loop")
					)

					fun simulateGotoLabel(label: AstLabel) = simulateGotoLabel(getStateFromLabel(label))

					for (ss in stms) {
						val s = ss.value
						when (s) {
							is AstStm.STM_LABEL -> {
								val nextIndex = getStateFromLabel(s.label)
								val lastStm = stateStms.lastOrNull()
								if ((lastStm !is AstStm.CONTINUE) && (lastStm !is AstStm.BREAK) && (lastStm !is AstStm.RETURN)) {
									stateStms.addAll(simulateGotoLabel(s.label))
								}
								flush()
								stateIndex2 = nextIndex
								stateStms = arrayListOf<AstStm>()
							}
							is AstStm.IF_GOTO -> {
								stateStms.add(AstStm.IF(
									s.cond.value,
									simulateGotoLabel(s.label).stm()
								))
							}
							is AstStm.GOTO -> {
								stateStms.addAll(simulateGotoLabel(s.label))
							}
							is AstStm.SWITCH_GOTO -> {
								//throw NotImplementedError("Must implement switch goto ")
								stateStms.add(AstStm.SWITCH(
									s.subject.value,
									simulateGotoLabel(s.default).stm(),
									s.cases.map {
										Pair(it.first, simulateGotoLabel(it.second).stm())
									}
								))
							}
							else -> {
								stateStms.add(s)
							}
						}
					}

					flush()

					fun extraReturn() = when (body.type.ret) {
						is AstType.VOID -> AstStm.RETURN_VOID()
						is AstType.BOOL -> AstStm.RETURN(false.lit)
						is AstType.BYTE, is AstType.SHORT, is AstType.CHAR, is AstType.INT -> AstStm.RETURN(0.lit)
						is AstType.LONG -> AstStm.RETURN(0L.lit)
						is AstType.FLOAT -> AstStm.RETURN(0f.lit)
						is AstType.DOUBLE -> AstStm.RETURN(0.0.lit)
						else -> AstStm.RETURN(null.lit)
					}

					val plainWhile =
						listOf(
							AstStm.WHILE("loop", true.lit,
								AstStm.SWITCH(gotostate, AstStm.NOP("no default"), cases)
							),
							extraReturn()
						).stm()


					if (traps.isEmpty()) {
						plainWhile
					} else {
						// Calculate ranges for try...catch
						val checkTraps = traps.map { trap ->
							val startState = getStateFromLabel(trap.start)
							val endState = getStateFromLabel(trap.end)
							val handlerState = getStateFromLabel(trap.handler)

							AstStm.IF(
								//(gotostate ge AstExpr.LITERAL(startState)) band (gotostate le AstExpr.LITERAL(endState)) band (AstExpr.CAUGHT_EXCEPTION() instanceof trap.exception),
								(gotostate ge startState.lit) band (gotostate lt endState.lit) band (AstExpr.CAUGHT_EXCEPTION() instanceof trap.exception),
								simulateGotoLabel(handlerState, insideCatch = true).stm()
							)
						}

						listOf(
							AstStm.WHILE("tryLoop", true.lit,
								AstStm.TRY_CATCH(plainWhile, stms(
									checkTraps.stms,
									AstStm.RETHROW()
								))
							),
							extraReturn()
						).stm()
					}
				}
			}
			else -> stm
		}

		stm = strip(stm)

		//if (hasLabels) locals.add(gotostate.local)

		return body.copy(types = types, stm = stm, traps = traps)
	}

}
