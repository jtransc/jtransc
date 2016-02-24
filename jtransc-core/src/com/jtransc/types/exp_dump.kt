package com.jtransc.types

import com.jtransc.ast.*
import com.jtransc.error.invalidOp
import com.jtransc.text.Indenter

//fun AstBody.dump() = dump(this)

fun dump(body: AstBody): Indenter {
	return Indenter.gen {
		for (local in body.locals) {
			line(javaDump(local.type) + " " + local.name + ";")
		}
		line(dump(body.stm))
	}
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
			//is AstStm.CALL_INSTANCE -> line("call")
			is AstStm.SET_FIELD_STATIC -> line(stm.clazz.fqname + "." + stm.field.name + " = " + dump(stm.expr) + ";")
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
		is AstExpr.CAST -> "((" + javaDump(expr.to) + ")" + dump(expr.expr) + ")"
		is AstExpr.CALL_BASE -> {
			val args = expr.args.map { dump(it) }
			val argsString = args.joinToString(", ");
			when (expr) {
				is AstExpr.CALL_INSTANCE -> dump(expr.obj) + "." + expr.method.name + "($argsString)"
				is AstExpr.CALL_SUPER -> "super($argsString)"
				is AstExpr.CALL_STATIC -> expr.clazz.fqname + "." + expr.method.name + "($argsString)"
				else -> invalidOp
			}
		}
		else -> "$expr"
	}
}

fun javaDump(type: AstType?): String {
	return when (type) {
		null -> "null"
		is AstType.VOID -> "void"
		is AstType.BYTE -> "byte"
		is AstType.SHORT -> "short"
		is AstType.CHAR -> "char"
		is AstType.INT -> "int"
		is AstType.LONG -> "long"
		is AstType.FLOAT -> "float"
		is AstType.DOUBLE -> "double"
		is AstType.ARRAY -> javaDump(type.element) + "[]"
		is AstType.REF -> type.fqname
		else -> type.mangle()
	}
}