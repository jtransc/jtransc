package com.jtransc.ast

import com.jtransc.error.invalidOp
import com.jtransc.error.noImpl

open class AstVisitor {
	open fun visit(clazz: AstClass) {
		for (field in clazz.fields) visit(field)
		for (method in clazz.methods) visit(method)
	}

	open fun visit(field: AstField) {
	}

	open fun visit(method: AstMethod) {
		val body = method.body
		if (body != null) visit(body)
	}

	open fun visit(body: AstBody?) {
		if (body == null) return
		visit(body.stm)
	}

	open fun visitStms(stms: List<AstStm>) {
		for (e in stms) visit(e)
	}

	open fun visitExprs(exprs: List<AstExpr>) {
		for (e in exprs) visit(e)
	}

	open fun visitStmsBox(stms: List<AstStm.Box>?) {
		if (stms != null) for (e in stms) visit(e)
	}

	open fun visitExprsBox(exprs: List<AstExpr.Box>?) {
		if (exprs != null) for (e in exprs) visit(e.value)
	}

	open fun visit(stm: AstStm.Box?) {
		visit(stm?.value)
	}

	open fun visit(expr: AstExpr.Box?) {
		visit(expr?.value)
	}

	open fun visit(stm: AstStm?) {
		when (stm) {
			null -> Unit
			is AstStm.STMS -> visit(stm)
			is AstStm.NOP -> visit(stm)
			is AstStm.STM_EXPR -> visit(stm)
			is AstStm.SET_LOCAL -> visit(stm)
			is AstStm.SET_ARRAY -> visit(stm)
			is AstStm.SET_ARRAY_LITERALS -> visit(stm)
			is AstStm.SET_FIELD_STATIC -> visit(stm)
			is AstStm.SET_FIELD_INSTANCE -> visit(stm)
			is AstStm.SET_NEW_WITH_CONSTRUCTOR -> visit(stm)
			is AstStm.IF -> visit(stm)
			is AstStm.IF_ELSE -> visit(stm)
			is AstStm.WHILE -> visit(stm)
			is AstStm.RETURN -> visit(stm)
			is AstStm.RETURN_VOID -> visit(stm)
			is AstStm.THROW -> visit(stm)
			is AstStm.RETHROW  -> visit(stm)
			is AstStm.TRY_CATCH -> visit(stm)
			is AstStm.BREAK -> visit(stm)
			is AstStm.CONTINUE -> visit(stm)
			is AstStm.SWITCH -> visit(stm)
			is AstStm.LINE -> visit(stm)
			is AstStm.STM_LABEL -> visit(stm)
			is AstStm.IF_GOTO -> visit(stm)
			is AstStm.GOTO -> visit(stm)
			is AstStm.SWITCH_GOTO -> visit(stm)
			is AstStm.MONITOR_ENTER -> visit(stm)
			is AstStm.MONITOR_EXIT -> visit(stm)
			else -> noImpl("$stm")
		}
	}

	open fun visit(expr: AstExpr?) {
		when (expr) {
			null -> Unit
			is AstExpr.THIS -> visit(expr)
			//is AstExpr.CLASS_CONSTANT -> visit(expr)
			is AstExpr.LITERAL -> visit(expr)
			is AstExpr.LITERAL_REFNAME -> visit(expr)
			is AstExpr.INVOKE_DYNAMIC_METHOD -> visit(expr)
			is AstExpr.LOCAL -> visit(expr)
			is AstExpr.TYPED_LOCAL -> visit(expr)
			is AstExpr.PARAM -> visit(expr)
			is AstExpr.CAUGHT_EXCEPTION -> visit(expr)
			is AstExpr.BINOP -> visit(expr)
			is AstExpr.UNOP -> visit(expr)
			is AstExpr.CALL_BASE -> visit(expr)
			is AstExpr.ARRAY_LENGTH -> visit(expr)
			is AstExpr.ARRAY_ACCESS -> visit(expr)
			is AstExpr.FIELD_INSTANCE_ACCESS -> visit(expr)
			is AstExpr.FIELD_STATIC_ACCESS -> visit(expr)
			is AstExpr.INSTANCE_OF -> visit(expr)
			is AstExpr.CAST -> visit(expr)
			is AstExpr.CHECK_CAST -> visit(expr)
			is AstExpr.NEW -> visit(expr)
			is AstExpr.NEW_WITH_CONSTRUCTOR -> visit(expr)
			is AstExpr.NEW_ARRAY -> visit(expr)
			is AstExpr.INTARRAY_LITERAL -> visit(expr)
			is AstExpr.STRINGARRAY_LITERAL -> visit(expr)
			is AstExpr.TERNARY -> visit(expr)
			else -> noImpl("$expr")
		}
	}

