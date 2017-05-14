package com.jtransc.backend.asm2

import com.jtransc.ast.*
import com.jtransc.backend.isEnd
import com.jtransc.backend.isEndOfBasicBlock
import com.jtransc.backend.isStatic
import com.jtransc.ds.Stack
import com.jtransc.error.invalidOp
import com.jtransc.error.unsupported
import com.jtransc.log.log
import com.jtransc.org.objectweb.asm.Opcodes
import com.jtransc.org.objectweb.asm.Type
import com.jtransc.org.objectweb.asm.tree.*
import java.util.*

class LocalsBuilder {
	val locals = arrayListOf<AstType>()
	fun setLocalType(index: Int, type: AstType) {
		while (locals.size <= index) locals += AstType.UNKNOWN("empty local: setLocalType")
		locals[index] = type
	}

	fun setFrame(frame: BasicFrame) {
		for ((index, l) in frame.locals.withIndex()) setLocalType(index, l)
	}
}

data class BasicFrame(val locals: ArrayList<AstType>, val stack: List<Operand>) {
	//fun linkTo(target: BasicFrame) {
	//	for ((src, dst) in this.locals.zip(target.locals)) {
	//		if (dst is AstType.COMMON) {
	//			dst.add(src)
	//		}
	//	}
	//	for ((src, dst) in this.stack.zip(target.stack)) {
	//		if (dst.type is AstType.COMMON) {
	//			(dst.type as AstType.COMMON).add(src.type)
	//		}
	//	}
	//}
}

class BasicBlock(val types: AstTypes, val blockContext: BlockContext, val clazz: AstType.REF, val method: MethodNode, val inputFrame: BasicFrame) {
	//val CTYPES = listOf(AstBinop.EQ, AstBinop.NE, AstBinop.LT, AstBinop.GE, AstBinop.GT, AstBinop.LE, AstBinop.EQ, AstBinop.NE)
	val JUMP_OPS = listOf(AstBinop.EQ, AstBinop.NE, AstBinop.LT, AstBinop.GE, AstBinop.GT, AstBinop.LE, AstBinop.EQ, AstBinop.NE)
	val TPRIM = listOf(AstType.INT, AstType.LONG, AstType.FLOAT, AstType.DOUBLE, AstType.OBJECT, AstType.BYTE, AstType.CHAR, AstType.SHORT)
	//var first: TIR? = null
	//var tail: TIR? = null
	val stms = arrayListOf<TIR>()
	val stack = Stack<Operand>()
	val locals = LocalsBuilder()

	val predecessors = hashSetOf<BasicBlock>()

	val allInputFrames = arrayListOf(inputFrame)
	lateinit var outputFrame: BasicFrame


