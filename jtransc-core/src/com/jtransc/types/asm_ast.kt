package com.jtransc.types

import com.jtransc.ast.*
import com.jtransc.ds.cast
import com.jtransc.error.*
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import java.util.*

val Handle.ast: AstMethodRef get() = AstMethodRef(this.owner.fqname, this.name, AstType.demangleMethod(this.desc))

fun Asm2Ast(method: MethodNode): AstBody = _Asm2Ast(method).call()

private class _Asm2Ast(method: MethodNode) {
	//val list = method.instructions
	val stms = ArrayList<AstStm>()
	val stack = Stack<AstExpr>()
	val tryCatchBlocks = method.tryCatchBlocks.cast<TryCatchBlockNode>()
	val i = method.instructions.first
	val locals = hashSetOf<AstLocal>()
	val labels = hashMapOf<LabelNode, AstLabel>()

	fun getType(value: Any?): AstType {
		return when (value) {
			is Int -> AstType.INT
			is String -> AstType.STRING // Or custom type?
		//else -> AstType.UNKNOWN
			else -> {
				throw InvalidOperationException("$value")
			}
		}
	}

	fun local(type: AstType, index: Int): AstLocal {
		val local = AstLocal(index, "local$index", type)
		locals.add(local)
		return local
	}

	var tempLocalId = 1000
	fun tempLocal(type: AstType): AstLocal {
		return local(type, tempLocalId++)
	}


	fun label(label: LabelNode): AstLabel {
		if (label !in labels) labels[label] = AstLabel("label_${label.label}")
		return labels[label]!!
	}

	fun stmAdd(s: AstStm) {
		assert(stack.size == 0)
		stms.add(s)
	}

	fun stackPush(e: AstExpr) {
		stack.push(e)
	}

	fun stackPop(): AstExpr = stack.pop()

	fun handleField(i: FieldInsnNode) {
		val isStatic = (i.opcode == Opcodes.GETSTATIC) || (i.opcode == Opcodes.PUTSTATIC)
		val ref = AstFieldRef(AstType.REF_INT2(i.owner).fqname.fqname, i.name, com.jtransc.ast.AstType.demangle(i.desc), isStatic)
		when (i.opcode) {
			Opcodes.GETSTATIC -> stackPush(AstExpr.STATIC_FIELD_ACCESS(ref))
			Opcodes.GETFIELD -> stackPush(AstExpr.INSTANCE_FIELD_ACCESS(ref, stackPop()))
			Opcodes.PUTSTATIC -> stmAdd(AstStm.SET_FIELD_STATIC(ref, stackPop()))

			Opcodes.PUTFIELD -> {
				stmAdd(AstStm.SET_FIELD_INSTANCE(ref, stackPop(), stackPop()))
				mustValidate("CHECK STACK ORDER!")
			}
			else -> invalidOp
		}
	}

	val PTYPES = listOf(AstType.INT, AstType.LONG, AstType.FLOAT, AstType.DOUBLE, AstType.OBJECT)

