package com.jtransc.backend.asm2

import com.jtransc.ast.*
import com.jtransc.backend.BaseAsmToAst
import com.jtransc.backend.isStatic
import com.jtransc.ds.cast
import com.jtransc.ds.hasFlag
import com.jtransc.injector.Singleton
import com.jtransc.org.objectweb.asm.Label
import com.jtransc.org.objectweb.asm.Opcodes
import com.jtransc.org.objectweb.asm.tree.*
import java.util.*
import kotlin.collections.set

@Singleton
class AsmToAst2(types: AstTypes, buildSettings: AstBuildSettings) : BaseAsmToAst(types, buildSettings) {
	override val expandFrames = true

	override fun genBody(classRef: AstType.REF, methodNode: MethodNode, types: AstTypes, source: String): AstBody {
		return AsmToAstMethodBody2(classRef, methodNode, types, source)
	}
}

data class PHIOption(val branch: AbstractInsnNode, val op: Operand)

class LocalBox(var local: Local)

interface Operand {
	val type: AstType
}

data class Local(override val type: AstType, val index: Int) : Operand {
	override fun toString(): String = "\$$index:$type"

	class Box(var local: Local)
}

fun Local.box() = Local.Box(this)

data class Constant(override val type: AstType, val v: Any?) : Operand {
	override fun toString(): String = "$v"
}

data class Param(override val type: AstType, val index: Int) : Operand {
	override fun toString(): String = "p$index"
}

data class This(val clazz: AstType.REF) : Operand {
	override val type = clazz
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
	val methodInstructions = method.instructions
	val methodRef = AstMethodRef(clazz.name, method.name, methodType)

	val referencedLabels = hashSetOf<Label>()

	// Find referenced labels
	for (f in methodInstructions) {
		when (f) {
			is JumpInsnNode -> referencedLabels += f.label.label
			is LookupSwitchInsnNode -> {
				referencedLabels += f.dflt.label
				referencedLabels += f.labels.map { it.label }
			}
			is TableSwitchInsnNode -> {
				referencedLabels += f.dflt.label
				referencedLabels += f.labels.map { it.label }
			}
		}
	}

	for (tcb in method.tryCatchBlocks) {
		referencedLabels += tcb.start.label
		referencedLabels += tcb.end.label
		referencedLabels += tcb.handler.label
	}

	// Remove unused labels
	for (f in methodInstructions.toArray().toList()) {
		if (f is LabelNode) {
			if (f.label !in referencedLabels) {
				methodInstructions.remove(f)
			}
		}
	}

	//method.instructions.remove()

	//println("---------------------------\n".repeat(10)); for (i in method.instructions.toArray().toList()) println(i.disasm())

	val entryLocals = LocalsBuilder()
	var varIndex = 0
	if (!method.isStatic()) {
		entryLocals.setLocalType(varIndex, clazz)
		varIndex++
	}
	for (arg in methodType.args) {
		entryLocals.setLocalType(varIndex, arg.type)
		varIndex += if (arg.type.isLongOrDouble()) 2 else 1
	}

	val builder = MethodBlocks(clazz, method, types)
	val context = builder.blockContext
	builder.buildTree(methodInstructions.first, BasicFrame(ArrayList(entryLocals.locals), listOf()))

	for (tcb in method.tryCatchBlocks) {
		val exceptionType = if (tcb.type != null) types.REF_INT(tcb.type) else AstType.THROWABLE
		val startBlock = builder.startToBlocks[tcb.start]!!

		builder.buildTree(tcb.handler, BasicFrame(ArrayList(startBlock.inputFrame.locals), listOf(CatchException(exceptionType))))
	}

	//for (i in method.instructions.toArray().toList()) {
	//	if (i in builder.startToBlocks) {
	//		println("-----")
	//		println(builder.startToBlocks[i]!!.stms.toList().joinToString("\n"))
	//	}
	//}

	// Create SSA form
	//SSABuilder(builder).build()

	// Remove PHI nodes
	//builder.removePHI()

	val tirToStm = TirToStm(methodType, context, types)
	for (i in method.instructions.toArray().toList()) {
		if (i in builder.startToBlocks) {
			tirToStm.convert(builder.startToBlocks[i]!!.stms)
		}
	}

