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

package com.jtransc.ast.feature

import com.jtransc.ast.*

object GotosFeature : AstFeature() {
	override fun remove(body: AstBody): AstBody {
		// @TODO: this should create simple blocks and do analysis like that, instead of creating a gigantic switch
		// @TODO: trying to generate whiles, ifs and so on to allow javascript be fast. See relooper paper.
		var stm = body.stm
		var locals = body.locals.toCollection(arrayListOf<AstLocal>())
		val traps = body.traps.toCollection(arrayListOf<AstTrap>())

		//val gotostate = AstLocal(-1, "_gotostate", AstType.INT)
		val gotostate = AstLocal(-1, "G", AstType.INT)
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
				if (!stm.stms.any { it is AstStm.STM_LABEL }) {
					stm
				}
				// With labels/gotos
				else {
					hasLabels = true
					val stms = stm.stms
					var stateIndex = 0
					var stateStms = arrayListOf<AstStm>()
					val cases = arrayListOf<Pair<Int, AstStm>>()

					fun flush() {
						cases.add(Pair(stateIndex, AstStm.STMS(stateStms)))
						stateIndex = -1
						stateStms = arrayListOf<AstStm>()
					}

					fun simulateGotoLabel(index: Int): AstStm = AstStm.STMS(
						AstStm.SET(gotostate, AstExpr.LITERAL(index)),
						AstStm.CONTINUE()
					)

					fun simulateGotoLabel(label: AstLabel): AstStm = simulateGotoLabel(getStateFromLabel(label))

					for (s in stms) {
						if (s is AstStm.STM_LABEL) {
							val nextIndex = getStateFromLabel(s.label)
							val lastStm = stateStms.lastOrNull()
							if ((lastStm !is AstStm.CONTINUE) && (lastStm !is AstStm.BREAK) && (lastStm !is AstStm.RETURN)) {
								stateStms.add(simulateGotoLabel(s.label))
							}
							flush()
							stateIndex = nextIndex
							stateStms = arrayListOf<AstStm>()
						} else if (s is AstStm.IF_GOTO) {
							stateStms.add(AstStm.IF(
								s.cond,
								simulateGotoLabel(s.label)
							))
						} else if (s is AstStm.SWITCH_GOTO) {
							//throw NotImplementedError("Must implement switch goto ")
							stateStms.add(AstStm.SWITCH(
								s.subject,
								simulateGotoLabel(s.default),
								s.cases.map {
									Pair(it.first, simulateGotoLabel(it.second))
								}
							))
						} else if (s is AstStm.GOTO) {
							stateStms.add(simulateGotoLabel(s.label))
						} else {
							stateStms.add(s)
						}
					}

					flush()

					val plainWhile = AstStm.WHILE(AstExpr.LITERAL(true),
						AstStm.SWITCH(AstExpr.LOCAL(gotostate), AstStm.NOP(), cases)
					)

					if (traps.isEmpty()) {
						plainWhile
					} else {
						// Calculate ranges for try...catch
						val checkTraps = traps.map { trap ->
							val startState = getStateFromLabel(trap.start)
							val endState = getStateFromLabel(trap.end)
							val handlerState = getStateFromLabel(trap.handler)

							AstStm.IF(
								(gotostate.expr ge startState.lit) band (gotostate.expr le endState.lit) band (AstExpr.CAUGHT_EXCEPTION() instanceof trap.exception),
								simulateGotoLabel(handlerState)
							)
						}

						AstStm.WHILE(AstExpr.LITERAL(true),
							AstStm.TRY_CATCH(plainWhile, AstStm.STMS(
								checkTraps.stms,
								AstStm.RETHROW
							))
						)
					}
				}
			}
			else -> stm
		}

		stm = strip(stm)

		if (hasLabels) {
			locals.add(gotostate)
		}

		return AstBody(stm, locals, traps)
	}
}
