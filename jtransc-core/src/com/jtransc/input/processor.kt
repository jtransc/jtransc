package com.jtransc.input

import com.jtransc.ast.AstBinop
import com.jtransc.ast.AstType
import com.jtransc.ast.AstUnop
import com.jtransc.error.invalidOp
import org.objectweb.asm.*

class AstInstructionMethodVisitor(val ip: Processor) : MethodVisitor(Opcodes.ASM5) {
	override fun visitMultiANewArrayInsn(desc: String?, dims: Int) {
		ip.multianewarray(desc, dims)
	}

	override fun visitFrame(type: Int, nLocal: Int, local: Array<Any>?, nStack: Int, stack: Array<Any>?) {
		//int type, int nLocal, Object[] local, int nStack,Object[] stack
		ip.frame(type, nLocal, local, nStack, stack)
	}

	override fun visitVarInsn(opcode: Int, index: Int) {
		when (opcode) {
			Opcodes.ILOAD -> ip.getlocal(AstType.INT, index)
			Opcodes.LLOAD -> ip.getlocal(AstType.LONG, index)
			Opcodes.FLOAD -> ip.getlocal(AstType.FLOAT, index)
			Opcodes.DLOAD -> ip.getlocal(AstType.DOUBLE, index)
			Opcodes.ALOAD -> ip.getlocal(AstType.OBJECT, index)
			Opcodes.ISTORE -> ip.putlocal(AstType.INT, index)
			Opcodes.LSTORE -> ip.putlocal(AstType.LONG, index)
			Opcodes.FSTORE -> ip.putlocal(AstType.FLOAT, index)
			Opcodes.DSTORE -> ip.putlocal(AstType.DOUBLE, index)
			Opcodes.ASTORE -> ip.putlocal(AstType.OBJECT, index)
			Opcodes.RET -> invalidOp
		}
	}

	override fun visitTryCatchBlock(start: Label?, end: Label?, handler: Label?, type: String?) {
		ip.tryCatchBlock(start, end, handler, type)
	}

	override fun visitLookupSwitchInsn(dflt: Label?, keys: IntArray, labels: Array<Label>) {
		ip.switch(dflt, keys.zip(labels))
	}

