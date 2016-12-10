package com.jtransc.backend.asm2

import com.jtransc.ast.*
import com.jtransc.backend.JvmOpcode
import com.jtransc.backend.asm1.disasm
import com.jtransc.backend.isEnd
import com.jtransc.backend.isEndOfBasicBlock
import com.jtransc.ds.Stack
import com.jtransc.error.invalidOp
import com.jtransc.org.objectweb.asm.Label
import com.jtransc.org.objectweb.asm.Opcodes
import com.jtransc.org.objectweb.asm.Type
import com.jtransc.org.objectweb.asm.tree.*
import java.util.*

interface TIR {
	//val target: Local?
	//val sources: List<Local>
	var prev: TIR?
	var next: TIR?

	class Mixin : TIR {
		//override val target: Local? = null
		//override val sources = listOf<Local>()
		override var prev: TIR? = null
		override var next: TIR? = null
	}

	data class LABEL(val label: Label) : TIR by Mixin()
	data class THIS(val dst: Local, val thisType: AstType) : TIR by Mixin()
	data class PHI(val dst: Local, val params: ArrayList<Operand> = arrayListOf()) : TIR by Mixin()
	data class PARAM(val dst: Local, val paramIndex: Int, val paramType: AstType) : TIR by Mixin()
	data class MOV(val dst: Local, val src: Operand) : TIR by Mixin()
	//data class CST(val dst: Local, val value: Number) : TOIR by Mixin()
	data class BINOP(val dst: Local, val l: Operand, val op: String, val r: Operand) : TIR by Mixin()

	data class UNOP(val dst: Local, val op: String, val r: Operand) : TIR by Mixin()
	data class CONV(val dst: Local, val src: Operand, val dstType: AstType) : TIR by Mixin()
	data class ASTORE(val array: Operand, val index: Operand, val value: Operand) : TIR by Mixin()
	data class ALOAD(val dst: Local, val array: Operand, val index: Operand) : TIR by Mixin()
	data class NEW(val temp: Local, val type: AstType) : TIR by Mixin()
	data class NEWARRAY(val temp: Local, val type: AstType, val len: Operand) : TIR by Mixin()
	data class JUMP_IF(val label: Label, val l: Operand, val op: String, val r: Operand) : TIR by Mixin()
	data class JUMP(val label: Label) : TIR by Mixin()
	data class RET(val v: Operand?) : TIR by Mixin()
	data class INVOKE(val dst: Local?, val obj: Local?, val method: AstMethodRef, val args: List<Operand>) : TIR by Mixin()
	data class CHECKCAST(val dst: Local, val type: AstType, val src: Operand) : TIR by Mixin()
	data class INSTANCEOF(val dst: Local, val type: AstType, val src: Operand) : TIR by Mixin()
	data class THROW(val ex: Operand) : TIR by Mixin()
	data class GETSTATIC(val dst: Local, val field: AstFieldRef) : TIR by Mixin()
	data class PUTSTATIC(val fieldRef: AstFieldRef, val src: Operand) : TIR by Mixin()
	data class GETFIELD(val dst: Local, val field: AstFieldRef, val obj: Operand) : TIR by Mixin()
	data class PUTFIELD(val field: AstFieldRef, val obj: Operand, val src: Operand) : TIR by Mixin()
}

interface Operand {
}

data class Local(val v: Int) : Operand {
}

data class Constant(val type: AstType, val v: Any?) : Operand {
}

data class CatchException(val type: AstType) : Operand {
}

class Definition {
}

// http://compilers.cs.uni-saarland.de/papers/bbhlmz13cc.pdf
// Simple and Efficient Construction of Static Single Assignment Form
// ----------------------------------------------------------------
// PASS1: Build untyped Basic Blocks with simple constant clean ups
// PASS2: SSA-Form
// PASS3: Type locals
// PASS4: Construct AstStm + AstExpr
// ----------------------------------------------------------------
fun AsmToAstMethodBody2(clazz: AstType.REF, method: MethodNode, types: AstTypes, source: String = "unknown.java"): AstBody {
	//val body = BasicBlockBuilder(types)
	val methodType = types.demangleMethod(method.desc)

	val builder = BlockCfgBuilder(types)
	builder.buildTree(method.instructions.first)
	for (tcb in method.tryCatchBlocks) {
		val exceptionType = if (tcb.type != null) types.REF_INT(tcb.type) else AstType.THROWABLE
		builder.buildTree(tcb.handler, initialStack = listOf(CatchException(exceptionType)))
	}

	println("--------")
	for (i in method.instructions.toArray().toList()) {
		if (i in builder.startToBlocks) {
			for (stm in builder.startToBlocks[i]!!.stms) {
				println("$stm")
			}
		}
	}

	return AstBody(
		types,
		AstStm.STMS(),
		methodType
	)
}

