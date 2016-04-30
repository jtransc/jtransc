/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jtransc.input.asm

import com.jtransc.org.objectweb.asm.ClassReader
import com.jtransc.org.objectweb.asm.Opcodes
import com.jtransc.org.objectweb.asm.tree.*
import java.io.InputStream
import java.util.*

object Main3 {
	@JvmStatic fun main(args: Array<String>) {
		//println(Simple1::class.qualifiedName.replace())
		val path = Simple1::class.java.canonicalName.replace('.', '/')
		println(path)
		val clazz = readClass(javaClass.getResourceAsStream("/$path.class"))
		println(clazz.methods)
		for (method in clazz.getMethods()) {
			println("---- ${method.name}")
			if (method.name == "<init>") continue
			val result = method.analyze()
			println("*******")
			println(result.mainBlock.render())
		}
	}
}

object Simple1 {
	@JvmStatic fun sum(a: Int, b: Int): Int {
		return a + b
	}

	@JvmStatic fun max(a: Int, b: Int): Int {
		return if ((a >= b)) a else b
	}

	@JvmStatic fun test1(a: Int, b: Int) {
		var out = 0
		for (n in 0 .. a) out += n
		for (m in a .. b) out += m * 2
	}
}


fun readClass(stream: InputStream): ClassNode {
	val classNode = ClassNode()
	ClassReader(stream).accept(classNode, ClassReader.EXPAND_FRAMES)
	return classNode
}

fun ClassNode.getMethods(): List<MethodNode> = this.methods.map { it as MethodNode }.toList()

fun ClassNode.getMethods(name: String): List<MethodNode> = this.methods.map { it as MethodNode }.filter { it.name == name }.toList()

fun ClassNode.getMethod(name: String): MethodNode = this.getMethods(name).first()

fun <T> Queue<T>.queue(value: T): Boolean = this.add(value)
fun <T> Queue<T>.dequeue(): T = this.remove()

fun MethodNode.getInstructionList(): List<AbstractInsnNode> {
	val out = arrayListOf<AbstractInsnNode>()
	for (i in this.instructions.iterator()) out.add(i as AbstractInsnNode)
	return out
}

sealed class Type {
	object BOOL : Type()

	object INT : Type()

	object UNIMPLEMENTED : Type()

}

sealed class Ref {
	class THIS(val type: Type) : Ref()
	class ARGUMENT(val type: Type, val index: Int) : Ref()
	class LOCAL(val type: Type, val index: Int) : Ref()

	override fun toString(): String = this.dump()
}

fun Ref.dump(): String = when (this) {
	is Ref.THIS -> "this"
	is Ref.ARGUMENT -> "arg$index"
	is Ref.LOCAL -> "local$index"
}

sealed class Expr(val type: Type) {
	class LITERAL(val value: Any?) : Expr(Type.UNIMPLEMENTED)
	class REF(val ref: Ref) : Expr(Type.UNIMPLEMENTED)
	class BINOP(val l: Expr, val op: String, val r: Expr) : Expr(l.type)
	class UNOP(val op: String, val r: Expr) : Expr(r.type)
	class ARRAY_LENGTH(val array: Expr) : Expr(Type.INT)
	class ARRAY_LOAD(val array: Expr, val index: Expr) : Expr(Type.UNIMPLEMENTED)

	override fun toString(): String = this.dump()

	/*
	override fun toString():String = when(this) {
		is LITERAL -> "$value;"
		is REF -> "$ref"
		is BINOP -> "$l $op $r"
		is UNOP -> "$op$r"
		is ARRAY_LENGTH -> "$array.length"
		is ARRAY_LOAD -> "$array[$index]"
	}
	*/
}

fun Expr.dump(): String = when (this) {
	is Expr.LITERAL -> "$value"
	is Expr.REF -> "$ref"
	is Expr.BINOP -> "$l $op $r"
	is Expr.UNOP -> "$op$r"
	is Expr.ARRAY_LENGTH -> "$array.length"
	is Expr.ARRAY_LOAD -> "$array[$index]"
}

data class StmLabel(val name: String) {
	override fun toString() = name
}

sealed class Stm {
	var prev: Stm? = null
	var next: Stm? = null

