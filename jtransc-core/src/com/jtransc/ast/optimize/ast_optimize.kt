package com.jtransc.ast.optimize

import com.jtransc.ast.*
import com.jtransc.log.log

//const val DEBUG = false
//const val DEBUG = true

// @TODO: rewrite using method.transformInplace
class AstOptimizer(val flags: AstBodyFlags) : AstVisitor() {
	val types: AstTypes = flags.types
	private var stm: AstStm? = null

	override fun visit(stm: AstStm?) {
		super.visit(stm)
		this.stm = stm
	}

	val METHODS_TO_STRIP = setOf<AstMethodRef>(
		AstMethodRef("kotlin.jvm.internal.Intrinsics".fqname, "checkParameterIsNotNull", AstType.METHOD(AstType.VOID, listOf(AstType.OBJECT, AstType.STRING)))
	)

	private val invertComparisons = mapOf(
		AstBinop.LT to AstBinop.GE,
		AstBinop.LE to AstBinop.GT,
		AstBinop.EQ to AstBinop.NE,
		AstBinop.NE to AstBinop.EQ,
		AstBinop.GT to AstBinop.LE,
		AstBinop.GE to AstBinop.LT
	)

	override fun visit(expr: AstExpr.BINOP) {
		super.visit(expr)
		val box = expr.box
		val left = expr.left.value
		val right = expr.right.value
		when (expr.op) {
			AstBinop.EQ, AstBinop.NE -> {
				if ((left is AstExpr.CAST) && (right is AstExpr.LITERAL)) {
					if (left.from == AstType.BOOL && left.to == AstType.INT && right.value is Int) {
						//println("optimize")
						val leftExpr = left.subject.value
						val toZero = right.value == 0
						val equals = expr.op == AstBinop.EQ

						box.value = if (toZero xor equals) leftExpr else AstExpr.UNOP(AstUnop.NOT, leftExpr)
						AstAnnotateExpressions.visitExprWithStm(stm, box)
					}
				}
			}
			AstBinop.LT, AstBinop.LE, AstBinop.GT, AstBinop.GE -> {
				if (!flags.strictfp && (left is AstExpr.BINOP) && (right is AstExpr.LITERAL)) {
					when (left.op) {
						AstBinop.CMPG, AstBinop.CMPL -> {
							val l = left.left
							val r = left.right
							val op = expr.op
							val compareValue = right.value
							if (compareValue == 0) {
								box.value = AstExpr.UNOP(AstUnop.NOT, AstExpr.BINOP(AstType.BOOL, l.value, invertComparisons[op]!!, r.value))
							} else {
								log.warn("WARNING: Unhandled float comparison (because compareValue != 0)! op = ${expr.op} :: compareValue = $compareValue")
							}
						}
						else -> Unit
					}
				}
			}
			else -> Unit
		}
	}


	override fun visit(expr: AstExpr.CALL_BASE) {
		//println("CALL_BASE:$expr:${expr.method}")
		super.visit(expr)
	}

	override fun visit(expr: AstExpr.CALL_INSTANCE) {
		super.visit(expr)

		if (flags.types.target.matches("js") && expr.method.name == "toString" && expr.obj.type == AstType.STRINGBUILDER) {
			var curr: AstExpr? = expr
			val params = arrayListOf<AstExpr>()
			var optimizable = true
			grab@ while (curr != null) {
				//println(curr)
				when (curr) {
					is AstExpr.CALL_INSTANCE -> {
						if (curr.method.name == "append") {
							if (curr.args.size != 1) {
								optimizable = false
								break@grab
							}
							params += curr.args.first().value
							curr = curr.obj.value
						} else if (curr.method.name == "toString") {
							if (params.isNotEmpty()) {
								optimizable = false
								break@grab
							} else {
								curr = curr.obj.value
							}
						} else {
							optimizable = false
							break@grab
						}
					}
					is AstExpr.NEW_WITH_CONSTRUCTOR -> {
						break@grab
					}
					else -> {
						optimizable = false
						break@grab
					}
				}
			}
			if (optimizable && params.isNotEmpty()) {
				expr.box.value = AstExpr.CONCAT_STRING(expr, params.reversed())
			}
			//println("optimize StringBuilder.toString: $optimizable: $params")
		}
	}

	override fun visit(expr: AstExpr.CALL_STATIC) {
		super.visit(expr)

		if (expr.method in METHODS_TO_STRIP) {
			//println("STRIP:${expr.method}")
			expr.stm?.box?.value = AstStm.NOP("method to strip")
		} else {
			//println("NO_STRIP:${expr.method}")
		}
	}