	val outStms = tirToStm.stms
	//println("--------")
	//for (stm in outStms) println(stm)

	val tryCatchBlocks = method.tryCatchBlocks.cast<TryCatchBlockNode>()

	return AstBody(
		types = types,
		stm = outStms.stm(),
		type = methodType,
		//tirToStm.locals.values.toList(),
		traps = tryCatchBlocks.map {
			AstTrap(
				start = context.label(it.start),
				end = context.label(it.end),
				handler = context.label(it.handler),
				exception = if (it.type != null) types.REF_INT2(it.type) else AstType.OBJECT
			)
		},
		flags = AstBodyFlags(types = types, strictfp = method.access.hasFlag(Opcodes.ACC_STRICT), hasDynamicInvoke = context.hasInvokeDynamic),
		methodRef = methodRef
	)//.optimize()
}

class DefinitionInfo {
	var decl: TIR? = null
	var uses = arrayListOf<TIR>()
}

interface DefinitionProcessor {
	fun use(tir: TIR, c: Operand): Unit
	fun def(tir: TIR, c: Local): Unit
}

class SSABuilder(val blocks: MethodBlocks) : DefinitionProcessor {
	val visited = hashSetOf<BasicBlock>()

	override fun use(tir: TIR, c: Operand) {
	}

	override fun def(tir: TIR, c: Local) {
		c.index
	}

	fun build() {
		build(blocks.first)
	}

	private fun build(bb: BasicBlock) {
		if (bb in visited) return
		visited += bb
		for (stm in bb.stms.toList()) {
			//println("SSA:$stm")
		}
		for (s in bb.allSuccessors) build(blocks.startToBlocks[s]!!)
	}
}

class BlockContext {
	var hasInvokeDynamic = false
	var tempId = 1000
	val labels = hashMapOf<Label, AstLabel>()
	val inputFrames = hashMapOf<AbstractInsnNode, BasicFrame>()
	fun createTemp(type: AstType) = Local(type, tempId++)
	fun getVar(type: AstType, v: Int) = Local(type, v)
	fun label(label: Label): AstLabel = labels.getOrPut(label) { AstLabel("$label") }
	fun label(label: LabelNode): AstLabel = label(label.label)
}

class MethodBlocks(val clazz: AstType.REF, val method: MethodNode, val types: AstTypes) {
	val blockContext = BlockContext()
	val startToBlocks = hashMapOf<AbstractInsnNode, BasicBlock>()
	lateinit var first: BasicBlock

	fun buildTree(start: AbstractInsnNode, initialFrame: BasicFrame) {
		first = build(start, onePredecessor = null, inputFrame = initialFrame)
	}

	private fun build(start: AbstractInsnNode, onePredecessor: BasicBlock?, inputFrame: BasicFrame): BasicBlock {
		val bbb1 = startToBlocks[start]
		if (bbb1 == null) { // Not processed yet!
			val bbb = BasicBlock(types, blockContext, clazz, method, inputFrame).apply {
				decodeBlock(start, onePredecessor)
			}
			startToBlocks[start] = bbb
			for (successor in bbb.allSuccessors) {
				build(successor, onePredecessor = bbb, inputFrame = bbb.outputFrame)
			}
		}
		return startToBlocks[start]!!
	}

	fun removePHI() {
		for (block in startToBlocks.values) {
			removePHI(block.stms.toList())
		}
	}

	fun removePHI(items: List<TIR>) {
		//for ((n, item) in items.withIndex()) {
		//	if (item is TIR.PHI) {
		//		val phi = item
		//		for (param in phi.params) {
		//			val predecessorStms = startToBlocks[param.branch]!!.first!!.toList()
		//			// @TODO: Use linkedlist nodes to totally avoid searching
		//			val placeHolderIndex = predecessorStms.indexOfLast { it is TIR.PHI_PLACEHOLDER }
		//			if (placeHolderIndex >= 0) {
		//				predecessorStms[placeHolderIndex] = TIR.MOV(phi.dst, param.op)
		//			} else {
		//				println("Not found PHI placeholder")
		//			}
		//		}
		//		items[n] = TIR.NOP(false)
		//	}
		//}
	}
}

