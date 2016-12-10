package com.jtransc.backend.asm2

import com.jtransc.ast.*
import com.jtransc.backend.JvmOpcode
import com.jtransc.backend.asm1.BasicBlock
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

interface TOIR {
	//val target: Local?
	//val sources: List<Local>
	var prev: TOIR?
	var next: TOIR?

	class Mixin : TOIR {
		//override val target: Local? = null
		//override val sources = listOf<Local>()
		override var prev: TOIR? = null
		override var next: TOIR? = null
	}

	data class LABEL(val label: Label) : TOIR by Mixin()
	data class THIS(val dst: Local, val thisType: AstType) : TOIR by Mixin()
	data class PHI(val dst: Local, val params: ArrayList<Operand> = arrayListOf()) : TOIR by Mixin()
	data class PARAM(val dst: Local, val paramIndex: Int, val paramType: AstType) : TOIR by Mixin()
	data class MOV(val dst: Local, val src: Operand) : TOIR by Mixin()
	//data class CST(val dst: Local, val value: Number) : TOIR by Mixin()
	data class BINOP(val dst: Local, val l: Operand, val op: String, val r: Operand) : TOIR by Mixin()
	data class UNOP(val dst: Local, val op: String, val r: Operand) : TOIR by Mixin()
	data class CONV(val dst: Local, val src: Operand, val dstType: AstType) : TOIR by Mixin()
	data class ASTORE(val array: Operand, val index: Operand, val value: Operand) : TOIR by Mixin()
	data class ALOAD(val dst: Local, val array: Operand, val index: Operand) : TOIR by Mixin()
	data class NEWARRAY(val temp: Local, val type: AstType, val len: Operand) : TOIR by Mixin()
	data class JUMP_IF(val label: Label, val l: Operand, val op: String, val r: Operand) : TOIR by Mixin()
	data class JUMP(val label: Label) : TOIR by Mixin()
	data class RET(val v: Operand?) : TOIR by Mixin()
	data class INVOKE(val dst: Local?, val obj: Local?, val method: AstMethodRef, val args: List<Operand>) : TOIR by Mixin()
}

interface Operand {
}

data class Local(val v: Int) : Operand {
}

data class Constant(val type: AstType, val v: Any?) : Operand {
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

	println("--------")
	for (i in method.instructions.toArray().toList()) {
		if (i in builder.nodesToBlocks) {
			for (stm in builder.nodesToBlocks[i]!!.stms) {
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
	val nodesToBlocks = hashMapOf<AbstractInsnNode, BasicBlockBuilder>()

	fun buildTree(start: AbstractInsnNode) {
		build(start, onePredecessor = null)
	}

	private fun build(start: AbstractInsnNode, onePredecessor: BasicBlockBuilder?) {
		val bbb1 = nodesToBlocks[start]
		if (bbb1 != null) { // Processed already!
			if (onePredecessor != null) {
				bbb1.registerPredecessor(onePredecessor)
			}
		} else {
			val bbb = BasicBlockBuilder(types, locals).apply {
				decodeBlock(start, onePredecessor)
			}
			nodesToBlocks[start] = bbb
			for (successor in bbb.allSuccessors) {
				build(successor, onePredecessor = bbb)
			}
		}
	}
}

class BasicBlockBuilder(val types: AstTypes, val locals: Locals) {
	val JUMP_OPS = listOf("==", "!=", "<", ">=", ">", "<=", "==", "!=")
	val TPRIM = listOf(AstType.INT, AstType.LONG, AstType.FLOAT, AstType.DOUBLE, AstType.OBJECT, AstType.BYTE, AstType.CHAR, AstType.SHORT)
	val stms = arrayListOf<TOIR>()
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
				stms += TOIR.PHI(phi)
				push(phi)
			}
		}
		if (predecessor !in predecessors) {
			predecessors += predecessor
			for ((index, item) in predecessor.stack.toList().withIndex()) {
				val phi = stms[index] as TOIR.PHI
				phi.params += item
			}
		}
	}

