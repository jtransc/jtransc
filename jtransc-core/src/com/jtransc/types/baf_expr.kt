package com.jtransc.types

import com.jtransc.ast.*
import com.jtransc.error.InvalidOperationException
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
		if (stack.isNotEmpty()) {
			println(stack)
			throw InvalidOperationException("Stack is not empty!")
		}
	}

	fun saveStack(): List<AstType> {
		//if (stack.size > 1) throw InvalidOperationException("@TODO: check when stack.size > 1 :: ${stack.size}")
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
		//if (types.size > 1) throw InvalidOperationException("@TODO: check when stack.size > 1 :: ${stack.size}")
		var index = 1000 // @TODO: hack
		for (type in types) {
			stack.push(AstExpr.LOCAL(local(type, index)))
			index++
		}
	}

	fun addStm(stm: AstStm) {
		val savedStack = saveStack()
		stms.add(stm)
		restoreStack(savedStack)
	}

	for (i in body.items) {
		println(i)
	}

	println("-----------------")

	for (i in body.items) {
		when (i) {
			is BAF.LABEL -> {
				addStm(AstStm.STM_LABEL(label(i.label)))
			}
			is BAF.FRAME -> {
				println("FRAME:" + i.localTypes + "," + i.stackTypes)
				stack.clear()
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
				addStm(AstStm.SET(local(i.type, i.index), expr))
			}
			is BAF.PUTFIELD -> {
				val expr = stack.pop()
				val field = i.ref
				if (field.isStatic != false) {
					//ensureEmptyStack()
					addStm(AstStm.SET_FIELD_STATIC(field.containingTypeRef, field, expr, false))
				} else {
					val obj = stack.pop()
					//ensureEmptyStack()
					addStm(AstStm.SET_FIELD_INSTANCE(obj, field, expr))
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
				stack.clear()
			}
			is BAF.GOTOIF0 -> {
				val l = stack.pop()
				val cond = AstExpr.BINOP(AstType.BOOL, l, i.operator, AstExpr.LITERAL(0))
				saveStack()
				stms.add(AstStm.IF_GOTO(cond, label(i.label)))
				usedLabels.add(label(i.label))
				stack.clear()
			}
			is BAF.GOTO -> {
				saveStack()
				stms.add(AstStm.GOTO(label(i.label)))
				usedLabels.add(label(i.label))
				stack.clear()
			}
			is BAF.CHECKCAST -> {
				stack.push(AstExpr.CAST(stack.pop(), i.type))
			}
			is BAF.CONV -> {
				val r = stack.pop()
				assert(r.type == i.src)
				stack.push(AstExpr.CAST(r, i.dst))
			}
			is BAF.ANEW -> {
				stack.push(AstExpr.NEW(i.type))
			}
			is BAF.INVOKE -> {
				val methodType = i.methodType
				val method = i.method
				val targetClass = method.containingClass.ref()
				val isCallStatic = i.type == BAF.InvokeType.STATIC
				val obj = if (!isCallStatic) stack.pop() else null
				val args = (0 until methodType.argCount).map { stack.pop() }

				val expr = when (i.type) {
					BAF.InvokeType.STATIC -> AstExpr.CALL_STATIC(targetClass, method, args)
					BAF.InvokeType.VIRTUAL, BAF.InvokeType.INTERFACE -> AstExpr.CALL_INSTANCE(obj!!, method, args)
					BAF.InvokeType.SPECIAL -> AstExpr.CALL_SPECIAL(obj!!, targetClass.name, method, args)
				}

				if (methodType.retVoid) {
					addStm(AstStm.STM_EXPR(expr))
				} else {
					stack.push(expr)
				}
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