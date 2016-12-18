package com.jtransc.backend.asm2

import com.jtransc.ast.*
import com.jtransc.backend.isEnd
import com.jtransc.backend.isEndOfBasicBlock
import com.jtransc.backend.isStatic
import com.jtransc.ds.Stack
import com.jtransc.error.invalidOp
import com.jtransc.error.unsupported
import com.jtransc.log.log
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
	fun toStmString(): String
	fun processDefs(p: DefinitionProcessor): Unit
	var dstBox:LocalBox?

	class Mixin : TIR {
		//override val target: Local? = null
		//override val sources = listOf<Local>()
		override var dstBox:LocalBox? = null
		override var prev: TIR? = null
		override var next: TIR? = null
		override fun toStmString() = this.toString()
		override fun processDefs(p: DefinitionProcessor) {
		}
	}

	data class NOP(val dummy: Boolean) : TIR by Mixin() {
		override fun processDefs(p: DefinitionProcessor) {
		}

		override fun toStmString() = ";"
	}

	data class PHI_PLACEHOLDER(val dummy: Boolean) : TIR by Mixin() {
		override fun processDefs(p: DefinitionProcessor) {
		}

		override fun toStmString() = "PHI_PLACEHOLDER"
	}

	data class LABEL(val label: Label) : TIR by Mixin() {
		override fun processDefs(p: DefinitionProcessor) {
		}

		override fun toStmString() = "$label"
	}

	data class PHI(val dst: Local, val params: ArrayList<PHIOption> = arrayListOf()) : TIR by Mixin() {
		override fun processDefs(p: DefinitionProcessor) {
			p.def(this, dst)
		}

		override fun toStmString() = "PHI"
	}

	data class MOV(val dst: Local, val src: Operand) : TIR by Mixin() {
		override fun processDefs(p: DefinitionProcessor) {
			p.use(this, src)
			p.def(this, dst)
		}

		override fun toStmString() = "$dst = $src;"
	}

	data class BINOP(val dst: Local, val l: Operand, val op: String, val r: Operand) : TIR by Mixin() {
		override fun processDefs(p: DefinitionProcessor) {
			p.use(this, l)
			p.use(this, r)
			p.def(this, dst)
		}

		override fun toStmString() = "$dst = $l $op $r;"
	}

	data class UNOP(val dst: Local, val op: String, val r: Operand) : TIR by Mixin() {
		override fun processDefs(p: DefinitionProcessor) {
			p.use(this, r)
			p.def(this, dst)
		}

		override fun toStmString() = "$dst = $op $r;"
	}

	data class CONV(val dst: Local, val src: Operand, val dstType: AstType) : TIR by Mixin() {
		override fun processDefs(p: DefinitionProcessor) {
			p.use(this, src)
			p.def(this, dst)
		}

		override fun toStmString() = "$dst = ($dstType)$src;"
	}

	data class ASTORE(val array: Operand, val index: Operand, val value: Operand) : TIR by Mixin() {
		override fun processDefs(p: DefinitionProcessor) {
			p.use(this, value)
			p.use(this, index)
			p.use(this, array)
		}

		override fun toStmString() = "$array[$index] = $value;"
	}

	data class ALOAD(val dst: Local, val array: Operand, val index: Operand) : TIR by Mixin() {
		override fun processDefs(p: DefinitionProcessor) {
			p.use(this, index)
			p.use(this, array)
			p.def(this, dst)
		}

		override fun toStmString() = "$dst = $array[$index];"
	}

	data class NEW(val dst: Local, val type: AstType) : TIR by Mixin() {
		override fun processDefs(p: DefinitionProcessor) {
			p.def(this, dst)
		}

		override fun toStmString() = "$dst = new $type();"
	}

	data class NEWARRAY(val dst: Local, val arrayType: AstType, val lens: List<Operand>) : TIR by Mixin() {
		override fun processDefs(p: DefinitionProcessor) {
			for (l in lens) p.use(this, l)
			p.def(this, dst)
		}

		override fun toStmString() = "$dst = new $arrayType$lens;"
	}

	data class JUMP_IF(val label: Label, val l: Operand, val op: String, val r: Operand) : TIR by Mixin() {
		override fun processDefs(p: DefinitionProcessor) {
			p.use(this, r)
			p.use(this, l)
		}

		override fun toStmString() = "if ($l $op $r) goto $label;"
	}

	data class JUMP(val label: Label) : TIR by Mixin() {
		override fun processDefs(p: DefinitionProcessor) {
		}

		override fun toStmString() = "goto $label;"
	}

	data class RET(val v: Operand?) : TIR by Mixin() {
		override fun processDefs(p: DefinitionProcessor) {
			if (v != null) p.use(this, v)
		}

		override fun toStmString() = if (v == null) "return;" else "return $v;"
	}

	data class INVOKE(val dst: Local?, val obj: Local?, val method: AstMethodRef, val args: List<Operand>) : TIR by Mixin() {
		override fun processDefs(p: DefinitionProcessor) {
			for (arg in args) p.use(this, arg)
			if (obj != null) p.use(this, obj)
			if (dst != null) p.def(this, dst)
		}

		override fun toStmString() = "$dst = $obj.$method($args)"
	}

	data class INSTANCEOF(val dst: Local, val type: AstType, val src: Operand) : TIR by Mixin() {
		override fun processDefs(p: DefinitionProcessor) {
			p.use(this, src)
			p.def(this, dst)
		}

		override fun toStmString() = "$dst = $src instanceof $type;"
	}

	data class THROW(val ex: Operand) : TIR by Mixin() {
		override fun processDefs(p: DefinitionProcessor) {
			p.use(this, ex)
		}

		override fun toStmString() = "throw $ex;"
	}

	data class GETSTATIC(val dst: Local, val field: AstFieldRef) : TIR by Mixin() {
		override fun processDefs(p: DefinitionProcessor) {
			p.def(this, dst)
		}

		override fun toStmString() = "$dst = $field;"
	}

	data class PUTSTATIC(val field: AstFieldRef, val src: Operand) : TIR by Mixin() {
		override fun processDefs(p: DefinitionProcessor) {
			p.use(this, src)
		}

		override fun toStmString() = "$field = $src;"
	}

	data class GETFIELD(val dst: Local, val field: AstFieldRef, val obj: Operand) : TIR by Mixin() {
		override fun processDefs(p: DefinitionProcessor) {
			p.use(this, obj)
			p.def(this, dst)
		}

		override fun toStmString() = "$dst = $obj.$field;"
	}

	data class PUTFIELD(val field: AstFieldRef, val obj: Operand, val src: Operand) : TIR by Mixin() {
		override fun processDefs(p: DefinitionProcessor) {
			p.use(this, obj)
			p.use(this, src)
		}

		override fun toStmString() = "$obj.$field = $src;"
	}

	data class ARRAYLENGTH(val dst: Local, val obj: Operand) : TIR by Mixin() {
		override fun processDefs(p: DefinitionProcessor) {
			p.use(this, obj)
			p.def(this, dst)
		}

		override fun toStmString() = "$dst = $obj.length;"
	}

	data class MONITOR(val obj: Operand, val enter: Boolean) : TIR by Mixin() {
		override fun processDefs(p: DefinitionProcessor) {
			p.use(this, obj)
		}

		override fun toStmString() = "MONITOR($obj, enter=$enter)"
	}

	data class SWITCH_GOTO(val subject: Operand, val label: Label?, val toMap: Map<Int, Label>) : TIR by Mixin() {
		override fun processDefs(p: DefinitionProcessor) {
			p.use(this, subject)
		}
	}
}