	fun handleInsn(i: InsnNode): Unit {
		val op = i.opcode
		when (i.opcode) {
			Opcodes.NOP -> stmAdd(AstStm.NOP);
			Opcodes.ACONST_NULL -> stackPush(AstExpr.LITERAL(null))
			in Opcodes.ICONST_M1..Opcodes.ICONST_5 -> stackPush(AstExpr.LITERAL((op - Opcodes.ICONST_0).toInt()))
			in Opcodes.LCONST_0..Opcodes.LCONST_1 -> stackPush(AstExpr.LITERAL((op - Opcodes.LCONST_0).toLong()))
			in Opcodes.FCONST_0..Opcodes.FCONST_2 -> stackPush(AstExpr.LITERAL((op - Opcodes.FCONST_0).toFloat()))
			in Opcodes.DCONST_0..Opcodes.DCONST_1 -> stackPush(AstExpr.LITERAL((op - Opcodes.DCONST_0).toDouble()))
			Opcodes.IALOAD, Opcodes.LALOAD, Opcodes.FALOAD, Opcodes.DALOAD, Opcodes.AALOAD, Opcodes.BALOAD, Opcodes.CALOAD, Opcodes.SALOAD -> {
				stackPush(AstExpr.ARRAY_ACCESS(stackPop(), stackPop()))
			}
			Opcodes.IASTORE, Opcodes.LASTORE, Opcodes.FASTORE, Opcodes.DASTORE, Opcodes.AASTORE, Opcodes.BASTORE, Opcodes.CASTORE, Opcodes.SASTORE -> {
				stmAdd(AstStm.SET_ARRAY(stackPop(), stackPop(), stackPop()))
			}
			Opcodes.POP -> stackPop()
			Opcodes.POP2 -> {
				stackPop()
				stackPop()
			}
			Opcodes.DUP -> {
				val value = stackPop()
				val local = tempLocal(value.type)

				stmAdd(AstStm.SET(local, value))
				stackPush(AstExpr.LOCAL(local))
				stackPush(AstExpr.LOCAL(local))
			}
		// @TODO: Must reproduce these opcodes!
			Opcodes.DUP_X1 -> noImpl
			Opcodes.DUP_X2 -> noImpl
			Opcodes.DUP2 -> noImpl
			Opcodes.DUP2_X1 -> noImpl
			Opcodes.DUP2_X2 -> noImpl
			Opcodes.SWAP -> {
				val v1 = stackPop()
				val v2 = stackPop()
				stackPush(v1)
				stackPush(v2)
			}

			Opcodes.INEG, Opcodes.LNEG, Opcodes.FNEG, Opcodes.DNEG -> stackPush(AstExpr.UNOP(AstUnop.NEG, stackPop()))

		// @TODO: try to homogeinize this!
			in Opcodes.IADD..Opcodes.DADD -> stackPush(AstExpr.BINOP(PTYPES[op - Opcodes.IADD], stackPop(), AstBinop.ADD, stackPop()))
			in Opcodes.ISUB..Opcodes.DSUB -> stackPush(AstExpr.BINOP(PTYPES[op - Opcodes.ISUB], stackPop(), AstBinop.SUB, stackPop()))
			in Opcodes.IMUL..Opcodes.DMUL -> stackPush(AstExpr.BINOP(PTYPES[op - Opcodes.IMUL], stackPop(), AstBinop.MUL, stackPop()))
			in Opcodes.IDIV..Opcodes.DDIV -> stackPush(AstExpr.BINOP(PTYPES[op - Opcodes.IDIV], stackPop(), AstBinop.DIV, stackPop()))
			in Opcodes.IREM..Opcodes.DREM -> stackPush(AstExpr.BINOP(PTYPES[op - Opcodes.IREM], stackPop(), AstBinop.REM, stackPop()))
			in Opcodes.ISHL..Opcodes.LSHL -> stackPush(AstExpr.BINOP(PTYPES[op - Opcodes.ISHL], stackPop(), AstBinop.SHL, stackPop()))
			in Opcodes.ISHR..Opcodes.LSHR -> stackPush(AstExpr.BINOP(PTYPES[op - Opcodes.ISHR], stackPop(), AstBinop.SHR, stackPop()))
			in Opcodes.IUSHR..Opcodes.LUSHR -> stackPush(AstExpr.BINOP(PTYPES[op - Opcodes.IUSHR], stackPop(), AstBinop.USHR, stackPop()))
			in Opcodes.IAND..Opcodes.LAND -> stackPush(AstExpr.BINOP(PTYPES[op - Opcodes.IAND], stackPop(), AstBinop.AND, stackPop()))
			in Opcodes.IOR..Opcodes.LOR -> stackPush(AstExpr.BINOP(PTYPES[op - Opcodes.IOR], stackPop(), AstBinop.OR, stackPop()))
			in Opcodes.IXOR..Opcodes.LXOR -> stackPush(AstExpr.BINOP(PTYPES[op - Opcodes.IXOR], stackPop(), AstBinop.XOR, stackPop()))

			Opcodes.I2L, Opcodes.F2L, Opcodes.D2L -> stackPush(AstExpr.CAST(stackPop(), AstType.LONG))
			Opcodes.I2F, Opcodes.L2F, Opcodes.D2F -> stackPush(AstExpr.CAST(stackPop(), AstType.FLOAT))
			Opcodes.I2D, Opcodes.L2D, Opcodes.L2D -> stackPush(AstExpr.CAST(stackPop(), AstType.DOUBLE))
			Opcodes.L2I, Opcodes.F2I, Opcodes.D2I -> stackPush(AstExpr.CAST(stackPop(), AstType.INT))
			Opcodes.I2B -> stackPush(AstExpr.CAST(stackPop(), AstType.BYTE))
			Opcodes.I2C -> stackPush(AstExpr.CAST(stackPop(), AstType.CHAR))
			Opcodes.I2S -> stackPush(AstExpr.CAST(stackPop(), AstType.SHORT))

			Opcodes.LCMP -> stackPush(AstExpr.BINOP(AstType.LONG, stackPop(), AstBinop.CMP, stackPop()))
			Opcodes.FCMPL -> stackPush(AstExpr.BINOP(AstType.FLOAT, stackPop(), AstBinop.CMPL, stackPop()))
			Opcodes.FCMPG -> stackPush(AstExpr.BINOP(AstType.FLOAT, stackPop(), AstBinop.CMPG, stackPop()))
			Opcodes.DCMPL -> stackPush(AstExpr.BINOP(AstType.DOUBLE, stackPop(), AstBinop.CMPL, stackPop()))
			Opcodes.DCMPG -> stackPush(AstExpr.BINOP(AstType.DOUBLE, stackPop(), AstBinop.CMPG, stackPop()))
			in Opcodes.IRETURN..Opcodes.ARETURN -> stmAdd(AstStm.RETURN(stackPop()))
			Opcodes.RETURN -> stmAdd(AstStm.RETURN(null))
			Opcodes.ARRAYLENGTH -> stackPush(AstExpr.ARRAY_LENGTH(stackPop()))
			Opcodes.ATHROW -> stmAdd(AstStm.THROW(stackPop()))
			Opcodes.MONITORENTER -> stmAdd(AstStm.MONITOR_ENTER(stackPop()))
			Opcodes.MONITOREXIT -> stmAdd(AstStm.MONITOR_EXIT(stackPop()))
			else -> invalidOp
		}
	}

