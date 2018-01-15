package com.jtransc.ast

import com.jtransc.error.invalidOp
import com.jtransc.error.noImpl

// @TODO: Can just use AstVisitor?
open class AstTransformer {
	open fun finish() {
	}

	open fun transform(body: AstBody) {
		transform(body.stm.box)
	}

	open fun transform(stm: AstStm.Box): AstStm {
		val result = transform(stm.value)
		stm.value = result
		return result
	}

	open fun transform(expr: AstExpr.Box): AstExpr {
		val result = transform(expr.value)
		expr.value = result
		return result
	}

	open fun transformStmsBox(stms: List<AstStm.Box>) {
		for (e in stms) transform(e)
	}

	open fun transformExprsBox(exprs: List<AstExpr.Box>) {
		for (e in exprs) transform(e.value)
	}

	open fun transform(stm: AstStm): AstStm {
		return when (stm) {
			is AstStm.STMS -> transform(stm)
			is AstStm.NOP -> transform(stm)
			is AstStm.STM_EXPR -> transform(stm)
			is AstStm.SET_LOCAL -> transform(stm)
			is AstStm.SET_ARRAY -> transform(stm)
			is AstStm.SET_ARRAY_LITERALS -> transform(stm)
			is AstStm.SET_FIELD_STATIC -> transform(stm)
			is AstStm.SET_FIELD_INSTANCE -> transform(stm)
			is AstStm.IF -> transform(stm)
			is AstStm.IF_ELSE -> transform(stm)
			is AstStm.WHILE -> transform(stm)
			is AstStm.RETURN -> transform(stm)
			is AstStm.RETURN_VOID -> transform(stm)
			is AstStm.THROW -> transform(stm)
			is AstStm.RETHROW  -> transform(stm)
			is AstStm.TRY_CATCH -> transform(stm)
			is AstStm.BREAK -> transform(stm)
			is AstStm.CONTINUE -> transform(stm)
			is AstStm.SWITCH -> transform(stm)
			is AstStm.LINE -> transform(stm)
			is AstStm.STM_LABEL -> transform(stm)
			is AstStm.IF_GOTO -> transform(stm)
			is AstStm.GOTO -> transform(stm)
			is AstStm.SWITCH_GOTO -> transform(stm)
			is AstStm.MONITOR_ENTER -> transform(stm)
			is AstStm.MONITOR_EXIT -> transform(stm)
			else -> noImpl("$stm")
		}
	}

	open fun transform(expr: AstExpr): AstExpr {
		return when (expr) {
			is AstExpr.THIS -> transform(expr)
			//is AstExpr.CLASS_CONSTANT -> visit(expr)
			is AstExpr.LITERAL -> transform(expr)
			is AstExpr.LITERAL_REFNAME -> transform(expr)
			is AstExpr.INVOKE_DYNAMIC_METHOD -> transform(expr)
			is AstExpr.LOCAL -> transform(expr)
			is AstExpr.TYPED_LOCAL -> transform(expr)
			is AstExpr.PARAM -> transform(expr)
			is AstExpr.CAUGHT_EXCEPTION -> transform(expr)
			is AstExpr.BINOP -> transform(expr)
			is AstExpr.UNOP -> transform(expr)
			is AstExpr.CALL_BASE -> transform(expr)
			is AstExpr.CONCAT_STRING -> transform(expr)
			is AstExpr.ARRAY_LENGTH -> transform(expr)
			is AstExpr.ARRAY_ACCESS -> transform(expr)
			is AstExpr.FIELD_INSTANCE_ACCESS -> transform(expr)
			is AstExpr.FIELD_STATIC_ACCESS -> transform(expr)
			is AstExpr.INSTANCE_OF -> transform(expr)
			is AstExpr.CAST -> transform(expr)
			is AstExpr.CHECK_CAST -> transform(expr)
			is AstExpr.NEW_WITH_CONSTRUCTOR -> transform(expr)
			is AstExpr.NEW_ARRAY -> transform(expr)
			is AstExpr.INTARRAY_LITERAL -> transform(expr)
			is AstExpr.OBJECTARRAY_LITERAL -> transform(expr)
			is AstExpr.TERNARY -> transform(expr)
			else -> noImpl("$expr")
		}
	}

	open fun transform(expr: AstExpr.CONCAT_STRING): AstExpr {
		for (arg in expr.args) transform(arg)
		return expr
	}