class Block {

}

class Locals {
	var tempId = 1000
	fun createTemp() = Local(tempId++)
}

class BlockCfgBuilder(val types: AstTypes) {
	val locals = Locals()
	val startToBlocks = hashMapOf<AbstractInsnNode, BasicBlockBuilder>()

	fun buildTree(start: AbstractInsnNode, initialStack: List<Operand>? = null) {
		build(start, onePredecessor = null, initialStack = initialStack)
	}

	private fun build(start: AbstractInsnNode, onePredecessor: BasicBlockBuilder?, initialStack: List<Operand>? = null) {
		val bbb1 = startToBlocks[start]
		if (bbb1 != null) { // Processed already!
			if (onePredecessor != null) {
				bbb1.registerPredecessor(onePredecessor)
			}
		} else {
			val bbb = BasicBlockBuilder(types, locals).apply {
				decodeBlock(start, onePredecessor, initialStack)
			}
			startToBlocks[start] = bbb
			for (successor in bbb.allSuccessors) {
				build(successor, onePredecessor = bbb, initialStack = null)
			}
		}
	}
}

class BasicBlockBuilder(val types: AstTypes, val locals: Locals) {
	val JUMP_OPS = listOf("==", "!=", "<", ">=", ">", "<=", "==", "!=")
	val TPRIM = listOf(AstType.INT, AstType.LONG, AstType.FLOAT, AstType.DOUBLE, AstType.OBJECT, AstType.BYTE, AstType.CHAR, AstType.SHORT)
	val stms = arrayListOf<TIR>()
	val stack = Stack<Operand>()

	fun pop(type: AstType): Operand {
		return stack.pop()
	}

	fun push(v: Operand) {
		stack.push(v)
	}

	fun getVar(v: Int) = Local(v)

	fun createTemp() = locals.createTemp()
	val allSuccessors = arrayListOf<AbstractInsnNode>()
	val jumpNodes = arrayListOf<AbstractInsnNode>()
	var nextDirectNode: AbstractInsnNode? = null

	val predecessors = hashSetOf<BasicBlockBuilder>()

	fun registerPredecessor(predecessor: BasicBlockBuilder) {
		if (predecessors.isEmpty()) {
			for (item in predecessor.stack.toList()) {
				val phi = createTemp()
				stms += TIR.PHI(phi)
				push(phi)
			}
		}
		if (predecessor !in predecessors) {
			predecessors += predecessor
			for ((index, item) in predecessor.stack.toList().withIndex()) {
				val phi = stms[index] as TIR.PHI
				phi.params += item
			}
		}
	}

	fun decodeBlock(start: AbstractInsnNode, onePredecessor: BasicBlockBuilder?, initialStack: List<Operand>? = null) {
		if (onePredecessor != null) registerPredecessor(onePredecessor)
		if (initialStack != null) {
			for (s in initialStack) push(s)
		}
		var current: AbstractInsnNode? = start
		while (current != null) {
			decodeIns(current)
			if (current.isEndOfBasicBlock()) {
				if (!current.isEnd()) {
					nextDirectNode = current.next
				}
				allSuccessors += jumpNodes
				if (nextDirectNode != null) allSuccessors += nextDirectNode!!
				break
			}
			current = current.next
		}
	}

	fun decodeIns(n: AbstractInsnNode) {
		println(JvmOpcode.disasm(n))
		when (n) {
			is FrameNode -> decodeIns(n)
			is JumpInsnNode -> decodeIns(n)
			is LdcInsnNode -> decodeIns(n)
			is LookupSwitchInsnNode -> decodeIns(n)
			is TableSwitchInsnNode -> decodeIns(n)
			is MultiANewArrayInsnNode -> decodeIns(n)
			is InvokeDynamicInsnNode -> decodeIns(n)
			is IincInsnNode -> decodeIns(n)
			is LabelNode -> decodeIns(n)
			is LineNumberNode -> decodeIns(n)
			is VarInsnNode -> decodeIns(n)
			is IntInsnNode -> decodeIns(n)
			is InsnNode -> decodeIns(n)
			is MethodInsnNode -> decodeIns(n)
			is TypeInsnNode -> decodeIns(n)
			is FieldInsnNode -> decodeIns(n)
			else -> TODO("$n")
		}
	}