	open fun visit(expr: AstExpr.CALL_BASE) {
		visit(expr.method)
		for (arg in expr.args) visit(arg)

		when (expr) {
			is AstExpr.CALL_INSTANCE -> visit(expr)
			is AstExpr.CALL_SUPER -> visit(expr)
			is AstExpr.CALL_STATIC -> visit(expr)
			//is AstExpr.CALL_SPECIAL -> visit(expr)
			else -> noImpl("$expr")
		}
	}

	open fun visit(ref: AstType.REF) {
	}

	open fun visit(local: AstLocal) {
	}

	open fun visit(label: AstLabel) {
	}

	open fun visit(ref: AstType) {
		for (c in ref.getRefClasses()) visit(c)
	}

	open fun visit(ref: FqName) {
		visit(AstType.REF(ref))
	}

	open fun visit(ref: AstFieldRef) {
		//visit(AstType.REF(ref))
	}

	open fun visit(ref: AstMethodRef) {
	}

	open fun visit(argument: AstArgument) {
	}

	open fun visit(stm: AstStm.STMS) {
		visitStmsBox(stm.stms)
	}

	open fun visit(stm: AstStm.NOP) {
	}

	open fun visit(stm: AstStm.SET_LOCAL) {
		visit(stm.local)
		visit(stm.expr)
	}

	open fun visit(stm: AstStm.SET_ARRAY) {
		visit(stm.array)
		visit(stm.index)
		visit(stm.expr)
	}

	open fun visit(stm: AstStm.SET_ARRAY_LITERALS) {
		visit(stm.array)
		for (v in stm.values) visit(v)
	}

	open fun visit(stm: AstStm.SET_FIELD_STATIC) {
		visit(stm.clazz)
		visit(stm.field)
		visit(stm.expr)
	}

	open fun visit(stm: AstStm.SET_FIELD_INSTANCE) {
		visit(stm.left)
		visit(stm.field)
		visit(stm.expr)
	}

	open fun visit(stm: AstStm.IF) {
		visit(stm.cond)
		visit(stm.strue)
	}

	open fun visit(stm: AstStm.IF_ELSE) {
		visit(stm.cond)
		visit(stm.strue)
		visit(stm.sfalse)
	}

	open fun visit(stm: AstStm.WHILE) {
		visit(stm.cond)
		visit(stm.iter)
	}

	open fun visit(stm: AstStm.RETURN) {
		visit(stm.retval)
	}

	open fun visit(stm: AstStm.RETURN_VOID) {
	}

	open fun visit(stm: AstStm.THROW) {
		visit(stm.exception)
	}

	open fun visit(stm: AstStm.RETHROW) {
	}

	open fun visit(stm: AstStm.TRY_CATCH) {
		visit(stm.trystm)
		visit(stm.catch)
	}

	open fun visit(stm: AstStm.SWITCH) {
		visit(stm.subject)
		visit(stm.default)
		for ((value, case) in stm.cases) {
			visit(case)
		}
	}

	open fun visit(stm: AstStm.STM_LABEL) {
		visit(stm.label)
	}

	open fun visit(stm: AstStm.IF_GOTO) {
		visit(stm.label)
		visit(stm.cond)
	}

	open fun visit(stm: AstStm.GOTO) {
		visit(stm.label)
	}

	open fun visit(stm: AstStm.SWITCH_GOTO) {
		visit(stm.subject)
		visit(stm.default)
		for ((value, case) in stm.cases) {
			visit(case)
		}
	}

	open fun visit(stm: AstStm.MONITOR_ENTER) {
		visit(stm.expr)
	}

	open fun visit(stm: AstStm.MONITOR_EXIT) {
		visit(stm.expr)
	}

