package com.jtransc.ast.optimize

import com.jtransc.ast.AstExpr
import com.jtransc.ast.AstStm
import com.jtransc.ast.AstType

const val DEBUG = false

fun AstStm.Box.optimize() = this.value.optimize()
fun AstExpr.Box.optimize() = this.value.optimize()

fun AstStm.optimize() {
	if (DEBUG) println("optimizing stm: $this")
	when (this) {
		is AstStm.LINE -> Unit
		is AstStm.NOP -> Unit
		is AstStm.STMS -> this.stms.forEach { it.optimize() }
		is AstStm.STM_EXPR -> {
			this.expr.optimize()
		}
		is AstStm.SET -> {
			this.expr.optimize()
		}
		is AstStm.RETURN -> {
			this.retval.optimize()
		}
		is AstStm.IF_GOTO -> {
			this.cond.optimize()
		}
		is AstStm.GOTO -> Unit
		is AstStm.RETURN_VOID -> Unit
	//else -> noImpl("Not implemented optimization for stm $this")
	}
}

fun AstExpr.optimize() {
	if (DEBUG) println("optimizing expr: $this")
	when (this) {
		is AstExpr.CAST -> {
			// DOUBLE CAST
			if (this.expr.value is AstExpr.CAST) {
				val cast1 = this
				val cast2 = this.expr.value as AstExpr.CAST
				if ((cast1.type is AstType.REF) && (cast2.type is AstType.REF)) {
					cast1.expr.value = cast2.expr.value
					cast1.optimize()
				}
			}
		}
		is AstExpr.CALL_STATIC -> {
			for (arg in this.args) {
				arg.value.optimize()
			}
		}
	//else -> noImpl("Not implemented optimization for expr $this")
	}
}
