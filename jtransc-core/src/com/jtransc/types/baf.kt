package com.jtransc.types

import com.jtransc.ast.*
import org.objectweb.asm.Attribute
import org.objectweb.asm.Handle
import org.objectweb.asm.Label

/**
 * Abstracts from ASM. Simplifies instructions. Stack based.
 */
interface BAF {
	data class Body(val methodRef: AstMethodRef, val items: List<BAF>)

	data class INVOKEDYNAMIC(val name: String?, val desc: String?, val handle: Handle, val args: List<Any?>) : BAF

	enum class InvokeType {
		VIRTUAL, SPECIAL, STATIC, INTERFACE
	}

	object NOP : BAF

	data class CONST(val value: Any?) : BAF
	data class BINOP(val type: AstType, val operator: AstBinop) : BAF
	data class UNOP(val type: AstType, val operator: AstUnop) : BAF
	data class CONV(val src: AstType, val dst: AstType) : BAF
	data class RET(val type: AstType) : BAF

	object RETVOID : BAF

	object ARRAYLENGTH : BAF

	object THROW : BAF

	data class MONITOR(val enter: Boolean) : BAF
	data class INVOKE(val owner: String?, val name: String?, val desc: String?, val itf: Boolean, val type: InvokeType) : BAF
	data class SWITCH(val dflt: Label?, val pairs: List<Pair<Int, Label?>>) : BAF
	data class tryCatchBlock(val start: Label?, val end: Label?, val handler: Label?, val type: String?) : BAF

	object CODE : BAF

	data class GETLOCAL(val type: AstType, val index: Int) : BAF
	data class PUTLOCAL(val type: AstType, val index: Int) : BAF
	data class FRAME(val type: Int, val localTypes: List<AstType>, val stackTypes: List<AstType>) : BAF

	data class NEWARRAY(val type: AstType, val dims: Int) : BAF
	data class ANEW(val type: AstType) : BAF
	data class CHECKCAST(val type: AstType) : BAF
	data class AINSTANCEOF(val type: AstType) : BAF

	data class MAXS(val maxStack: Int, val maxLocal: Int) : BAF
	data class LABEL(val label: Label?) : BAF
	data class ARRAYGET(val type: AstType) : BAF
	data class ARRAYSET(val type: AstType) : BAF

	object POP : BAF

	object POP2 : BAF

	object DUP : BAF

	object DUPX1 : BAF

	object DUPX2 : BAF

	object DUP2 : BAF

	object DUP2_X1 : BAF

	object DUP2_X2 : BAF

	object SWAP : BAF

	data class IINC(val index: Int, val increment: Int) : BAF
	data class LINE(val line: Int, val start: Label?) : BAF
	data class GOTO(val label: Label?) : BAF
	data class GOTOIF0(val operator: AstBinop, val label: Label?) : BAF
	data class GOTOIF_I(val operator: AstBinop, val label: Label?) : BAF
	data class GOTOIF_A(val operator: AstBinop, val label: Label?) : BAF
	data class GOTOIFNULL(val operator: AstBinop, val label: Label?) : BAF

	object END : BAF

	data class GETFIELD(val ref: AstFieldRef) : BAF
	data class PUTFIELD(val ref: AstFieldRef) : BAF
	data class LOCALVARIABLE(val name: String?, val desc: String?, val signature: String?, val start: Label?, val end: Label?, val index: Int) : BAF
	data class ATTR(val attr: Attribute?) : BAF
	data class PARAM(val name: String?, val access: Int) : BAF
}

