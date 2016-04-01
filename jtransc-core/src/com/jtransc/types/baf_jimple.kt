package com.jtransc.types

import com.jtransc.ast.AstType
import com.jtransc.error.invalidOp
import com.jtransc.error.noImpl
import java.util.*

/*
fun Baf2Jimple(body: BAF.Body): Jimple.Body {
	val out = ArrayList<Jimple>()
	val stack = Stack<Jimple.Local>()

	var localId = 0

	fun alloc(type: AstType): Jimple.Local {
		return Jimple.Local(type, localId++)
	}

	for (i in body.items) {
		when (i) {
			is BAF.LABEL -> {
				out.add(Jimple.LABEL(Jimple.ILabel("${i.label}")))
			}
			is BAF.CONST -> {
				val target = alloc(i.type)
				stack.push(target)
				out.add(Jimple.CONST(target, i.value))
			}
			is BAF.BINOP -> {
				val target = alloc(i.type)
				val r = stack.pop()
				val l = stack.pop()
				stack.push(target)
				out.add(Jimple.BINOP(target, l, r, i.operator))
			}
			else -> invalidOp("$i")
		}
	}
	return Jimple.Body(out)
}
*/
