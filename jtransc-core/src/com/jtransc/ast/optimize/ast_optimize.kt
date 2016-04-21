package com.jtransc.ast.optimize

import com.jtransc.ast.*
import com.jtransc.types.Locals

//const val DEBUG = false
//const val DEBUG = true

object AstOptimizer : AstVisitor() {
	private var stm: AstStm? = null

	override fun visit(stm: AstStm?) {
		super.visit(stm)
		this.stm = stm
	}

	override fun visit(body: AstBody) {
		// @TODO: this should be easier when having the SSA form
		for (local in body.locals) {
			if (local.writes.size == 1) {
				val write = local.writes[0]
				var writeExpr2 = write.expr.value
				while (writeExpr2 is AstExpr.CAST) writeExpr2 = writeExpr2.expr.value
				val writeExpr = writeExpr2
				//println("Single write: $local = $writeExpr")
				when (writeExpr) {
					is AstExpr.PARAM, is AstExpr.THIS -> { // LITERALS!
						for (read in local.reads) {
							//println("  :: read: $read")
							read.box.value = write.expr.value.clone()
						}
						write.box.value = AstStm.NOP()
						local.writes.clear()
						local.reads.clear()
					}
				}
				//println("Written once! $local")
			}
		}

		super.visit(body)

		// REMOVE UNUSED VARIABLES
		body.locals = body.locals.filter { it.isUsed }
	}

	override fun visit(stms: AstStm.STMS) {
		super.visit(stms)

		for (n in 1 until stms.stms.size) {
			val abox = stms.stms[n - 1]
			val bbox = stms.stms[n - 0]
			val a = abox.value
			val b = bbox.value
			//println("${a.javaClass}")

			if (a is AstStm.SET_LOCAL && b is AstStm.SET_LOCAL) {
				val alocal = a.local.local
				val blocal = b.local.local
				val aexpr = a.expr.value
				val bexpr = b.expr.value
				if (aexpr is AstExpr.LOCAL && bexpr is AstExpr.LOCAL) {
					//println("double set locals! $alocal = ${aexpr.local} :: ${blocal} == ${bexpr.local}")
					if ((alocal == bexpr.local) && (aexpr.local == blocal)) {
						//println("LOCAL[a]:" + alocal)

						blocal.writes.remove(b)
						alocal.reads.remove(bexpr)

						val aold = a
						abox.value = AstStm.NOP()
						bbox.value = aold

						//println("LOCAL[b]:" + alocal)
						//println("double set! CROSS!")
					}
				}
			}

			// @TODO: Still fails!
			//if (a is AstStm.SET_LOCAL && a.expr.value is AstExpr.LOCAL) {
			//	//val blocal = a.expr.value as AstExpr.LOCAL
			//	val alocal = a.local.local
			//	if (alocal.writesCount == 1 && alocal.readCount == 1 && alocal.reads.first().stm == b) {
			//		alocal.reads.first().box.value = a.expr.value
			//		abox.value = AstStm.NOP()
			//		alocal.writes.clear()
			//		alocal.reads.clear()
			//	}
			//}
		}
	}

	override fun visit(stm: AstStm.SET_LOCAL) {
		super.visit(stm)
		val box = stm.box
		val expr = stm.expr.value

		if (expr is AstExpr.LOCAL) {
			// FIX: Assigning a value to itself
			if (stm.local.local == expr.local) {
				val local = stm.local.local
				local.writes.remove(stm)
				local.reads.remove(expr)
				box.value = AstStm.NOP()
				return
			}
		}

		// Dummy cast
		if (expr is AstExpr.CAST && stm.local.type == expr.from) {
			val exprBox = expr.box
			exprBox.value = expr.expr.value
			return
		}
	}

	override fun visit(expr: AstExpr.CAST) {
		super.visit(expr)

		val castTo = expr.to
		val child = expr.expr.value

		val box = expr.box

		//println("${expr.expr.type} -> ${expr.to}")

		// DUMMY CAST
		if (expr.expr.type == castTo) {
			box.value = expr.expr.value
			visit(box)
			return
		}

		// DOUBLE CAST
		if (child is AstExpr.CAST) {
			val cast1 = expr
			val cast2 = child
			if ((cast1.type is AstType.REF) && (cast2.type is AstType.REF)) {
				cast1.expr.value = cast2.expr.value
				visit(box)
			}
			return
		}

		// CAST LITERAL
		if (child is AstExpr.LITERAL) {
			val literalValue = child.value
			if (literalValue is Int) {
				val box = expr.box
				when (castTo) {
					AstType.BYTE -> box.value = AstExpr.LITERAL(literalValue.toByte())
					AstType.SHORT -> box.value = AstExpr.LITERAL(literalValue.toShort())
					//AstType.CHAR -> expr.box.value = AstExpr.LITERAL(literalValue.toChar())
				}
				box.value.stm = stm
				return
			}
		}
	}
}

object AstAnnotateExpressions : AstVisitor() {
	private var stm: AstStm? = null

	override fun visit(stm: AstStm?) {
		super.visit(stm)
		this.stm = stm
	}

	override fun visit(expr: AstExpr?) {
		super.visit(expr)
		expr?.stm = stm
	}
}

fun AstBody.optimize() = this.apply {
	AstAnnotateExpressions.visit(this)
	AstOptimizer.visit(this)
}
fun AstStm.Box.optimize() = this.apply {
	AstAnnotateExpressions.visit(this)
	AstOptimizer.visit(this)
}
fun AstExpr.Box.optimize() = this.apply {
	AstAnnotateExpressions.visit(this)
	AstOptimizer.visit(this)
}

fun AstStm.optimize() = this.let { this.box.optimize().value }
fun AstExpr.optimize() = this.let { this.box.optimize().value }
