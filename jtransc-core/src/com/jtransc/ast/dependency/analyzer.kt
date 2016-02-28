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

package com.jtransc.ast.dependency

import com.jtransc.ast.*

object AstDependencyAnalyzer {
	@JvmStatic fun analyze(program: AstProgram, body: AstBody?): AstReferences {
		val types = hashSetOf<FqName>()
		val fields = hashSetOf<AstFieldRef>()
		val methods = hashSetOf<AstMethodRef>()

		fun ana(type: AstType) {
			types.addAll(type.getRefTypesFqName())
		}

		fun ana(expr: AstExpr?) {
			if (expr == null) return
			when (expr) {
				is AstExpr.CAST -> {
					ana(expr.from)
					ana(expr.to)
					ana(expr.expr)
				}
				is AstExpr.NEW -> {
					ana(expr.target)
				}
				is AstExpr.NEW_ARRAY -> {
					for (c in expr.counts) ana(c)
					ana(expr.element)
				}
				is AstExpr.ARRAY_ACCESS -> {
					ana(expr.type)
					ana(expr.array)
					ana(expr.index)
				}
				is AstExpr.ARRAY_LENGTH -> {
					ana(expr.array)
				}
				is AstExpr.BINOP -> {
					ana(expr.left)
					ana(expr.right)
				}
				is AstExpr.CALL_BASE -> {
					ana(expr.method.type)
					for (arg in expr.args) ana(arg)
					methods.add(expr.method)
					if (expr is AstExpr.CALL_INSTANCE) ana(expr.obj)
				}
				is AstExpr.CAUGHT_EXCEPTION -> {
					ana(expr.type)
				}
				is AstExpr.CLASS_CONSTANT -> {
					types.addAll(expr.type.getRefTypesFqName())
				}
				is AstExpr.INSTANCE_FIELD_ACCESS -> {
					ana(expr.expr)
					fields.add(expr.field)
				}
				is AstExpr.STATIC_FIELD_ACCESS -> {
					fields.add(expr.field)
				}
				is AstExpr.INSTANCE_OF -> {
					ana(expr.checkType)
				}
				is AstExpr.UNOP -> ana(expr.right)
				is AstExpr.THIS -> {
					ana(expr.type)
				}
				is AstExpr.LITERAL -> {
				}
				is AstExpr.LOCAL -> {
				}
				is AstExpr.PARAM -> {
					ana(expr.type)
				}
				else -> throw NotImplementedError("Not implemented $expr")
			}
		}

		fun ana(stm: AstStm?) {
			if (stm == null) return
			when (stm) {
				is AstStm.STMS -> for (s in stm.stms) ana(s)
				is AstStm.STM_EXPR -> ana(stm.expr)
				is AstStm.GOTO -> {
				}
				is AstStm.CONTINUE -> {
				}
				is AstStm.BREAK -> {
				}
				is AstStm.STM_LABEL -> {
				}
				is AstStm.IF_GOTO -> ana(stm.cond)
				is AstStm.MONITOR_ENTER -> ana(stm.expr)
				is AstStm.MONITOR_EXIT -> ana(stm.expr)
				is AstStm.SET -> ana(stm.expr)
				is AstStm.SET_ARRAY -> {
					ana(stm.expr); ana(stm.index)
				}
				is AstStm.SET_FIELD_INSTANCE -> {
					fields.add(stm.field); ana(stm.left); ana(stm.expr)
				}
				is AstStm.SET_FIELD_STATIC -> {
					fields.add(stm.field);  ana(stm.expr)
				}
				is AstStm.RETURN -> ana(stm.retval)
				is AstStm.IF -> {
					ana(stm.cond); ana(stm.strue); ana(stm.sfalse)
				}
				is AstStm.THROW -> ana(stm.value)
				is AstStm.WHILE -> {
					ana(stm.cond); ana(stm.iter)
				}
				is AstStm.TRY_CATCH -> {
					ana(stm.trystm);
					ana(stm.catch)
					/*
					for (catch in stm.catches) {
						ana(catch.first)
						ana(catch.second)
					}
					*/
				}
				is AstStm.SWITCH_GOTO -> {
					ana(stm.subject)
				}
				is AstStm.SWITCH -> {
					ana(stm.subject); ana(stm.default)
					for (catch in stm.cases) ana(catch.second)
				}
				is AstStm.SET_NEW_WITH_CONSTRUCTOR -> {
					ana(stm.target)
					ana(stm.method.type)
					for (arg in stm.args) {
						ana(arg)
					}
				}
				else -> throw NotImplementedError("Not implemented STM $stm")
			}
		}

		if (body != null) {
			for (local in body.locals) ana(local.type)

			ana(body.stm)
		}

		return AstReferences(
			program = program,
			classes = types.map { AstClassRef(it) }.toSet(),
			fields = fields.toSet(),
			methods = methods.toSet()
		)
	}
}