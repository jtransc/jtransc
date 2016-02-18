package com.jtransc.input

import com.jtransc.ast.*
import com.jtransc.error.noImpl
import com.jtransc.input.asm.getMethods
import com.jtransc.io.readBytes
import com.jtransc.types.Asm2Baf
import com.jtransc.types.dump
import com.jtransc.types.toExpr
import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.SyncVfsFile
import com.jtransc.vfs.ZipVfs
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode
import java.io.IOException

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
		//methods = classNode.methods.filterIsInstance<MethodNode>().map { AstMethodBuilder(it).method }
		methods = noImpl
	)
}

fun AstAnnotationBuilder(node: AnnotationNode): AstAnnotation {
	noImpl
}

object AstMethodBuilderTestExample {
	@JvmStatic fun add(a: Int, b: Int) = a + b
	@JvmStatic fun max(a: Int, b: Int) = if (a > b) a else b
	//@JvmStatic fun test() = Array<Array<IntArray>>(0) { Array<IntArray>(0) { IntArray(0) } }
}

fun <T> Class<T>.readClassNode(): ClassNode {
	val bytes = this.readBytes()
	return ClassNode().apply {
		ClassReader(bytes).accept(this, ClassReader.EXPAND_FRAMES)
	}
}

object AstMethodBuilderTest {
	@JvmStatic fun main(args: Array<String>) {
		val clazz = AstMethodBuilderTestExample::class.java.readClassNode()
		for (method in clazz.getMethods()) {
			println("::${method.name}")
			println(dump(Asm2Baf(clazz, method).toExpr()))
		}
		//println(Asm2Baf(clazz, method).toExpr())
		//val builder = AstMethodBuilder(node.methods[0] as MethodNode)
	}
}

class AstMethodBuilder(val clazz: AstClassBuilder, val node: MethodNode) {
	val method: AstMethod get() {
		/*
		return AstMethod(
			name = node.name,
			annotations = listOf(),
			isExtraAdded = false,
			modifiers = node.access,
			body = node.toBaf().toExpr()
		)
		*/
		noImpl
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