	fun handleMultiArray(i: MultiANewArrayInsnNode) {
		when (i.opcode) {
			Opcodes.MULTIANEWARRAY -> {
				stackPush(AstExpr.NEW_ARRAY(AstType.REF_INT(i.desc) as AstType.ARRAY, (0 until i.dims).map { stackPop() }))
			}
			else -> invalidOp("$i")
		}
	}

	fun handleType(i: TypeInsnNode) {
		when (i.opcode) {
			Opcodes.NEW -> stackPush(AstExpr.NEW(AstType.REF_INT2(i.desc)))
			Opcodes.ANEWARRAY -> {
				stackPush(AstExpr.NEW_ARRAY(AstType.REF_INT(i.desc) as AstType.ARRAY, listOf(stackPop())))
			}
			Opcodes.CHECKCAST -> {
				stackPush(AstExpr.CAST(stackPop(), AstType.REF_INT(i.desc)))
			}
			Opcodes.INSTANCEOF -> {
				stackPush(AstExpr.INSTANCE_OF(stackPop(), AstType.REF_INT(i.desc)))
			}
			else -> invalidOp("$i")
		}
	}

	fun handleVar(i: VarInsnNode) {
		val op = i.opcode
		when (i.opcode) {
			in Opcodes.ILOAD..Opcodes.ALOAD -> {
				stackPush(AstExpr.LOCAL(local(PTYPES[op - Opcodes.ILOAD], i.`var`)))
			}
			in Opcodes.ISTORE..Opcodes.ASTORE -> {
				stmAdd(AstStm.SET(local(PTYPES[op - Opcodes.ILOAD], i.`var`), stackPop()))
			}
			Opcodes.RET -> deprecated
			else -> invalidOp
		}
	}

	val JUMPOPS = listOf(AstBinop.EQ, AstBinop.NE, AstBinop.LT, AstBinop.GE, AstBinop.GT, AstBinop.LE, AstBinop.EQ, AstBinop.NE)

	fun addJump(cond: AstExpr?, label: AstLabel) {
		// SERIALIZE ALL THE STACK!
		assert(stack.size == 0)
		stmAdd(AstStm.IF_GOTO(label, cond))
	}

	fun handleJump(i: JumpInsnNode) {
		val op = i.opcode
		when (op) {
			in Opcodes.IFEQ..Opcodes.IFLE -> {
				addJump(AstExpr.BINOP(AstType.BOOL, stackPop(), JUMPOPS[op - Opcodes.IFEQ], AstExpr.LITERAL(0)), label(i.label))
			}
			in Opcodes.IFNULL..Opcodes.IFNONNULL -> {
				addJump(AstExpr.BINOP(AstType.BOOL, stackPop(), JUMPOPS[op - Opcodes.IFNULL], AstExpr.LITERAL(null)), label(i.label))
			}
			in Opcodes.IF_ICMPEQ..Opcodes.IF_ACMPNE -> {
				addJump(AstExpr.BINOP(AstType.BOOL, stackPop(), JUMPOPS[op - Opcodes.IF_ICMPEQ], stackPop()), label(i.label))
			}
			Opcodes.GOTO -> {
				addJump(null, label(i.label))
			}
			Opcodes.JSR -> deprecated
			else -> invalidOp
		}
	}

	fun handleLdc(i: LdcInsnNode) {
		stackPush(AstExpr.LITERAL(i.cst))
	}

