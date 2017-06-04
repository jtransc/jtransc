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
		if (settings.relooper) {
			try {
				return removeRelooper(body, types) ?: removeMachineState(body, types)
			} catch (t: Throwable) {
				t.printStackTrace()
				return removeMachineState(body, types)
			}
		} else {
			return removeMachineState(body, types)
		}
	}

	private fun removeRelooper(body: AstBody, types: AstTypes): AstBody? {
		class BasicBlock(var index: Int) {
			var node: Relooper.Node? = null
			val stms = arrayListOf<AstStm>()
			var next: BasicBlock? = null
			var condExpr: AstExpr? = null
			var ifNext: BasicBlock? = null

			//val targets by lazy { (listOf(next, ifNext) + (switchNext?.values ?: listOf())).filterNotNull() }

			override fun toString(): String = "BasicBlock($index)"
		}

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
		for (stmBox in stms) {
			val stm = stmBox.value
			when (stm) {
				is AstStm.STM_LABEL -> {
					val prev = current
					current = getBBForLabel(stm.label)
					prev.next = current
				}
				is AstStm.GOTO -> {
					val prev = current
					current = createBB()
					prev.next = getBBForLabel(stm.label)
				}
				is AstStm.IF_GOTO -> {
					val prev = current
					current = createBB()
					prev.condExpr = stm.cond.value
					prev.ifNext = getBBForLabel(stm.label)
					prev.next = current
				}
				is AstStm.SWITCH_GOTO -> {
					// Not handled switches yet!
					return null
				}
				is AstStm.RETURN, is AstStm.THROW, is AstStm.RETHROW -> {
					current.stms += stm
					val prev = current
					current = createBB()
					prev.next = null
				}
				else -> {
					current.stms += stm
				}
			}
		}

		val relooper = Relooper(types)
		for (n in bblist) {
			n.node = relooper.node(n.stms)
			//println("NODE(${n.index}): ${n.stms}")
			//if (n.next != null) println("   -> ${n.next}")
			//if (n.ifNext != null) println("   -> ${n.ifNext} [${n.condExpr}]")
		}
		for (n in bblist) {
			val next = n.next
			val ifNext = n.ifNext
			if (next != null) relooper.edge(n.node!!, next.node!!)
			if (n.condExpr != null && ifNext != null) relooper.edge(n.node!!, ifNext.node!!, n.condExpr!!)
		}

		try {
			return body.copy(
				stm = relooper.render(bblist[0].node!!)?.optimize(body.flags) ?: return null
			)
		} catch (e: RelooperException) {
			//println("RelooperException: ${e.message}")
			return null
		}
		//return AstBody(relooper.render(bblist[0].node!!) ?: return null, body.locals, body.traps)
	}

	fun removeMachineState(body: AstBody, types: AstTypes): AstBody {
		// @TODO: this should create simple blocks and do analysis like that, instead of creating a gigantic switch
		// @TODO: trying to generate whiles, ifs and so on to allow javascript be fast. See relooper paper.
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

					fun simulateGotoLabel(index: Int) = listOf(
						gotostate.setTo(index.lit),
						AstStm.CONTINUE()
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
							AstStm.WHILE(true.lit,
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
								simulateGotoLabel(handlerState).stm()
							)
						}

						listOf(
							AstStm.WHILE(true.lit,
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
