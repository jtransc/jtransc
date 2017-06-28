package com.jtransc.ast.feature.method

import com.jtransc.ast.*
import com.jtransc.error.invalidOp
import com.jtransc.error.noImpl

class UndeterministicParameterEvaluationFeature : AstMethodFeature() {
	override fun add(method: AstMethod, body: AstBody, settings: AstBuildSettings, types: AstTypes): AstBody {
		return body.copy(stm = MyProcess().process(body.stm))
	}

	class MyProcess {
		var lastLocalIndex: Int = 0

		fun process(stm: AstStm): AstStm = stm.processStm()

		fun AstStm.processStm() = this.stms.processStm().stms

		fun List<AstStm>.processStm(): List<AstStm> {
			val stms = this
			val out = arrayListOf<AstStm>()
			for (stm in stms) {
				when (stm) {
					is AstStm.NOP -> out += stm
					is AstStm.LINE -> out += stm
					is AstStm.RETURN_VOID -> out += stm
					is AstStm.STM_LABEL -> out += stm
					is AstStm.GOTO -> out += stm
					is AstStm.RETURN -> out += AstStm.RETURN(stm.retval.processExpr(out))
					is AstStm.STMS -> out += stm.stmsUnboxed.processStm().stms
					is AstStm.STM_EXPR -> {
						val expr = stm.expr.processExpr(out);
						val dummy = (expr is AstExpr.LITERAL && expr.value == null)
						if (!dummy) out += AstStm.STM_EXPR(expr)
					}
					is AstStm.IF -> out += AstStm.IF(stm.cond.processExpr(out), stm.strue.value.processStm())
					is AstStm.IF_ELSE -> out += AstStm.IF_ELSE(stm.cond.processExpr(out), stm.strue.value.processStm(), stm.sfalse.value.processStm())
					is AstStm.IF_GOTO -> out += AstStm.IF_GOTO(stm.label, stm.cond.processExpr(out))
					is AstStm.SWITCH -> out += AstStm.SWITCH(stm.subject.processExpr(out), stm.default.value.processStm(), stm.cases.map { it.first to it.second.value.processStm() })
					is AstStm.SWITCH_GOTO -> out += AstStm.SWITCH_GOTO(stm.subject.processExpr(out), stm.default, stm.cases)
					is AstStm.MONITOR_ENTER -> out += AstStm.MONITOR_ENTER(stm.expr.processExpr(out))
					is AstStm.MONITOR_EXIT -> out += AstStm.MONITOR_EXIT(stm.expr.processExpr(out))
					is AstStm.THROW -> out += AstStm.THROW(stm.exception.processExpr(out))
					is AstStm.WHILE -> out += AstStm.WHILE(stm.cond.processExpr(out), stm.iter.value)
					is AstStm.SET_LOCAL -> out += AstStm.SET_LOCAL(stm.local, stm.expr.processExpr(out), true)
					is AstStm.SET_ARRAY -> {
						// Like this to keep order
						val value = stm.expr.processExpr(out)
						val index = stm.index.processExpr(out)
						val array = stm.array.processExpr(out)
						out += AstStm.SET_ARRAY(array, index, value)
					}
					is AstStm.SET_ARRAY_LITERALS -> {
						out += AstStm.SET_ARRAY_LITERALS(stm.array.processExpr(out), stm.startIndex, stm.values.map { it.processExpr(out).box })
					}
					is AstStm.SET_FIELD_INSTANCE -> {
						val expr = stm.expr.processExpr(out)
						val l = stm.left.processExpr(out)
						out += AstStm.SET_FIELD_INSTANCE(stm.field, l, expr)
					}
					is AstStm.SET_FIELD_STATIC -> {
						val expr = stm.expr.processExpr(out)
						out += AstStm.SET_FIELD_STATIC(stm.field, expr)
					}
					else -> {
						//out += stm
						noImpl("UndeterministicParameterEvaluationFeature: $stm")
					}
				}
			}
			return out
		}

		fun AstExpr.Box.processExpr(stms: ArrayList<AstStm>): AstExpr = this.value.processExpr(stms)

		private fun alloc(type: AstType): AstLocal {
			val id = lastLocalIndex++
			return AstLocal(9000 + id, "ltt$id", type)
		}

		inline private fun ArrayList<AstStm>.buildLocalExpr(callback: () -> AstExpr): AstExpr.LOCAL {
			val stms = this
			val expr = callback()
			val local = alloc(expr.type)
			stms += local.setTo(expr)
			return local.expr
		}

