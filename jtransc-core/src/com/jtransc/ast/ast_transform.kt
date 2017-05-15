package com.jtransc.ast

fun AstMethod.transformInplace(transform: BuilderBase.(AstElement) -> AstElement) {
	if (this.body != null) {
		val base = BuilderBase(this.types)
		object : AstVisitor() {
			override fun visit(stm: AstStm?) {
				super.visit(stm)
				if (stm != null) {
					val transformedStm = base.transform(stm)
					if (stm != transformedStm) {
						stm.replaceWith(transformedStm as AstStm)
					}
				}
			}

			override fun visit(expr: AstExpr?) {
				super.visit(expr)
				if (expr != null) {
					val transformedExpr = base.transform(expr)
					if (expr != transformedExpr) {
						expr.replaceWith(transformedExpr as AstExpr)
					}
				}
			}
		}.visit(this.body!!)
	}
}
