package com.jtransc.input

import com.jtransc.ast.*
import com.jtransc.ds.cast
import com.jtransc.ds.createPairs
import com.jtransc.ds.hasFlag
import com.jtransc.types.Asm2Ast
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode
import java.util.*

fun AnnotationNode.toAst(): AstAnnotation {
	val ref = AstType.demangle(this.desc) as AstType.REF
	return AstAnnotation(ref, this.values.createPairs().map { Pair(it.first as String, it.second as String) }.toMap(), true)
}

fun ClassNode.isInterface() = this.access hasFlag Opcodes.ACC_INTERFACE
fun ClassNode.isAbstract() = this.access hasFlag Opcodes.ACC_ABSTRACT
fun ClassNode.hasSuperclass() = this.superName != null
fun ClassNode.getInterfaces() = this.interfaces.cast<String>()
fun ClassNode.getMethods() = this.methods.cast<MethodNode>()
fun ClassNode.getFields() = this.fields.cast<FieldNode>()

fun <T> Concat(vararg list: List<T>?): List<T> {
	var out = listOf<T>()
	for (l in list) if (l != null) out += l
	return out
}

fun ClassNode.getAnnotations() = Concat(this.visibleAnnotations, this.invisibleAnnotations).filterNotNull().filterIsInstance<AnnotationNode>().map { AstAnnotationBuilder(it) }
fun MethodNode.getAnnotations() = Concat(this.visibleAnnotations, this.invisibleAnnotations).filterNotNull().filterIsInstance<AnnotationNode>().map { AstAnnotationBuilder(it) }
fun FieldNode.getAnnotations() = Concat(this.visibleAnnotations, this.invisibleAnnotations).filterNotNull().filterIsInstance<AnnotationNode>().map { AstAnnotationBuilder(it) }

fun MethodNode.isStatic() = this.access hasFlag Opcodes.ACC_STATIC
fun MethodNode.visibility() = if (this.access hasFlag Opcodes.ACC_PUBLIC) {
	AstVisibility.PUBLIC
} else if (this.access hasFlag Opcodes.ACC_PROTECTED) {
	AstVisibility.PROTECTED
} else {
	AstVisibility.PRIVATE
}


fun MethodNode.astRef(clazz: AstClass) = AstMethodRef(clazz.name, this.name, AstType.demangleMethod(this.desc))

class AsmToAst : AstClassGenerator {
	override fun generateClass(program: AstProgram, fqname: FqName): AstClass {
		val classNode = ClassNode().apply {
			ClassReader(program.getClassBytes(fqname)).accept(this, ClassReader.EXPAND_FRAMES)
		}

		val astClass = AstClass(
			program = program,
			name = FqName.fromInternal(classNode.name),
			modifiers = AstModifiers(classNode.access),
			annotations = classNode.getAnnotations(),
			extending = if (classNode.hasSuperclass() && !classNode.isInterface()) FqName.fromInternal(classNode.superName) else null,
			implementing = classNode.getInterfaces().map { FqName.fromInternal(it) }
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
			generateBody = { if (mods.isConcrete) Asm2Ast(containingClass.ref.type, method, containingClass.program) else null }
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

fun AstAnnotationValue(value: Any?): Any? {
	if (value == null) return null
	val clazz = value.javaClass
	if (clazz.isArray && clazz.componentType == java.lang.String::class.java) {
		val array = value as Array<String>
		return AstFieldWithoutTypeRef((AstType.demangle(array[0]) as AstType.REF).name, array[1])
	}
	if (value is ArrayList<*>) {
		return value.map { AstAnnotationValue(it) }
	}
	return value
}

fun AstAnnotationBuilder(node: AnnotationNode): AstAnnotation {
	val type = AstType.demangle(node.desc) as AstType.REF
	val fields = hashMapOf<String, Any?>()
	if (node.values != null) {
		val values = node.values
		var n = 0
		while (n < values.size) {
			val name = values[n++] as String
			val value = values[n++]
			fields[name] = AstAnnotationValue(value)
		}
		//println(node.values)
		//println(node.values)
	}
	return AstAnnotation(type, fields, true)
}