	fun decodeIns(n: FieldInsnNode) {
		val owner = types.REF_INT2(n.owner)
		val op = n.opcode
		val fieldRef = AstFieldRef(owner.name, n.name, types.demangle(n.desc))
		when (op) {
			Opcodes.GETSTATIC -> {
				val temp = createTemp()
				stms += TIR.GETSTATIC(temp, fieldRef)
				push(temp)
			}
			Opcodes.PUTSTATIC -> {
				val src = pop(owner)
				stms += TIR.PUTSTATIC(fieldRef, src)
			}
			Opcodes.GETFIELD -> {
				val temp = createTemp()
				val obj = pop(owner)
				stms += TIR.GETFIELD(temp, fieldRef, obj)
				push(temp)
			}
			Opcodes.PUTFIELD -> {
				val temp = createTemp()
				val src = pop(owner)
				val obj = pop(owner)
				stms += TIR.PUTFIELD(fieldRef, obj, src)
				push(temp)
			}
		}
	}

	fun decodeIns(n: TypeInsnNode) {
		val type = types.REF_INT(n.desc)
		when (n.opcode) {
			Opcodes.NEW -> {
				val temp = createTemp()
				stms += TIR.NEW(temp, type)
				push(temp)
			}
			Opcodes.ANEWARRAY -> {
				val temp = createTemp()
				val len = pop(AstType.INT)
				stms += TIR.NEWARRAY(temp, AstType.ARRAY(type), len)
				push(temp)
			}
			Opcodes.CHECKCAST -> {
				val temp = createTemp()
				val obj = pop(AstType.OBJECT)
				stms += TIR.CHECKCAST(temp, type, obj)
				push(temp)
			}
			Opcodes.INSTANCEOF -> {
				val temp = createTemp()
				val obj = pop(AstType.OBJECT)
				stms += TIR.INSTANCEOF(temp, type, obj)
				push(temp)
			}
		}
	}

	fun decodeIns(n: MultiANewArrayInsnNode) {
		TODO()
	}

	fun decodeIns(n: TableSwitchInsnNode) {
		nextDirectNode = n.dflt
		jumpNodes += n.labels
		TODO()
	}

	fun decodeIns(n: LookupSwitchInsnNode) {
		nextDirectNode = n.dflt
		jumpNodes += n.labels
		TODO()
	}

	fun decodeIns(n: InvokeDynamicInsnNode) {
		TODO()
	}

	fun decodeIns(n: LdcInsnNode) {
		val cst = n.cst
		when (cst) {
			is Int -> push(Constant(AstType.INT, cst))
			is Float -> push(Constant(AstType.FLOAT, cst))
			is Long -> push(Constant(AstType.LONG, cst))
			is Double -> push(Constant(AstType.DOUBLE, cst))
			is String -> push(Constant(AstType.STRING, cst))
			is Type -> push(Constant(AstType.OBJECT, types.REF_INT(cst.internalName)))
			else -> invalidOp
		}
	}

	fun decodeIns(n: JumpInsnNode) {
		jumpNodes += n.label

		when (n.opcode) {
			Opcodes.JSR -> TODO()
			in Opcodes.IFEQ..Opcodes.IFLE -> {
				val op = JUMP_OPS[n.opcode - Opcodes.IFEQ]
				val value = stack.pop()
				stms += TIR.JUMP_IF(n.label.label, value, op, Constant(AstType.INT, 0))
			}
			Opcodes.IFNULL, Opcodes.IFNONNULL -> {
				val op = JUMP_OPS[n.opcode - Opcodes.IFNULL]
				val value = stack.pop()
				stms += TIR.JUMP_IF(n.label.label, value, op, Constant(AstType.OBJECT, null))
			}
			in Opcodes.IF_ICMPEQ..Opcodes.IF_ACMPNE -> {
				val op = JUMP_OPS[n.opcode - Opcodes.IF_ICMPEQ]
				val valueL = stack.pop()
				val valueR = stack.pop()
				stms += TIR.JUMP_IF(n.label.label, valueL, op, valueR)
			}
			Opcodes.GOTO -> {
				stms += TIR.JUMP(n.label.label)
			}
		}
		//n.label
	}

	fun decodeIns(n: IincInsnNode) {
		stms += TIR.BINOP(getVar(n.`var`), getVar(n.`var`), "+", Constant(AstType.INT, 1))
	}

	fun decodeIns(n: FrameNode) {
		Unit // Do nothing. We calculate frames ourselves to full compatibility with versions less than Java6.
	}

	fun decodeIns(n: LabelNode) {
		stms += TIR.LABEL(n.label)
		//Unit // Do nothing. We handle basic blocks in other place.
	}