	open fun transform(expr: AstExpr.CALL_BASE): AstExpr {
		transform(expr.method)
		for (arg in expr.args) transform(arg)

		when (expr) {
			is AstExpr.CALL_INSTANCE -> transform(expr)
			is AstExpr.CALL_SUPER -> transform(expr)
			is AstExpr.CALL_STATIC -> transform(expr)
			//is AstExpr.CALL_SPECIAL -> visit(expr)
			else -> noImpl("$expr")
		}

		return expr
	}

	open fun transform(ref: AstType.REF) {
	}

	open fun visit(local: AstLocal) {
	}

	open fun visit(label: AstLabel) {
	}

	open fun transform(ref: AstType) {
		for (c in ref.getRefClasses()) transform(c)
	}

	open fun transform(ref: FqName) {
		transform(AstType.REF(ref))
	}

	open fun transform(ref: AstFieldRef) {
		//visit(AstType.REF(ref))
	}

	open fun transform(ref: AstMethodRef) {
	}

	open fun transform(argument: AstArgument) {
	}

	open fun transform(stm: AstStm.STMS): AstStm {
		transformStmsBox(stm.stms)
		return stm
	}

	open fun transform(stm: AstStm.NOP): AstStm {
		return stm
	}

	open fun transform(stm: AstStm.SET_LOCAL): AstStm {
		transform(stm.local)
		transform(stm.expr)
		return stm
	}

	open fun transform(stm: AstStm.SET_ARRAY): AstStm {
		transform(stm.array)
		transform(stm.index)
		transform(stm.expr)
		return stm
	}

	open fun transform(stm: AstStm.SET_ARRAY_LITERALS): AstStm {
		transform(stm.array)
		for (v in stm.values) transform(v)
		return stm
	}

	open fun transform(stm: AstStm.SET_FIELD_STATIC): AstStm {
		transform(stm.clazz)
		transform(stm.field)
		transform(stm.expr)
		return stm
	}

	open fun transform(stm: AstStm.SET_FIELD_INSTANCE): AstStm {
		transform(stm.left)
		transform(stm.field)
		transform(stm.expr)
		return stm
	}

	open fun transform(stm: AstStm.IF): AstStm {
		transform(stm.cond)
		transform(stm.strue)
		return stm
	}

	open fun transform(stm: AstStm.IF_ELSE): AstStm {
		transform(stm.cond)
		transform(stm.strue)
		transform(stm.sfalse)
		return stm
	}

	open fun transform(stm: AstStm.WHILE): AstStm {
		transform(stm.cond)
		transform(stm.iter)
		return stm
	}

	open fun transform(stm: AstStm.RETURN): AstStm {
		transform(stm.retval)
		return stm
	}

	open fun transform(stm: AstStm.RETURN_VOID): AstStm {
		return stm
	}

	open fun transform(stm: AstStm.THROW): AstStm {
		transform(stm.exception)
		return stm
	}

	open fun transform(stm: AstStm.RETHROW): AstStm {
		return stm
	}

	open fun transform(stm: AstStm.TRY_CATCH): AstStm {
		transform(stm.trystm)
		transform(stm.catch)
		return stm
	}

	open fun transform(stm: AstStm.SWITCH): AstStm {
		transform(stm.subject)
		transform(stm.default)
		for ((value, case) in stm.cases) {
			transform(case)
		}
		return stm
	}

	open fun transform(stm: AstStm.STM_LABEL): AstStm {
		visit(stm.label)
		return stm
	}

	open fun transform(stm: AstStm.IF_GOTO): AstStm {
		visit(stm.label)
		transform(stm.cond)
		return stm
	}

	open fun transform(stm: AstStm.GOTO): AstStm {
		visit(stm.label)
		return stm
	}

	open fun transform(stm: AstStm.SWITCH_GOTO): AstStm {
		transform(stm.subject)
		visit(stm.default)
		for ((value, case) in stm.cases) {
			visit(case)
		}
		return stm
	}

	open fun transform(stm: AstStm.MONITOR_ENTER): AstStm {
		transform(stm.expr)
		return stm
	}

	open fun transform(stm: AstStm.MONITOR_EXIT): AstStm {
		transform(stm.expr)
		return stm
	}

	open fun transform(stm: AstStm.BREAK): AstStm {
		return stm
	}

	open fun transform(stm: AstStm.CONTINUE): AstStm {
		return stm
	}

	open fun transform(stm: AstStm.LINE): AstStm {
		return stm
	}

	open fun transform(stm: AstStm.STM_EXPR): AstStm {
		transform(stm.expr)
		return stm
	}

