package com.jtransc.ast.optimize

import com.jtransc.ast.*
import com.jtransc.lang.toBool

class AstCastOptimizer : AstTransformer() {
	override fun transform(cast: AstExpr.CAST): AstExpr {
		val castTo = cast.to
		val child = cast.subject.value

		// DUMMY CAST
		if (cast.subject.type == castTo) {
			return transform(cast.subject.value)
		}

		// DOUBLE CAST
		if (child is AstExpr.CAST) {
			if (castTo is AstType.Primitive && child.to is AstType.Primitive) {
				if (child.to.canHold(castTo)) {
					return transform(child.subject).castTo(castTo)
				}
			} else if (castTo.isNotPrimitive() && child.to.isNotPrimitive()) {
				return transform(child.subject).castTo(castTo)
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
					else -> super.transform(cast)
				}
			}
		}

		return super.transform(cast)
	}
}