	override fun visit(body: AstBody?) {
		if (body == null) return

		// @TODO: this should be easier when having the SSA form
		for (local in body.locals) {
			if (local.writes.size == 1) {
				val write = local.writes[0]
				var writeExpr2 = write.expr.value
				while (writeExpr2 is AstExpr.CAST) writeExpr2 = writeExpr2.subject.value
				val writeExpr = writeExpr2
				//println("Single write: $local = $writeExpr")
				when (writeExpr) {
					is AstExpr.PARAM, is AstExpr.THIS -> { // LITERALS!
						for (read in local.reads) {
							//println("  :: read: $read")
							read.box.value = write.expr.value.clone()
						}
						write.box.value = AstStm.NOP("optimized literal")
						local.writes.clear()
						local.reads.clear()
					}
				}
				//println("Written once! $local")
			}
		}

		super.visit(body)

		// REMOVE UNUSED VARIABLES
		//body.locals = body.locals.filter { it.isUsed }

		val unoptstms = body.stm.expand()
		//if (unoptstms.any { it is AstStm.NOP }) {
		//	println("done")
		//}
		val optstms = unoptstms.filter { it !is AstStm.NOP }
		body.stm = optstms.stm()

		//if (unoptstms.any { it is AstStm.NOP }) {
		//	println("done")
		//}

		val stms = body.stm.expand().map { it.box }
		var n = 0
		while (n < stms.size) {
			val startStmIndex = n
			val abox = stms[n++]
			val a = abox.value
			if ((a is AstStm.SET_ARRAY) && (a.index.value is AstExpr.LITERAL) && (a.array.value is AstExpr.CAST) && ((a.array.value as AstExpr.CAST).subject.value is AstExpr.LOCAL)) {
				val exprs = arrayListOf<AstExpr.Box>()
				val alocal = ((a.array.value as AstExpr.CAST).subject.value as AstExpr.LOCAL).local
				val baseindex = (a.index.value as AstExpr.LITERAL).value as Int
				var lastindex = baseindex
				exprs += a.expr
				while (n < stms.size) {
					val bbox = stms[n++]
					val b = bbox.value

					if ((b is AstStm.SET_ARRAY) && (b.index.value is AstExpr.LITERAL) && (b.array.value is AstExpr.CAST) && ((b.array.value as AstExpr.CAST).subject.value is AstExpr.LOCAL)) {
						val blocal = ((b.array.value as AstExpr.CAST).subject.value as AstExpr.LOCAL).local
						val nextindex = (b.index.value as AstExpr.LITERAL).value as Int

						if (alocal == blocal && nextindex == lastindex + 1) {
							exprs += b.expr
							//println("$baseindex, $lastindex, $nextindex")
							lastindex = nextindex
							continue
						}
					}
					n--
					break
				}

				if (baseindex != lastindex) {
					for (m in startStmIndex until n) stms[m].value = AstStm.NOP("array_literals")
					stms[startStmIndex].value = AstStm.SET_ARRAY_LITERALS(a.array.value, baseindex, exprs)
					//println("ranged: $baseindex, $lastindex")
				}
			}
		}
	}

