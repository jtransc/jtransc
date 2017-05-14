package com.jtransc.ast.optimize

import com.jtransc.ast.*
import com.jtransc.lang.toBool

class AstCastOptimizer : AstTransformer() {
	override fun transform(expr: AstExpr.CAST): AstExpr {
		val castTo = expr.to
		val child = expr.expr.value

		// DUMMY CAST
		if (expr.expr.type == castTo) {
			return transform(expr.expr.value)
		}

		// DOUBLE CAST
		if (child is AstExpr.CAST) {
			if (castTo.isNotPrimitive() && child.to.isNotPrimitive()) {
				return transform(child.castTo(castTo))
			}
		}

		// CAST LITERAL
		if (child is AstExpr.LITERAL) {
			val literalValue = child.value
			if (literalValue is Number) {
				return when (castTo) {
					AstType.BOOL -> literalValue.toBool().lit
					AstType.BYTE -> literalValue.toByte().lit
					AstType.SHORT -> literalValue.toShort().lit
					AstType.CHAR -> literalValue.toInt().toChar().lit
					AstType.INT -> literalValue.toInt().lit
					AstType.LONG -> literalValue.toLong().lit
					AstType.FLOAT -> literalValue.toFloat().lit
					AstType.DOUBLE -> literalValue.toDouble().lit
					else -> super.transform(expr)
				}
			}
		}

		return super.transform(expr)
	}
}