data class PHIOption(val branch: AbstractInsnNode, val op: Operand)

class LocalBox(var local: Local)

interface Operand {
	val type: AstType
}

data class Local(override val type: AstType, val index: Int) : Operand {
	override fun toString(): String = "\$$index"
}

data class Constant(override val type: AstType, val v: Any?) : Operand {
	override fun toString(): String = "$v"
}

data class Param(override val type: AstType, val index: Int) : Operand {
	override fun toString(): String = "p$index"
}

data class This(override val type: AstType) : Operand {
	override fun toString(): String = "this"
}

data class CatchException(override val type: AstType) : Operand {
	override fun toString(): String = "exception"
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

	val builder = MethodBlocks(clazz, method, types)
	builder.buildTree(method.instructions.first)
	for (tcb in method.tryCatchBlocks) {
		val exceptionType = if (tcb.type != null) types.REF_INT(tcb.type) else AstType.THROWABLE
		builder.buildTree(tcb.handler, initialStack = listOf(CatchException(exceptionType)))
	}

	SSABuilder(builder).build()

	// Remove PHI nodes
	//builder.removePHI()

	val outStms = arrayListOf<TIR>()

	for (i in method.instructions.toArray().toList()) {
		if (i in builder.startToBlocks) {
			outStms += builder.startToBlocks[i]!!.stms
		}
	}

