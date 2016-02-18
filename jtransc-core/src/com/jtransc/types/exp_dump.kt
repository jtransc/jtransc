package com.jtransc.types

import com.jtransc.ast.*
import com.jtransc.text.Indenter

//fun AstBody.dump() = dump(this)

fun dump(body: AstBody): Indenter {
	return dump(body.stm)
}

fun dump(stm: AstStm): Indenter {
	return Indenter.gen {
		when (stm) {
			is AstStm.STMS -> {
				if (stm.stms.size == 1) {
					line(dump(stm.stms.first()))
				} else {
					line("{")
					indent {
						for (s in stm.stms) line(dump(s))
					}
					line("}")
				}
			}
			is AstStm.STM_LABEL -> line(":" + stm.label.name)
			is AstStm.SET -> line(stm.local.name + " = " + dump(stm.expr) + ";")
			is AstStm.SET_FIELD_INSTANCE -> line(dump(stm.left) + "." + stm.field.name + " = " + dump(stm.expr) + ";")
			is AstStm.STM_EXPR -> line(dump(stm.expr) + ";")
			is AstStm.GOTO -> line("goto " + stm.label.name + ";")
			is AstStm.IF_GOTO -> line("if (" + dump(stm.cond) + ") goto " + stm.label.name + ";")
			is AstStm.RETURN -> line("return " + dump(stm.retval) + ";")
			else -> line("$stm")
		}
	}
}

fun dump(expr: AstExpr?): String {
	return when (expr) {
		null -> ""
		is AstExpr.BINOP -> dump(expr.left) + " " + expr.op.symbol + " " + dump(expr.right)
		is AstExpr.LITERAL -> expr.value.toString()
		is AstExpr.LOCAL -> expr.local.name
		is AstExpr.CAST -> "((" + dump(expr.to) + ")" + dump(expr.expr) + ")"
		else -> "$expr"
	}
}

fun dump(type: AstType?): String {
	return when (type) {
		null -> "null"
		is AstType.INT -> "int"
		is AstType.DOUBLE -> "double"
		is AstType.REF -> type.fqname
		else -> type.mangle()
	}
}