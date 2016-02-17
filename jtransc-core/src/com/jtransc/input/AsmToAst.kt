package com.jtransc.input

import com.jtransc.ast.*
import com.jtransc.error.invalidOp
import com.jtransc.error.noImpl
import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.SyncVfsFile
import com.jtransc.vfs.ZipVfs
import org.objectweb.asm.*
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode
import java.io.IOException
import java.util.*

object AsmToAst {
	fun createProgramAst(dependencies: List<String>, entryPoint: String, classPaths2: List<String>, localVfs: SyncVfsFile, refs: Set<AstRef>): AstProgram {
		return createProgramAst2(
			AstType.REF(entryPoint),
			VfsClassResolver(classPaths2.map { if (it.endsWith(".jar")) ZipVfs(it) else LocalVfs(it) }),
			(refs + dependencies.map { AstClassRef(it) }).toSet()
		)
	}

	fun createProgramAst2(entryPoint: AstType.REF, resolver: ClassResolver, references: Set<AstRef>): AstProgram {
		val programBuilder = AstProgramBuilder(resolver)
		val clazz = programBuilder[entryPoint.name]
		return AstProgram(entryPoint.name, programBuilder.classes)
	}
}

class AstProgramBuilder(private val resolver: ClassResolver) {
	private val classesMap = hashMapOf<FqName, AstClassBuilder>()

	operator fun get(clazz: FqName): AstClassBuilder {
		if (clazz !in classesMap) {
			classesMap[clazz] = AstClassBuilder(this, resolver.get(clazz))
		}
		return classesMap[clazz]!!
	}

	val classes: List<AstClass> get() = classesMap.values.map { it.clazz }
}

class AstClassBuilder(val program: AstProgramBuilder, val bytes: ByteArray) {
	val classNode = ClassNode().apply {
		ClassReader(bytes).accept(this, ClassReader.EXPAND_FRAMES)
	}

	val clazz = AstClass(
		name = FqName(classNode.name),
		implCode = null,
		modifiers = classNode.access,
		annotations = classNode.visibleAnnotations.filterIsInstance<AnnotationNode>().map { AstAnnotationBuilder(it) },
		methods = classNode.methods.filterIsInstance<MethodNode>().map { AstMethodBuilder(it).method }
	)
}

fun AstAnnotationBuilder(node: AnnotationNode): AstAnnotation {
	noImpl
}

