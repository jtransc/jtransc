package com.jtransc.types

import com.jtransc.ast.AstBody
import com.jtransc.ast.AstExpr
import com.jtransc.ast.AstLocal
import com.jtransc.ast.AstStm
import com.jtransc.error.noImpl
import java.util.*

fun Jimple2Expr(input: Jimple.Body): AstBody {
	val out = ArrayList<AstStm>()

	fun local(l: Jimple.Local): AstLocal {
		noImpl
	}
	fun localExpr(l: Jimple.Local): AstExpr {
		noImpl
	}

	for (i in input.items) {
		when (i) {
			is Jimple.CONST -> out.add(AstStm.SET(local(i.target), AstExpr.LITERAL(i.value)))
			//is Jimple.PARAM -> out.add(AstStm.SET(local(i.target), AstExpr.PARAM(i.index)))
			is Jimple.BINOP -> out.add(AstStm.SET(local(i.target), AstExpr.BINOP(i.target.type, localExpr(i.left), i.operator, localExpr(i.right))))
			is Jimple.RETURN -> out.add(AstStm.RETURN(localExpr(i.value)))
			is Jimple.RETURNVOID -> out.add(AstStm.RETURN(null))
			else -> noImpl
		}
	}
	return AstBody(AstStm.STMS(out), listOf(), listOf())
}