	class RETURN(val expr: Expr?) : Stm()
	class EXPR(val expr: Expr?) : Stm()
	class SET(val ref: Ref, val expr: Expr?) : Stm()
	class STMS(val stms: List<Stm>) : Stm()
	class IF(val cond: Expr, val yes: Stm) : Stm()
	class IF_ELSE(val cond: Expr, val yes: Stm, val no: Stm) : Stm()
	class LABEL(val label: StmLabel) : Stm()
	class GOTO(val label: StmLabel) : Stm()

	override fun toString(): String = this.dump()
	/*
	override fun toString():String = when(this) {
		is RETURN -> "return $expr;"
		is EXPR -> "$expr;"
		is SET -> "$ref = $expr;"
	}
	*/
}

fun Iterable<Stm>.calcLinks(handler: ((stm: Stm) -> Unit)? = null): Iterable<Stm> {
	var prev: Stm? = null
	for (it in this) {
		if (handler != null) handler(it)
		it.prev = prev
		prev?.next = it
		if (it is Stm.STMS) it.stms.calcLinks(handler)
		prev = it
	}
	return this
}

fun Stm.dump(): String = when (this) {
	is Stm.RETURN -> "return $expr;"
	is Stm.EXPR -> "$expr;"
	is Stm.SET -> "$ref = $expr;"
	is Stm.STMS -> "{ ${stms.map { it.dump() }.joinToString("")} }"
	is Stm.IF -> "if ($cond) $yes"
	is Stm.IF_ELSE -> "if ($cond) $yes else $no"
	is Stm.LABEL -> "$label:"
	is Stm.GOTO -> "goto $label;"
}

class EvalFuncContext(val code: List<Stm>, val args: List<Any?>) {
	var retval: Any? = null
	val labels = hashMapOf<StmLabel, Stm.LABEL>()
	val locals = arrayListOf<Any?>(null, null, null, null)

	init {
		println(code.joinToString("\n"))
		code.calcLinks() {
			if (it is Stm.LABEL) labels[it.label] = it
		}
	}

	fun eval(): EvalFuncContext {
		eval(code.firstOrNull())
		return this
	}

	private fun Ref.eval(context: EvalFuncContext): Any? = when (this) {
		is Ref.ARGUMENT -> context.args[this.index]
		is Ref.LOCAL -> context.locals[this.index]
		else -> throw RuntimeException("Unhandled ref $this")
	}

	private fun Expr.eval(context: EvalFuncContext): Any? = when (this) {
		is Expr.REF -> ref.eval(context)
		is Expr.BINOP -> {
			val lv = l.eval(context)
			val rv = r.eval(context)
			when (op) {
				"<" -> (lv as Int) < (rv as Int)
				else -> throw RuntimeException("Unhandled op $op")
			}
		}
		else -> throw RuntimeException("Unhandled expr $this")
	}

	private fun eval(stm: Stm?) {
		var node = stm
		nodeloop@while (node != null) {
			println("EXEC: $node")
			when (node) {
				is Stm.LABEL -> Unit
				is Stm.RETURN -> {
					this.retval = node.expr?.eval(this)
					return
				}
				is Stm.STMS -> {
					node = node.stms.first()
					continue@nodeloop
				}
				is Stm.SET -> {
					locals[(node.ref as Ref.LOCAL).index] = node.expr?.eval(this)
				}
				is Stm.IF -> {
					val result = node.cond.eval(this)
					if (result as Boolean == true) {
						node = node.yes
						continue@nodeloop
					}
				}
				else -> throw RuntimeException("Unhandled stm $node")
			}
			node = node.next
		}
	}
}

class Frame(
	val stackInput: List<Ref> = listOf<Ref>(),
	val locals: ArrayList<Ref> = arrayListOf<Ref>()
) {
	val stack = Stack<Expr>().let {
		for (i in stackInput) it.add(Expr.REF(i))
		it
	}

	companion object {
		fun checkCompatible(a: Frame, b: Frame) {

		}
	}

	override fun toString() = "Frame(stackInput=$stackInput, stack=$stack, locals=$locals)"
}

class BasicBlockBranch(val condition: Expr, val target: BasicBlock, val stms: List<Stm>)

class BasicBlock(val id: Int, val first: AbstractInsnNode, val frame: Frame) {
	val labelName = "label_$id"
	val stms = arrayListOf<Stm>()
	val branches = arrayListOf<BasicBlockBranch>()
	var defaultStms = listOf<Stm>()
	var defaultTarget: BasicBlock? = null

