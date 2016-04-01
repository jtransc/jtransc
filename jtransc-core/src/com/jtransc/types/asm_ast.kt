package com.jtransc.types

import com.jtransc.ast.*
import com.jtransc.ds.cast
import com.jtransc.error.InvalidOperationException
import com.jtransc.error.invalidOp
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import java.util.*

/*
fun Asm2Ast(methodRef: AstMethodRef, list: InsnList): AstStm {
	val stms = ArrayList<AstStm>()
	val stack = Stack<AstExpr>()
	var i = list.first

	fun getType(value:Any?): AstType {
		return when (value) {
			is Int -> AstType.INT
			is String -> AstType.STRING // Or custom type?
		//else -> AstType.UNKNOWN
			else -> {
				throw InvalidOperationException("$value")
			}
		}
	}

	fun handleField(i: FieldInsnNode) {
		val isStatic = (i.opcode == Opcodes.GETSTATIC) || (i.opcode == Opcodes.PUTSTATIC)
		val ref = AstFieldRef(AstType.REF_INT2(i.owner).fqname.fqname, i.name, com.jtransc.ast.AstType.demangle(i.desc), isStatic)
		when (i.opcode) {
			Opcodes.GETSTATIC -> stack.push(AstExpr.STATIC_FIELD_ACCESS(ref))
			Opcodes.PUTSTATIC -> BAF.PUTFIELD(ref)
			Opcodes.GETFIELD -> BAF.GETFIELD(ref)
			Opcodes.PUTFIELD -> BAF.PUTFIELD(ref)
			else -> invalidOp
		}
	}

	fun handleInsn(i: InsnNode) {
		out.add(when (i.opcode) {
			Opcodes.NOP -> BAF.NOP
			Opcodes.ACONST_NULL -> BAF.CONST(null)
			Opcodes.ICONST_M1 -> BAF.CONST(-1)
			Opcodes.ICONST_0 -> BAF.CONST(0)
			Opcodes.ICONST_1 -> BAF.CONST(1)
			Opcodes.ICONST_2 -> BAF.CONST(2)
			Opcodes.ICONST_3 -> BAF.CONST(3)
			Opcodes.ICONST_4 -> BAF.CONST(4)
			Opcodes.ICONST_5 -> BAF.CONST(5)
			Opcodes.LCONST_0 -> BAF.CONST(0)
			Opcodes.LCONST_1 -> BAF.CONST(1)
			Opcodes.FCONST_0 -> BAF.CONST(0f)
			Opcodes.FCONST_1 -> BAF.CONST(1f)
			Opcodes.FCONST_2 -> BAF.CONST(2f)
			Opcodes.DCONST_0 -> BAF.CONST(0.0)
			Opcodes.DCONST_1 -> BAF.CONST(1.0)
			Opcodes.IALOAD -> BAF.ARRAYGET(AstType.INT)
			Opcodes.LALOAD -> BAF.ARRAYGET(AstType.LONG)
			Opcodes.FALOAD -> BAF.ARRAYGET(AstType.FLOAT)
			Opcodes.DALOAD -> BAF.ARRAYGET(AstType.DOUBLE)
			Opcodes.AALOAD -> BAF.ARRAYGET(AstType.OBJECT)
			Opcodes.BALOAD -> BAF.ARRAYGET(AstType.BYTE)
			Opcodes.CALOAD -> BAF.ARRAYGET(AstType.CHAR)
			Opcodes.SALOAD -> BAF.ARRAYGET(AstType.SHORT)
			Opcodes.IASTORE -> BAF.ARRAYSET(AstType.INT)
			Opcodes.LASTORE -> BAF.ARRAYSET(AstType.LONG)
			Opcodes.FASTORE -> BAF.ARRAYSET(AstType.FLOAT)
			Opcodes.DASTORE -> BAF.ARRAYSET(AstType.DOUBLE)
			Opcodes.AASTORE -> BAF.ARRAYSET(AstType.OBJECT)
			Opcodes.BASTORE -> BAF.ARRAYSET(AstType.BYTE)
			Opcodes.CASTORE -> BAF.ARRAYSET(AstType.CHAR)
			Opcodes.SASTORE -> BAF.ARRAYSET(AstType.SHORT)
			Opcodes.POP -> BAF.POP
			Opcodes.POP2 -> BAF.POP2
			Opcodes.DUP -> BAF.DUP
			Opcodes.DUP_X1 -> BAF.DUPX1
			Opcodes.DUP_X2 -> BAF.DUPX2
			Opcodes.DUP2 -> BAF.DUP2
			Opcodes.DUP2_X1 -> BAF.DUP2_X1
			Opcodes.DUP2_X2 -> BAF.DUP2_X2
			Opcodes.SWAP -> BAF.SWAP
			Opcodes.IADD -> BAF.BINOP(AstType.INT, AstBinop.ADD)
			Opcodes.LADD -> BAF.BINOP(AstType.LONG, AstBinop.ADD)
			Opcodes.FADD -> BAF.BINOP(AstType.FLOAT, AstBinop.ADD)
			Opcodes.DADD -> BAF.BINOP(AstType.DOUBLE, AstBinop.ADD)
			Opcodes.ISUB -> BAF.BINOP(AstType.INT, AstBinop.SUB)
			Opcodes.LSUB -> BAF.BINOP(AstType.LONG, AstBinop.SUB)
			Opcodes.FSUB -> BAF.BINOP(AstType.FLOAT, AstBinop.SUB)
			Opcodes.DSUB -> BAF.BINOP(AstType.DOUBLE, AstBinop.SUB)
			Opcodes.IMUL -> BAF.BINOP(AstType.INT, AstBinop.MUL)
			Opcodes.LMUL -> BAF.BINOP(AstType.LONG, AstBinop.MUL)
			Opcodes.FMUL -> BAF.BINOP(AstType.FLOAT, AstBinop.MUL)
			Opcodes.DMUL -> BAF.BINOP(AstType.DOUBLE, AstBinop.MUL)
			Opcodes.IDIV -> BAF.BINOP(AstType.INT, AstBinop.DIV)
			Opcodes.LDIV -> BAF.BINOP(AstType.LONG, AstBinop.DIV)
			Opcodes.FDIV -> BAF.BINOP(AstType.FLOAT, AstBinop.DIV)
			Opcodes.DDIV -> BAF.BINOP(AstType.DOUBLE, AstBinop.DIV)
			Opcodes.IREM -> BAF.BINOP(AstType.INT, AstBinop.REM)
			Opcodes.LREM -> BAF.BINOP(AstType.LONG, AstBinop.REM)
			Opcodes.FREM -> BAF.BINOP(AstType.FLOAT, AstBinop.REM)
			Opcodes.DREM -> BAF.BINOP(AstType.DOUBLE, AstBinop.REM)
			Opcodes.INEG -> BAF.UNOP(AstType.INT, AstUnop.NEG)
			Opcodes.LNEG -> BAF.UNOP(AstType.LONG, AstUnop.NEG)
			Opcodes.FNEG -> BAF.UNOP(AstType.FLOAT, AstUnop.NEG)
			Opcodes.DNEG -> BAF.UNOP(AstType.DOUBLE, AstUnop.NEG)
			Opcodes.ISHL -> BAF.BINOP(AstType.INT, AstBinop.SHL)
			Opcodes.LSHL -> BAF.BINOP(AstType.LONG, AstBinop.SHL)
			Opcodes.ISHR -> BAF.BINOP(AstType.INT, AstBinop.SHR)
			Opcodes.LSHR -> BAF.BINOP(AstType.LONG, AstBinop.SHR)
			Opcodes.IUSHR -> BAF.BINOP(AstType.INT, AstBinop.USHR)
			Opcodes.LUSHR -> BAF.BINOP(AstType.LONG, AstBinop.USHR)
			Opcodes.IAND -> BAF.BINOP(AstType.INT, AstBinop.AND)
			Opcodes.LAND -> BAF.BINOP(AstType.LONG, AstBinop.AND)
			Opcodes.IOR -> BAF.BINOP(AstType.INT, AstBinop.OR)
			Opcodes.LOR -> BAF.BINOP(AstType.LONG, AstBinop.OR)
			Opcodes.IXOR -> BAF.BINOP(AstType.INT, AstBinop.XOR)
			Opcodes.LXOR -> BAF.BINOP(AstType.LONG, AstBinop.XOR)
			Opcodes.I2L -> BAF.CONV(AstType.INT, AstType.LONG)
			Opcodes.I2F -> BAF.CONV(AstType.INT, AstType.FLOAT)
			Opcodes.I2D -> BAF.CONV(AstType.INT, AstType.DOUBLE)
			Opcodes.L2I -> BAF.CONV(AstType.LONG, AstType.INT)
			Opcodes.L2F -> BAF.CONV(AstType.LONG, AstType.FLOAT)
			Opcodes.L2D -> BAF.CONV(AstType.LONG, AstType.DOUBLE)
			Opcodes.F2I -> BAF.CONV(AstType.FLOAT, AstType.INT)
			Opcodes.F2L -> BAF.CONV(AstType.FLOAT, AstType.LONG)
			Opcodes.F2D -> BAF.CONV(AstType.FLOAT, AstType.DOUBLE)
			Opcodes.D2I -> BAF.CONV(AstType.DOUBLE, AstType.INT)
			Opcodes.D2L -> BAF.CONV(AstType.DOUBLE, AstType.LONG)
			Opcodes.D2F -> BAF.CONV(AstType.DOUBLE, AstType.FLOAT)
			Opcodes.I2B -> BAF.CONV(AstType.INT, AstType.BYTE)
			Opcodes.I2C -> BAF.CONV(AstType.INT, AstType.CHAR)
			Opcodes.I2S -> BAF.CONV(AstType.INT, AstType.SHORT)
			Opcodes.LCMP -> BAF.BINOP(AstType.LONG, AstBinop.CMP)
			Opcodes.FCMPL -> BAF.BINOP(AstType.FLOAT, AstBinop.CMPL)
			Opcodes.FCMPG -> BAF.BINOP(AstType.FLOAT, AstBinop.CMPG)
			Opcodes.DCMPL -> BAF.BINOP(AstType.DOUBLE, AstBinop.CMPL)
			Opcodes.DCMPG -> BAF.BINOP(AstType.DOUBLE, AstBinop.CMPG)
			Opcodes.IRETURN -> BAF.RET(AstType.INT)
			Opcodes.LRETURN -> BAF.RET(AstType.LONG)
			Opcodes.FRETURN -> BAF.RET(AstType.FLOAT)
			Opcodes.DRETURN -> BAF.RET(AstType.DOUBLE)
			Opcodes.ARETURN -> BAF.RET(AstType.OBJECT)
			Opcodes.RETURN -> BAF.RETVOID
			Opcodes.ARRAYLENGTH -> BAF.ARRAYLENGTH
			Opcodes.ATHROW -> BAF.THROW
			Opcodes.MONITORENTER -> BAF.MONITOR(true)
			Opcodes.MONITOREXIT -> BAF.MONITOR(false)
			else -> invalidOp
		})
	}

	fun handleType(i: TypeInsnNode) {
		out.add(when (i.opcode) {
			Opcodes.NEW -> BAF.ANEW(AstType.REF_INT2(i.desc))
			Opcodes.ANEWARRAY -> BAF.NEWARRAY(AstType.REF_INT(i.desc), 1)
			Opcodes.CHECKCAST -> BAF.CHECKCAST(AstType.REF_INT(i.desc))
			Opcodes.INSTANCEOF -> BAF.AINSTANCEOF(AstType.REF_INT(i.desc))
			else -> throw InvalidOperationException("$i")
		})
	}

	fun handleVar(i: VarInsnNode) {
		out.add(when (i.opcode) {
			Opcodes.ILOAD -> BAF.GETLOCAL(AstType.INT, i.`var`)
			Opcodes.LLOAD -> BAF.GETLOCAL(AstType.LONG, i.`var`)
			Opcodes.FLOAD -> BAF.GETLOCAL(AstType.FLOAT, i.`var`)
			Opcodes.DLOAD -> BAF.GETLOCAL(AstType.DOUBLE, i.`var`)
			Opcodes.ALOAD -> BAF.GETLOCAL(AstType.OBJECT, i.`var`)
			Opcodes.ISTORE -> BAF.PUTLOCAL(AstType.INT, i.`var`)
			Opcodes.LSTORE -> BAF.PUTLOCAL(AstType.LONG, i.`var`)
			Opcodes.FSTORE -> BAF.PUTLOCAL(AstType.FLOAT, i.`var`)
			Opcodes.DSTORE -> BAF.PUTLOCAL(AstType.DOUBLE, i.`var`)
			Opcodes.ASTORE -> BAF.PUTLOCAL(AstType.OBJECT, i.`var`)
			Opcodes.RET -> invalidOp
			else -> invalidOp
		})
	}

	fun handleJump(i: JumpInsnNode) {
		out.add(when (i.opcode) {
			Opcodes.IFEQ -> BAF.GOTOIF0(AstBinop.EQ, i.label.label)
			Opcodes.IFNE -> BAF.GOTOIF0(AstBinop.NE, i.label.label)
			Opcodes.IFLT -> BAF.GOTOIF0(AstBinop.LT, i.label.label)
			Opcodes.IFGE -> BAF.GOTOIF0(AstBinop.GE, i.label.label)
			Opcodes.IFGT -> BAF.GOTOIF0(AstBinop.GT, i.label.label)
			Opcodes.IFLE -> BAF.GOTOIF0(AstBinop.LE, i.label.label)
			Opcodes.IF_ICMPEQ -> BAF.GOTOIF_I(AstBinop.EQ, i.label.label)
			Opcodes.IF_ICMPNE -> BAF.GOTOIF_I(AstBinop.NE, i.label.label)
			Opcodes.IF_ICMPLT -> BAF.GOTOIF_I(AstBinop.LT, i.label.label)
			Opcodes.IF_ICMPGE -> BAF.GOTOIF_I(AstBinop.GE, i.label.label)
			Opcodes.IF_ICMPGT -> BAF.GOTOIF_I(AstBinop.GT, i.label.label)
			Opcodes.IF_ICMPLE -> BAF.GOTOIF_I(AstBinop.LE, i.label.label)
			Opcodes.IF_ACMPEQ -> BAF.GOTOIF_A(AstBinop.EQ, i.label.label)
			Opcodes.IF_ACMPNE -> BAF.GOTOIF_A(AstBinop.NE, i.label.label)
			Opcodes.GOTO -> BAF.GOTO(i.label.label)
			Opcodes.JSR -> invalidOp
			Opcodes.IFNULL -> BAF.GOTOIFNULL(AstBinop.EQ, i.label.label)
			Opcodes.IFNONNULL -> BAF.GOTOIFNULL(AstBinop.NE, i.label.label)
			else -> invalidOp
		})
	}

	fun handleLdc(i: LdcInsnNode) {
		out.add(when (i.cst) {
			null -> invalidOp
			is Int -> BAF.CONST(i.cst)
			is Float -> BAF.CONST(i.cst)
			is Long -> BAF.CONST(i.cst)
			is Double -> BAF.CONST(i.cst)
			is String -> BAF.CONST(i.cst)
			is org.objectweb.asm.Type -> BAF.CONST(i.cst)
			is org.objectweb.asm.Handle -> BAF.CONST(i.cst)
			is ShortArray -> BAF.CONST(i.cst)
			else -> invalidOp
		})
	}

	fun handleInt(i: IntInsnNode) {
		out.add(when (i.opcode) {
			Opcodes.BIPUSH -> BAF.CONST(i.operand.toByte())
			Opcodes.SIPUSH -> BAF.CONST(i.operand.toShort())
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
				BAF.NEWARRAY(type, 1)
			}
			else -> invalidOp
		})
	}

	fun handleMethod(i: MethodInsnNode) {
		val methodRef = com.jtransc.ast.AstMethodRef(AstType.REF_INT2(i.owner).fqname.fqname, i.name, AstType.demangleMethod(i.desc))
		out.add(BAF.INVOKE(methodRef, i.itf, when (i.opcode) {
			Opcodes.INVOKEVIRTUAL -> BAF.InvokeType.VIRTUAL
			Opcodes.INVOKESPECIAL -> BAF.InvokeType.SPECIAL
			Opcodes.INVOKESTATIC -> BAF.InvokeType.STATIC
			Opcodes.INVOKEINTERFACE -> BAF.InvokeType.INTERFACE
			else -> invalidOp
		}))
	}

	fun handleLookupSwitch(i: LookupSwitchInsnNode) {
		out.add(BAF.SWITCH(i.dflt.label, i.keys.cast<Int>().zip(i.labels.cast<LabelNode>().map { it.label })))
	}

	fun handleTableSwitch(i: TableSwitchInsnNode) {
		out.add(BAF.SWITCH(i.dflt.label, (i.min..i.max).zip(i.labels.cast<LabelNode>().map { it.label })))
	}

	fun handleInvokeDynamic(i: InvokeDynamicInsnNode) {
		out.add(BAF.INVOKEDYNAMIC(i.name, i.desc, i.bsm, i.bsmArgs.toList()))
	}

	fun handleLabel(i: LabelNode) {
		out.add(BAF.LABEL(i.label))
	}

	fun handleIinc(i: IincInsnNode) {
		out.add(BAF.IINC(i.`var`, i.incr))
	}

	fun handleLineNumber(i: LineNumberNode) {
		out.add(BAF.LINE(i.line, i.start.label))
	}

	fun handleFrame(i: FrameNode) {
		out.add(BAF.FRAME(i.type, i.local.map { getType(it) }, i.stack.map { getType(it) }))
	}

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

	return BAF.Body(methodRef, out)
}
*/
