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
import com.jtransc.error.noImpl

// @TODO: Use generic visitor!
object AstDependencyAnalyzer {
	enum class Reason { UNKNWON, STATIC }

	class Config(
		val reason: Reason = Reason.UNKNWON,
		val methodHandler: (AstExpr.CALL_BASE, AstDependencyAnalyzerGen) -> Unit = { b, d -> }
	)

	@JvmStatic fun analyze(program: AstProgram, body: AstBody?, name: String? = null, config: Config): AstReferences {
		return AstDependencyAnalyzerGen(program, body, name, config = config).references
	}

	class AstDependencyAnalyzerGen(program: AstProgram, val body: AstBody?, val name: String? = null, val config: Config) {
		val methodHandler = config.methodHandler
		val ignoreExploring = hashSetOf<AstRef>()
		val allSortedRefs = linkedSetOf<AstRef>()
		val allSortedRefsStaticInit = linkedSetOf<AstRef>()
		val types = hashSetOf<FqName>()
		val fields = hashSetOf<AstFieldRef>()
		val methods = hashSetOf<AstMethodRef>()

		fun ignoreExploring(ref: AstRef) {
			ignoreExploring += ref
		}

		fun flow() {
			if (config.reason == Reason.STATIC) {
				//println("Conditional execution in ${body?.methodRef ?: $name}")
			}
		}

		fun ana(method: AstMethodRef) {
			if (method in ignoreExploring) return
			allSortedRefs.add(method)
			methods.add(method)
		}

		fun ana(field: AstFieldRef) {
			if (field in ignoreExploring) return
			allSortedRefs.add(field)
			fields.add(field)
		}

		fun ana(type: AstType) {
			//if (type in ignoreExploring) return
			allSortedRefs.addAll(type.getRefTypes())
			types.addAll(type.getRefTypesFqName())
		}

		fun ana(types: List<AstType>) = types.forEach { ana(it) }

		fun ana(expr: AstExpr.Box?) = ana(expr?.value)
		fun ana(stm: AstStm.Box?) = ana(stm?.value)

		fun ana(expr: AstExpr?) {
			if (expr == null) return
			when (expr) {
				is AstExpr.BaseCast -> {
					//is AstExpr.CAST -> {
					ana(expr.from)
					ana(expr.to)
					ana(expr.subject)
				}
				is AstExpr.NEW_ARRAY -> {
					for (c in expr.counts) ana(c)
					ana(expr.arrayType)
					allSortedRefsStaticInit += expr.arrayType.getRefClasses()
				}
				is AstExpr.INTARRAY_LITERAL -> {
					//for (c in expr.values) ana(c)
					ana(expr.arrayType)
				}
				is AstExpr.OBJECTARRAY_LITERAL -> {
					//for (c in expr.values) ana(c)
					ana(expr.arrayType)
					allSortedRefsStaticInit += AstType.STRING
				}
				is AstExpr.ARRAY_ACCESS -> {
					ana(expr.type)
					ana(expr.array)
					ana(expr.index)
				}
				is AstExpr.ARRAY_LENGTH -> {
					ana(expr.array)
				}
				is AstExpr.TERNARY -> {
					ana(expr.cond)
					ana(expr.etrue)
					ana(expr.efalse)
				}
				is AstExpr.BINOP -> {
					ana(expr.left)
					ana(expr.right)
				}
				is AstExpr.CALL_BASE -> {
					methodHandler(expr, this)
					ana(expr.method.type)
					for (arg in expr.args) ana(arg)
					ana(expr.method)
					if (expr is AstExpr.CALL_STATIC) ana(expr.clazz)
					if (expr is AstExpr.CALL_INSTANCE) ana(expr.obj)
					if (expr is AstExpr.CALL_SUPER) ana(expr.obj)
					allSortedRefsStaticInit += expr.method
				}
				is AstExpr.CONCAT_STRING -> {
					ana(expr.original)
				}
				is AstExpr.CAUGHT_EXCEPTION -> {
					ana(expr.type)
				}
				is AstExpr.FIELD_INSTANCE_ACCESS -> {
					ana(expr.expr)
					ana(expr.field)
				}
				is AstExpr.FIELD_STATIC_ACCESS -> {
					ana(expr.clazzName)
					ana(expr.field)
					allSortedRefsStaticInit += expr.field.containingTypeRef
				}
				is AstExpr.INSTANCE_OF -> {
					ana(expr.expr)
					ana(expr.checkType)
				}
				is AstExpr.UNOP -> ana(expr.right)
				is AstExpr.THIS -> ana(expr.type)
				is AstExpr.LITERAL -> {
					val value = expr.value
					when (value) {
						is AstType -> {
							ana(value)
							allSortedRefsStaticInit += value.getRefClasses()
						}
					//null -> Unit
					//is Void, is String -> Unit
					//is Boolean, is Byte, is Char, is Short, is Int, is Long -> Unit
					//is Float, is Double -> Unit
						is AstMethodHandle -> {
							ana(value.type)
							ana(value.methodRef)
							allSortedRefsStaticInit += value.methodRef
						}
					//else -> invalidOp("Literal: ${expr.value}")
					}
				}
				is AstExpr.LOCAL -> Unit
				is AstExpr.TYPED_LOCAL -> Unit
				is AstExpr.PARAM -> ana(expr.type)
				is AstExpr.INVOKE_DYNAMIC_METHOD -> {
					ana(expr.type)
					ana(expr.methodInInterfaceRef)
					ana(expr.methodToConvertRef)
					ana(expr.methodInInterfaceRef.allClassRefs)
					ana(expr.methodToConvertRef.allClassRefs)
					allSortedRefsStaticInit += expr.methodInInterfaceRef
					allSortedRefsStaticInit += expr.methodToConvertRef
				}
				is AstExpr.NEW_WITH_CONSTRUCTOR -> {
					ana(expr.target)
					allSortedRefsStaticInit += expr.target
					for (arg in expr.args) ana(arg)
					ana(AstExpr.CALL_STATIC(expr.constructor, expr.args.unbox, isSpecial = true))
				}
			//is AstExpr.REF -> ana(expr.expr)
				is AstExpr.LITERAL_REFNAME -> {
					ana(expr.type)
				}
				else -> noImpl("Not implemented $expr")
			}
		}