	fun decodeIns(n: LineNumberNode) {
		Unit
	}

	fun decodeIns(n: MethodInsnNode) {
		val ownerType = types.REF_INT(n.owner)
		val methodType = types.demangleMethod(n.desc)
		val ownerTypeRef = if (ownerType is AstType.ARRAY) AstType.OBJECT else ownerType as AstType.REF
		val methodRef = AstMethodRef(ownerTypeRef.name, n.name, methodType)
		val retType = methodType.ret

		val args = methodType.args.reversed().map { pop(it.type) }

		val obj = if (n.opcode != Opcodes.INVOKESTATIC) {
			pop(ownerType) as Local
		} else {
			null
		}

		val res = if (methodType.retVoid) null else createTemp()

		stms += TIR.INVOKE(res, obj, methodRef, args)
		if (res != null) push(res)
	}

	fun decodeIns(n: VarInsnNode) {
		when (n.opcode) {
			in Opcodes.ILOAD..Opcodes.ALOAD -> load(TPRIM[n.opcode - Opcodes.ILOAD], n.`var`)
			in Opcodes.ISTORE..Opcodes.ASTORE -> store(TPRIM[n.opcode - Opcodes.ISTORE], n.`var`)
			Opcodes.RET -> invalidOp("Unsupported RET")
		}
	}

	fun decodeIns(n: IntInsnNode) {
		when (n.opcode) {
			Opcodes.BIPUSH, Opcodes.SIPUSH -> const(AstType.INT, n.operand)
			Opcodes.NEWARRAY -> {
				val type = when (n.operand) {
					Opcodes.T_BOOLEAN -> AstType.BOOL
					Opcodes.T_CHAR -> AstType.CHAR
					Opcodes.T_FLOAT -> AstType.FLOAT
					Opcodes.T_DOUBLE -> AstType.DOUBLE
					Opcodes.T_BYTE -> AstType.BYTE
					Opcodes.T_SHORT -> AstType.SHORT
					Opcodes.T_INT -> AstType.INT
					Opcodes.T_LONG -> AstType.LONG
					else -> invalidOp
				}
				val temp = createTemp()
				val len = pop(AstType.INT)
				stms += TIR.NEWARRAY(temp, type, len)
				push(temp)
			}
		}
	}