	println("--------")
	for (stm in outStms) {
		println(stm)
	}

	return AstBody(
		types,
		AstStm.STMS(),
		methodType
	)
}

interface DefinitionProcessor {
	fun use(tir: TIR, c: Operand): Unit
	fun def(tir: TIR, c: Local): Unit
}

class SSABuilder(val blocks: MethodBlocks) : DefinitionProcessor {
	override fun use(tir: TIR, c: Operand) {
	}

	override fun def(tir: TIR, c: Local) {
		c.index
	}

	fun build() {
		build(blocks.first)
	}

	private fun build(bb: BasicBlock) {
		for (stm in bb.stms) {
			println("SSA:$stm")
			stm.processDefs(this)
		}
		for (s in bb.allSuccessors) build(blocks.startToBlocks[s]!!)
	}
}

class BlockContext {
	var hasInvokeDynamic = false
	var tempId = 1000
	fun createTemp(type: AstType) = Local(type, tempId++)
	fun getVar(type: AstType, v: Int) = Local(type, v)
}

class MethodBlocks(val clazz: AstType.REF, val method: MethodNode, val types: AstTypes) {
	val locals = BlockContext()
	val startToBlocks = hashMapOf<AbstractInsnNode, BasicBlock>()
	lateinit var first: BasicBlock

	fun buildTree(start: AbstractInsnNode, initialStack: List<Operand>? = null) {
		first = build(start, onePredecessor = null, initialStack = initialStack)
	}

	private fun build(start: AbstractInsnNode, onePredecessor: BasicBlock?, initialStack: List<Operand>? = null): BasicBlock {
		val bbb1 = startToBlocks[start]
		if (bbb1 != null) { // Processed already!
			if (onePredecessor != null) {
				bbb1.registerPredecessor(onePredecessor)
			}
		} else {
			val bbb = BasicBlock(types, locals).apply {
				decodeBlock(clazz, method, start, onePredecessor, initialStack)
			}
			startToBlocks[start] = bbb
			for (successor in bbb.allSuccessors) {
				build(successor, onePredecessor = bbb, initialStack = null)
			}
		}
		return startToBlocks[start]!!
	}

	fun removePHI() {
		for (block in startToBlocks.values) {
			removePHI(block.stms)
		}
	}

	fun removePHI(items: ArrayList<TIR>) {
		for ((n, item) in items.withIndex()) {
			if (item is TIR.PHI) {
				val phi = item
				for (param in phi.params) {
					val predecessorStms = startToBlocks[param.branch]!!.stms
					// @TODO: Use linkedlist nodes to totally avoid searching
					val placeHolderIndex = predecessorStms.indexOfLast { it is TIR.PHI_PLACEHOLDER }
					if (placeHolderIndex >= 0) {
						predecessorStms[placeHolderIndex] = TIR.MOV(phi.dst, param.op)
					} else {
						println("Not found PHI placeholder")
					}
				}
				items[n] = TIR.NOP(false)
			}
		}
	}
}