	open fun visit(stm: AstStm.SET_NEW_WITH_CONSTRUCTOR) {
		visit(stm.local)
		visit(stm.method)
		visit(stm.target)
		visitExprsBox(stm.args)
	}

	open fun visit(stm: AstStm.BREAK) {
	}

	open fun visit(stm: AstStm.CONTINUE) {
	}

	open fun visit(stm: AstStm.LINE) {
	}

	open fun visit(stm: AstStm.STM_EXPR) {
		visit(stm.expr)
	}

	open fun visit(expr: AstExpr.THIS) {
		visit(expr.ref)
	}

	open fun visit(expr: AstExpr.LITERAL) {
		if (expr.value is AstType) {
			visit(expr.value)
		}
	}

	open fun visit(expr: AstExpr.LITERAL_REFNAME) {
		when (expr.value) {
			is AstType -> visit(expr.value)
			is AstMethodRef -> visit(expr.value)
			is AstFieldRef -> visit(expr.value)
			else -> invalidOp("Unknown AstExpr.LITERAL_REFNAME value type : ${expr.value?.javaClass} : ${expr.value}")
		}
	}

	open fun visit(expr: AstExpr.INVOKE_DYNAMIC_METHOD) {
		visit(expr.methodInInterfaceRef)
		visit(expr.methodToConvertRef)
		for (arg in expr.startArgs) visit(arg)
	}

	open fun visit(expr: AstExpr.LOCAL) {
		visit(expr.local)
	}

	open fun visit(expr: AstExpr.TYPED_LOCAL) {
		visit(expr.local)
		visit(expr.type)
	}

	open fun visit(expr: AstExpr.PARAM) {
		visit(expr.argument)
	}

	open fun visit(expr: AstExpr.CAUGHT_EXCEPTION) {
		visit(expr.type)
	}

	open fun visit(expr: AstExpr.BINOP) {
		visit(expr.left)
		visit(expr.right)
	}

	open fun visit(expr: AstExpr.UNOP) {
		visit(expr.right)
	}


	open fun visit(expr: AstExpr.CALL_INSTANCE) {
		visit(expr.obj)
	}

	open fun visit(expr: AstExpr.CALL_SUPER) {
		visit(expr.obj)
	}

	//open fun visit(expr: AstExpr.CALL_SPECIAL) {
	//	visit(expr.obj)
	//}

	open fun visit(expr: AstExpr.CALL_STATIC) {
	}

	open fun visit(expr: AstExpr.ARRAY_LENGTH) {
		visit(expr.array)

	}
	open fun visit(expr: AstExpr.ARRAY_ACCESS) {
		visit(expr.array)
		visit(expr.index)
	}

	open fun visit(expr: AstExpr.FIELD_INSTANCE_ACCESS) {
		visit(expr.field)
		visit(expr.expr)
	}

	open fun visit(expr: AstExpr.FIELD_STATIC_ACCESS) {
		visit(expr.field)
	}

	open fun visit(expr: AstExpr.INSTANCE_OF) {
		visit(expr.expr)
		visit(expr.checkType)
	}

	open fun visit(expr: AstExpr.CAST) {
		visit(expr.subject)
		visit(expr.type)
	}

	open fun visit(expr: AstExpr.CHECK_CAST) {
		visit(expr.subject)
		visit(expr.type)
	}

	open fun visit(expr: AstExpr.NEW) {
		visit(expr.target)
		visit(expr.type)
	}

	open fun visit(expr: AstExpr.NEW_WITH_CONSTRUCTOR) {
		visit(expr.target)
		visit(expr.type)
		visitExprsBox(expr.args)
	}

	open fun visit(expr: AstExpr.NEW_ARRAY) {
		visit(expr.arrayType)
		visitExprsBox(expr.counts)
	}

	open fun visit(expr: AstExpr.INTARRAY_LITERAL) {
		visit(expr.arrayType)
		//visitExprs(expr.values)
	}

	open fun visit(expr: AstExpr.STRINGARRAY_LITERAL) {
		visit(expr.arrayType)
		//visitExprs(expr.values)
	}

	open fun visit(expr: AstExpr.TERNARY) {
		visit(expr.cond)
		visit(expr.etrue)
		visit(expr.efalse)
	}
}

fun AstBody.visit(visitor: AstVisitor) {
	visitor.visit(this)
}
