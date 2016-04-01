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

class AsmToAst : AstClassGenerator {
	override fun generateClass(program: AstProgram, fqname: FqName): AstClass {
		program.readClassToGenerate()
		val classNode = ClassNode().apply {
			ClassReader(program.getClassBytes(fqname)).accept(this, ClassReader.EXPAND_FRAMES)
		}

		val clazz = AstClass(
			program = program,
			name = FqName.fromInternal(classNode.name),
			modifiers = classNode.access,
			annotations = classNode.visibleAnnotations.filterIsInstance<AnnotationNode>().map { AstAnnotationBuilder(it) }
			//methods = classNode.methods.filterIsInstance<MethodNode>().map { AstMethodBuilder(it).method }
		)

		//for (method in classNode.methods) clazz.add(AstMethod())
		//for (field in classNode.fields) clazz.add(AstMethod())

		return clazz
	}
}

fun AstAnnotationBuilder(node: AnnotationNode): AstAnnotation {
	noImpl
}

class AstMethodBuilderTestExample {
	companion object {
		@JvmStatic fun add(a: Int, b: Int) = a + b
		@JvmStatic fun max(a: Int, b: Int) = if (a > b) a else b
		@JvmStatic fun max2(a: Int, b: Int) = (if (a > b) a * 2 else b * 3) * 4
		@JvmStatic fun max3(a: Long, b: Long) = (if (a > b) a * 2 else b * 3) * 4
		@JvmStatic fun callStatic() = add(1, 2) + add(3, 4)
		@JvmStatic fun callStatic2() {
			add(1, 2) + add(3, 4)
		}
		@JvmStatic fun sumAll(items: Array<Int>):Int {
			var sum = 0
			for (i in items) sum += i
			return sum
		}
		@JvmStatic fun sumAllPrim(items: IntArray):Int {
			var sum = 0
			for (i in items) sum += i
			return sum
		}
		@JvmStatic fun multiArray() = Array<Array<IntArray>>(0) { Array<IntArray>(0) { IntArray(0) } }
		@JvmStatic fun instantiate():Int {
			return MyClass().test() * 2;
		}
	}

	var a :Int = 10

	fun demo(b:Int) = (a + b).toLong()

	class MyClass() {
		fun test():Int {
			return 10;
		}
	}
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
			val methodType = AstType.demangleMethod(method.desc)
			println("::${method.name} :: $methodType")
			//val jimple = Baf2Jimple(Asm2Baf(clazz, method))
			println(dump(Asm2Ast(AstType.REF_INT2(clazz.name), method)))
			//println(jimple)
		}
		//println(Asm2Baf(clazz, method).toExpr())
		//val builder = AstMethodBuilder(node.methods[0] as MethodNode)
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
