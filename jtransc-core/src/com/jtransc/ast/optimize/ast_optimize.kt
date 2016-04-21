package com.jtransc.ast.optimize

import com.jtransc.ast.AstExpr
import com.jtransc.ast.AstStm
import com.jtransc.ast.AstType
import com.jtransc.ast.AstVisitor

//const val DEBUG = false
//const val DEBUG = true

object AstOptimizer : AstVisitor() {
	override fun visit(stm: AstStm.SET) {
		super.visit(stm)
		val expr = stm.expr.value

		if (expr is AstExpr.LOCAL) {
			// FIX: Assigning a value to itself
			if (stm.local.local == expr.local) {
				stm.box.value = AstStm.NOP()
				return
			}
		}
	}

	override fun visit(expr: AstExpr.CAST) {
		super.visit(expr)

		val castTo = expr.to
		val child = expr.expr.value

		//println("${expr.expr.type} -> ${expr.to}")

		// DUMMY CAST
		if (expr.expr.type == castTo) {
			expr.box.value = expr.expr.value
			expr.expr.optimize()
			return
		}

		// DOUBLE CAST
		if (child is AstExpr.CAST) {
			val cast1 = expr
			val cast2 = child
			if ((cast1.type is AstType.REF) && (cast2.type is AstType.REF)) {
				cast1.expr.value = cast2.expr.value
				cast1.optimize()
			}
			return
		}

		// CAST LITERAL
		if (child is AstExpr.LITERAL) {
			val literalValue = child.value
			if (literalValue is Int) {
				when (castTo) {
					AstType.BYTE -> expr.box.value = AstExpr.LITERAL(literalValue.toByte())
					AstType.SHORT -> expr.box.value = AstExpr.LITERAL(literalValue.toShort())
					//AstType.CHAR -> expr.box.value = AstExpr.LITERAL(literalValue.toChar())
				}
				return
			}
		}
	}
}

fun AstStm.Box.optimize() = this.apply { AstOptimizer.visit(this) }
fun AstExpr.Box.optimize() = this.apply { AstOptimizer.visit(this) }

fun AstStm.optimize() = this.let { this.box.optimize().value }
fun AstExpr.optimize() = this.let { this.box.optimize().value }
