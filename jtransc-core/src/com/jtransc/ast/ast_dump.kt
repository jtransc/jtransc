package com.jtransc.ast

import com.jtransc.error.invalidOp
import com.jtransc.error.noImpl
import com.jtransc.text.Indenter
import com.jtransc.text.Indenter.Companion

//fun AstBody.dump() = dump(this)

fun AstBody.dump(types: AstTypes) = dump(types, this)

fun dump(types: AstTypes, body: AstBody): Indenter {
	return Indenter {
		for (local in body.locals) {
			line(javaDump(body.types, local.type) + " " + local.name + ";")
		}
		line(dump(types, body.stm))
	}
}

fun dump(types: AstTypes, expr: AstStm.Box?): Indenter {
	return dump(types, expr?.value)
}

fun dump(types: AstTypes, stm: AstStm?): Indenter {
	return Indenter {
		when (stm) {
			null -> {

			}
			is AstStm.STMS -> {
				if (stm.stms.size == 1) {
					line(dump(types, stm.stms.first()))
				} else {
					line("{")
					indent {
						for (s in stm.stms) line(dump(types, s))
					}
					line("}")
				}
			}
			is AstStm.STM_LABEL -> line(":" + stm.label.name)
			is AstStm.SET_LOCAL -> line(stm.local.name + " = " + dump(types, stm.expr) + ";")
			is AstStm.SET_FIELD_INSTANCE -> line(dump(types, stm.left) + "." + stm.field.name + " = " + dump(types, stm.expr) + ";")
			is AstStm.SET_ARRAY_LITERALS -> line(dump(types, stm.array) + "[${stm.startIndex}..${stm.startIndex + stm.values.size - 1}] = ${stm.values.map { dump(types, it) }};")
			is AstStm.STM_EXPR -> line(dump(types, stm.expr) + ";")
			is AstStm.IF_GOTO -> line("if (" + dump(types, stm.cond) + ") goto " + stm.label.name + ";")
			is AstStm.GOTO -> line("goto " + stm.label.name + ";")
			is AstStm.RETURN -> line("return " + dump(types, stm.retval) + ";")
			is AstStm.RETURN_VOID -> line("return;")
		//is AstStm.CALL_INSTANCE -> line("call")
			is AstStm.SET_FIELD_STATIC -> line(stm.clazz.fqname + "." + stm.field.name + " = " + dump(types, stm.expr) + ";")
			is AstStm.SET_ARRAY -> line(dump(types, stm.array) + "[" + dump(types, stm.index) + "] = " + dump(types, stm.expr) + ";")
			is AstStm.LINE -> {
				//line("LINE(${stm.line})")
			}
			is AstStm.NOP -> line("NOP(${stm.reason})")
			is AstStm.CONTINUE -> line("continue;")
			is AstStm.BREAK -> line("break;")
			is AstStm.THROW -> line("throw ${dump(types, stm.exception)};")
			is AstStm.IF -> line("if (${dump(types, stm.cond)})") { line(dump(types, stm.strue)) }
			is AstStm.IF_ELSE -> {
				line("if (${dump(types, stm.cond)})") { line(dump(types, stm.strue)) }
				line("else") { line(dump(types, stm.sfalse)) }
			}
			is AstStm.WHILE -> {
				line("while (${dump(types, stm.cond)})") {
					line(dump(types, stm.iter))
				}
			}
			is AstStm.SWITCH -> {
				line("switch (${dump(types, stm.subject)})") {
					for ((indices, case) in stm.cases) line("case ${indices.joinToString(", ")}: ${dump(types, case)}")
					line("default: ${dump(types, stm.default)}")
				}
			}
			is AstStm.SWITCH_GOTO -> {
				line("switch (${dump(types, stm.subject)})") {
					for ((indices, case) in stm.cases) line("case ${indices.joinToString(", ")}: goto $case;")
					line("default: goto ${stm.default};")
				}
			}
			is AstStm.MONITOR_ENTER -> line("MONITOR_ENTER(${dump(types, stm.expr)})")
			is AstStm.MONITOR_EXIT -> line("MONITOR_EXIT(${dump(types, stm.expr)})")
			else -> noImpl("$stm")
		}
	}
}