	fun handleInt(i: IntInsnNode) {
		when (i.opcode) {
			Opcodes.BIPUSH -> stackPush(AstExpr.LITERAL(i.operand.toByte()))
			Opcodes.SIPUSH -> stackPush(AstExpr.LITERAL(i.operand.toShort()))
			Opcodes.NEWARRAY -> {
				val type = when (i.operand) {
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
				stackPush(AstExpr.NEW_ARRAY(AstType.ARRAY(type, 1), listOf(stackPop())))
			}
			else -> invalidOp
		}
	}

	fun handleMethod(i: MethodInsnNode) {
		val clazz = AstType.REF(i.owner)
		val methodRef = com.jtransc.ast.AstMethodRef(AstType.REF_INT2(i.owner).fqname.fqname, i.name, AstType.demangleMethod(i.desc))
		val isSpecial = i.opcode == Opcodes.INVOKESPECIAL

		val obj = if (i.opcode != Opcodes.INVOKESTATIC) {
			stackPop()
		} else {
			null
		}

		val args = methodRef.type.args.map { stackPop() }

		when (i.opcode) {
			Opcodes.INVOKESTATIC -> {
				stackPush(AstExpr.CALL_STATIC(clazz, methodRef, args, isSpecial))
			}
			Opcodes.INVOKEVIRTUAL, Opcodes.INVOKEINTERFACE -> {
				stackPush(AstExpr.CALL_INSTANCE(obj!!, methodRef, args, isSpecial))
			}
			Opcodes.INVOKESPECIAL -> {
				stackPush(AstExprUtils.INVOKE_SPECIAL(obj!!, methodRef, args))
			}
			else -> invalidOp
		}

		if (methodRef.type.retVoid) {
			stmAdd(AstStm.STM_EXPR(stackPop()))
		}
	}

	fun handleLookupSwitch(i: LookupSwitchInsnNode) {
		stmAdd(AstStm.SWITCH_GOTO(
			stackPop(),
			label(i.dflt),
			i.keys.cast<Int>().zip(i.labels.cast<LabelNode>().map { label(it) })
		))
	}

	fun handleTableSwitch(i: TableSwitchInsnNode) {
		stmAdd(AstStm.SWITCH_GOTO(
			stackPop(),
			label(i.dflt),
			(i.min..i.max).zip(i.labels.cast<LabelNode>().map { label(it) })
		))
	}

	fun handleInvokeDynamic(i: InvokeDynamicInsnNode) {
		stackPush(AstExprUtils.INVOKE_DYNAMIC(
			AstMethodWithoutClassRef(i.name, AstType.demangleMethod(i.desc)),
			i.bsm.ast,
			i.bsmArgs.map { AstExpr.LITERAL(it) }
		))
	}

	fun handleLabel(i: LabelNode) {
		stmAdd(AstStm.STM_LABEL(label(i)))
	}

	fun handleIinc(i: IincInsnNode) {
		val local = local(AstType.INT, i.`var`)
		stmAdd(AstStm.SET(local, AstExpr.LOCAL(local) + AstExpr.LITERAL(1)))
	}

	fun handleLineNumber(i: LineNumberNode) {
		stmAdd(AstStm.LINE(i.line))
	}

	fun handleFrame(i: FrameNode) {
		//BAF.FRAME(i.type, i.local.map { getType(it) }, i.stack.map { getType(it) })
	}

	fun call():AstBody {
		var i = this.i
		while (i != null) {
			when (i) {
				is FieldInsnNode -> handleField(i)
				is InsnNode -> handleInsn(i)
				is TypeInsnNode -> handleType(i)
				is VarInsnNode -> handleVar(i)
				is JumpInsnNode -> handleJump(i)
				is LdcInsnNode -> handleLdc(i)
				is IntInsnNode -> handleInt(i)
				is MethodInsnNode -> handleMethod(i)
				is LookupSwitchInsnNode -> handleLookupSwitch(i)
				is TableSwitchInsnNode -> handleTableSwitch(i)
				is InvokeDynamicInsnNode -> handleInvokeDynamic(i)
				is LabelNode -> handleLabel(i)
				is IincInsnNode -> handleIinc(i)
				is LineNumberNode -> handleLineNumber(i)
				is FrameNode -> handleFrame(i)
				else -> invalidOp("$i")
			}
			i = i.next
		}

		return AstBody(
			AstStm.STMS(stms),
			locals.toList(),
			tryCatchBlocks.map {
				AstTrap(
					start = label(it.start),
					end = label(it.end),
					handler = label(it.handler),
					exception = if (it.type != null) AstType.REF(it.type) else AstType.OBJECT
				)
			}
		)
	}
}