	override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label?, vararg labels: Label?) {
		ip.switch(dflt, (min..max).zip(labels))
	}

	override fun visitJumpInsn(opcode: Int, label: Label?) {
		when (opcode) {
			Opcodes.IFEQ -> ip.gotoif0(AstBinop.EQ, label)
			Opcodes.IFNE -> ip.gotoif0(AstBinop.NE, label)
			Opcodes.IFLT -> ip.gotoif0(AstBinop.LT, label)
			Opcodes.IFGE -> ip.gotoif0(AstBinop.GE, label)
			Opcodes.IFGT -> ip.gotoif0(AstBinop.GT, label)
			Opcodes.IFLE -> ip.gotoif0(AstBinop.LE, label)
			Opcodes.IF_ICMPEQ -> ip.gotoif_i(AstBinop.EQ, label)
			Opcodes.IF_ICMPNE -> ip.gotoif_i(AstBinop.NE, label)
			Opcodes.IF_ICMPLT -> ip.gotoif_i(AstBinop.LT, label)
			Opcodes.IF_ICMPGE -> ip.gotoif_i(AstBinop.GE, label)
			Opcodes.IF_ICMPGT -> ip.gotoif_i(AstBinop.GT, label)
			Opcodes.IF_ICMPLE -> ip.gotoif_i(AstBinop.LE, label)
			Opcodes.IF_ACMPEQ -> ip.gotoif_a(AstBinop.EQ, label)
			Opcodes.IF_ACMPNE -> ip.gotoif_a(AstBinop.NE, label)
			Opcodes.GOTO -> ip.goto(label)
			Opcodes.JSR -> invalidOp
			Opcodes.IFNULL -> ip.gotoifnull(AstBinop.EQ, label)
			Opcodes.IFNONNULL -> ip.gotoifnull(AstBinop.NE, label)
		}
	}

	override fun visitLdcInsn(cst: Any?) {
		when (cst) {
			null -> invalidOp
			is Int -> ip.iconst(cst)
			is Float -> ip.fconst(cst)
			is Long -> ip.lconst(cst)
			is Double -> ip.dconst(cst)
			is String -> ip.strconst(cst)
			is org.objectweb.asm.Type -> ip.typeconst(cst)
			is org.objectweb.asm.Handle -> ip.handleconst(cst)
			is ShortArray -> ip.classconst(cst)
			else -> invalidOp
		}
	}

	override fun visitIntInsn(opcode: Int, operand: Int) {
		//super.visitIntInsn(opcode, operand)
		when (opcode) {
			Opcodes.BIPUSH -> ip.bconst(operand.toByte())
			Opcodes.SIPUSH -> ip.sconst(operand.toShort())
			Opcodes.NEWARRAY -> {
				val type = when (operand) {
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
				ip.newarray(type)
			}
		}
	}

	override fun visitTypeInsn(opcode: Int, type: String?) {
		when (opcode) {
			Opcodes.NEW -> ip.anew(type)
			Opcodes.ANEWARRAY -> ip.anewarray(type)
			Opcodes.CHECKCAST -> ip.acheckcast(type)
			Opcodes.INSTANCEOF -> ip.ainstanceof(type)
		}
	}


	override fun visitMaxs(maxStack: Int, maxLocal: Int) {
		ip.maxs(maxStack, maxLocal)
	}

	override fun visitInvokeDynamicInsn(name: String?, desc: String?, handle: Handle?, vararg args: Any?) {
		ip.invokedynamic(name, desc, handle, args)
	}

	override fun visitLabel(label: Label?) {
		ip.label(label)
	}

	override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, desc: String?, itf: Boolean) {
		val type = when (opcode) {
			Opcodes.INVOKEVIRTUAL -> Processor.InvokeType.VIRTUAL
			Opcodes.INVOKESPECIAL -> Processor.InvokeType.SPECIAL
			Opcodes.INVOKESTATIC -> Processor.InvokeType.STATIC
			Opcodes.INVOKEINTERFACE -> Processor.InvokeType.INTERFACE
			else -> invalidOp
		}
		ip.invoke(type, owner, name, desc, itf)
	}

	override fun visitInsn(opcode: Int): Unit {
		when (opcode) {
			Opcodes.NOP -> Unit
			Opcodes.ACONST_NULL -> ip.aconstnull()
			Opcodes.ICONST_M1 -> ip.iconst(-1)
			Opcodes.ICONST_0 -> ip.iconst(0)
			Opcodes.ICONST_1 -> ip.iconst(1)
			Opcodes.ICONST_2 -> ip.iconst(2)
			Opcodes.ICONST_3 -> ip.iconst(3)
			Opcodes.ICONST_4 -> ip.iconst(4)
			Opcodes.ICONST_5 -> ip.iconst(5)
			Opcodes.LCONST_0 -> ip.lconst(0)
			Opcodes.LCONST_1 -> ip.lconst(1)
			Opcodes.FCONST_0 -> ip.fconst(0f)
			Opcodes.FCONST_1 -> ip.fconst(1f)
			Opcodes.FCONST_2 -> ip.fconst(2f)
			Opcodes.DCONST_0 -> ip.dconst(0.0)
			Opcodes.DCONST_1 -> ip.dconst(1.0)
			Opcodes.IALOAD -> ip.arrayget(AstType.INT)
			Opcodes.LALOAD -> ip.arrayget(AstType.LONG)
			Opcodes.FALOAD -> ip.arrayget(AstType.FLOAT)
			Opcodes.DALOAD -> ip.arrayget(AstType.DOUBLE)
			Opcodes.AALOAD -> ip.arrayget(AstType.OBJECT)
			Opcodes.BALOAD -> ip.arrayget(AstType.BYTE)
			Opcodes.CALOAD -> ip.arrayget(AstType.CHAR)
			Opcodes.SALOAD -> ip.arrayget(AstType.SHORT)
			Opcodes.IASTORE -> ip.arrayset(AstType.INT)
			Opcodes.LASTORE -> ip.arrayset(AstType.LONG)
			Opcodes.FASTORE -> ip.arrayset(AstType.FLOAT)
			Opcodes.DASTORE -> ip.arrayset(AstType.DOUBLE)
			Opcodes.AASTORE -> ip.arrayset(AstType.OBJECT)
			Opcodes.BASTORE -> ip.arrayset(AstType.BYTE)
			Opcodes.CASTORE -> ip.arrayset(AstType.CHAR)
			Opcodes.SASTORE -> ip.arrayset(AstType.SHORT)
			Opcodes.POP -> ip.pop()
			Opcodes.POP2 -> ip.pop2()
			Opcodes.DUP -> ip.dup()
			Opcodes.DUP_X1 -> ip.dupx1()
			Opcodes.DUP_X2 -> ip.dupx2()
			Opcodes.DUP2 -> ip.dup2()
			Opcodes.DUP2_X1 -> ip.dup2_x1()
			Opcodes.DUP2_X2 -> ip.dup2_x2()
			Opcodes.SWAP -> ip.swap()
			Opcodes.IADD -> ip.binop(AstType.INT, AstBinop.ADD)
			Opcodes.LADD -> ip.binop(AstType.LONG, AstBinop.ADD)
			Opcodes.FADD -> ip.binop(AstType.FLOAT, AstBinop.ADD)
			Opcodes.DADD -> ip.binop(AstType.DOUBLE, AstBinop.ADD)
			Opcodes.ISUB -> ip.binop(AstType.INT, AstBinop.SUB)
			Opcodes.LSUB -> ip.binop(AstType.LONG, AstBinop.SUB)
			Opcodes.FSUB -> ip.binop(AstType.FLOAT, AstBinop.SUB)
			Opcodes.DSUB -> ip.binop(AstType.DOUBLE, AstBinop.SUB)
			Opcodes.IMUL -> ip.binop(AstType.INT, AstBinop.MUL)
			Opcodes.LMUL -> ip.binop(AstType.LONG, AstBinop.MUL)
			Opcodes.FMUL -> ip.binop(AstType.FLOAT, AstBinop.MUL)
			Opcodes.DMUL -> ip.binop(AstType.DOUBLE, AstBinop.MUL)
			Opcodes.IDIV -> ip.binop(AstType.INT, AstBinop.DIV)
			Opcodes.LDIV -> ip.binop(AstType.LONG, AstBinop.DIV)
			Opcodes.FDIV -> ip.binop(AstType.FLOAT, AstBinop.DIV)
			Opcodes.DDIV -> ip.binop(AstType.DOUBLE, AstBinop.DIV)
			Opcodes.IREM -> ip.binop(AstType.INT, AstBinop.REM)
			Opcodes.LREM -> ip.binop(AstType.LONG, AstBinop.REM)
			Opcodes.FREM -> ip.binop(AstType.FLOAT, AstBinop.REM)
			Opcodes.DREM -> ip.binop(AstType.DOUBLE, AstBinop.REM)
			Opcodes.INEG -> ip.unop(AstType.INT, AstUnop.NEG)
			Opcodes.LNEG -> ip.unop(AstType.LONG, AstUnop.NEG)
			Opcodes.FNEG -> ip.unop(AstType.FLOAT, AstUnop.NEG)
			Opcodes.DNEG -> ip.unop(AstType.DOUBLE, AstUnop.NEG)
			Opcodes.ISHL -> ip.binop(AstType.INT, AstBinop.SHL)
			Opcodes.LSHL -> ip.binop(AstType.LONG, AstBinop.SHL)
			Opcodes.ISHR -> ip.binop(AstType.INT, AstBinop.SHR)
			Opcodes.LSHR -> ip.binop(AstType.LONG, AstBinop.SHR)
			Opcodes.IUSHR -> ip.binop(AstType.INT, AstBinop.USHR)
			Opcodes.LUSHR -> ip.binop(AstType.LONG, AstBinop.USHR)
			Opcodes.IAND -> ip.binop(AstType.INT, AstBinop.AND)
			Opcodes.LAND -> ip.binop(AstType.LONG, AstBinop.AND)
			Opcodes.IOR -> ip.binop(AstType.INT, AstBinop.OR)
			Opcodes.LOR -> ip.binop(AstType.LONG, AstBinop.OR)
			Opcodes.IXOR -> ip.binop(AstType.INT, AstBinop.XOR)
			Opcodes.LXOR -> ip.binop(AstType.LONG, AstBinop.XOR)
			Opcodes.I2L -> ip.conv(AstType.INT, AstType.LONG)
			Opcodes.I2F -> ip.conv(AstType.INT, AstType.FLOAT)
			Opcodes.I2D -> ip.conv(AstType.INT, AstType.DOUBLE)
			Opcodes.L2I -> ip.conv(AstType.LONG, AstType.INT)
			Opcodes.L2F -> ip.conv(AstType.LONG, AstType.FLOAT)
			Opcodes.L2D -> ip.conv(AstType.LONG, AstType.DOUBLE)
			Opcodes.F2I -> ip.conv(AstType.FLOAT, AstType.INT)
			Opcodes.F2L -> ip.conv(AstType.FLOAT, AstType.LONG)
			Opcodes.F2D -> ip.conv(AstType.FLOAT, AstType.DOUBLE)
			Opcodes.D2I -> ip.conv(AstType.DOUBLE, AstType.INT)
			Opcodes.D2L -> ip.conv(AstType.DOUBLE, AstType.LONG)
			Opcodes.D2F -> ip.conv(AstType.DOUBLE, AstType.FLOAT)
			Opcodes.I2B -> ip.conv(AstType.INT, AstType.BYTE)
			Opcodes.I2C -> ip.conv(AstType.INT, AstType.CHAR)
			Opcodes.I2S -> ip.conv(AstType.INT, AstType.SHORT)
			Opcodes.LCMP -> ip.binop(AstType.LONG, AstBinop.CMP)
			Opcodes.FCMPL -> ip.binop(AstType.FLOAT, AstBinop.CMPL)
			Opcodes.FCMPG -> ip.binop(AstType.FLOAT, AstBinop.CMPG)
			Opcodes.DCMPL -> ip.binop(AstType.DOUBLE, AstBinop.CMPL)
			Opcodes.DCMPG -> ip.binop(AstType.DOUBLE, AstBinop.CMPG)
			Opcodes.IRETURN -> ip.ret(AstType.INT)
			Opcodes.LRETURN -> ip.ret(AstType.LONG)
			Opcodes.FRETURN -> ip.ret(AstType.FLOAT)
			Opcodes.DRETURN -> ip.ret(AstType.DOUBLE)
			Opcodes.ARETURN -> ip.ret(AstType.OBJECT)
			Opcodes.RETURN -> ip.retvoid()
			Opcodes.ARRAYLENGTH -> ip.arraylength()
			Opcodes.ATHROW -> ip.athrow()
			Opcodes.MONITORENTER -> ip.monitor(true)
			Opcodes.MONITOREXIT -> ip.monitor(false)
		}
	}

	override fun visitIincInsn(index: Int, increment: Int) {
		ip.iinc(index, increment)
	}

	override fun visitLineNumber(line: Int, start: Label?) {
		//super.visitLineNumber(p0, p1)
		ip.line(line, start)
	}

	override fun visitEnd() {
		ip.end()
	}

	override fun visitLocalVariable(name: String?, desc: String?, signature: String?, start: Label?, end: Label?, index: Int) {
		//super.visitLocalVariable(p0, p1, p2, p3, p4, p5)
		ip.localVariable(name, desc, signature, start, end, index)
	}

	override fun visitParameter(name: String?, access: Int) {
		// access = ACC_FINAL | ACC_SYNTHETIC |ACC_MANDATED
		ip.param(name, access)
		//super.visitParameter(p0, p1)
	}

	override fun visitAttribute(attr: Attribute?) {
		ip.attr(attr)
	}

	override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, desc: String?) {
		when (opcode) {
			Opcodes.GETSTATIC -> ip.getfield(true, owner, name, desc)
			Opcodes.PUTSTATIC -> ip.putfield(true, owner, name, desc)
			Opcodes.GETFIELD -> ip.getfield(false, owner, name, desc)
			Opcodes.PUTFIELD -> ip.putfield(false, owner, name, desc)
		}
	}

	override fun visitCode() {
		ip.code()
	}

	override fun visitInsnAnnotation(p0: Int, p1: TypePath?, p2: String?, p3: Boolean): AnnotationVisitor? {
		//return super.visitInsnAnnotation(p0, p1, p2, p3)
		return null
	}

	override fun visitParameterAnnotation(p0: Int, p1: String?, p2: Boolean): AnnotationVisitor? {
		//return super.visitParameterAnnotation(p0, p1, p2)
		return null
	}

	override fun visitLocalVariableAnnotation(p0: Int, p1: TypePath?, p2: Array<out Label>?, p3: Array<out Label>?, p4: IntArray?, p5: String?, p6: Boolean): AnnotationVisitor? {
		//return super.visitLocalVariableAnnotation(p0, p1, p2, p3, p4, p5, p6)
		return null
	}

	override fun visitAnnotationDefault(): AnnotationVisitor? {
		return null
	}

	override fun visitAnnotation(desc: String?, visible: Boolean): AnnotationVisitor? {
		//return super.visitAnnotation(p0, p1)
		return null
	}

	override fun visitTypeAnnotation(typeRef: Int, typePath: TypePath?, desc: String?, visible: Boolean): AnnotationVisitor? {
		//return super.visitTypeAnnotation(p0, p1, p2, p3)
		return null
	}

	override fun visitTryCatchAnnotation(typeRef: Int, typePath: TypePath?, desc: String?, visible: Boolean): AnnotationVisitor? {
		//ip.tryCatchAnnotation()
		return null // visitor
	}

	open class Processor {
		enum class InvokeType {
			VIRTUAL, SPECIAL, STATIC, INTERFACE
		}

		open fun const(value: Any?): Unit {

		}

		fun aconstnull(): Unit {
			const(null)
		}

		fun bconst(value: Byte): Unit {
			const(value)
		}

		fun sconst(value: Short): Unit {
			const(value)
		}

		fun iconst(value: Int): Unit {
			const(value)
		}

		fun lconst(value: Long): Unit {
			const(value)
		}

		fun fconst(value: Float): Unit {
			const(value)
		}

		fun dconst(value: Double): Unit {
			const(value)
		}

		fun strconst(value: String): Unit {
			const(value)
		}

		fun typeconst(value: Type) {
			const(value)
		}

		fun handleconst(value: Handle) {
			const(value)
		}

		fun classconst(value: ShortArray) {
			const(value)
		}

		open fun binop(type: AstType, operator: AstBinop): Unit {
		}

		open fun unop(type: AstType, operator: AstUnop): Unit {
		}

		open fun conv(src: AstType, dst: AstType): Unit {
		}

		open fun ret(type: AstType): Unit {
		}

		open fun retvoid(): Unit {
		}

		open fun arraylength(): Unit {
		}

		open fun athrow(): Unit {
		}

		open fun monitor(enter: Boolean) {

		}

		open fun invoke(type: InvokeType, owner: String?, name: String?, desc: String?, itf: Boolean) {
		}

		fun invokedynamic(name: String?, desc: String?, handle: Handle?, args: Array<out Any?>) {
		}

		open fun switch(dflt: Label?, pairs: List<Pair<Int, Label?>>) {
		}

		open fun tryCatchBlock(start: Label?, end: Label?, handler: Label?, type: String?) {
		}

		open fun code() {
		}

		open fun getlocal(type: AstType, index: Int) {
		}

		open fun putlocal(type: AstType, index: Int) {
		}

		open fun frame(type: Int, nLocal: Int, local: Array<Any>?, nStack: Int, stack: Array<Any>?) {
		}

		open fun multianewarray(desc: String?, dims: Int) {
		}

		open fun newarray(type: AstType.Primitive) {
		}

		open fun anewarray(type: String?) {
		}

		open fun anew(type: String?) {
		}

		open fun acheckcast(type: String?) {
		}

		open fun ainstanceof(type: String?) {
		}

		open fun maxs(maxStack: Int, maxLocal: Int) {
		}

		open fun label(label: Label?) {
		}

		open fun arrayget(type: AstType) {
		}

		open fun arrayset(type: AstType) {
		}

		open fun pop() {
		}

		open fun pop2() {
		}

		open fun dup() {
		}

		open fun dupx1() {
		}

		open fun dupx2() {
		}

		open fun dup2() {
		}

		open fun dup2_x1() {
		}

		open fun dup2_x2() {
		}

		open fun swap() {
		}

		open fun iinc(index: Int, increment: Int) {
		}

		open fun line(line: Int, start: Label?) {
		}

		open fun gotoif0(eq: AstBinop, label: Label?) {
		}

		open fun gotoif_i(eq: AstBinop, label: Label?) {
		}

		open fun gotoif_a(eq: AstBinop, label: Label?) {
		}

		open fun goto(label: Label?) {
		}

		open fun gotoifnull(eq: AstBinop, label: Label?) {
		}

		open fun end() {
		}

		open fun getfield(static: Boolean, owner: String?, name: String?, desc: String?) {
		}

		open fun putfield(static: Boolean, owner: String?, name: String?, desc: String?) {
		}

		open fun localVariable(name: String?, desc: String?, signature: String?, start: Label?, end: Label?, index: Int) {
		}

		open fun attr(attr: Attribute?) {
		}

		open fun param(name: String?, access: Int) {
		}
	}
}
