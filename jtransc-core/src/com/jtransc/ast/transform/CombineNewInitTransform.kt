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

import com.jtransc.ast.*
import java.util.*

object CombineNewInitTransform : AstTransform() {
	/*
	private fun transform(stms: ArrayList<AstStm>) {
		var newToLocal = hashMapOf<AstExpr.LValueExpr, Pair<Int, AstType.REF>>()

		for (n in 0 until stms.size) {
			val stm = stms[n]

			// empty NEW
			if (stm is AstStm.SET && stm.expr.value is AstExpr.NEW) {
				val setLocal = stm.local
				val newExpr = stm.expr as AstExpr.NEW
				newToLocal[setLocal] = Pair(n, newExpr.type)
			}

			// CALL <init> constructor method
			if (stm is AstStm.STM_EXPR && stm.expr.value is AstExpr.CALL_INSTANCE) {
				val callExpr = stm.expr.value as AstExpr.CALL_INSTANCE

				val callLocal = if (callExpr.obj is AstExpr.LOCAL) {
					callExpr.obj
				} else if (callExpr.obj is AstExpr.CAST && callExpr.obj.expr is AstExpr.LOCAL) {
					callExpr.obj.expr
				} else {
					null
				}

				if (callLocal != null) {
					if (callExpr.isSpecial && callExpr.method.name == "<init>") {
						if (callLocal in newToLocal) {
							val (instantiateIndex, instantiateType) = newToLocal[callLocal]!!
							if (callExpr.method.containingClass != instantiateType.name) {
								throw AssertionError("Unexpected new + <init> call!")
							}
							stms[instantiateIndex] = AstStm.NOP()
							stms[n] = AstStm.SET_NEW_WITH_CONSTRUCTOR(callLocal, instantiateType, callExpr.method, callExpr.args)
							newToLocal.remove(callLocal)
						}
					}
				}
			}
		}

		// All new must have their <init> counterpart!, so we assert that!
        // TODO Commented, but important for kotlin classes that must be initializated!!
		//assert(newToLocal.isEmpty())
		if (newToLocal.isNotEmpty()) {
			//println("WARNING (combining new+<init>): $newToLocal couldn't combine. This would make native instantiations to fail, otherwise this will work just fine.")
			//println(stms.joinToString("\n"))
		}
	}
	*/

	override fun invoke(body: AstBody): AstBody {
		/*
		if (body.stm is AstStm.STMS) {
			val stms = body.stm.stms.toCollection(arrayListOf<AstStm>())
			transform(stms)
			return AstBody(AstStm.STMS(stms.filter { it !is AstStm.NOP }), body.locals, body.traps)
		}

		return body
		*/
		return body
	}
}