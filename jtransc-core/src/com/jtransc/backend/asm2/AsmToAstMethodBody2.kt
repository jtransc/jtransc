package com.jtransc.backend.asm2

import com.jtransc.ast.*
import com.jtransc.backend.JvmOpcode
import com.jtransc.backend.asm1.disasm
import com.jtransc.ds.Stack
import com.jtransc.error.invalidOp
import com.jtransc.org.objectweb.asm.Opcodes
import com.jtransc.org.objectweb.asm.tree.*
import java.util.*

interface BAF {
	//var prev: BAF? = null
	//var next: BAF? = null

	data class THIS(val dst: Local, val thisType: AstType) : BAF
	data class PARAM(val dst: Local, val paramIndex: Int, val paramType: AstType) : BAF
	data class MOV(val dst: Local, val src: Operand) : BAF
	data class CST(val dst: Local, val value: Number) : BAF
	data class BINOP(val dst: Local, val l: Operand, val op: String, val r: Operand) : BAF
	data class UNOP(val dst: Local, val op: String, val r: Operand) : BAF
	data class CONV(val dst: Local, val src: Operand, val dstType: AstType) :  BAF
	data class ASTORE(val array: Operand, val index: Operand, val value: Operand) :  BAF
	data class ALOAD(val dst: Local, val array: Operand, val index: Operand) :  BAF
	data class NEWARRAY(val temp: Local, val type: AstType, val len: Operand) : BAF
	data class RET(val v: Operand?) : BAF
}

interface Operand {
}

data class Local(val v: Int) : Operand {
}

data class Constant(val v: Number) : Operand {
}

class Definition {
}

// http://compilers.cs.uni-saarland.de/papers/bbhlmz13cc.pdf
// Simple and Efficient Construction of Static Single Assignment Form
fun AsmToAstMethodBody2(clazz: AstType.REF, method: MethodNode, types: AstTypes, source: String = "unknown.java"): AstBody {
	val body = AstToMethodBody(types)
	val methodType = types.demangleMethod(method.desc)

	body.decode(method.instructions.toArray().toList())
	println(body.nodes.joinToString("\n"))
	/*
	//val node = method.instructions.first
	//node.next
	for (tcb in method.tryCatchBlocks) {
		tcb.invisibleTypeAnnotations
	}
	*/
	return AstBody(
		types,
		AstStm.STMS(),
		methodType
	)
}

class AstToMethodBody(
	val types: AstTypes
) {
	fun decode(items: List<AbstractInsnNode>) {
		for (n in items) {
			decodeIns(n)
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
		TODO()
	}

	fun decodeIns(n: LookupSwitchInsnNode) {
		TODO()
	}

	fun decodeIns(n: InvokeDynamicInsnNode) {
		TODO()
	}

	fun decodeIns(n: LdcInsnNode) {
		TODO()
	}

	val JUMP_OPS = listOf("==", "!=", "<", ">=", ">", "<=", "==", "!=")

	fun decodeIns(n: JumpInsnNode) {
		when (n.opcode) {
			Opcodes.JSR -> TODO()
			in Opcodes.IFEQ..Opcodes.IFLE -> {
				JUMP_OPS[n.opcode - Opcodes.IFEQ]
				val value = stack.pop()
			}
			Opcodes.IFNULL, Opcodes.IFNONNULL -> {
				val value = stack.pop()
			}
			in Opcodes.IF_ICMPEQ..Opcodes.IF_ACMPNE -> {
				JUMP_OPS[n.opcode - Opcodes.IFEQ]
				val value2 = stack.pop()
				val value1 = stack.pop()
			}
			Opcodes.GOTO -> {
				Unit
			}
		}
		//n.label
	}

	fun decodeIns(n: IincInsnNode) {
		nodes += BAF.BINOP(getVar(n.`var`), getVar(n.`var`), "+", Constant(1))
	}

	fun decodeIns(n: FrameNode) {
		Unit
	}

	fun decodeIns(n: LabelNode) {
		Unit
	}

	fun decodeIns(n: LineNumberNode) {
		Unit
	}

	fun decodeIns(n: MethodInsnNode) {
		val owner = types.REF_INT(n.owner)
		val methodType = types.demangleMethod(n.desc)

		for (arg in methodType.args.reversed()) {
			pop(arg.type)
		}

		val res = createTemp()

		when (n.opcode) {
			Opcodes.INVOKEVIRTUAL, Opcodes.INVOKESPECIAL, Opcodes.INVOKEINTERFACE -> {
				pop(owner)
			}
			Opcodes.INVOKESTATIC -> {

			}
		}
		stack.push(res)
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
				nodes += BAF.NEWARRAY(temp, type, len)
				stack.push(temp)
			}
		}
	}

	val TPRIM = listOf(AstType.INT, AstType.LONG, AstType.FLOAT, AstType.DOUBLE, AstType.OBJECT, AstType.BYTE, AstType.CHAR, AstType.SHORT)

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
				stack.push(p)
				stack.push(p)
			}
			Opcodes.DUP_X1 -> TODO()
			Opcodes.DUP_X2 -> TODO()
			Opcodes.DUP2 -> TODO()
			Opcodes.DUP2_X1 -> TODO()
			Opcodes.DUP2_X2 -> TODO()

			Opcodes.SWAP -> {
				val p1 = stack.pop()
				val p2 = stack.pop()
				stack.push(p1)
				stack.push(p2)
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
			Opcodes.DCMPG  -> TODO()

			Opcodes.RETURN  -> ret(AstType.VOID)
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
		nodes += BAF.ASTORE(array, index, value)
	}

	private fun arrayLoad(astType: AstType) {
		val temp = createTemp()
		val array = pop(AstType.ARRAY(astType))
		val index = pop(AstType.INT)
		nodes += BAF.ALOAD(temp, array, index)
		stack.push(temp)
	}

	fun conv(src: AstType, dst: AstType) {
		val temp = createTemp()
		val srcVar = pop(src)
		nodes += BAF.CONV(temp, srcVar, dst)
		stack.push(temp)
	}

	val nodes = arrayListOf<BAF>()
	val stack = Stack<Operand>()

	fun getVar(v: Int) = Local(v)

	var tempId = 1000
	fun createTemp() = Local(tempId++)

	fun pop(type: AstType): Operand {
		return stack.pop()
	}

	fun ret(type: AstType) {
		if (type == AstType.VOID) {
			nodes += BAF.RET(null)
		} else {
			nodes += BAF.RET(pop(type))
		}
	}

	fun const(type: AstType, v: Number) {
		//val res = createTemp()
		//nodes += Node.CST(res, v)
		stack.push(Constant(v))
	}

	fun load(type: AstType, idx: Int) {
		val temp = createTemp()
		nodes += BAF.MOV(temp, getVar(idx))
		stack.push(temp)
	}

	fun store(type: AstType, idx: Int) {
		nodes += BAF.MOV(getVar(idx), pop(type))
	}

	fun binop(type: AstType, op: String) {
		val res = createTemp()
		val r = pop(type)
		val l = pop(type)
		nodes += BAF.BINOP(res, l, op, r)
		stack.push(res)
	}

	fun unop(type: AstType, op: String) {
		val res = createTemp()
		val r = pop(type)
		nodes += BAF.UNOP(res, op, r)
		stack.push(res)
	}
}