	class RenderContext {
		val rendered = hashSetOf<BasicBlock>()
	}

	fun renderToStm(context: RenderContext = RenderContext()): List<Stm> {
		if (context.rendered.contains(this)) return listOf()
		context.rendered.add(this)

		val stmList = arrayListOf<Stm>()

		stmList.add(Stm.LABEL(StmLabel(labelName)))
		stmList.addAll(stms)
		for (branch in branches) {
			stmList.add(Stm.IF(branch.condition, Stm.STMS(
				branch.stms + Stm.GOTO(StmLabel(branch.target.labelName))
			)))
		}
		if (defaultTarget != null) {
			stmList.addAll(defaultTarget!!.stms)
			stmList.add(Stm.GOTO(StmLabel(defaultTarget!!.labelName)))
		}
		if (defaultTarget != null) stmList.addAll(defaultTarget!!.renderToStm(context))
		for (branch in branches) stmList.addAll(branch.target.renderToStm(context))

		return stmList
	}

	fun render(context: RenderContext = RenderContext()) {
		if (context.rendered.contains(this)) return
		context.rendered.add(this)

		println("$labelName:")
		for (stm in stms) println("$stm")
		for (branch in branches) {
			println("if (${branch.condition}) {")
			for (stm in branch.stms) println(stm)
			println("goto ${branch.target.labelName}")
			println("}")
		}
		if (defaultTarget != null) {
			for (stm in defaultTarget!!.stms) println(stm)
			println("goto ${defaultTarget?.labelName}")
		}
		defaultTarget?.render(context)
		for (branch in branches) branch.target.render(context)
	}
}

data class AnalyzeResult(val mainBlock: BasicBlock)

fun <T> Iterable<T>.toStack(): Stack<T> {
	var out = Stack<T>()
	for (it in this) out.add(it)
	return out
}

