package com.jtransc.backend.asm2

import com.jtransc.ast.*
import com.jtransc.org.objectweb.asm.Label
import java.util.*

class TIRList {
	var head: TIR? = null
	var tail: TIR? = null
	fun add(n: TIR) {
		if (head == null) head = n
		tail?.append(n)
		tail = n
	}
}

interface TIR : Iterable<TIR> {
	//val target: Local?
	//val sources: List<Local>
	var prev: TIR?
	var next: TIR?
	fun toStmString(): String
	var dstBox: LocalBox?

	class Mixin : TIR {
		override fun iterator(): Iterator<TIR> = object : Iterator<TIR> {
			var current: TIR? = this@Mixin
			override fun hasNext(): Boolean = current != null
			override fun next(): TIR {
				val old = current
				current = current?.next
				return old!!
			}
		}

		//override val target: Local? = null
		//override val sources = listOf<Local>()
		override var dstBox: LocalBox? = null
		override var prev: TIR? = null
		override var next: TIR? = null
		override fun toStmString() = this.toString()
	}

	interface Def : TIR {
		val dst: Local
	}

	data class NOP(val dummy: Boolean = true) : TIR by Mixin() {
		override fun toStmString() = ";"
	}

	data class PHI_PLACEHOLDER(val dummy: Boolean) : TIR by Mixin() {
		override fun toStmString() = "PHI_PLACEHOLDER"
	}

	data class LABEL(val label: Label) : TIR by Mixin() {
		override fun toStmString() = "$label"
	}

	data class PHI(override val dst: Local, val params: ArrayList<PHIOption> = arrayListOf()) : TIR by Mixin(), Def {
		override fun toStmString() = "PHI"
	}

	data class MOV(override val dst: Local, val src: Operand) : TIR by Mixin(), Def {
		override fun toStmString() = "$dst = $src;"
	}

	data class BINOP(override val dst: Local, val l: Operand, val op: AstBinop, val r: Operand) : TIR by Mixin(), Def {
		override fun toStmString() = "$dst = $l $op $r;"
	}

	data class UNOP(override val dst: Local, val op: AstUnop, val r: Operand) : TIR by Mixin(), Def {
		override fun toStmString() = "$dst = $op $r;"
	}

	data class CONV(override val dst: Local, val src: Operand, val dstType: AstType, val checked: Boolean) : TIR by Mixin(), Def {
		override fun toStmString() = "$dst = ($dstType)$src;"
	}

	data class ARRAY_STORE(val array: Operand, val elementType: AstType, val index: Operand, val value: Operand) : TIR by Mixin() {
		override fun toStmString() = "$array[$index] = $value;"
	}

	data class ARRAY_LOAD(override val dst: Local, val array: Operand, val elementType: AstType, val index: Operand) : TIR by Mixin(), Def {
		override fun toStmString() = "$dst = $array[$index];"
	}

	data class NEW(override val dst: Local, val type: AstType.REF) : TIR by Mixin(), Def {
		override fun toStmString() = "$dst = new $type();"
	}

	data class NEWARRAY(override val dst: Local, val arrayType: AstType.ARRAY, val lens: List<Operand>) : TIR by Mixin(), Def {
		override fun toStmString() = "$dst = new $arrayType$lens;"
	}

	data class RET(val v: Operand?) : TIR by Mixin() {
		override fun toStmString() = if (v == null) "return;" else "return $v;"
	}

	interface INVOKE_COMMON : TIR {
		val obj: Operand?
		val method: AstMethodRef
		val args: List<Operand>
		val isSpecial: Boolean
	}

	data class INVOKE_VOID(override val obj: Operand?, override val method: AstMethodRef, override val args: List<Operand>, override val isSpecial: Boolean) : INVOKE_COMMON, TIR by Mixin() {
		override fun toStmString() = "$obj.$method($args)"
	}

	data class INVOKE(override val dst: Local, override val obj: Operand?, override val method: AstMethodRef, override val args: List<Operand>, override val isSpecial: Boolean) : INVOKE_COMMON, TIR by Mixin(), Def {
		override fun toStmString() = "$dst = $obj.$method($args)"
	}

	data class INSTANCEOF(override val dst: Local, val type: AstType, val src: Operand) : TIR by Mixin(), Def {
		override fun toStmString() = "$dst = $src instanceof $type;"
	}

	data class THROW(val ex: Operand) : TIR by Mixin() {
		override fun toStmString() = "throw $ex;"
	}

	data class GETSTATIC(override val dst: Local, val field: AstFieldRef) : TIR by Mixin(), Def {
		override fun toStmString() = "$dst = $field;"
	}

	data class PUTSTATIC(val field: AstFieldRef, val src: Operand) : TIR by Mixin() {
		override fun toStmString() = "$field = $src;"
	}

	data class GETFIELD(override val dst: Local, val field: AstFieldRef, val obj: Operand) : TIR by Mixin(), Def {
		override fun toStmString() = "$dst = $obj.$field;"
	}

	data class PUTFIELD(val field: AstFieldRef, val obj: Operand, val src: Operand) : TIR by Mixin() {
		override fun toStmString() = "$obj.$field = $src;"
	}

	data class ARRAYLENGTH(override val dst: Local, val obj: Operand) : TIR by Mixin(), Def {
		override fun toStmString() = "$dst = $obj.length;"
	}

	data class MONITOR(val obj: Operand, val enter: Boolean) : TIR by Mixin() {
		override fun toStmString() = "MONITOR($obj, enter=$enter)"
	}

	data class SWITCH_GOTO(val subject: Operand, val deflt: Label, val cases: Map<Int, Label>) : TIR by Mixin() {
		override fun toStmString() = "SWITCH*"
	}

	data class JUMP_IF(val label: Label, val l: Operand, val op: AstBinop, val r: Operand) : TIR by Mixin() {
		override fun toStmString() = "if ($l $op $r) goto $label;"
	}

	data class JUMP(val label: Label) : TIR by Mixin() {
		override fun toStmString() = "goto $label;"
	}

	fun prepend(prev: TIR) {
		prev.prev = this.prev
		this.prev = prev
		prev.next = this
	}

	fun append(next: TIR) {
		next.next = this.next
		this.next = next
		next.prev = this
	}
}
