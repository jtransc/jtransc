package com.jtransc.types

import com.jtransc.ast.*
import com.jtransc.error.InvalidOperationException
import com.jtransc.error.invalidOp
import com.jtransc.error.noImpl
import org.objectweb.asm.Label
import java.util.*

fun BAF.Body.toExpr() = Baf2Expr(this)

fun Baf2Expr(body: BAF.Body): AstBody {
	val stms = ArrayList<AstStm>()
	val usedLabels = mutableSetOf<AstLabel>()
	val stack = Stack<AstExpr>()
	val methodRef = body.methodRef
	val isStatic = methodRef.isStatic != false
	val containingClass = methodRef.containingClass
	val locals = LinkedHashMap<Pair<AstType, Int>, AstLocal>()

	//methodRef.allClassRefs

	var localId = 0
	fun local(type: AstType, index: Int): AstLocal {
		val pair = Pair(type, index)
		if (pair !in locals) {
			locals[pair] = AstLocal(index, "v" + localId++, type)
		}
		return locals[pair]!!
	}
	fun label(label: Label?): AstLabel {
		return AstLabel("label_$label")
	}
	fun ensureEmptyStack() {
		if (stack.isNotEmpty()) throw InvalidOperationException("Stack is not empty!")
	}
	fun saveStack(): List<AstType> {
		if (stack.size > 1) throw InvalidOperationException("@TODO: check when stack.size > 1 :: ${stack.size}")
		var index = 1000 // @TODO: hack
		val types = ArrayList<AstType>()
		while (stack.isNotEmpty()) {
			val s = stack.pop()
			stms.add(AstStm.SET(local(s.type, index), s))
			types.add(s.type)
			index++
		}
		return types
	}
	fun restoreStack(types: List<AstType>) {
		if (types.size > 1) throw InvalidOperationException("@TODO: check when stack.size > 1 :: ${stack.size}")
		var index = 1000 // @TODO: hack
		for (type in types) {
			stack.push(AstExpr.LOCAL(local(type, index)))
			index++
		}
	}

	for (i in body.items) {
		println(i)
	}
	println("-----------------")

	for (i in body.items) {
		when (i) {
			is BAF.LABEL -> {
				saveStack()
				stms.add(AstStm.STM_LABEL(label(i.label)))
			}
			is BAF.FRAME -> {
				saveStack()
				println("FRAME:" + i.localTypes + "," + i.stackTypes)
				restoreStack(i.stackTypes)
			}
			is BAF.LINE -> {
			}
			is BAF.CONST -> {
				stack.push(AstExpr.LITERAL(i.value))
			}
			is BAF.GETLOCAL -> {
				stack.push(AstExpr.LOCAL(local(i.type, i.index)))
			}
			is BAF.PUTLOCAL -> {
				val expr = stack.pop()
				val saved = saveStack()
				stms.add(AstStm.SET(local(i.type, i.index), expr))
				restoreStack(saved)
			}
			is BAF.PUTFIELD -> {
				val expr = stack.pop()
				val field = i.ref
				if (field.isStatic != false) {
					val obj = stack.pop()
					ensureEmptyStack()
					stms.add(AstStm.SET_FIELD_INSTANCE(obj, field, expr))
				} else {
					ensureEmptyStack()
					stms.add(AstStm.SET_FIELD_STATIC(field.containingTypeRef, field, expr, false))
				}
			}
			is BAF.BINOP -> {
				val r = stack.pop()
				val l = stack.pop()
				stack.push(AstExpr.BINOP(i.type, l, i.operator, r))
			}
			is BAF.UNOP -> {
				val r = stack.pop()
				stack.push(AstExpr.UNOP(i.operator, r))
			}
			is BAF.RET -> {
				val r = stack.pop()
				ensureEmptyStack()
				stms.add(AstStm.RETURN(r))
			}
			is BAF.RETVOID -> {
				ensureEmptyStack()
				stms.add(AstStm.RETURN(null))
			}
			is BAF.GOTOIF_I -> {
				val r = stack.pop()
				val l = stack.pop()
				val cond = AstExpr.BINOP(AstType.BOOL, l, i.operator, r)
				saveStack()
				stms.add(AstStm.IF_GOTO(cond, label(i.label)))
				usedLabels.add(label(i.label))
			}
			is BAF.GOTO -> {
				saveStack()
				stms.add(AstStm.GOTO(label(i.label)))
				usedLabels.add(label(i.label))
			}
			is BAF.CHECKCAST -> {
				stack.push(AstExpr.CAST(i.type, stack.pop()))
			}
			is BAF.ANEW -> {
				stack.push(AstExpr.LITERAL("anew!"))
			}
			is BAF.INVOKE -> {
				println("INVOKE: $i")
			}
			is BAF.NOP -> {
			}
			is BAF.NEWARRAY -> {
				val count = stack.pop()
				stack.push(AstExpr.LITERAL("ARRAY!"))
			}
			else -> {
				throw NotImplementedError("Not implemented: $i")
			}
		}
	}

	val stms2 = stms.filter {
		if (it is AstStm.STM_LABEL) {
			it.label in usedLabels
		} else {
			true
		}
	}

	val stms3 = if (stms2.lastOrNull() == AstStm.RETURN(null)) {
		stms2.dropLast(1)
	} else {
		stms2
	}

	return AstBody(AstStm.STMS(stms3), locals.values.toList(), listOf())
}