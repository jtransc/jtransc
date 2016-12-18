package com.jtransc.backend.asm2

import com.jtransc.ast.*
import com.jtransc.ast.optimize.optimize
import com.jtransc.backend.BaseAsmToAst
import com.jtransc.backend.asm1.disasm
import com.jtransc.backend.isStatic
import com.jtransc.ds.cast
import com.jtransc.ds.hasFlag
import com.jtransc.injector.Singleton
import com.jtransc.org.objectweb.asm.Label
import com.jtransc.org.objectweb.asm.Opcodes
import com.jtransc.org.objectweb.asm.tree.*
import kotlin.collections.set

@Singleton
class AsmToAst2(types: AstTypes) : BaseAsmToAst(types) {
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

	//for (i in method.instructions.toArray().toList()) println(i.disasm())

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
	builder.buildTree(methodInstructions.first, BasicFrame(entryLocals.locals.toList(), listOf()))

	for (tcb in method.tryCatchBlocks) {
		val exceptionType = if (tcb.type != null) types.REF_INT(tcb.type) else AstType.THROWABLE
		builder.buildTree(tcb.handler, BasicFrame(entryLocals.locals.toList(), listOf(CatchException(exceptionType))))
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
		types,
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
	)//.optimize()
}

class TirToStm(val methodType: AstType.METHOD, val blockContext: BlockContext, val types: AstTypes) {
	val locals = hashMapOf<Local, AstLocal>()
	val stms = arrayListOf<AstStm>()
	var id = 0

	val Local.ast: AstLocal get() {
		val canonicalLocal = Local(this.type.simplify(), this.index)
		return locals.getOrPut(canonicalLocal) { AstLocal(id++, canonicalLocal.type) }
	}
	val Label.ast: AstLabel get() = blockContext.label(this)

	val Local.expr: AstExpr.LOCAL get() = AstExpr.LOCAL(this.ast)

	val Operand.expr: AstExpr get() = when (this) {
		is Constant -> AstExpr.LITERAL(this.v)
		is Param -> AstExpr.PARAM(AstArgument(this.index, this.type))
		is Local -> AstExpr.LOCAL(this.ast)
		is This -> AstExpr.THIS(this.clazz.name)
		//is CatchException -> AstExpr.CAUGHT_EXCEPTION(this.type)
		is CatchException -> AstExpr.CAUGHT_EXCEPTION(AstType.OBJECT)
		else -> TODO("$this")
	}

	fun convert(tirs: List<TIR>) {
		for (tir in tirs) {
			when (tir) {
				is TIR.NOP -> Unit
				is TIR.MOV -> stms += AstStm.SET_LOCAL(tir.dst.expr, tir.src.expr.castTo(tir.dst.type))
				is TIR.INSTANCEOF -> stms += AstStm.SET_LOCAL(tir.dst.expr, AstExpr.INSTANCE_OF(tir.src.expr, tir.type as AstType.Reference))
				is TIR.CONV -> stms += AstStm.SET_LOCAL(tir.dst.expr, AstExpr.CAST(tir.src.expr, tir.dst.type))
				is TIR.ARRAYLENGTH -> stms += AstStm.SET_LOCAL(tir.dst.expr, AstExpr.ARRAY_LENGTH(tir.obj.expr))
				is TIR.NEW -> stms += AstStm.SET_LOCAL(tir.dst.expr, AstExpr.NEW(tir.type))
				is TIR.NEWARRAY -> stms += AstStm.SET_LOCAL(tir.dst.expr, AstExpr.NEW_ARRAY(tir.arrayType, tir.lens.map { it.expr }))
				is TIR.UNOP -> stms += AstStm.SET_LOCAL(tir.dst.expr, AstExpr.UNOP(tir.op, tir.r.expr))
				is TIR.BINOP -> stms += AstStm.SET_LOCAL(tir.dst.expr, AstExpr.BINOP(tir.dst.type, tir.l.expr, tir.op, tir.r.expr))
				is TIR.ARRAY_STORE -> {
					stms += AstStm.SET_ARRAY(tir.array.expr, tir.index.expr, tir.value.expr.castTo(tir.array.type.elementType))
				}
				is TIR.ARRAY_LOAD -> {
					stms += AstStm.SET_LOCAL(tir.dst.expr, AstExpr.ARRAY_ACCESS(tir.array.expr, tir.index.expr))
				}
				is TIR.GETSTATIC -> stms += AstStm.SET_LOCAL(tir.dst.expr, AstExpr.FIELD_STATIC_ACCESS(tir.field))
				is TIR.GETFIELD -> stms += AstStm.SET_LOCAL(tir.dst.expr, AstExpr.FIELD_INSTANCE_ACCESS(tir.field, tir.obj.expr.castTo(tir.field.containingTypeRef)))
				is TIR.PUTSTATIC -> stms += AstStm.SET_FIELD_STATIC(tir.field, tir.src.expr.castTo(tir.field.type))
				is TIR.PUTFIELD -> stms += AstStm.SET_FIELD_INSTANCE(tir.field, tir.obj.expr.castTo(tir.field.containingTypeRef), tir.src.expr.castTo(tir.field.type))
				is TIR.INVOKE_COMMON -> {
					val method = tir.method
					val args = tir.args.zip(method.type.args).map { it.first.expr.castTo(it.second.type) }
					val expr = if (tir.obj != null) {
						AstExpr.CALL_INSTANCE(tir.obj!!.expr.castTo(tir.method.containingClassType), tir.method, args)
					} else {
						AstExpr.CALL_STATIC(tir.method, args)
					}
					if (tir is TIR.INVOKE) {
						stms += AstStm.SET_LOCAL(tir.dst.expr, expr)
					} else {
						stms += AstStm.STM_EXPR(expr)
					}
				}
				is TIR.MONITOR -> stms += if (tir.enter) AstStm.MONITOR_ENTER(tir.obj.expr) else AstStm.MONITOR_EXIT(tir.obj.expr)
			// control flow:
				is TIR.LABEL -> stms += AstStm.STM_LABEL(tir.label.ast)
				is TIR.JUMP -> stms += AstStm.GOTO(tir.label.ast)
				is TIR.JUMP_IF -> stms += AstStm.IF_GOTO(tir.label.ast, AstExpr.BINOP(AstType.BOOL, tir.l.expr, tir.op, tir.r.expr))
				is TIR.SWITCH_GOTO -> stms += AstStm.SWITCH_GOTO(tir.subject.expr, tir.deflt.ast, tir.cases.map { it.key to it.value.ast })
				is TIR.RET -> stms += if (tir.v != null) AstStm.RETURN(tir.v.expr.castTo(methodType.ret)) else AstStm.RETURN_VOID()
				is TIR.THROW -> stms += AstStm.THROW(tir.ex.expr)
			//is TIR.PHI_PLACEHOLDER -> stms += AstStm.NOP("PHI_PLACEHOLDER")
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

