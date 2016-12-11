package com.jtransc.backend.asm2

import com.jtransc.ast.*
import com.jtransc.backend.JvmOpcode
import com.jtransc.backend.asm1.disasm
import com.jtransc.backend.ast
import com.jtransc.backend.isEnd
import com.jtransc.backend.isEndOfBasicBlock
import com.jtransc.ds.Stack
import com.jtransc.ds.cast
import com.jtransc.error.invalidOp
import com.jtransc.error.noImpl
import com.jtransc.error.unsupported
import com.jtransc.org.objectweb.asm.Handle
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
	data class NEWARRAY(val temp: Local, val arrayType: AstType, val lens: List<Operand>) : TIR by Mixin()
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
	data class ARRAYLENGTH(val dst: Local, val obj: Operand) : TIR by Mixin()
	data class MONITOR(val dst: Operand, val enter: Boolean) : TIR by Mixin()
	data class SWITCH_GOTO(val subject: Operand, val label: Label?, val toMap: Map<Int, Label>) : TIR by Mixin()
}

interface Operand {
	val type: AstType
}

data class Local(override val type: AstType, val v: Int) : Operand {
}

data class Constant(override val type: AstType, val v: Any?) : Operand {
}

data class CatchException(override val type: AstType) : Operand {
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

class BlockContext {
	var hasInvokeDynamic = false
	var tempId = 1000
	fun createTemp(type: AstType) = Local(type, tempId++)
}

class BlockCfgBuilder(val types: AstTypes) {
	val locals = BlockContext()
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

class BasicBlockBuilder(val types: AstTypes, val blockContext: BlockContext) {
	val JUMP_OPS = listOf("==", "!=", "<", ">=", ">", "<=", "==", "!=")
	val TPRIM = listOf(AstType.INT, AstType.LONG, AstType.FLOAT, AstType.DOUBLE, AstType.OBJECT, AstType.BYTE, AstType.CHAR, AstType.SHORT)
	val stms = arrayListOf<TIR>()
	val stack = Stack<Operand>()

	fun pop(type: AstType): Operand {
		return stack.pop()
	}

	fun pop(): Operand {
		return stack.pop()
	}

	fun pop2(): List<Operand> {
		val v1 = stack.pop()
		if (v1.type.isLongOrDouble()) {
			return listOf(v1)
		} else {
			val v2 = stack.pop()
			return listOf(v2, v1)
		}
	}

	fun push(v: Operand) {
		stack.push(v)
	}

	fun push(l: List<Operand>) {
		for (v in l) stack.push(v)
	}

	fun getVar(type: AstType, v: Int) = Local(type, v)

	fun createTemp(type: AstType) = blockContext.createTemp(type)
	val allSuccessors = arrayListOf<AbstractInsnNode>()
	val jumpNodes = arrayListOf<AbstractInsnNode>()
	var nextDirectNode: AbstractInsnNode? = null

	val predecessors = hashSetOf<BasicBlockBuilder>()

	fun registerPredecessor(predecessor: BasicBlockBuilder) {
		if (predecessors.isEmpty()) {
			for (item in predecessor.stack.toList()) {
				val phi = createTemp(item.type)
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
				val dst = createTemp(fieldRef.type)
				stms += TIR.GETSTATIC(dst, fieldRef)
				push(dst)
			}
			Opcodes.PUTSTATIC -> {
				val src = pop(owner)
				stms += TIR.PUTSTATIC(fieldRef, src)
			}
			Opcodes.GETFIELD -> {
				val dst = createTemp(fieldRef.type)
				val obj = pop(owner)
				stms += TIR.GETFIELD(dst, fieldRef, obj)
				push(dst)
			}
			Opcodes.PUTFIELD -> {
				val dst = createTemp(fieldRef.type)
				val src = pop(owner)
				val obj = pop(owner)
				stms += TIR.PUTFIELD(fieldRef, obj, src)
				push(dst)
			}
		}
	}

	fun decodeIns(n: TypeInsnNode) {
		val type = types.REF_INT(n.desc)
		when (n.opcode) {
			Opcodes.NEW -> {
				val dst = createTemp(type)
				stms += TIR.NEW(dst, type)
				push(dst)
			}
			Opcodes.ANEWARRAY -> {
				val arrayType = AstType.ARRAY(type)
				val len = pop(AstType.INT)
				val dst = createTemp(arrayType)
				stms += TIR.NEWARRAY(dst, arrayType, listOf(len))
				push(dst)
			}
			Opcodes.CHECKCAST -> {
				val obj = pop()
				val dst = createTemp(obj.type)
				stms += TIR.CHECKCAST(dst, type, obj)
				push(dst)
			}
			Opcodes.INSTANCEOF -> {
				val dst = createTemp(AstType.BOOL)
				val obj = pop(AstType.OBJECT)
				stms += TIR.INSTANCEOF(dst, type, obj)
				push(dst)
			}
		}
	}

