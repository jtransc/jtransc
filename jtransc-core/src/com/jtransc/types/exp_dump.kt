package com.jtransc.types

import com.jtransc.ast.*
import com.jtransc.error.invalidOp
import com.jtransc.error.noImpl
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

fun dump(expr: AstStm.Box?): Indenter {
	return dump(expr?.value)
}


fun dump(stm: AstStm?): Indenter {
	return Indenter.gen {
		when (stm) {
			null -> {

			}
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
			is AstStm.SET_LOCAL -> line(stm.local.name + " = " + dump(stm.expr) + ";")
			is AstStm.SET_FIELD_INSTANCE -> line(dump(stm.left) + "." + stm.field.name + " = " + dump(stm.expr) + ";")
			is AstStm.STM_EXPR -> line(dump(stm.expr) + ";")
			is AstStm.IF_GOTO -> line("if (" + dump(stm.cond) + ") goto " + stm.label.name + ";")
			is AstStm.GOTO -> line("goto " + stm.label.name + ";")
			is AstStm.RETURN -> line("return " + dump(stm.retval) + ";")
			is AstStm.RETURN_VOID -> line("return;")
			//is AstStm.CALL_INSTANCE -> line("call")
			is AstStm.SET_FIELD_STATIC -> line(stm.clazz.fqname + "." + stm.field.name + " = " + dump(stm.expr) + ";")
			is AstStm.SET_ARRAY -> line(dump(stm.array) + "[" + dump(stm.index) + "] = " + dump(stm.expr) + ";")
			is AstStm.LINE -> {
				//line("LINE(${stm.line})")
			}
			is AstStm.NOP -> line("NOP(${stm.reason})")
			is AstStm.CONTINUE -> line("continue;")
			is AstStm.BREAK -> line("break;")
			is AstStm.THROW -> line("throw ${dump(stm.value)};")
			is AstStm.IF -> line("if (${dump(stm.cond)})") { line(dump(stm.strue)) }
			is AstStm.IF_ELSE -> {
				line("if (${dump(stm.cond)})") { line(dump(stm.strue)) }
				line("else") { line(dump(stm.sfalse)) }
			}
			is AstStm.WHILE -> {
				line("while (${dump(stm.cond)})") {
					line(dump(stm.iter))
				}
			}
			is AstStm.SWITCH -> {
				line("switch (${dump(stm.subject)})") {
					for ((index, case) in stm.cases) line("case $index: ${dump(case)}")
					line("default: ${dump(stm.default)}")
				}
			}
			else -> noImpl("$stm")
		}
	}
}

fun dump(expr: AstExpr.Box?): String {
	return dump(expr?.value)
}

fun AstExpr?.exprDump() = dump(this)

fun dump(expr: AstExpr?): String {
	return when (expr) {
		null -> ""
		is AstExpr.BINOP -> "(" + dump(expr.left) + " " + expr.op.symbol + " " + dump(expr.right) + ")"
		is AstExpr.UNOP -> "(" + expr.op.symbol + dump(expr.right) + ")"
		is AstExpr.LITERAL -> {
			val value = expr.value
			when (value) {
				is String -> "\"$value\""
				else -> "$value"
			}
		}
		is AstExpr.LocalExpr -> expr.name
		is AstExpr.CAUGHT_EXCEPTION -> "__expr"
		is AstExpr.ARRAY_LENGTH -> "(${dump(expr.array)}).length"
		is AstExpr.ARRAY_ACCESS -> dump(expr.array) + "[" + dump(expr.index) + "]"
		is AstExpr.INSTANCE_FIELD_ACCESS -> dump(expr.expr) + "." + expr.field.name
		is AstExpr.STATIC_FIELD_ACCESS -> "" + expr.clazzName + "." + expr.field.name
		is AstExpr.CAST -> "((" + javaDump(expr.to) + ")" + dump(expr.expr) + ")"
		is AstExpr.NEW -> "new " + expr.target.fqname + "()"
		is AstExpr.TERNARY -> dump(expr.cond) + " ? " + dump(expr.etrue) + " : " + dump(expr.efalse)
		//is AstExpr.REF -> "REF(" + dump(expr.expr) + ")"
		is AstExpr.NEW_ARRAY -> "new " + expr.arrayType.element + "[" + expr.counts.map { dump(it) }.joinToString(", ") + "]"
		is AstExpr.CALL_BASE -> {
			val args = expr.args.map { dump(it) }
			val argsString = args.joinToString(", ");
			when (expr) {
				is AstExpr.CALL_INSTANCE -> (if (expr.isSpecial) "special." else "") + dump(expr.obj) + "." + expr.method.name + "($argsString)"
				is AstExpr.CALL_SUPER -> "super.${expr.method.name}($argsString)"
				is AstExpr.CALL_STATIC -> expr.clazz.fqname + "." + expr.method.name + "($argsString)"
				else -> invalidOp
			}
		}
		else -> noImpl("$expr")
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