	fun decodeBlock(start: AbstractInsnNode, onePredecessor: BasicBlockBuilder?) {
		if (onePredecessor != null) registerPredecessor(onePredecessor)
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
			else -> TODO()
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
				stms += TOIR.JUMP_IF(n.label.label, value, op, Constant(AstType.INT, 0))
			}
			Opcodes.IFNULL, Opcodes.IFNONNULL -> {
				val value = stack.pop()
				stms += TOIR.JUMP_IF(n.label.label, value, if (n.opcode == Opcodes.IFNULL) "==" else "!=", Constant(AstType.OBJECT, null))
			}
			in Opcodes.IF_ICMPEQ..Opcodes.IF_ACMPNE -> {
				val op= JUMP_OPS[n.opcode - Opcodes.IFEQ]
				val valueL = stack.pop()
				val valueR = stack.pop()
				stms += TOIR.JUMP_IF(n.label.label, valueL, op, valueR)
			}
			Opcodes.GOTO -> {
				stms += TOIR.JUMP(n.label.label)
			}
		}
		//n.label
	}

	fun decodeIns(n: IincInsnNode) {
		stms += TOIR.BINOP(getVar(n.`var`), getVar(n.`var`), "+", Constant(AstType.INT, 1))
	}

	fun decodeIns(n: FrameNode) {
		Unit // Do nothing. We calculate frames ourselves to full compatibility with versions less than Java6.
	}

	fun decodeIns(n: LabelNode) {
		stms += TOIR.LABEL(n.label)
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

		stms += TOIR.INVOKE(res, obj, methodRef, args)
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
				stms += TOIR.NEWARRAY(temp, type, len)
				push(temp)
			}
		}
	}

	fun decodeIns(n: InsnNode) {
		when (n.opcode) {
			in Opcodes.INEG..Opcodes.DNEG -> unop(TPRIM[n.opcode - Opcodes.INEG], "-")

			in Opcodes.IADD..Opcodes.DADD -> binop(TPRIM[n.opcode - Opcodes.IADD], "+")
			in Opcodes.ISUB..Opcodes.DSUB -> binop(TPRIM[n.opcode - Opcodes.ISUB], "-")
			in Opcodes.IMUL..Opcodes.DMUL -> binop(TPRIM[n.opcode - Opcodes.IMUL], "*")
			in Opcodes.IDIV..Opcodes.DDIV -> binop(TPRIM[n.opcode - Opcodes.IDIV], "/")
			in Opcodes.IREM..Opcodes.DREM -> binop(TPRIM[n.opcode - Opcodes.IREM], "%")

			in Opcodes.ISHL..Opcodes.LSHL -> binop(TPRIM[n.opcode - Opcodes.ISHL], "<<")
			in Opcodes.ISHR..Opcodes.LSHR -> binop(TPRIM[n.opcode - Opcodes.ISHR], ">>")
			in Opcodes.IUSHR..Opcodes.LUSHR -> binop(TPRIM[n.opcode - Opcodes.IUSHR], ">>>")
			in Opcodes.IAND..Opcodes.LAND -> binop(TPRIM[n.opcode - Opcodes.IAND], "&")
			in Opcodes.IOR..Opcodes.LOR -> binop(TPRIM[n.opcode - Opcodes.IOR], "|")
			in Opcodes.IXOR..Opcodes.LXOR -> binop(TPRIM[n.opcode - Opcodes.IXOR], "^")

			Opcodes.NOP -> Unit

			Opcodes.ACONST_NULL -> TODO()

			in Opcodes.ICONST_M1..Opcodes.ICONST_5 -> const(AstType.INT, (n.opcode - Opcodes.ICONST_0).toInt())
			in Opcodes.LCONST_0..Opcodes.LCONST_1 -> const(AstType.LONG, (n.opcode - Opcodes.LCONST_0).toLong())
			in Opcodes.FCONST_0..Opcodes.FCONST_2 -> const(AstType.FLOAT, (n.opcode - Opcodes.FCONST_0).toFloat())
			in Opcodes.DCONST_0..Opcodes.DCONST_1 -> const(AstType.DOUBLE, (n.opcode - Opcodes.DCONST_0).toDouble())

			in Opcodes.IALOAD..Opcodes.SALOAD -> arrayLoad(TPRIM[n.opcode - Opcodes.IALOAD])
			in Opcodes.IASTORE..Opcodes.SASTORE -> arrayStore(TPRIM[n.opcode - Opcodes.IASTORE])

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
			in Opcodes.IRETURN..Opcodes.ARETURN -> ret(TPRIM[n.opcode - Opcodes.IRETURN])

			Opcodes.ARRAYLENGTH -> TODO()
			Opcodes.ATHROW -> TODO()

			Opcodes.MONITORENTER -> TODO()
			Opcodes.MONITOREXIT -> TODO()
		}
	}

	private fun arrayStore(astType: AstType) {
		val value = pop(astType)
		val index = pop(AstType.INT)
		val array = pop(AstType.ARRAY(astType))
		stms += TOIR.ASTORE(array, index, value)
	}

	private fun arrayLoad(astType: AstType) {
		val temp = createTemp()
		val array = pop(AstType.ARRAY(astType))
		val index = pop(AstType.INT)
		stms += TOIR.ALOAD(temp, array, index)
		push(temp)
	}

	fun conv(src: AstType, dst: AstType) {
		val temp = createTemp()
		val srcVar = pop(src)
		stms += TOIR.CONV(temp, srcVar, dst)
		push(temp)
	}

	fun ret(type: AstType) {
		if (type == AstType.VOID) {
			stms += TOIR.RET(null)
		} else {
			stms += TOIR.RET(pop(type))
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
		stms += TOIR.MOV(getVar(idx), pop(type))
	}

	fun binop(type: AstType, op: String) {
		val res = createTemp()
		val r = pop(type)
		val l = pop(type)
		stms += TOIR.BINOP(res, l, op, r)
		push(res)
	}

	fun unop(type: AstType, op: String) {
		val res = createTemp()
		val r = pop(type)
		stms += TOIR.UNOP(res, op, r)
		push(res)
	}
}