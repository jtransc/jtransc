package com.jtransc.backend.asm2

import com.jtransc.ast.*
import com.jtransc.ds.cast
import com.jtransc.org.objectweb.asm.Label

class TirToStm(val methodType: AstType.METHOD, val blockContext: BlockContext, val types: AstTypes) {
	val locals = hashMapOf<Local, AstLocal>()
	val stms = arrayListOf<AstStm>()
	var id = 0

	fun AstType.convertType(): AstType {
		val type = this
		return when (type) {
			is AstType.COMMON -> {
				if (type.single != null) {
					type.single!!.convertType()
				} else {
					if (type.elements.any { it is AstType.Primitive }) {
						getCommonTypePrim(type.elements.cast<AstType.Primitive>())
					} else {
						AstType.OBJECT
					}
				}
			}
			else -> type
		}
	}

	val Local.ast: AstLocal get() {
		val canonicalLocal = Local(this.type.convertType(), this.index)
		if (canonicalLocal.type is AstType.UNKNOWN) {
			println("ASSERT UNKNOWN!: $canonicalLocal")
		}
		return locals.getOrPut(canonicalLocal) { AstLocal(id++, canonicalLocal.type) }
	}
	val Label.ast: AstLabel get() = blockContext.label(this)

	val Local.expr: AstExpr.LOCAL get() = AstExpr.LOCAL(this.ast)

	val Operand.expr: AstExpr get() = when (this) {
		is Constant -> this.v.lit
		is Param -> AstExpr.PARAM(AstArgument(this.index, this.type.convertType()))
		is Local -> AstExpr.LOCAL(this.ast)
		is This -> AstExpr.THIS(this.clazz.name)
	//is CatchException -> AstExpr.CAUGHT_EXCEPTION(this.type)
		is CatchException -> AstExpr.CAUGHT_EXCEPTION(AstType.OBJECT)
		else -> TODO("$this")
	}

	fun convert(tirs: List<TIR>) {
		for (tir in tirs) {
			when (tir) {
				is TIR.NOP -> Unit
				is TIR.MOV -> stms += tir.dst.expr.setTo(tir.src.expr.castTo(tir.dst.type))
				is TIR.INSTANCEOF -> stms += tir.dst.expr.setTo(AstExpr.INSTANCE_OF(tir.src.expr, tir.type as AstType.Reference))
				is TIR.CONV -> stms += tir.dst.expr.setTo(tir.src.expr.castTo(tir.dst.type))
				is TIR.ARRAYLENGTH -> stms += tir.dst.expr.setTo(AstExpr.ARRAY_LENGTH(tir.obj.expr))
				is TIR.NEW -> stms += tir.dst.expr.setTo(AstExpr.NEW(tir.type))
				is TIR.NEWARRAY -> stms += tir.dst.expr.setTo(AstExpr.NEW_ARRAY(tir.arrayType, tir.lens.map { it.expr }))
				is TIR.UNOP -> stms += tir.dst.expr.setTo(AstExpr.UNOP(tir.op, tir.r.expr))
				is TIR.BINOP -> {
					val leftType = when (tir.op) {
						AstBinop.LCMP, AstBinop.EQ, AstBinop.NE, AstBinop.GE, AstBinop.LE, AstBinop.GT, AstBinop.LT -> tir.l.type
						AstBinop.CMPG, AstBinop.CMPL -> AstType.DOUBLE
						else -> tir.dst.type
					}
					val rightType = when (tir.op) {
						AstBinop.SHL, AstBinop.SHR, AstBinop.USHR -> AstType.INT
						else -> leftType
					}
					stms += tir.dst.expr.setTo(AstExpr.BINOP(tir.dst.type, tir.l.expr.castTo(leftType), tir.op, tir.r.expr.castTo(rightType)))
				}
				is TIR.ARRAY_STORE -> {
					stms += AstStm.SET_ARRAY(tir.array.expr, tir.index.expr, tir.value.expr.castTo(tir.elementType.convertType()))
				}
				is TIR.ARRAY_LOAD -> {
					stms += tir.dst.expr.setTo(AstExpr.ARRAY_ACCESS(tir.array.expr, tir.index.expr))
				}
				is TIR.GETSTATIC -> stms += tir.dst.expr.setTo(AstExpr.FIELD_STATIC_ACCESS(tir.field))
				is TIR.GETFIELD -> stms += tir.dst.expr.setTo(AstExpr.FIELD_INSTANCE_ACCESS(tir.field, tir.obj.expr.castTo(tir.field.containingTypeRef)))
				is TIR.PUTSTATIC -> stms += AstStm.SET_FIELD_STATIC(tir.field, tir.src.expr.castTo(tir.field.type))
				is TIR.PUTFIELD -> stms += AstStm.SET_FIELD_INSTANCE(tir.field, tir.obj.expr.castTo(tir.field.containingTypeRef), tir.src.expr.castTo(tir.field.type))
				is TIR.INVOKE_COMMON -> {
					val method = tir.method
					val args = tir.args.zip(method.type.args).map { it.first.expr.castTo(it.second.type) }
					val expr = if (tir.obj != null) {
						AstExpr.CALL_INSTANCE(tir.obj!!.expr.castTo(tir.method.containingClassType), tir.method, args, isSpecial = tir.isSpecial)
					} else {
						AstExpr.CALL_STATIC(tir.method, args, isSpecial = tir.isSpecial)
					}
					if (tir is TIR.INVOKE) {
						stms += tir.dst.expr.setTo(expr)
					} else {
						stms += AstStm.STM_EXPR(expr)
					}
				}
				is TIR.MONITOR -> stms += if (tir.enter) AstStm.MONITOR_ENTER(tir.obj.expr) else AstStm.MONITOR_EXIT(tir.obj.expr)
			// control flow:
				is TIR.LABEL -> stms += AstStm.STM_LABEL(tir.label.ast)
				is TIR.JUMP -> stms += AstStm.GOTO(tir.label.ast)
				is TIR.JUMP_IF -> {
					val t1 = tir.l.expr.type
					stms += AstStm.IF_GOTO(tir.label.ast, AstExpr.BINOP(AstType.BOOL, tir.l.expr, tir.op, tir.r.expr.castTo(t1)))
				}
				is TIR.SWITCH_GOTO -> stms += AstStm.SWITCH_GOTO(
					tir.subject.expr,
					tir.deflt.ast,
					tir.cases.entries.groupBy { it.value }.map { it.value.map { it.key } to it.key.ast }
				)
				is TIR.RET -> {
					//if (methodType.ret == AstType.REF("j.ClassInfo")) {
					//	println("go!")
					//}
					stms += if (tir.v != null) AstStm.RETURN(tir.v.expr.castTo(methodType.ret)) else AstStm.RETURN_VOID()
				}
				is TIR.THROW -> stms += AstStm.THROW(tir.ex.expr)
			//is TIR.PHI_PLACEHOLDER -> stms += AstStm.NOP("PHI_PLACEHOLDER")
				else -> TODO("$tir")
			}
		}
	}
}