fun MethodNode.analyze(): AnalyzeResult {
	val frame = Frame()
	val explored: MutableSet<AbstractInsnNode> = linkedSetOf<AbstractInsnNode>()
	val toExplore: Queue<BasicBlock> = LinkedList<BasicBlock>()
	val basicBlocks = hashMapOf<AbstractInsnNode, BasicBlock>()
	var localIndex = 0
	var bbIndex = 0

	fun allocLocal(type: Type) = Ref.LOCAL(type, localIndex++)

	fun queue(node: AbstractInsnNode, frame: Frame): BasicBlock {
		if (!basicBlocks.containsKey(node)) {
			val newFrame = Frame(
				frame.stack.map { allocLocal(it.type) }.toCollection(arrayListOf<Ref.LOCAL>()),
				frame.locals.clone() as ArrayList<Ref>
			)
			basicBlocks[node] = BasicBlock(bbIndex++, node, newFrame)
		}
		val bb = basicBlocks[node]!!
		Frame.checkCompatible(bb.frame, frame)

		if (!explored.contains(node)) {
			explored.add(node)
			toExplore.queue(bb)
		}

		return bb
	}


	frame.locals.addAll(listOf(
		Ref.ARGUMENT(Type.INT, 0),
		Ref.ARGUMENT(Type.INT, 1),
		Ref.ARGUMENT(Type.INT, 2)
	))

	val mainBasicBlock = queue(this.instructions.first, frame)

	val indexMap = this.getInstructionList().mapIndexed { i, instruction -> Pair(i, instruction) }.associateBy { it.second }.mapValues { it.value.first }

	branchloop@while (toExplore.isNotEmpty()) {
		val bb = toExplore.dequeue()
		val frame = bb.frame
		val stack = frame.stack
		val locals = frame.locals
		val stms = bb.stms
		var node: AbstractInsnNode? = bb.first

		fun checkEmptyStack() {
			if (stack.isNotEmpty()) throw RuntimeException("Stack is not empty!, is : ${stack.size}")
		}

		//println(":: ${bb.frame}")
		basicblockloop@while (node != null) {
			//val nodeIndex = indexMap[node]
			//println("$nodeIndex: $node")
			when (node) {
				is VarInsnNode -> {
					when (node.opcode) {
						Opcodes.ILOAD, Opcodes.LLOAD, Opcodes.FLOAD, Opcodes.DLOAD, Opcodes.ALOAD -> {
							stack.push(Expr.REF(locals[node.`var`]))
						}
						Opcodes.ISTORE, Opcodes.LSTORE, Opcodes.FSTORE, Opcodes.DSTORE, Opcodes.ASTORE -> {
							//val newLocal = allocLocal(Type.INT); locals[node.`var`] = newLocal
							val newLocal = locals[node.`var`]

							stms.add(Stm.SET(newLocal, stack.pop()))
						}
						Opcodes.RET -> throw RuntimeException()
						else -> throw RuntimeException("Not implemented")
					}
				}
				is InsnNode -> {
					when (node.opcode) {
						Opcodes.IADD, Opcodes.LADD, Opcodes.FADD, Opcodes.DADD -> {
							val r = stack.pop()
							val l = stack.pop()
							stack.push(Expr.BINOP(l, "+", r))
						}
						Opcodes.IRETURN, Opcodes.LRETURN, Opcodes.FRETURN, Opcodes.DRETURN, Opcodes.ARETURN -> {
							stms.add(Stm.RETURN(stack.pop()))
							checkEmptyStack()
							break@basicblockloop
						}
						Opcodes.RETURN -> {
							stms.add(Stm.RETURN(null))
							checkEmptyStack()
							break@basicblockloop
						}
						Opcodes.ICONST_M1, Opcodes.ICONST_0, Opcodes.ICONST_1, Opcodes.ICONST_2, Opcodes.ICONST_3, Opcodes.ICONST_4, Opcodes.ICONST_5 -> {
							stack.push(Expr.LITERAL(node.opcode - Opcodes.ICONST_0))
						}
						Opcodes.ARRAYLENGTH -> {
							val array = stack.pop()
							stack.push(Expr.ARRAY_LENGTH(array))
						}
						Opcodes.IALOAD, Opcodes.LALOAD, Opcodes.FALOAD, Opcodes.DALOAD, Opcodes.AALOAD, Opcodes.BALOAD, Opcodes.CALOAD, Opcodes.SALOAD -> {
							val array = stack.pop()
							val index = stack.pop()
							stack.push(Expr.ARRAY_LOAD(array, index))
						}
						Opcodes.NOP, Opcodes.ACONST_NULL, Opcodes.LCONST_0, Opcodes.LCONST_1, Opcodes.FCONST_0, Opcodes.FCONST_1, Opcodes.FCONST_2, Opcodes.DCONST_0, Opcodes.DCONST_1, Opcodes.IASTORE, Opcodes.LASTORE, Opcodes.FASTORE, Opcodes.DASTORE, Opcodes.AASTORE, Opcodes.BASTORE, Opcodes.CASTORE, Opcodes.SASTORE, Opcodes.POP, Opcodes.POP2, Opcodes.DUP, Opcodes.DUP_X1, Opcodes.DUP_X2, Opcodes.DUP2, Opcodes.DUP2_X1, Opcodes.DUP2_X2, Opcodes.SWAP, Opcodes.ISUB, Opcodes.LSUB, Opcodes.FSUB, Opcodes.DSUB, Opcodes.IMUL, Opcodes.LMUL, Opcodes.FMUL, Opcodes.DMUL, Opcodes.IDIV, Opcodes.LDIV, Opcodes.FDIV, Opcodes.DDIV, Opcodes.IREM, Opcodes.LREM, Opcodes.FREM, Opcodes.DREM, Opcodes.INEG, Opcodes.LNEG, Opcodes.FNEG, Opcodes.DNEG, Opcodes.ISHL, Opcodes.LSHL, Opcodes.ISHR, Opcodes.LSHR, Opcodes.IUSHR, Opcodes.LUSHR, Opcodes.IAND, Opcodes.LAND, Opcodes.IOR, Opcodes.LOR, Opcodes.IXOR, Opcodes.LXOR, Opcodes.I2L, Opcodes.I2F, Opcodes.I2D, Opcodes.L2I, Opcodes.L2F, Opcodes.L2D, Opcodes.F2I, Opcodes.F2L, Opcodes.F2D, Opcodes.D2I, Opcodes.D2L, Opcodes.D2F, Opcodes.I2B, Opcodes.I2C, Opcodes.I2S, Opcodes.LCMP, Opcodes.FCMPL, Opcodes.FCMPG, Opcodes.DCMPL, Opcodes.DCMPG, Opcodes.ATHROW, Opcodes.MONITORENTER, Opcodes.MONITOREXIT -> {
							throw RuntimeException("${node.opcode}")
						}
						else -> throw RuntimeException()
					}
				}
				is JumpInsnNode -> {
					when (node.opcode) {
						Opcodes.JSR -> throw RuntimeException()
						else -> {
							val res = when (node.opcode) {
								Opcodes.GOTO -> Pair(0, "")
								Opcodes.IFEQ -> Pair(1, "==")
								Opcodes.IFNE -> Pair(1, "!=")
								Opcodes.IFLT -> Pair(1, "<")
								Opcodes.IFGE -> Pair(1, ">=")
								Opcodes.IFGT -> Pair(1, ">")
								Opcodes.IFLE -> Pair(1, "<=")
								Opcodes.IFNULL -> Pair(1, "==")
								Opcodes.IFNONNULL -> Pair(1, "!=")
								Opcodes.IF_ICMPEQ -> Pair(2, "==")
								Opcodes.IF_ICMPNE -> Pair(2, "!=")
								Opcodes.IF_ICMPLT -> Pair(2, "<")
								Opcodes.IF_ICMPGE -> Pair(2, ">=")
								Opcodes.IF_ICMPGT -> Pair(2, ">")
								Opcodes.IF_ICMPLE -> Pair(2, "<=")
								Opcodes.IF_ACMPEQ -> Pair(2, "==")
								Opcodes.IF_ACMPNE -> Pair(2, "!=")
								else -> throw RuntimeException()
							}

							val count = res.first
							val op = res.second

							val serializedStack = arrayListOf<Ref>()
							fun createSerializedStack() {
								val stack2 = stack.toList()
								stack.clear()

								for (it in stack2) {
									val local = allocLocal(it.type)
									stms.add(Stm.SET(local, it))
									serializedStack.add(local)
								}
							}

							fun mergeStack(dst: List<Ref>, src: List<Ref>): List<Stm> {
								return dst.zip(src).map { Stm.SET(it.first, Expr.REF(it.second)) }
							}

							when (count) {
								0 -> {
									val bb1 = queue(node.label, frame)
									bb.defaultTarget = bb1
									bb.defaultStms = mergeStack(bb1.frame.stackInput, serializedStack)

									createSerializedStack()
									checkEmptyStack()
									break@basicblockloop
								}
								1, 2 -> {
									val cmp = if (count == 1) {
										val l = stack.pop()
										Expr.BINOP(l, op, Expr.LITERAL(0))
									} else {
										val l = stack.pop()
										val r = stack.pop()
										Expr.BINOP(l, op, r)
									}

									createSerializedStack()
									checkEmptyStack()

									val bb1 = queue(node.label, frame)
									val bb2 = queue(node.next, frame)
									bb.branches.add(BasicBlockBranch(cmp, bb1, mergeStack(bb1.frame.stackInput, serializedStack)))
									bb.defaultTarget = bb2
									bb.defaultStms = mergeStack(bb2.frame.stackInput, serializedStack)
									break@basicblockloop
								}
								else -> throw RuntimeException()
							}
						}
					}
				}
				is IntInsnNode -> {
					when (node.opcode) {
						Opcodes.BIPUSH, Opcodes.SIPUSH -> {
							stack.push(Expr.LITERAL(node.operand))
						}
						Opcodes.NEWARRAY -> throw RuntimeException()
						else -> throw RuntimeException()
					}
				}
				is MethodInsnNode -> {
					when (node.opcode) {
						Opcodes.INVOKEVIRTUAL -> throw RuntimeException()
						Opcodes.INVOKESPECIAL -> throw RuntimeException()
						Opcodes.INVOKESTATIC -> throw RuntimeException()
						Opcodes.INVOKEINTERFACE -> throw RuntimeException()
						else -> throw RuntimeException()
					}
				}
				is IincInsnNode -> {
					val local = locals[node.`var`]
					stms.add(Stm.SET(local, Expr.BINOP(Expr.REF(local), "+", Expr.LITERAL(1))))
				}
				is LineNumberNode -> Unit
				is LabelNode -> Unit
				is FrameNode -> Unit
				else -> {
					throw RuntimeException("Not implemented Node: $node")
				}
			}
			node = node.next
			if (explored.contains(node)) break
		}
	}

	return AnalyzeResult(mainBasicBlock)
}
