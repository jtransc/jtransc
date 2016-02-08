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

package com.jtransc.ast.transform

import com.jtransc.ast.AstBody
import com.jtransc.ast.AstTransform

object RemoveTransitiveLocalsTransform : AstTransform() {
	override fun invoke(body: AstBody): AstBody {
		/*
		var locals = body.locals

		data class LocalInfo(val local: AstLocal) {
			var lastTarget: AstExpr.LValueExpr? = null
			var setCount: Int = 0
		}

		fun getTransitiveLocals(stm: AstStm): Map<AstLocal, LocalInfo> {
			val localInfo = hashMapOf<AstLocal, LocalInfo>()

			fun process(stm: AstStm): AstStm {
				when (stm) {
					is AstStm.STMS -> {
						return AstStm.STMS(stm.stms.map { process(it) })
					}
					is AstStm.SET -> {
						val setToExpr = stm.expr
						if (setToExpr is AstExpr.LValueExpr) {
							if (stm.local !in localInfo) {
								localInfo[stm.local] = LocalInfo(stm.local)
							}

							// Expand transitives
							val info = localInfo[stm.local]!!
							info.setCount++

							if (setToExpr is AstExpr.LOCAL) {
								localInfo[stm.local]!!.lastTarget = localInfo[setToExpr.local]?.lastTarget ?: setToExpr
							} else {
								localInfo[stm.local]!!.lastTarget = setToExpr
							}
						}
					}
					else -> {

					}
				}
				return stm
			}

			process(stm)
			return localInfo.filter { it.value.setCount == 1 }
		}

		fun replaceTransitiveLocals(stm: AstStm, localInfos: Map<AstLocal, LocalInfo>): AstStm {
			println("--------------------")
			for (i in localInfos.values) {
				println("REMOEV: ${i.local.name} and replace with ${i.lastTarget}")
			}
			return stm
		}

		fun removeTransitiveLocals(locals:List<AstLocal>, localInfos: Map<AstLocal, LocalInfo>): List<AstLocal> {
			return locals.filter { it in localInfos }
		}

		val localInfos = getTransitiveLocals(body.stm)
		if (localInfos.size > 0) {
			val finalStm = replaceTransitiveLocals(body.stm, localInfos)
			val finalLocals = removeTransitiveLocals(body.locals, localInfos)
			return AstBody(finalStm, finalLocals)
		} else {
			return body
		}
		*/

		// It should ensure that fields set are not set after it, it would lead to an invalid behaviour!
		return body
	}
}