		fun AstExpr.processExpr(stms: ArrayList<AstStm>): AstExpr {
			val expr = this
			return when (expr) {
				is AstExpr.LITERAL -> expr
				is AstExpr.LOCAL -> {
					//stms.buildLocalExpr { expr }
					expr
				}
				is AstExpr.PARAM -> expr
				is AstExpr.THIS -> expr
				is AstExpr.NEW -> expr
				is AstExpr.INTARRAY_LITERAL -> expr
				is AstExpr.NEW_ARRAY -> {
					val length = expr.counts.map { it.processExpr(stms) }
					stms.buildLocalExpr { AstExpr.NEW_ARRAY(expr.arrayType, length) }
				}
				is AstExpr.FIELD_STATIC_ACCESS -> expr
				is AstExpr.ARRAY_ACCESS -> {
					val array = expr.array.processExpr(stms)
					val index = expr.index.processExpr(stms)
					//stms.buildLocalExpr { AstExpr.ARRAY_ACCESS(array, index) }
					AstExpr.ARRAY_ACCESS(array, index)
				}
				is AstExpr.INSTANCE_OF -> {
					val l = expr.expr.processExpr(stms)
					//stms.buildLocalExpr { AstExpr.INSTANCE_OF(l, expr.checkType) }
					AstExpr.INSTANCE_OF(l, expr.checkType)
				}
				is AstExpr.ARRAY_LENGTH -> {
					AstExpr.ARRAY_LENGTH(expr.array.processExpr(stms))
				}
				is AstExpr.FIELD_INSTANCE_ACCESS -> {
					val l = expr.expr.processExpr(stms)
					//stms.buildLocalExpr { AstExpr.FIELD_INSTANCE_ACCESS(expr.field, l) }
					AstExpr.FIELD_INSTANCE_ACCESS(expr.field, l)
				}
				is AstExpr.BINOP -> {
					val l = expr.left.processExpr(stms)
					val r = expr.right.processExpr(stms)
					//stms.buildLocalExpr { AstExpr.BINOP(expr.type, l, expr.op, r) }
					AstExpr.BINOP(expr.type, l, expr.op, r)
				}
				is AstExpr.UNOP -> {
					//val r = expr.right.processExpr(stms)
					//stms.buildLocalExpr { AstExpr.UNOP(expr.op, r) }
					AstExpr.UNOP(expr.op, expr.right.processExpr(stms))
				}
				is AstExpr.CAST -> {
					expr.subject.processExpr(stms).castTo(expr.type)
					//expr.processExpr(stms)
					/*
					val subjectWithoutCasts = expr.subject.value.withoutCasts()
					if (subjectWithoutCasts is AstExpr.LITERAL || subjectWithoutCasts is AstExpr.LocalExpr) {
						expr
					} else {
						val subject = expr.subject.processExpr(stms)
						stms.buildLocalExpr { subject.castTo(expr.type) }
					}
					*/
				}
				is AstExpr.CHECK_CAST -> {
					expr.subject.processExpr(stms).checkedCastTo(expr.type)
				}
				is AstExpr.CALL_BASE -> {
					val method = expr.method
					val isSpecial = expr.isSpecial
					val args = expr.args.map { it.processExpr(stms) }
					val newExpr = when (expr) {
						is AstExpr.CALL_BASE_OBJECT -> {
							val obj = expr.obj.processExpr(stms)
							if (expr is AstExpr.CALL_SUPER) {
								AstExpr.CALL_SUPER(obj, expr.target, method, args, isSpecial = isSpecial)
							} else {
								AstExpr.CALL_INSTANCE(obj, method, args, isSpecial = isSpecial)
							}
						}
						is AstExpr.CALL_STATIC -> {
							AstExpr.CALL_STATIC(method, args, isSpecial = isSpecial)
						}
						else -> invalidOp
					}
					if (method.type.retVoid) {
						stms += AstStm.STM_EXPR(newExpr)
						AstExpr.LITERAL(null, dummy = true)
					} else {
						stms.buildLocalExpr { newExpr }
					}
				}
				is AstExpr.INVOKE_DYNAMIC_METHOD -> {
					val args = expr.startArgs.map { it.processExpr(stms).box }
					AstExpr.INVOKE_DYNAMIC_METHOD(expr.methodInInterfaceRef, expr.methodToConvertRef, expr.extraArgCount, args)
				}
				is AstExpr.NEW_WITH_CONSTRUCTOR -> {
					val args = expr.args.map { it.processExpr(stms) }
					stms.buildLocalExpr { AstExpr.NEW_WITH_CONSTRUCTOR(expr.constructor, args) }
				}
				else -> noImpl("UndeterministicParameterEvaluationFeature: $this")
			//else -> expr
			}
		}
	}
}