class AstMethodBuilder(val node: MethodNode) {
	val method:AstMethod = noImpl
	private val stack = Stack<AstExpr>()
	val visitor = AstInstructionMethodVisitor(object : AstInstructionMethodVisitor.Processor() {
		val stack = Stack<AstExpr>()
		val stms = ArrayList<AstStm>()

		override fun const(value: Any?) {
			stack.push(AstExpr.LITERAL(value))
		}

		override fun binop(type: AstType, operator: AstBinop) {
			val l = stack.pop()
			val r = stack.pop()
			if (l.type != type) invalidOp
			if (r.type != type) invalidOp
			stack.push(AstExpr.BINOP(type, l, operator, r))
		}

		override fun unop(type: AstType, operator: AstUnop) {
			stack.push(AstExpr.UNOP(operator, stack.pop()))
		}

		override fun conv(src: AstType, dst: AstType) {
			stack.push(AstExpr.CAST(src, dst, stack.pop()))
		}

		override fun ret(type: AstType) {
			val op = stack.pop()
			emptystack()
			stms.add(AstStm.RETURN(op))
		}

		private fun emptystack() {
			if (stack.isNotEmpty()) invalidOp
		}

		override fun retvoid() {
			emptystack()
			stms.add(AstStm.RETURN(null))
		}

		override fun arraylength() {
			stack.push(AstExpr.ARRAY_LENGTH(stack.pop()))
		}

		override fun athrow() {
			val element = stack.pop()
			emptystack()
			stms.add(AstStm.THROW(element))
		}

		override fun monitor(enter: Boolean) {
			val element = stack.pop()
			emptystack()
			stms.add(if (enter) AstStm.MONITOR_ENTER(element) else AstStm.MONITOR_EXIT(element))
			//super.monitor(enter)
		}

		override fun invoke(type: InvokeType, owner: String?, name: String?, desc: String?, itf: Boolean) {
			//super.invoke(type, owner, name, desc, itf)
			noImpl
		}

		override fun switch(dflt: Label?, pairs: List<Pair<Int, Label?>>) {
			//stms.add(AstStm.SWITCH())
			noImpl
		}

		override fun tryCatchBlock(start: Label?, end: Label?, handler: Label?, type: String?) {
			//super.tryCatchBlock(start, end, handler, type)
			noImpl
		}

		override fun code() {
			//super.code()
			noImpl
		}

		override fun getlocal(type: AstType, index: Int) {
			noImpl
		}

		override fun putlocal(type: AstType, index: Int) {
			noImpl
		}

		override fun frame(type: Int, nLocal: Int, local: Array<Any>?, nStack: Int, stack: Array<Any>?) {
			noImpl
		}

		override fun multianewarray(desc: String?, dims: Int) {
			noImpl
		}

		override fun newarray(type: AstType.Primitive) {
			noImpl
		}

		override fun anewarray(type: String?) {
			noImpl
		}

		override fun anew(type: String?) {
			noImpl
		}

		override fun acheckcast(type: String?) {
			noImpl
		}

		override fun ainstanceof(type: String?) {
			noImpl
		}

		override fun maxs(maxStack: Int, maxLocal: Int) {
			noImpl
		}

		override fun label(label: Label?) {
			noImpl
		}

		override fun arrayget(type: AstType) {
			noImpl
		}

		override fun arrayset(type: AstType) {
			noImpl
		}

		override fun pop() {
			noImpl
		}

		override fun pop2() {
			noImpl
		}

		override fun dup() {
			noImpl
		}

		override fun dupx1() {
			noImpl
		}

		override fun dupx2() {
			noImpl
		}

		override fun dup2() {
			noImpl
		}

		override fun dup2_x1() {
			noImpl
		}

		override fun dup2_x2() {
			noImpl
		}

		override fun swap() {
			noImpl
		}

		override fun iinc(index: Int, increment: Int) {
			noImpl
		}

		override fun line(line: Int, start: Label?) {
			noImpl
		}

		override fun gotoif0(eq: AstBinop, label: Label?) {
			noImpl
		}

		override fun gotoif_i(eq: AstBinop, label: Label?) {
			noImpl
		}

		override fun gotoif_a(eq: AstBinop, label: Label?) {
			noImpl
		}

		override fun goto(label: Label?) {
			noImpl
		}

		override fun gotoifnull(eq: AstBinop, label: Label?) {
			noImpl
		}

		override fun end() {
			noImpl
		}

		override fun getfield(static: Boolean, owner: String?, name: String?, desc: String?) {
			noImpl
		}

		override fun putfield(static: Boolean, owner: String?, name: String?, desc: String?) {
			noImpl
		}

		override fun localVariable(name: String?, desc: String?, signature: String?, start: Label?, end: Label?, index: Int) {
			noImpl
		}

		override fun attr(attr: Attribute?) {
			noImpl
		}

		override fun param(name: String?, access: Int) {
			noImpl
		}
	})

	init {
		var node = node.instructions.first
		while (node != null) {
			node = node.next
		}
	}
}




interface ClassResolver {
	operator fun get(clazz: FqName): ByteArray
}

class VfsClassResolver(val classPaths: List<SyncVfsFile>) : ClassResolver {
	override operator fun get(clazz: FqName): ByteArray {
		val path = clazz.internalFqname + ".class"
		for (classPath in classPaths) {
			try {
				return classPath[path].readBytes()
			} catch (e: IOException) {
			}
		}
		throw ClassNotFoundException(clazz.fqname)
	}
}