	override fun visit(stm: AstStm.STMS) {
		super.visit(stm)

		for (n in 1 until stm.stms.size) {
			val abox = stm.stms[n - 1]
			val bbox = stm.stms[n - 0]
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
						abox.value = AstStm.NOP("optimized set local")
						bbox.value = aold

						//println("LOCAL[b]:" + alocal)
						//println("double set! CROSS!")
					}
				}
			}

			// @TODO: Can't do this since like this because we are not using SSA form
			//if (a is AstStm.SET_LOCAL && a.expr.value is AstExpr.LOCAL) {
			//	//val blocal = a.expr.value as AstExpr.LOCAL
			//	val alocal = a.local.local
			//	if (alocal.writesCount == 1 && alocal.readCount == 1 && alocal.reads.first().stm == b) {
			//		alocal.reads.first().box.value = a.expr.value
			//		abox.value = AstStm.NOP("optimized set local 2")
			//		alocal.writes.clear()
			//		alocal.reads.clear()
			//	}
			//}
		}

		val finalStms = stm.stms.filter { it.value !is AstStm.NOP }

		if (finalStms.size == 1) {
			stm.box.value = finalStms.first().value
		}
	}

	override fun visit(expr: AstExpr.UNOP) {
		super.visit(expr)
		if (expr.op == AstUnop.NOT) {
			val right = expr.right.value
			when (right) {
				is AstExpr.BINOP -> {
					val newop = when (right.op) {
						AstBinop.NE -> AstBinop.EQ
						AstBinop.EQ -> AstBinop.NE
						AstBinop.LT -> AstBinop.GE
						AstBinop.LE -> AstBinop.GT
						AstBinop.GT -> AstBinop.LE
						AstBinop.GE -> AstBinop.LT
						else -> null
					}
					if (newop != null) expr.box.value = AstExpr.BINOP(right.type, right.left.value, newop, right.right.value)
				}
				is AstExpr.UNOP -> {
					if (right.op == AstUnop.NOT) {
						// negate twice!
						expr.box.value = right.right.value
					}
				}
			}
		}
	}

	override fun visit(stm: AstStm.SET_LOCAL) {
		super.visit(stm)
		val box = stm.box
		val expr = stm.expr.value
		val storeLocal = stm.local.local

		if (expr is AstExpr.LOCAL) {
			// FIX: Assigning a value to itself
			if (storeLocal == expr.local) {
				storeLocal.writes.remove(stm)
				storeLocal.reads.remove(expr)
				box.value = AstStm.NOP("assign to itself")
				return
			}
		}

		// Do not assign and remove variables that are not going to be used!
		if (storeLocal.readCount == 0 && storeLocal.writesCount == 1) {
			box.value = AstStm.STM_EXPR(stm.expr.value)
			storeLocal.writes.clear()
			visit(box)
			return
		}

		// Dummy cast
		if (expr is AstExpr.CAST && stm.local.type == expr.from) {
			val exprBox = expr.box
			exprBox.value = expr.subject.value
			AstAnnotateExpressions.visitExprWithStm(stm, exprBox)
			return
		}
	}

	override fun visit(stm: AstStm.IF) {
		super.visit(stm)
		val strue = stm.strue.value
		if (strue is AstStm.IF) {
			val cond = AstExpr.BINOP(AstType.BOOL, stm.cond.value, AstBinop.BAND, strue.cond.value)
			stm.box.value = AstStm.IF(cond, strue.strue.value)
		}
	}

	override fun visit(stm: AstStm.IF_ELSE) {
		super.visit(stm)
		val cond = stm.cond.value
		val strue = stm.strue.value
		val sfalse = stm.sfalse.value
		if ((strue is AstStm.SET_LOCAL) && (sfalse is AstStm.SET_LOCAL) && (strue.local.local == sfalse.local.local)) {
			val local = strue.local
			// TERNARY OPERATOR
			//println("ternary!")
			local.local.writes.remove(strue)
			local.local.writes.remove(sfalse)

			val newset = local.setTo(AstExpr.TERNARY(cond, strue.expr.value, sfalse.expr.value, types))
			stm.box.value = newset
			local.local.writes.add(newset)
		}
	}

	override fun visit(stm: AstStm.STM_EXPR) {
		// Remove unnecessary cast
		while (stm.expr.value is AstExpr.CAST) {
			//println(stm.expr.value)
			stm.expr.value = (stm.expr.value as AstExpr.CAST).subject.value
		}
		super.visit(stm)
		if (stm.expr.value.isPure()) {
			stm.box.value = AstStm.NOP("pure stm")
		}
	}
}


object AstAnnotateExpressions : AstVisitor() {
	private var stm: AstStm? = null

	fun visitExprWithStm(stm: AstStm?, box: AstExpr.Box) {
		this.stm = stm
		visit(box)
	}

	override fun visit(stm: AstStm?) {
		this.stm = stm
		super.visit(stm)
	}

	override fun visit(expr: AstExpr?) {
		expr?.stm = stm
		super.visit(expr)
	}
}

val OPTIMIZATIONS = listOf<() -> AstTransformer>(
	{ AstCastOptimizer() }
)

val STM_OPTIMIZATIONS = listOf<() -> AstTransformer>(
	{ AstLocalTyper() }
)

fun AstBody.optimize() = this.apply {
	AstAnnotateExpressions.visit(this)
	AstOptimizer(this.flags).visit(this)
	for (opt in OPTIMIZATIONS) opt().transformAndFinish(this)
	for (opt in STM_OPTIMIZATIONS) opt().transformAndFinish(this)
	invalidate()
}

fun AstStm.Box.optimize(flags: AstBodyFlags) = this.apply {
	AstAnnotateExpressions.visit(this)
	AstOptimizer(flags).visit(this)
	for (opt in OPTIMIZATIONS) opt().transformAndFinish(this)
	for (opt in STM_OPTIMIZATIONS) opt().transformAndFinish(this)
}

fun AstExpr.Box.optimize(types: AstTypes, strictfp: Boolean = true) = this.optimize(AstBodyFlags(types, strictfp))

fun AstExpr.Box.optimize(flags: AstBodyFlags) = this.apply {
	AstAnnotateExpressions.visit(this)
	AstOptimizer(flags).visit(this)
	for (opt in OPTIMIZATIONS) opt().transformAndFinish(this)
}

fun AstStm.optimize(types: AstTypes, strictfp: Boolean = true): AstStm = this.let { this.box.optimize(AstBodyFlags(types, strictfp)).value }
fun AstStm.optimize(flags: AstBodyFlags): AstStm = this.let { this.box.optimize(flags).value }
fun AstExpr.optimize(flags: AstBodyFlags): AstExpr = this.let { this.box.optimize(flags).value }