	fun decodeIns(n: InsnNode) {
		val op = n.opcode
		when (op) {
			in Opcodes.INEG..Opcodes.DNEG -> unop(TPRIM[op - Opcodes.INEG], "-")

			in Opcodes.IADD..Opcodes.DADD -> binop(TPRIM[op - Opcodes.IADD], "+")
			in Opcodes.ISUB..Opcodes.DSUB -> binop(TPRIM[op - Opcodes.ISUB], "-")
			in Opcodes.IMUL..Opcodes.DMUL -> binop(TPRIM[op - Opcodes.IMUL], "*")
			in Opcodes.IDIV..Opcodes.DDIV -> binop(TPRIM[op - Opcodes.IDIV], "/")
			in Opcodes.IREM..Opcodes.DREM -> binop(TPRIM[op - Opcodes.IREM], "%")

			in Opcodes.ISHL..Opcodes.LSHL -> binop(TPRIM[op - Opcodes.ISHL], "<<")
			in Opcodes.ISHR..Opcodes.LSHR -> binop(TPRIM[op - Opcodes.ISHR], ">>")
			in Opcodes.IUSHR..Opcodes.LUSHR -> binop(TPRIM[op - Opcodes.IUSHR], ">>>")
			in Opcodes.IAND..Opcodes.LAND -> binop(TPRIM[op - Opcodes.IAND], "&")
			in Opcodes.IOR..Opcodes.LOR -> binop(TPRIM[op - Opcodes.IOR], "|")
			in Opcodes.IXOR..Opcodes.LXOR -> binop(TPRIM[op - Opcodes.IXOR], "^")

			Opcodes.NOP -> Unit

			Opcodes.ACONST_NULL -> TODO()

			in Opcodes.ICONST_M1..Opcodes.ICONST_5 -> const(AstType.INT, (op - Opcodes.ICONST_0).toInt())
			in Opcodes.LCONST_0..Opcodes.LCONST_1 -> const(AstType.LONG, (op - Opcodes.LCONST_0).toLong())
			in Opcodes.FCONST_0..Opcodes.FCONST_2 -> const(AstType.FLOAT, (op - Opcodes.FCONST_0).toFloat())
			in Opcodes.DCONST_0..Opcodes.DCONST_1 -> const(AstType.DOUBLE, (op - Opcodes.DCONST_0).toDouble())

			in Opcodes.IALOAD..Opcodes.SALOAD -> arrayLoad(TPRIM[op - Opcodes.IALOAD])
			in Opcodes.IASTORE..Opcodes.SASTORE -> arrayStore(TPRIM[op - Opcodes.IASTORE])

			Opcodes.POP -> stack.pop()
			Opcodes.POP2 -> TODO()
			Opcodes.DUP -> {
				val p = stack.pop()
				push(p)
				push(p)
			}
			Opcodes.DUP_X1 -> TODO()
			Opcodes.DUP_X2 -> TODO()
			Opcodes.DUP2 -> TODO()
			Opcodes.DUP2_X1 -> TODO()
			Opcodes.DUP2_X2 -> TODO()

			Opcodes.SWAP -> {
				val p1 = stack.pop()
				val p2 = stack.pop()
				push(p1)
				push(p2)
			}

			Opcodes.I2L -> conv(AstType.INT, AstType.LONG)
			Opcodes.I2F -> conv(AstType.INT, AstType.FLOAT)
			Opcodes.I2D -> conv(AstType.INT, AstType.DOUBLE)

			Opcodes.L2I -> conv(AstType.LONG, AstType.INT)
			Opcodes.L2F -> conv(AstType.LONG, AstType.FLOAT)
			Opcodes.L2D -> conv(AstType.LONG, AstType.DOUBLE)

			Opcodes.F2I -> conv(AstType.FLOAT, AstType.INT)
			Opcodes.F2L -> conv(AstType.FLOAT, AstType.LONG)
			Opcodes.F2D -> conv(AstType.FLOAT, AstType.DOUBLE)

			Opcodes.D2I -> conv(AstType.DOUBLE, AstType.INT)
			Opcodes.D2L -> conv(AstType.DOUBLE, AstType.LONG)
			Opcodes.D2F -> conv(AstType.DOUBLE, AstType.FLOAT)

			Opcodes.I2B -> conv(AstType.INT, AstType.BYTE)
			Opcodes.I2C -> conv(AstType.INT, AstType.CHAR)
			Opcodes.I2S -> conv(AstType.INT, AstType.SHORT)

			Opcodes.LCMP -> TODO()
			Opcodes.FCMPL -> TODO()
			Opcodes.FCMPG -> TODO()
			Opcodes.DCMPL -> TODO()
			Opcodes.DCMPG -> TODO()

			Opcodes.RETURN -> ret(AstType.VOID)
			in Opcodes.IRETURN..Opcodes.ARETURN -> ret(TPRIM[op - Opcodes.IRETURN])

			Opcodes.ARRAYLENGTH -> TODO()
			Opcodes.ATHROW -> {
				val ex = pop(AstType.OBJECT)
				stms += TIR.THROW(ex)
			}

			Opcodes.MONITORENTER -> TODO()
			Opcodes.MONITOREXIT -> TODO()
		}
	}

	private fun arrayStore(astType: AstType) {
		val value = pop(astType)
		val index = pop(AstType.INT)
		val array = pop(AstType.ARRAY(astType))
		stms += TIR.ASTORE(array, index, value)
	}

	private fun arrayLoad(astType: AstType) {
		val temp = createTemp()
		val array = pop(AstType.ARRAY(astType))
		val index = pop(AstType.INT)
		stms += TIR.ALOAD(temp, array, index)
		push(temp)
	}

	fun conv(src: AstType, dst: AstType) {
		val temp = createTemp()
		val srcVar = pop(src)
		stms += TIR.CONV(temp, srcVar, dst)
		push(temp)
	}

	fun ret(type: AstType) {
		if (type == AstType.VOID) {
			stms += TIR.RET(null)
		} else {
			stms += TIR.RET(pop(type))
		}
	}

	fun const(type: AstType, v: Number) {
		//val res = createTemp()
		//nodes += Node.CST(res, v)
		push(Constant(type, v))
	}

	fun load(type: AstType, idx: Int) {
		//val temp = createTemp()
		//stms += TOIR.MOV(temp, getVar(idx))
		//stack.push(temp)
		push(getVar(idx))
	}

	fun store(type: AstType, idx: Int) {
		stms += TIR.MOV(getVar(idx), pop(type))
	}

	fun binop(type: AstType, op: String) {
		val res = createTemp()
		val r = pop(type)
		val l = pop(type)
		stms += TIR.BINOP(res, l, op, r)
		push(res)
	}

	fun unop(type: AstType, op: String) {
		val res = createTemp()
		val r = pop(type)
		stms += TIR.UNOP(res, op, r)
		push(res)
	}
}