	fun getVar(type: AstType, idx: Int): Local {
		val stype = type.simplify()
		val type2 = if (stype.simplify() == AstType.OBJECT) locals.locals.getOrNull(idx) ?: stype else stype
		val type3 = if (type2.simplify() is AstType.UNKNOWN) AstType.OBJECT else type2
		val out = blockContext.getVar(type3, idx)
		if (out.type is AstType.UNKNOWN) {
			println("ASSERT UNKNOWN")
		}
		return out
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

	fun add(n: TIR) {
		//if (first == null) first = n
		//tail?.append(n)
		//tail = n
		stms.add(n)
	}

	fun push(v: Operand) {
		stack.push(v)
	}

	fun push(l: List<Operand>) {
		for (v in l) stack.push(v)
	}

	lateinit var start: AbstractInsnNode

	fun createTemp(type: AstType) = blockContext.createTemp(type)
	val allSuccessors = arrayListOf<AbstractInsnNode>()
	val jumpNodes = arrayListOf<AbstractInsnNode>()
	var nextDirectNode: AbstractInsnNode? = null

	//fun registerPredecessor(predecessor: BasicBlock) {
	//	if (predecessor !in predecessors) {
	//		predecessors += predecessor
//
	//		predecessor.outputFrame.linkTo(oneInputFrame)
	//	}
	//}

	fun <T, U> zipWithMissing(a: List<T>, b: List<U>, missingA: (Int) -> T, missingB: (Int) -> U): List<Pair<T, U>> {
		val size = Math.max(a.size, b.size)
		return (0 until size).map { a.getOrElse(it, missingA) to b.getOrElse(it, missingB) }
	}

	fun decodeBlock(start: AbstractInsnNode, onePredecessor: BasicBlock?) {
		this.start = start

		val isEntryNode = onePredecessor == null

		// @TODO: Remove this from here!
		if (isEntryNode) {
			val methodType = types.demangleMethod(method.desc)
			var varIndex = 0
			if (!method.isStatic()) {
				add(TIR.MOV(Local(clazz, varIndex), This(clazz)))
				locals.setLocalType(varIndex, AstType.COMMON(clazz))
				varIndex++
			}
			for (arg in methodType.args) {
				add(TIR.MOV(Local(arg.type, varIndex), Param(arg.type, arg.index)))
				locals.setLocalType(varIndex, AstType.COMMON(arg.type))
				varIndex += if (arg.type.isLongOrDouble()) 2 else 1
			}
		}

		//if (onePredecessor != null) registerPredecessor(onePredecessor)

		locals.setFrame(inputFrame)
		for (s in inputFrame.stack) {
			push(s)
		}

		var current: AbstractInsnNode = start
		while (true) {
			val isLastIns = current.next == null || current.next is LabelNode || current.isEndOfBasicBlock()
			decodeIns(current)
			if (isLastIns) {
				if (!current.isEnd()) {
					nextDirectNode = current.next
				}
				allSuccessors += jumpNodes
				if (nextDirectNode != null) allSuccessors += nextDirectNode!!

				if (!current.isEnd() && (current.next is LabelNode)) {
					add(TIR.JUMP((current.next as LabelNode).label))
				}
				break
			}
			current = current.next
		}

		val alreadyCreatedFrame = allSuccessors.map { blockContext.inputFrames[it] }.filterNotNull().firstOrNull()

		if (alreadyCreatedFrame != null) {
			outputFrame = alreadyCreatedFrame

			while (outputFrame.locals.size < locals.locals.size) outputFrame.locals += AstType.UNKNOWN("empty local merge")
			for ((index, src) in locals.locals.withIndex()) {
				val dst = outputFrame.locals[index]
				if (dst is AstType.COMMON) dst.add(src)
				if (dst is AstType.UNKNOWN) outputFrame.locals[index] = src
			}

			// Merge frame types!
			for ((src, dst) in zipWithMissing(locals.locals, outputFrame.locals, { AstType.UNKNOWN("locals empty (a)") }, { AstType.UNKNOWN("locals empty (b)") })) {
				if (dst is AstType.COMMON) dst.add(src)
			}
			for ((src, dst) in stack.toList().zip(outputFrame.stack)) {
				val dstType = dst.type
				if (dstType is AstType.COMMON) dstType.add(src.type)
			}
		} else {
			val frame = BasicFrame(ArrayList(locals.locals.toList().map {
				AstType.COMMON(it)
			}), stack.toList().map {
				createTemp(AstType.COMMON(it.type))
			})
			for (s in allSuccessors) blockContext.inputFrames[s] = frame
			outputFrame = frame
		}

		//println("outputFrame: $outputFrame")

		if (outputFrame.stack.isNotEmpty()) {
			val items = arrayListOf<TIR>()
			for ((v, op) in outputFrame.stack.zip(stack.toList()).reversed()) {
				if (v is Local) {
					if (v.type.simplify() is AstType.UNKNOWN) {
						println("AstType.UNKNOWN!!!")
					}
					items.add(TIR.MOV(v, op))
				}
			}
			stms.addAll(stms.size - 1, items)
		}
	}

	fun decodeIns(n: AbstractInsnNode) {
		//println(JvmOpcode.disasm(n))
		when (n) {
			is FrameNode -> decodeFrameNode(n)
			is JumpInsnNode -> decodeJumpNode(n)
			is LdcInsnNode -> decodeLdcNode(n)
			is LookupSwitchInsnNode -> decodeLookupSwitchNode(n)
			is TableSwitchInsnNode -> decodeTableSwitchNode(n)
			is MultiANewArrayInsnNode -> decodeMultiANewArrayNode(n)
			is InvokeDynamicInsnNode -> decodeInvokeDynamicNode(n)
			is IincInsnNode -> decodeIincNode(n)
			is LabelNode -> decodeLabelNode(n)
			is LineNumberNode -> decodeLineNode(n)
			is VarInsnNode -> decodeVarNode(n)
			is IntInsnNode -> decodeIntNode(n)
			is InsnNode -> decodeInsNode(n)
			is MethodInsnNode -> decodeMethodNode(n)
			is TypeInsnNode -> decodeTypeNode(n)
			is FieldInsnNode -> decodeFieldNode(n)
			else -> TODO("$n")
		}
	}

	fun decodeFieldNode(n: FieldInsnNode) {
		val owner = types.REF_INT2(n.owner)
		val op = n.opcode
		val fieldRef = AstFieldRef(owner.name, n.name, types.demangle(n.desc))
		when (op) {
			Opcodes.GETSTATIC -> {
				val dst = createTemp(fieldRef.type)
				add(TIR.GETSTATIC(dst, fieldRef))
				push(dst)
			}
			Opcodes.PUTSTATIC -> {
				val src = pop()
				add(TIR.PUTSTATIC(fieldRef, src))
			}
			Opcodes.GETFIELD -> {
				val obj = pop()
				val dst = createTemp(fieldRef.type)
				add(TIR.GETFIELD(dst, fieldRef, obj))
				push(dst)
			}
			Opcodes.PUTFIELD -> {
				val src = pop()
				val obj = pop()
				add(TIR.PUTFIELD(fieldRef, obj, src))
			}
		}
	}

	fun decodeTypeNode(n: TypeInsnNode) {
		val type = types.REF_INT(n.desc)
		when (n.opcode) {
			Opcodes.NEW -> {
				val dst = createTemp(type)
				add(TIR.NEW(dst, type as AstType.REF))
				push(dst)
			}
			Opcodes.ANEWARRAY -> {
				val arrayType = AstType.ARRAY(type)
				val len = pop()
				val dst = createTemp(arrayType)
				add(TIR.NEWARRAY(dst, arrayType, listOf(len)))
				push(dst)
			}
			Opcodes.CHECKCAST -> {
				val obj = pop()
				val dst = createTemp(type)
				add(TIR.CONV(dst, obj, type))
				push(dst)
			}
			Opcodes.INSTANCEOF -> {
				val dst = createTemp(AstType.BOOL)
				val obj = pop()
				add(TIR.INSTANCEOF(dst, type, obj))
				push(dst)
			}
		}
	}

	fun decodeMultiANewArrayNode(n: MultiANewArrayInsnNode) {
		val arrayType = types.REF_INT(n.desc).asArray()
		val dst = createTemp(arrayType)
		add(TIR.NEWARRAY(dst, arrayType, (0 until n.dims).map { pop() }.reversed()))
		push(dst)
	}

	fun decodeTableSwitchNode(n: TableSwitchInsnNode) {
		nextDirectNode = n.dflt
		jumpNodes += n.labels

		val subject = pop()

		add(TIR.SWITCH_GOTO(
			subject,
			n.dflt.label,
			n.labels.withIndex().map { (n.min + it.index) to it.value.label }.toMap()
		))
	}

	fun decodeLookupSwitchNode(n: LookupSwitchInsnNode) {
		nextDirectNode = n.dflt
		jumpNodes += n.labels

		val subject = pop()

		add(TIR.SWITCH_GOTO(
			subject,
			n.dflt.label,
			n.keys.zip(n.labels).map { it.first to it.second.label }.toMap()
		))
	}

	fun decodeInvokeDynamicNode(n: InvokeDynamicInsnNode) {
		blockContext.hasInvokeDynamic = true
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

	fun decodeLdcNode(n: LdcInsnNode) {
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

	fun decodeJumpNode(n: JumpInsnNode) {
		jumpNodes += n.label

		when (n.opcode) {
			Opcodes.JSR -> unsupported("JSR/RET")
			in Opcodes.IFEQ..Opcodes.IFLE -> {
				val op = JUMP_OPS[n.opcode - Opcodes.IFEQ]
				val value = stack.pop()
				add(TIR.JUMP_IF(n.label.label, value, op, Constant(AstType.INT, 0)))
			}
			Opcodes.IFNULL, Opcodes.IFNONNULL -> {
				val op = JUMP_OPS[n.opcode - Opcodes.IFNULL]
				val value = stack.pop()
				add(TIR.JUMP_IF(n.label.label, value, op, Constant(AstType.OBJECT, null)))
			}
			in Opcodes.IF_ICMPEQ..Opcodes.IF_ACMPNE -> {
				val op = JUMP_OPS[n.opcode - Opcodes.IF_ICMPEQ]
				val valueR = stack.pop()
				val valueL = stack.pop()
				add(TIR.JUMP_IF(n.label.label, valueL, op, valueR))
			}
			Opcodes.GOTO -> {
				add(TIR.JUMP(n.label.label))
			}
		}
		//n.label
	}

	private fun emptyStack() {
		if (stack.isNotEmpty()) {
			log.warn("Stack is not empty on ATHROW or RETURN")
		}
		while (stack.isNotEmpty()) stack.pop()
	}

	fun decodeIincNode(n: IincInsnNode) {
		add(TIR.BINOP(getVar(AstType.INT, n.`var`), getVar(AstType.INT, n.`var`), AstBinop.ADD, Constant(AstType.INT, 1)))
	}

	//private fun getLocalType(index: Int) = locals.getOrElse(index) { AstType.UNKNOWN }

	fun decodeFrameNode(n: FrameNode) {
		//if (true) {
		//	// Do nothing. We calculate frames ourselves to full compatibility with versions less than Java6.
		//} else {
		//	// Take advantage of frames to get information without calculcating it
		//	for ((index, l) in n.local.withIndex()) {
		//		when (l) {
		//			is Int -> {
		//				when (l) {
		//					Opcodes.TOP -> locals.setLocalType(index, AstType.UNKNOWN) // UNUSED SLOTS
		//					Opcodes.INTEGER -> locals.setLocalType(index, AstType.INT)
		//					Opcodes.FLOAT -> locals.setLocalType(index, AstType.FLOAT)
		//					Opcodes.DOUBLE -> locals.setLocalType(index, AstType.DOUBLE)
		//					Opcodes.LONG -> locals.setLocalType(index, AstType.LONG)
		//					Opcodes.NULL -> locals.setLocalType(index, AstType.OBJECT)
		//					Opcodes.UNINITIALIZED_THIS -> locals.setLocalType(index, this.clazz)
		//					else -> TODO("Type $l")
		//				}
		//			}
		//			is String -> locals.setLocalType(index, types.REF_INT(l))
		//			is LabelNode -> TODO("LabelNode")
		//			else -> TODO("Type $l ${l.javaClass}")
		//		}
		//	}
		//}
	}

	fun decodeLabelNode(n: LabelNode) {
		add(TIR.LABEL(n.label))

		//Unit // Do nothing. We handle basic blocks in other place.
	}

	fun decodeLineNode(n: LineNumberNode) {
		Unit
	}

	fun decodeMethodNode(n: MethodInsnNode) {
		val isSpecial = n.opcode == Opcodes.INVOKESPECIAL
		val ownerType = types.REF_INT(n.owner)
		val methodType = types.demangleMethod(n.desc)
		val ownerTypeRef = if (ownerType is AstType.ARRAY) AstType.OBJECT else ownerType as AstType.REF
		val methodRef = AstMethodRef(ownerTypeRef.name, n.name, methodType)
		val args = methodType.args.map { pop() }.reversed()
		val obj = if (n.opcode != Opcodes.INVOKESTATIC) pop() else null
		if (methodType.retVoid) {
			add(TIR.INVOKE_VOID(obj, methodRef, args, isSpecial))
		} else {
			val dst = createTemp(methodType.ret)
			add(TIR.INVOKE(dst, obj, methodRef, args, isSpecial))
			push(dst)
		}
	}

	fun decodeVarNode(n: VarInsnNode) {
		when (n.opcode) {
			in Opcodes.ILOAD..Opcodes.ALOAD -> load(TPRIM[n.opcode - Opcodes.ILOAD], n.`var`)
			in Opcodes.ISTORE..Opcodes.ASTORE -> store(TPRIM[n.opcode - Opcodes.ISTORE], n.`var`)
			Opcodes.RET -> unsupported("JSR/RET")
		}
	}

	fun decodeIntNode(n: IntInsnNode) {
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
				val len = pop()
				val dst = createTemp(arrayType)
				add(TIR.NEWARRAY(dst, arrayType, listOf(len)))
				push(dst)
			}
		}
	}

	fun decodeInsNode(n: InsnNode) {
		val op = n.opcode
		when (op) {
			Opcodes.NOP -> Unit

			in Opcodes.INEG..Opcodes.DNEG -> unop(TPRIM[op - Opcodes.INEG], AstUnop.NEG)

			in Opcodes.IADD..Opcodes.DADD -> binop(TPRIM[op - Opcodes.IADD], AstBinop.ADD)
			in Opcodes.ISUB..Opcodes.DSUB -> binop(TPRIM[op - Opcodes.ISUB], AstBinop.SUB)
			in Opcodes.IMUL..Opcodes.DMUL -> binop(TPRIM[op - Opcodes.IMUL], AstBinop.MUL)
			in Opcodes.IDIV..Opcodes.DDIV -> binop(TPRIM[op - Opcodes.IDIV], AstBinop.DIV)
			in Opcodes.IREM..Opcodes.DREM -> binop(TPRIM[op - Opcodes.IREM], AstBinop.REM)

			in Opcodes.ISHL..Opcodes.LSHL -> binop(TPRIM[op - Opcodes.ISHL], AstBinop.SHL)
			in Opcodes.ISHR..Opcodes.LSHR -> binop(TPRIM[op - Opcodes.ISHR], AstBinop.SHR)
			in Opcodes.IUSHR..Opcodes.LUSHR -> binop(TPRIM[op - Opcodes.IUSHR], AstBinop.USHR)
			in Opcodes.IAND..Opcodes.LAND -> binop(TPRIM[op - Opcodes.IAND], AstBinop.AND)
			in Opcodes.IOR..Opcodes.LOR -> binop(TPRIM[op - Opcodes.IOR], AstBinop.OR)
			in Opcodes.IXOR..Opcodes.LXOR -> binop(TPRIM[op - Opcodes.IXOR], AstBinop.XOR)

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

			Opcodes.L2I, Opcodes.F2I, Opcodes.D2I -> conv(AstType.INT)
			Opcodes.I2L, Opcodes.F2L, Opcodes.D2L -> conv(AstType.LONG)
			Opcodes.I2F, Opcodes.L2F, Opcodes.D2F -> conv(AstType.FLOAT)
			Opcodes.I2D, Opcodes.L2D, Opcodes.F2D -> conv(AstType.DOUBLE)

			Opcodes.I2B -> conv(AstType.BYTE)
			Opcodes.I2C -> conv(AstType.CHAR)
			Opcodes.I2S -> conv(AstType.SHORT)

			Opcodes.LCMP -> binop(AstType.INT, AstBinop.LCMP)
			Opcodes.FCMPL, Opcodes.DCMPL -> binop(AstType.INT, AstBinop.CMPL)
			Opcodes.FCMPG, Opcodes.DCMPG -> binop(AstType.INT, AstBinop.CMPG)

			Opcodes.RETURN -> {
				ret(AstType.VOID)
				emptyStack()
			}
			in Opcodes.IRETURN..Opcodes.ARETURN -> {
				ret(TPRIM[op - Opcodes.IRETURN])
				emptyStack()
			}

			Opcodes.ARRAYLENGTH -> {
				val array = pop()
				val dst = createTemp(AstType.INT)
				add(TIR.ARRAYLENGTH(dst, array))
				push(dst)
			}
			Opcodes.ATHROW -> {
				val ex = pop()
				add(TIR.THROW(ex))
				emptyStack()
			}
			Opcodes.MONITORENTER, Opcodes.MONITOREXIT -> {
				val obj = pop()
				add(TIR.MONITOR(obj, enter = (op == Opcodes.MONITORENTER)))
			}
		}
	}

	private fun arrayStore(elementType: AstType) {
		//val arrayType = AstType.ARRAY(elementType)
		val value = pop()
		val index = pop()
		val array = pop()
		if (array.type.simplify() !is AstType.ARRAY) {
			println("ARRAY not array type: ${array.type}")
		}
		add(TIR.ARRAY_STORE(array, elementType, index, value))
	}

	private fun arrayLoad(elementType: AstType) {
		//val arrayType = AstType.ARRAY(elementType)
		val index = pop()
		val array = pop()
		val dst = createTemp(elementType)
		add(TIR.ARRAY_LOAD(dst, array, elementType, index))
		push(dst)
	}

	fun conv(dstType: AstType) {
		val srcVar = pop()
		val dst = createTemp(dstType)
		add(TIR.CONV(dst, srcVar, dstType))
		push(dst)
	}

	fun ret(type: AstType) {
		if (type == AstType.VOID) {
			add(TIR.RET(null))
		} else {
			add(TIR.RET(pop()))
		}
	}

	fun const(type: AstType, v: Any?) = push(Constant(type, v))
	fun load(type: AstType, idx: Int) {
		push(getVar(type, idx))
	}

	fun store(type: AstType, idx: Int) {
		val res = pop()
		val type2 = res.type
		val v = getVar(type2, idx)
		if (v.type.simplify() is AstType.UNKNOWN) {
			println("AstType.UNKNOWN!!!")
		}
		locals.setLocalType(idx, type2)
		add(TIR.MOV(v, res))
	}

	fun binop(resultType: AstType, op: AstBinop) {
		val r = pop()
		val l = pop()
		val dst = createTemp(resultType)
		add(TIR.BINOP(dst, l, op, r))
		push(dst)
	}

	fun unop(type: AstType, op: AstUnop) {
		val r = pop()
		val dst = createTemp(type)
		add(TIR.UNOP(dst, op, r))
		push(dst)
	}
}