		fun ana(stm: AstStm?) {
			if (stm == null) return
			when (stm) {
				is AstStm.STMS -> for (s in stm.stms) ana(s)
				is AstStm.STM_EXPR -> ana(stm.expr)
				is AstStm.STM_LABEL -> Unit
				is AstStm.MONITOR_ENTER -> ana(stm.expr)
				is AstStm.MONITOR_EXIT -> ana(stm.expr)
				is AstStm.SET_LOCAL -> ana(stm.expr)
				is AstStm.SET_ARRAY -> {
					ana(stm.array);
					ana(stm.expr); ana(stm.index)
				}
				is AstStm.SET_ARRAY_LITERALS -> {
					ana(stm.array)
					for (v in stm.values) ana(v)
				}
				is AstStm.SET_FIELD_INSTANCE -> {
					ana(stm.field); ana(stm.left); ana(stm.expr)
				}
				is AstStm.SET_FIELD_STATIC -> {
					ana(stm.field); ana(stm.expr)
					allSortedRefsStaticInit += stm.field.containingTypeRef
				}
				is AstStm.RETURN -> ana(stm.retval)
				is AstStm.RETURN_VOID -> Unit

				is AstStm.THROW -> {
					ana(stm.exception)
				}
				is AstStm.CONTINUE -> Unit
				is AstStm.BREAK -> Unit

				is AstStm.TRY_CATCH -> {
					ana(stm.trystm);
					ana(stm.catch)
				}
				is AstStm.LINE -> Unit
				is AstStm.NOP -> Unit

				is AstStm.SWITCH_GOTO -> {
					flow()
					ana(stm.subject)
				}
				is AstStm.SWITCH -> {
					flow()
					ana(stm.subject); ana(stm.default)
					for (catch in stm.cases) ana(catch.second)
				}
				is AstStm.IF_GOTO -> {
					flow()
					ana(stm.cond)
				}
				is AstStm.GOTO -> {
					flow()
				}
				is AstStm.IF -> {
					flow()
					ana(stm.cond); ana(stm.strue);
				}
				is AstStm.IF_ELSE -> {
					flow()
					ana(stm.cond); ana(stm.strue); ana(stm.sfalse)
				}
				is AstStm.WHILE -> {
					flow()
					ana(stm.cond); ana(stm.iter)
				}

				else -> noImpl("Not implemented STM $stm")
			}
		}

		init {
			if (body != null) {
				for (local in body.locals) ana(local.type)

				ana(body.stm)

				for (trap in body.traps) ana(trap.exception)
			}
		}

		val references = AstReferences(
			program = program,
			allSortedRefs = allSortedRefs,
			allSortedRefsStaticInit = allSortedRefsStaticInit,
			classes = types.map { AstType.REF(it) }.toSet(),
			fields = fields.toSet(),
			methods = methods.toSet()
		)
	}
}