fun dump(types: AstTypes, expr: AstExpr.Box?): String {
	return dump(types, expr?.value)
}

fun AstExpr?.exprDump(types: AstTypes) = dump(types, this)

fun List<AstStm>.dump(types: AstTypes) = dump(types, this.stm())
fun AstStm.dump(types: AstTypes) = dump(types, this)
fun AstExpr.dump(types: AstTypes) = dump(types, this)

fun dump(types: AstTypes, expr: AstExpr?): String {
	return when (expr) {
		null -> ""
		is AstExpr.BINOP -> {
			"(" + dump(types, expr.left) + " " + expr.op.symbol + " " + dump(types, expr.right) + ")"
		}
		is AstExpr.UNOP -> "(" + expr.op.symbol + dump(types, expr.right) + ")"
		is AstExpr.LITERAL -> {
			val value = expr.value
			when (value) {
				is String -> "\"$value\""
				else -> "$value"
			}
		}
		is AstExpr.LocalExpr -> expr.name
		is AstExpr.CAUGHT_EXCEPTION -> "__expr"
		is AstExpr.ARRAY_LENGTH -> "(${dump(types, expr.array)}).length"
		is AstExpr.ARRAY_ACCESS -> dump(types, expr.array) + "[" + dump(types, expr.index) + "]"
		is AstExpr.FIELD_INSTANCE_ACCESS -> dump(types, expr.expr) + "." + expr.field.name
		is AstExpr.FIELD_STATIC_ACCESS -> "" + expr.clazzName + "." + expr.field.name
		is AstExpr.BaseCast -> "((" + javaDump(types, expr.to) + ")" + dump(types, expr.subject) + ")"
		is AstExpr.INSTANCE_OF -> "(" + dump(types, expr.expr) + " instance of " + javaDump(types, expr.checkType) + ")"
		is AstExpr.NEW -> "new " + expr.target.fqname + "()"
		is AstExpr.NEW_WITH_CONSTRUCTOR -> dump(types, AstExpr.CALL_INSTANCE(AstExpr.NEW(expr.type), expr.constructor, expr.args.map { it.value }, isSpecial = false))
		is AstExpr.TERNARY -> dump(types, expr.cond) + " ? " + dump(types, expr.etrue) + " : " + dump(types, expr.efalse)
	//is AstExpr.REF -> "REF(" + dump(expr.expr) + ")"
		is AstExpr.NEW_ARRAY -> "new " + expr.arrayType.element + "[" + expr.counts.map { dump(types, it) }.joinToString(", ") + "]"
		is AstExpr.CALL_BASE -> {
			val args = expr.args.map { dump(types, it) }
			val argsString = args.joinToString(", ");
			when (expr) {
				is AstExpr.CALL_INSTANCE -> (if (expr.isSpecial) "special." else "") + dump(types, expr.obj) + "." + expr.method.name + "($argsString)"
				is AstExpr.CALL_SUPER -> "super.${expr.method.name}($argsString)"
				is AstExpr.CALL_STATIC -> expr.clazz.fqname + "." + expr.method.name + "($argsString)"
				else -> invalidOp
			}
		}
		is AstExpr.INVOKE_DYNAMIC_METHOD -> {
			"invokeDynamic(${expr.extraArgCount}, ${expr.methodInInterfaceRef}, ${expr.methodToConvertRef})(${expr.startArgs.map { dump(types, it) }.joinToString(", ")})"
		}
		else -> noImpl("$expr")
	}
}

fun javaDump(types: AstTypes, type: AstType?): String {
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
		is AstType.ARRAY -> javaDump(types, type.element) + "[]"
		is AstType.REF -> type.fqname
		else -> type.mangleExt()
	}
}