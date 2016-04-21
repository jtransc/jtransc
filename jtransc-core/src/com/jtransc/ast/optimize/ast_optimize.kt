package com.jtransc.ast.optimize

import com.jtransc.ast.AstExpr
import com.jtransc.ast.AstStm
import com.jtransc.ast.AstType
import com.jtransc.ast.AstVisitor

//const val DEBUG = false
//const val DEBUG = true

object AstOptimizer : AstVisitor() {
	override fun visit(expr: AstExpr.CAST) {
		super.visit(expr)

		// DUMMY CAST
		if (expr.expr.type == expr.to) {
			expr.box.value = expr.expr.value
		}
		// DOUBLE CAST
		else if (expr.expr.value is AstExpr.CAST) {
			val cast1 = expr
			val cast2 = expr.expr.value as AstExpr.CAST
			if ((cast1.type is AstType.REF) && (cast2.type is AstType.REF)) {
				cast1.expr.value = cast2.expr.value
				cast1.optimize()
			}
		}
	}
}

fun AstStm.Box.optimize() = this.value.optimize()
fun AstExpr.Box.optimize() = this.value.optimize()

fun AstStm.optimize() = AstOptimizer.visit(this)
fun AstExpr.optimize() = AstOptimizer.visit(this)