	open fun transform(expr: AstExpr.THIS): AstExpr {
		transform(expr.ref)
		return expr
	}

	open fun transform(expr: AstExpr.LITERAL): AstExpr {
		if (expr.value is AstType) {
			transform(expr.value)
		}
		return expr
	}

	open fun transform(expr: AstExpr.LITERAL_REFNAME): AstExpr {
		when (expr.value) {
			is AstType -> transform(expr.value)
			is AstMethodRef -> transform(expr.value)
			is AstFieldRef -> transform(expr.value)
			else -> invalidOp("Unknown AstExpr.LITERAL_REFNAME value type : ${expr.value?.javaClass} : ${expr.value}")
		}
		return expr
	}

	open fun transform(expr: AstExpr.INVOKE_DYNAMIC_METHOD): AstExpr {
		transform(expr.methodInInterfaceRef)
		transform(expr.methodToConvertRef)
		for (arg in expr.startArgs) transform(arg)
		return expr
	}

	open fun transform(expr: AstExpr.LOCAL): AstExpr {
		visit(expr.local)
		return expr
	}

	open fun transform(expr: AstExpr.TYPED_LOCAL): AstExpr {
		visit(expr.local)
		transform(expr.type)
		return expr
	}

	open fun transform(expr: AstExpr.PARAM): AstExpr {
		transform(expr.argument)
		return expr
	}

	open fun transform(expr: AstExpr.CAUGHT_EXCEPTION): AstExpr {
		transform(expr.type)
		return expr
	}

	open fun transform(expr: AstExpr.BINOP): AstExpr {
		transform(expr.left)
		transform(expr.right)
		return expr
	}

	open fun transform(expr: AstExpr.UNOP): AstExpr {
		transform(expr.right)
		return expr
	}


	open fun transform(expr: AstExpr.CALL_INSTANCE): AstExpr {
		transform(expr.obj)
		return expr
	}

	open fun transform(expr: AstExpr.CALL_SUPER): AstExpr {
		transform(expr.obj)
		return expr
	}

	//open fun visit(expr: AstExpr.CALL_SPECIAL) {
	//	visit(expr.obj)
	//}

	open fun transform(expr: AstExpr.CALL_STATIC): AstExpr {
		return expr
	}

	open fun transform(expr: AstExpr.ARRAY_LENGTH): AstExpr {
		transform(expr.array)

		return expr
	}
	open fun transform(expr: AstExpr.ARRAY_ACCESS): AstExpr {
		transform(expr.array)
		transform(expr.index)
		return expr
	}

	open fun transform(expr: AstExpr.FIELD_INSTANCE_ACCESS): AstExpr {
		transform(expr.field)
		transform(expr.expr)
		return expr
	}

	open fun transform(expr: AstExpr.FIELD_STATIC_ACCESS): AstExpr {
		transform(expr.field)
		return expr
	}

	open fun transform(expr: AstExpr.INSTANCE_OF): AstExpr {
		transform(expr.expr)
		transform(expr.checkType)
		return expr
	}

	open fun transform(cast: AstExpr.CAST): AstExpr {
		transform(cast.subject)
		transform(cast.type)
		return cast
	}

	open fun transform(cast: AstExpr.CHECK_CAST): AstExpr {
		transform(cast.subject)
		transform(cast.type)
		return cast
	}

	open fun transform(expr: AstExpr.NEW_WITH_CONSTRUCTOR): AstExpr {
		transform(expr.target)
		transform(expr.type)
		transformExprsBox(expr.args)
		return expr
	}

	open fun transform(expr: AstExpr.NEW_ARRAY): AstExpr {
		transform(expr.arrayType)
		transformExprsBox(expr.counts)
		return expr
	}

	open fun transform(expr: AstExpr.INTARRAY_LITERAL): AstExpr {
		transform(expr.arrayType)
		//visitExprs(expr.values)
		return expr
	}

	open fun transform(expr: AstExpr.OBJECTARRAY_LITERAL): AstExpr {
		transform(expr.arrayType)
		//visitExprs(expr.values)
		return expr
	}

	open fun transform(expr: AstExpr.TERNARY): AstExpr {
		transform(expr.cond)
		transform(expr.etrue)
		transform(expr.efalse)
		return expr
	}

	open fun transformAndFinish(body: AstBody) {
		transform(body)
		finish()
	}

	open fun transformAndFinish(box: AstStm.Box) {
		transform(box)
		finish()
	}

	open fun transformAndFinish(box: AstExpr.Box) {
		transform(box)
		finish()
	}
}