class OutputStackElement(val operand: Operand, val target: Local) {

}

class BasicBlock(val types: AstTypes, val blockContext: BlockContext) {
	val JUMP_OPS = listOf("==", "!=", "<", ">=", ">", "<=", "==", "!=")
	val TPRIM = listOf(AstType.INT, AstType.LONG, AstType.FLOAT, AstType.DOUBLE, AstType.OBJECT, AstType.BYTE, AstType.CHAR, AstType.SHORT)
	var tail: TIR? = null
	val stms = arrayListOf<TIR>()
	val stack = Stack<Operand>()
	val outputStack = arrayListOf<OutputStackElement>()

	fun getVar(type: AstType, v: Int) = blockContext.getVar(type, v)

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
		n.prev = tail
		tail?.next = n
		stms += n
		tail = n
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

	val predecessors = hashSetOf<BasicBlock>()

	fun registerPredecessor(predecessor: BasicBlock) {
		if (predecessors.isEmpty()) {
			for (item in predecessor.outputStack.toList()) {
				add(TIR.PHI(item.target))
				push(item.target)
			}
		}
		if (predecessor !in predecessors) {
			predecessors += predecessor
			for ((index, item) in predecessor.outputStack.toList().withIndex()) {
				val phi = stms[index] as TIR.PHI
				phi.params += PHIOption(predecessor.start, item.operand)
			}
		}
	}

