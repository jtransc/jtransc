package com.jtransc.types

import com.jtransc.ast.AstBody
import com.jtransc.ast.AstExpr
import com.jtransc.ast.AstStm
import com.jtransc.error.noImpl
import java.util.*

fun BAF.Body.toExpr() = Baf2Expr(this)

fun Baf2Expr(input: BAF.Body): AstBody {
	val out = ArrayList<AstStm>()
	val stack = Stack<AstExpr>()
	for (i in input.items) {
		when (i) {
			is BAF.CONST -> {
				AstExpr.LITERAL(i.value)
			}
			else -> noImpl
		}
	}
	return AstBody(AstStm.STMS(out), listOf(), listOf())
}