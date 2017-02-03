package com.jtransc.gen.common

import com.jtransc.ast.AstExpr

object PeepholeMatcher {
	fun matchOptionalCastAndStaticInvoke(e: AstExpr): AstExpr.CALL_STATIC? {
		if (e is AstExpr.CAST && e.expr.value is AstExpr.CALL_STATIC) {
			return e.expr.value as AstExpr.CALL_STATIC
		}
		if (e is AstExpr.CALL_STATIC) {
			return e
		}
		return null
	}
}