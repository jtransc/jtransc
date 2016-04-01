package com.jtransc.input

import com.jtransc.ast.*
import com.jtransc.ds.cast
import com.jtransc.ds.createPairs
import com.jtransc.ds.hasFlag
import com.jtransc.error.noImpl
import com.jtransc.io.readBytes
import com.jtransc.types.*
import com.jtransc.vfs.*
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode
import soot.ClassMember
import soot.SootField
import soot.SootMethod
import soot.tagkit.AnnotationDefaultTag
import soot.tagkit.SignatureTag
import java.io.File
import java.io.IOException

fun AnnotationNode.toAst():AstAnnotation {
	val ref = AstType.demangle(this.desc) as AstType.REF
	return AstAnnotation(ref, this.values.createPairs().map { Pair(it.first as String, it.second as String) }.toMap(), true)
}

fun ClassNode.isInterface() = this.access hasFlag Opcodes.ACC_INTERFACE
fun ClassNode.isAbstract() = this.access hasFlag Opcodes.ACC_ABSTRACT
fun ClassNode.hasSuperclass() = this.superName != null
fun ClassNode.getInterfaces() = this.interfaces.cast<String>()
fun ClassNode.getMethods() = this.methods.cast<MethodNode>()
fun ClassNode.getFields() = this.fields.cast<FieldNode>()


fun ClassNode.getAnnotations() = this.visibleAnnotations?.filterNotNull()?.filterIsInstance<AnnotationNode>()?.map { AstAnnotationBuilder(it) } ?: listOf()
fun MethodNode.getAnnotations() = this.visibleAnnotations?.filterNotNull()?.filterIsInstance<AnnotationNode>()?.map { AstAnnotationBuilder(it) } ?: listOf()
fun FieldNode.getAnnotations() = this.visibleAnnotations?.filterNotNull()?.filterIsInstance<AnnotationNode>()?.map { AstAnnotationBuilder(it) } ?: listOf()

fun MethodNode.isStatic() = this.access hasFlag Opcodes.ACC_STATIC
fun MethodNode.visibility() = if (this.access hasFlag Opcodes.ACC_PUBLIC) {
	AstVisibility.PUBLIC
} else if (this.access hasFlag Opcodes.ACC_PROTECTED) {
	AstVisibility.PROTECTED
} else {
	AstVisibility.PRIVATE
}


fun MethodNode.astRef(clazz:AstClass) = AstMethodRef(clazz.name, this.name, AstType.demangleMethod(this.desc), this.isStatic())

class AsmToAst : AstClassGenerator {
	override fun generateClass(program: AstProgram, fqname: FqName): AstClass {
		program.readClassToGenerate()
		val classNode = ClassNode().apply {
			ClassReader(program.getClassBytes(fqname)).accept(this, ClassReader.EXPAND_FRAMES)
		}

		val astClass = AstClass(
			program = program,
			name = FqName.fromInternal(classNode.name),
			modifiers = AstModifiers(classNode.access),
			annotations = classNode.getAnnotations(),
			extending = if (classNode.hasSuperclass() && !classNode.isInterface()) FqName(classNode.superName) else null,
			implementing = classNode.getInterfaces().map { FqName(it) }
		)
		program.add(astClass)

		for (method in classNode.getMethods().map { generateMethod(astClass, it) }) {
			astClass.add(method)
		}

		for (field in classNode.getFields().map { generateField(astClass, it) }) {
			astClass.add(field)
		}

		return astClass
	}

	fun generateMethod(containingClass: AstClass, method: MethodNode): AstMethod {
		val mods = AstModifiers(method.access)
		val methodRef = method.astRef(containingClass)
		return AstMethod(
			containingClass = containingClass,
			annotations = method.getAnnotations(),
			name = method.name,
			type = methodRef.type,
			signature = methodRef.type.mangle(),
			genericSignature = method.signature,
			defaultTag = method.annotationDefault,
			modifiers = mods,
			generateBody = { if (mods.isConcrete) Asm2Ast(containingClass.ref.type, method) else null }
		)
	}

	fun generateField(containingClass: AstClass, field: FieldNode): AstField {
		val mods = AstModifiers(field.access)
		return AstField(
			containingClass = containingClass,
			name = field.name,
			annotations = field.getAnnotations(),
			type = AstType.demangle(field.desc),
			descriptor = field.desc,
			genericSignature = field.signature,
			modifiers = mods,
			constantValue = field.value
		)
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