	fun decodeIns(n: MultiANewArrayInsnNode) {
		val arrayType = types.REF_INT(n.desc) as AstType.ARRAY
		val dst = createTemp(arrayType)
		stms += TIR.NEWARRAY(dst, arrayType, (0 until n.dims).map { pop(AstType.INT) }.reversed())
		push(dst)
	}

	fun decodeIns(n: TableSwitchInsnNode) {
		nextDirectNode = n.dflt
		jumpNodes += n.labels

		val subject = pop()

		stms += TIR.SWITCH_GOTO(
			subject,
			n.dflt.label,
			n.labels.withIndex().map { (n.min + it.index) to it.value.label }.toMap()
		)
	}

	fun decodeIns(n: LookupSwitchInsnNode) {
		nextDirectNode = n.dflt
		jumpNodes += n.labels

		val subject = pop()

		stms += TIR.SWITCH_GOTO(
			subject,
			n.dflt.label,
			n.keys.zip(n.labels).map { it.first to it.second.label }.toMap()
		)
	}

	fun decodeIns(n: InvokeDynamicInsnNode) {
		//hasInvokeDynamic = true
		//val dynamicResult = AstExprUtils.INVOKE_DYNAMIC(
		//	AstMethodWithoutClassRef(i.name, types.demangleMethod(i.desc)),
		//	i.bsm.ast(types),
		//	i.bsmArgs.map {
		//		when (it) {
		//			is Type -> when (it.sort) {
		//				Type.METHOD -> AstExpr.LITERAL(types.demangleMethod(it.descriptor), types)
		//				else -> noImpl("${it.sort} : $it")
		//			}
		//			is Handle -> {
		//				val kind = AstMethodHandle.Kind.fromId(it.tag)
		//				val type = types.demangleMethod(it.desc)
		//				AstExpr.LITERAL(AstMethodHandle(type, AstMethodRef(FqName.fromInternal(it.owner), it.name, type), kind), types)
		//			}
		//			else -> AstExpr.LITERAL(it, types)
		//		}
		//	}
		//)
		//if (dynamicResult is AstExpr.INVOKE_DYNAMIC_METHOD) {
		//	// dynamicResult.startArgs = stackPopToLocalsCount(dynamicResult.extraArgCount).map { AstExpr.LOCAL(it) }.reversed()
		//	dynamicResult.startArgs = (0 until dynamicResult.extraArgCount).map { stackPop() }.reversed()
		//}
		//stackPush(dynamicResult)
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
			Opcodes.JSR -> unsupported("JSR/RET")
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
		stms += TIR.BINOP(getVar(AstType.INT, n.`var`), getVar(AstType.INT, n.`var`), "+", Constant(AstType.INT, 1))
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

		val res = if (methodType.retVoid) null else createTemp(methodType.ret)

		stms += TIR.INVOKE(res, obj, methodRef, args)
		if (res != null) push(res)
	}

	fun decodeIns(n: VarInsnNode) {
		when (n.opcode) {
			in Opcodes.ILOAD..Opcodes.ALOAD -> load(TPRIM[n.opcode - Opcodes.ILOAD], n.`var`)
			in Opcodes.ISTORE..Opcodes.ASTORE -> store(TPRIM[n.opcode - Opcodes.ISTORE], n.`var`)
			Opcodes.RET -> unsupported("JSR/RET")
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
				val arrayType = AstType.ARRAY(type)
				val len = pop(AstType.INT)
				val dst = createTemp(arrayType)
				stms += TIR.NEWARRAY(dst, type, listOf(len))
				push(dst)
			}
		}
	}

