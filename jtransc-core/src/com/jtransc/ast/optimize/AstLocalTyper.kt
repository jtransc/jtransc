package com.jtransc.ast.optimize

import com.jtransc.ast.*

class AstLocalTyper : AstTransformer() {
	class LocalInfo(val local: AstLocal) {
		var valid = true
		val types = hashSetOf<AstType>()
		val casts = hashSetOf<AstExpr.CAST>()
		val exprCasts = hashSetOf<AstExpr.CAST>()
		val setLocals = hashSetOf<AstStm.SET_LOCAL>()
	}

	val locals = hashMapOf<AstLocal, LocalInfo>()

	private val AstLocal.info get() = locals.getOrPut(this) { LocalInfo(this) }
	private val AstExpr.LOCAL.info get() = local.info

	override fun visit(local: AstLocal) {
		local.info.valid = false
	}

	override fun transform(cast: AstExpr.CAST): AstExpr {
		val child = cast.subject.value
		if (child is AstExpr.LOCAL) {
			val info = child.local.info
			info.types += cast.type
			info.casts += cast
			info.exprCasts += cast
			return cast
		}
		return super.transform(cast)
	}

	override fun transform(stm: AstStm.RETURN): AstStm {
		return super.transform(stm)
	}

	override fun transform(stm: AstStm.SET_LOCAL): AstStm {
		val expr = transform(stm.expr)
		val info = stm.local.info
		if (expr is AstExpr.CAST) {
			//println("ASSIGN CASTED!")
			info.types += expr.subject.type
			info.casts += expr
			info.setLocals += stm
			return stm
		} else {
			info.valid = false
		}
		return super.transform(stm)
	}

	override fun transformAndFinish(body: AstBody) {
		//if (body.methodRef.name == "java8Test2Main") println("++++")

		super.transformAndFinish(body)

		//if (body.methodRef.name == "java8Test2Main") println("++++")

		for (info in locals.values.filter { it.valid && (it.types.size == 1) && it.types.first().isReference() }) {
			val oldLocal = info.local
			val newType = info.types.first()
			val newLocal = oldLocal.copy(type = newType)

			for (cast in info.casts) {
				val castLocal = cast.subject.value
				if (castLocal is AstExpr.LOCAL) {
					cast.replaceWith(newLocal.expr)
				}
			}

			for (setLocal in info.setLocals) {
				//setLocal.replaceWith(newLocal.setTo(setLocal.expr.value.withoutCasts().castTo(newType)))
				setLocal.replaceWith(newLocal.setTo(setLocal.expr.value.withoutCasts().castTo(newType)))
				//setLocal.replaceWith(newLocal.setTo(setLocal.expr.value.withoutCasts().castToUnoptimized(newType)))
			}

			//if (body.methodRef.name == "intToBigEndian") {
			//	println("Transformed: $oldLocal to $newLocal")
			//	println("")
			//}
		}

		body.invalidateLocals()

		//val newLocals = body.locals
//
		//if (body.methodRef.name == "intToBigEndian") {
		//	println("----")
		//}
	}
}