package com.jtransc.input

import com.jtransc.ast.*
import com.jtransc.ds.cast
import com.jtransc.ds.createPairs
import com.jtransc.error.noImpl
import com.jtransc.io.readBytes
import com.jtransc.types.*
import com.jtransc.vfs.*
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode
import java.io.File
import java.io.IOException

fun AnnotationNode.toAst():AstAnnotation {
	val ref = AstType.demangle(this.desc) as AstType.REF
	return AstAnnotation(ref, this.values.createPairs().map { Pair(it.first as String, it.second as String) }.toMap(), true)
}

/*
object AsmToAst {
	fun createProgramAst(dependencies: List<String>, entryPoint: String, classPaths2: List<String>, localVfs: SyncVfsFile, refs: Set<AstRef>): AstProgram {
		return createProgramAst2(
			AstType.REF(entryPoint),
			MergedLocalAndJars(classPaths2),
			(refs + dependencies.map { AstClassRef(it) }).toSet()
		)
	}

	fun createProgramAst2(entryPoint: AstType.REF, classPaths: SyncVfsFile, references: Set<AstRef>): AstProgram {
		val resolver = VfsClassResolver(classPaths)
		val programBuilder = AstProgramBuilder(resolver)
		val clazz = programBuilder[entryPoint.name]
		return AstProgram(entryPoint.name, programBuilder.classes, classPaths)
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
		modifiers = classNode.access,
		annotations = classNode.visibleAnnotations.filterIsInstance<AnnotationNode>().map { AstAnnotationBuilder(it) },
		//methods = classNode.methods.filterIsInstance<MethodNode>().map { AstMethodBuilder(it).method }
	)
}

fun AstAnnotationBuilder(node: AnnotationNode): AstAnnotation {
	noImpl
}

object AstMethodBuilderTestExample {
	@JvmStatic fun add(a: Int, b: Int) = a + b
	@JvmStatic fun max(a: Int, b: Int) = if (a > b) a else b
	@JvmStatic fun max2(a: Int, b: Int) = (if (a > b) a * 2 else b * 3) * 4
	@JvmStatic fun max3(a: Long, b: Long) = (if (a > b) a * 2 else b * 3) * 4
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
		for (method in clazz.methods.cast<MethodNode>()) {
			println("::${method.name}")
			//val jimple = Baf2Jimple(Asm2Baf(clazz, method))
			println(dump(Asm2Baf(clazz, method).toExpr()))
			//println(jimple)
		}
		//println(Asm2Baf(clazz, method).toExpr())
		//val builder = AstMethodBuilder(node.methods[0] as MethodNode)
	}
}

class AstMethodBuilder(val clazz: AstClassBuilder, val node: MethodNode) {
	val method: AstMethod get() {

		//return AstMethod(
		//	name = node.name,
		//	annotations = listOf(),
		//	isExtraAdded = false,
		//	modifiers = node.access,
		//	body = node.toBaf().toExpr()
		//)

		noImpl
	}
}

interface ClassResolver {
	operator fun get(clazz: FqName): ByteArray
}

class VfsClassResolver(val classPaths: SyncVfsFile) : ClassResolver {
	override operator fun get(clazz: FqName): ByteArray {
		val path = clazz.internalFqname + ".class"
		try {
			return classPaths[path].readBytes()
		} catch (e: IOException) {
		}
		throw ClassNotFoundException(clazz.fqname)
	}
}

*/

