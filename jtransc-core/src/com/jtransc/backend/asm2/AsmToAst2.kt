package com.jtransc.backend.asm2

import com.jtransc.ast.*
import com.jtransc.backend.BaseAsmToAst
import com.jtransc.backend.asm1.AsmToAstMethodBody1
import com.jtransc.ds.cast
import com.jtransc.ds.hasFlag
import com.jtransc.error.invalidOp
import com.jtransc.injector.Singleton
import com.jtransc.org.objectweb.asm.Label
import com.jtransc.org.objectweb.asm.Opcodes
import com.jtransc.org.objectweb.asm.tree.AbstractInsnNode
import com.jtransc.org.objectweb.asm.tree.LabelNode
import com.jtransc.org.objectweb.asm.tree.MethodNode
import com.jtransc.org.objectweb.asm.tree.TryCatchBlockNode
import java.util.*
import kotlin.collections.List
import kotlin.collections.arrayListOf
import kotlin.collections.contains
import kotlin.collections.hashMapOf
import kotlin.collections.indexOfLast
import kotlin.collections.listOf
import kotlin.collections.plusAssign
import kotlin.collections.set
import kotlin.collections.toList
import kotlin.collections.withIndex

@Singleton
class AsmToAst2(types: AstTypes) : BaseAsmToAst(types) {
	override fun genBody(classRef: AstType.REF, methodNode: MethodNode, types: AstTypes, source: String): AstBody {
		return AsmToAstMethodBody1(classRef, methodNode, types, source)
	}
}

data class PHIOption(val branch: AbstractInsnNode, val op: Operand)

class LocalBox(var local: Local)

interface Operand {
	val type: AstType
}

data class Local(override val type: AstType, val index: Int) : Operand {
	override fun toString(): String = "\$$index"

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

	val builder = MethodBlocks(clazz, method, types)
	val context = builder.blockContext
	builder.buildTree(method.instructions.first)
	for (tcb in method.tryCatchBlocks) {
		val exceptionType = if (tcb.type != null) types.REF_INT(tcb.type) else AstType.THROWABLE
		builder.buildTree(tcb.handler, initialStack = listOf(CatchException(exceptionType)))
	}

	// Create SSA form
	SSABuilder(builder).build()

	// Remove PHI nodes
	builder.removePHI()

	val tirToStm = TirToStm(context, types)
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
		AstStm.STMS(outStms),
		methodType,
		tirToStm.locals.values.toList(),
		tryCatchBlocks.map {
			AstTrap(
				start = context.label(it.start),
				end = context.label(it.end),
				handler = context.label(it.handler),
				exception = if (it.type != null) types.REF_INT2(it.type) else AstType.OBJECT
			)
		},
		AstBodyFlags(strictfp = method.access.hasFlag(Opcodes.ACC_STRICT), types = types, hasDynamicInvoke = context.hasInvokeDynamic)
	)
}

class TirToStm(val blockContext: BlockContext, val types: AstTypes) {
	val locals = hashMapOf<Local, AstLocal>()
	val stms = arrayListOf<AstStm>()
	var id = 0

	val Local.ast: AstLocal get() = locals.getOrPut(this) { AstLocal(id++, this.type) }
	val Label.ast: AstLabel get() = blockContext.label(this)

	val Local.expr: AstExpr.LOCAL get() = AstExpr.LOCAL(this.ast)

	val Operand.expr: AstExpr get() = when (this) {
		is Constant -> AstExpr.LITERAL(this.v, types)
		is Param -> AstExpr.PARAM(AstArgument(this.index, this.type))
		is Local -> AstExpr.LOCAL(this.ast)
		is This -> AstExpr.THIS(this.clazz.name)
		is CatchException -> AstExpr.CAUGHT_EXCEPTION(this.type)
		else -> TODO("$this")
	}

	fun convert(tirs: List<TIR>) {
		for (tir in tirs) {
			when (tir) {
				is TIR.NOP -> Unit
				is TIR.MOV -> stms += AstStm.SET_LOCAL(tir.dst.expr, tir.src.expr)
				is TIR.NEW -> stms += AstStm.SET_LOCAL(tir.dst.expr, AstExpr.NEW(tir.type))
				is TIR.NEWARRAY -> stms += AstStm.SET_LOCAL(tir.dst.expr, AstExpr.NEW_ARRAY(tir.arrayType, tir.lens.map { it.expr }))
				is TIR.BINOP -> stms += AstStm.SET_LOCAL(tir.dst.expr, AstExpr.BINOP(tir.dst.type, tir.l.expr, tir.op, tir.r.expr))
				is TIR.ASTORE -> stms += AstStm.SET_ARRAY(tir.array.expr, tir.index.expr, tir.value.expr)
				is TIR.ALOAD -> stms += AstStm.SET_LOCAL(tir.dst.expr, AstExpr.ARRAY_ACCESS(tir.array.expr, tir.index.expr))
				is TIR.GETSTATIC -> stms += AstStm.SET_LOCAL(tir.dst.expr, AstExpr.FIELD_STATIC_ACCESS(tir.field))
				is TIR.GETFIELD -> stms += AstStm.SET_LOCAL(tir.dst.expr, AstExpr.FIELD_INSTANCE_ACCESS(tir.field, tir.obj.expr))
				is TIR.INVOKE_COMMON -> {
					val args = tir.args.map { it.expr }
					val expr = if (tir.obj != null) {
						AstExpr.CALL_INSTANCE(tir.obj!!.expr, tir.method, args)
					} else {
						AstExpr.CALL_STATIC(tir.method, args)
					}
					if (tir is TIR.INVOKE) {
						stms += AstStm.SET_LOCAL(tir.dst.expr, expr)
					} else {
						stms += AstStm.STM_EXPR(expr)
					}
				}
				// control flow:
				is TIR.LABEL -> stms += AstStm.STM_LABEL(tir.label.ast)
				is TIR.JUMP -> stms += AstStm.GOTO(tir.label.ast)
				is TIR.JUMP_IF -> stms += AstStm.IF_GOTO(tir.label.ast, AstExpr.BINOP(AstType.BOOL, tir.l.expr, tir.op, tir.r.expr))
				is TIR.RET -> stms += if (tir.v != null) AstStm.RETURN(tir.v.expr) else AstStm.RETURN_VOID()
				is TIR.THROW -> stms += AstStm.THROW(tir.ex.expr)
				else -> TODO("$tir")
			}
		}
	}
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
			//println("SSA:$stm")
		}
		for (s in bb.allSuccessors) build(blocks.startToBlocks[s]!!)
	}
}

class BlockContext {
	var hasInvokeDynamic = false
	var tempId = 1000
	val labels = hashMapOf<Label, AstLabel>()
	fun createTemp(type: AstType) = Local(type, tempId++)
	fun getVar(type: AstType, v: Int) = Local(type, v)
	fun label(label: Label): AstLabel = labels.getOrPut(label) { AstLabel("$label") }
	fun label(label: LabelNode): AstLabel = label(label.label)
}

class MethodBlocks(val clazz: AstType.REF, val method: MethodNode, val types: AstTypes) {
	val blockContext = BlockContext()
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
			val bbb = BasicBlock(types, blockContext).apply {
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

