package com.jtransc.types

import com.jtransc.ast.AstBinop
import com.jtransc.ast.AstType
import com.jtransc.ast.AstUnop
import org.objectweb.asm.Attribute
import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.tree.LabelNode

interface BAF {
	data class Body(val items: List<BAF>)

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
	data class FRAME(val type: Int, val nLocal: Int, val local: Array<Any>?, val nStack: Int, val stack: Array<Any>?) : BAF
	data class MULTIANEWARRAY(val desc: String?, val dims: Int) : BAF
	data class NEWARRAY(val type: AstType.Primitive) : BAF
	data class ANEWARRAY(val type: String?) : BAF
	data class ANEW(val type: String?) : BAF
	data class ACHECKCAST(val type: String?) : BAF
	data class AINSTANCEOF(val type: String?) : BAF
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
	data class GOTOIF0(val eq: AstBinop, val label: Label?) : BAF
	data class GOTOIF_I(val eq: AstBinop, val label: Label?) : BAF
	data class GOTOIF_A(val eq: AstBinop, val label: Label?) : BAF
	data class GOTO(val label: Label?) : BAF
	data class GOTOIFNULL(val eq: AstBinop, val label: Label?) : BAF

	object END : BAF

	data class GETFIELD(val static: Boolean, val owner: String?, val name: String?, val desc: String?) : BAF
	data class PUTFIELD(val static: Boolean, val owner: String?, val name: String?, val desc: String?) : BAF
	data class LOCALVARIABLE(val name: String?, val desc: String?, val signature: String?, val start: Label?, val end: Label?, val index: Int) : BAF
	data class ATTR(val attr: Attribute?) : BAF
	data class PARAM(val name: String?, val access: Int) : BAF
}