	fun decodeBlock(clazz: AstType.REF, method: MethodNode, start: AbstractInsnNode, onePredecessor: BasicBlock?, initialStack: List<Operand>? = null) {
		this.start = start

		val isEntryNode = onePredecessor == null

		if (isEntryNode) {
			val methodType = types.demangleMethod(method.desc)
			var varIndex = 0
			if (!method.isStatic()) {
				add(TIR.MOV(Local(clazz, varIndex++), This(clazz)))
			}
			for (arg in methodType.args) {
				add(TIR.MOV(Local(arg.type, varIndex), Param(arg.type, arg.index)))
				varIndex += if (arg.type.isLongOrDouble()) 2 else 1
			}
		}

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
		outputStack += stack.toList().map { OutputStackElement(it, createTemp(it.type)) }
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
				val dst = createTemp(fieldRef.type)
				add(TIR.PUTFIELD(fieldRef, obj, src))
				push(dst)
			}
		}
	}

	fun decodeTypeNode(n: TypeInsnNode) {
		val type = types.REF_INT(n.desc)
		when (n.opcode) {
			Opcodes.NEW -> {
				val dst = createTemp(type)
				add(TIR.NEW(dst, type))
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
				val dst = createTemp(obj.type)
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
		val arrayType = types.REF_INT(n.desc) as AstType.ARRAY
		val dst = createTemp(arrayType)
		add(TIR.NEWARRAY(dst, arrayType, (0 until n.dims).map { pop() }.reversed()))
		push(dst)
	}

	fun decodeTableSwitchNode(n: TableSwitchInsnNode) {
		nextDirectNode = n.dflt
		jumpNodes += n.labels

		val subject = pop()

		phiPlaceholder()
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

		phiPlaceholder()
		add(TIR.SWITCH_GOTO(
			subject,
			n.dflt.label,
			n.keys.zip(n.labels).map { it.first to it.second.label }.toMap()
		))
	}

	fun decodeInvokeDynamicNode(n: InvokeDynamicInsnNode) {
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
				phiPlaceholder()
				add(TIR.JUMP_IF(n.label.label, value, op, Constant(AstType.INT, 0)))
			}
			Opcodes.IFNULL, Opcodes.IFNONNULL -> {
				val op = JUMP_OPS[n.opcode - Opcodes.IFNULL]
				val value = stack.pop()
				phiPlaceholder()
				add(TIR.JUMP_IF(n.label.label, value, op, Constant(AstType.OBJECT, null)))
			}
			in Opcodes.IF_ICMPEQ..Opcodes.IF_ACMPNE -> {
				val op = JUMP_OPS[n.opcode - Opcodes.IF_ICMPEQ]
				val valueL = stack.pop()
				val valueR = stack.pop()
				phiPlaceholder()
				add(TIR.JUMP_IF(n.label.label, valueL, op, valueR))
			}
			Opcodes.GOTO -> {
				phiPlaceholder()
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

	private fun phiPlaceholder() {
		for (n in 0 until stack.length) add(TIR.PHI_PLACEHOLDER(false))
	}

	fun decodeIincNode(n: IincInsnNode) {
		add(TIR.BINOP(getVar(AstType.INT, n.`var`), getVar(AstType.INT, n.`var`), "+", Constant(AstType.INT, 1)))
	}

	fun decodeFrameNode(n: FrameNode) {
		Unit // Do nothing. We calculate frames ourselves to full compatibility with versions less than Java6.
	}

	fun decodeLabelNode(n: LabelNode) {
		//add(TIR.LABEL(n.label))

		//Unit // Do nothing. We handle basic blocks in other place.
	}

	fun decodeLineNode(n: LineNumberNode) {
		Unit
	}

	fun decodeMethodNode(n: MethodInsnNode) {
		val ownerType = types.REF_INT(n.owner)
		val methodType = types.demangleMethod(n.desc)
		val ownerTypeRef = if (ownerType is AstType.ARRAY) AstType.OBJECT else ownerType as AstType.REF
		val methodRef = AstMethodRef(ownerTypeRef.name, n.name, methodType)
		val args = methodType.args.reversed().map { pop() }
		val obj = if (n.opcode != Opcodes.INVOKESTATIC) pop() as Local else null
		val res = if (methodType.retVoid) null else createTemp(methodType.ret)
		add(TIR.INVOKE(res, obj, methodRef, args))
		if (res != null) push(res)
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
				add(TIR.NEWARRAY(dst, type, listOf(len)))
				push(dst)
			}
		}
	}

	fun decodeInsNode(n: InsnNode) {
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

			Opcodes.L2I, Opcodes.F2I, Opcodes.D2I -> conv(AstType.INT)
			Opcodes.I2L, Opcodes.F2L, Opcodes.D2L -> conv(AstType.LONG)
			Opcodes.I2F, Opcodes.L2F, Opcodes.D2F -> conv(AstType.FLOAT)
			Opcodes.I2D, Opcodes.L2D, Opcodes.F2D -> conv(AstType.DOUBLE)

			Opcodes.I2B -> conv(AstType.BYTE)
			Opcodes.I2C -> conv(AstType.CHAR)
			Opcodes.I2S -> conv(AstType.SHORT)

			Opcodes.LCMP -> binop(AstType.INT, "cmp")
			Opcodes.FCMPL, Opcodes.DCMPL -> binop(AstType.INT, "cmpl")
			Opcodes.FCMPG, Opcodes.DCMPG -> binop(AstType.INT, "cmpg")

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
		add(TIR.ASTORE(array, index, value))
	}

	private fun arrayLoad(elementType: AstType) {
		//val arrayType = AstType.ARRAY(elementType)
		val index = pop()
		val array = pop()
		val dst = createTemp(elementType)
		add(TIR.ALOAD(dst, array, index))
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
	fun load(type: AstType, idx: Int) = push(getVar(type, idx))
	fun store(type: AstType, idx: Int) = add(TIR.MOV(getVar(type, idx), pop()))

	fun binop(resultType: AstType, op: String) {
		val r = pop()
		val l = pop()
		val dst = createTemp(resultType)
		add(TIR.BINOP(dst, l, op, r))
		push(dst)
	}

	fun unop(type: AstType, op: String) {
		val r = pop()
		val dst = createTemp(type)
		add(TIR.UNOP(dst, op, r))
		push(dst)
	}
}