	fun decodeIns(n: InsnNode) {
		val op = n.opcode
		when (op) {
			Opcodes.NOP -> Unit

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

			Opcodes.ACONST_NULL -> const(AstType.OBJECT, null)
			in Opcodes.ICONST_M1..Opcodes.ICONST_5 -> const(AstType.INT, (op - Opcodes.ICONST_0).toInt())
			in Opcodes.LCONST_0..Opcodes.LCONST_1 -> const(AstType.LONG, (op - Opcodes.LCONST_0).toLong())
			in Opcodes.FCONST_0..Opcodes.FCONST_2 -> const(AstType.FLOAT, (op - Opcodes.FCONST_0).toFloat())
			in Opcodes.DCONST_0..Opcodes.DCONST_1 -> const(AstType.DOUBLE, (op - Opcodes.DCONST_0).toDouble())

			in Opcodes.IALOAD..Opcodes.SALOAD -> arrayLoad(TPRIM[op - Opcodes.IALOAD])
			in Opcodes.IASTORE..Opcodes.SASTORE -> arrayStore(TPRIM[op - Opcodes.IASTORE])

			Opcodes.POP -> pop()
			Opcodes.POP2 -> pop2()

			Opcodes.DUP -> {
				val p = pop()
				push(p)
				push(p)
			}
			Opcodes.DUP2 -> {
				val p = pop2()
				push(p)
				push(p)
			}
			Opcodes.DUP_X1 -> {
				val p1 = pop()
				val p2 = pop()
				push(p1)
				push(p2)
				push(p1)
			}
			Opcodes.DUP_X2 -> {
				val p1 = pop()
				val p23 = pop2()
				push(p1)
				push(p23)
				push(p1)
			}
			Opcodes.DUP2_X1 -> {
				val p12 = pop2()
				val p3 = pop()
				push(p12)
				push(p3)
				push(p12)

			}
			Opcodes.DUP2_X2 -> {
				val p12 = pop2()
				val p34 = pop2()
				push(p12)
				push(p34)
				push(p12)
			}
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

			Opcodes.LCMP -> binop(AstType.INT, "cmp")
			Opcodes.FCMPL, Opcodes.DCMPL -> binop(AstType.INT, "cmpl")
			Opcodes.FCMPG, Opcodes.DCMPG -> binop(AstType.INT, "cmpg")

			Opcodes.RETURN -> ret(AstType.VOID)
			in Opcodes.IRETURN..Opcodes.ARETURN -> ret(TPRIM[op - Opcodes.IRETURN])

			Opcodes.ARRAYLENGTH -> {
				val array = pop()
				val dst = createTemp(AstType.INT)
				stms += TIR.ARRAYLENGTH(dst, array)
				push(dst)
			}
			Opcodes.ATHROW -> {
				val ex = pop(AstType.OBJECT)
				stms += TIR.THROW(ex)
			}
			Opcodes.MONITORENTER, Opcodes.MONITOREXIT -> {
				val obj = pop()
				stms += TIR.MONITOR(obj, enter = (op == Opcodes.MONITORENTER))
			}
		}
	}

	private fun arrayStore(elementType: AstType) {
		val arrayType = AstType.ARRAY(elementType)
		val value = pop(elementType)
		val index = pop(AstType.INT)
		val array = pop(arrayType)
		stms += TIR.ASTORE(array, index, value)
	}

	private fun arrayLoad(elementType: AstType) {
		val arrayType = AstType.ARRAY(elementType)
		val index = pop(AstType.INT)
		val array = pop(arrayType)
		val dst = createTemp(elementType)
		stms += TIR.ALOAD(dst, array, index)
		push(dst)
	}

	fun conv(src: AstType, dstType: AstType) {
		val srcVar = pop(src)
		val dst = createTemp(dstType)
		stms += TIR.CONV(dst, srcVar, dstType)
		push(dst)
	}

	fun ret(type: AstType) {
		if (type == AstType.VOID) {
			stms += TIR.RET(null)
		} else {
			stms += TIR.RET(pop(type))
		}
	}

	fun const(type: AstType, v: Any?) {
		//val res = createTemp()
		//nodes += Node.CST(res, v)
		push(Constant(type, v))
	}

	fun load(type: AstType, idx: Int) {
		//val temp = createTemp()
		//stms += TOIR.MOV(temp, getVar(idx))
		//stack.push(temp)
		push(getVar(type, idx))
	}

	fun store(type: AstType, idx: Int) {
		stms += TIR.MOV(getVar(type, idx), pop(type))
	}

	fun binop(resultType: AstType, op: String) {
		val r = pop()
		val l = pop()
		val dst = createTemp(resultType)
		stms += TIR.BINOP(dst, l, op, r)
		push(dst)
	}

	fun unop(type: AstType, op: String) {
		val r = pop(type)
		val dst = createTemp(type)
		stms += TIR.UNOP(dst, op, r)
		push(dst)
	}
}