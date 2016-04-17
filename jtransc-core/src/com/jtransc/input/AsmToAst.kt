package com.jtransc.input

import com.jtransc.ast.*
import com.jtransc.ds.cast
import com.jtransc.ds.createPairs
import com.jtransc.ds.hasFlag
import com.jtransc.error.noImpl
import com.jtransc.lang.ReflectedArray
import com.jtransc.types.Asm2Ast
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
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

fun ClassNode.getAnnotations() = Concat(this.visibleAnnotations, this.invisibleAnnotations).filterNotNull().filterIsInstance<AnnotationNode>().map { AstAnnotationBuilder(it) }.filterBlackList()
fun MethodNode.getAnnotations() = Concat(this.visibleAnnotations, this.invisibleAnnotations).filterNotNull().filterIsInstance<AnnotationNode>().map { AstAnnotationBuilder(it) }.filterBlackList()
fun FieldNode.getAnnotations() = Concat(this.visibleAnnotations, this.invisibleAnnotations).filterNotNull().filterIsInstance<AnnotationNode>().map { AstAnnotationBuilder(it) }.filterBlackList()

fun MethodNode.getParameterAnnotations(): List<List<AstAnnotation>> {
	val type = AstType.demangleMethod(this.desc)

	return this.visibleParameterAnnotations?.toList()?.map { it?.filterNotNull()?.filterIsInstance<AnnotationNode>()?.map { AstAnnotationBuilder(it) }?.filterBlackList() ?: listOf() }
		?: (0 until type.argCount).map { listOf<AstAnnotation>() }
}

fun MethodNode.isStatic() = this.access hasFlag Opcodes.ACC_STATIC
fun MethodNode.visibility() = if (this.access hasFlag Opcodes.ACC_PUBLIC) {
	AstVisibility.PUBLIC
} else if (this.access hasFlag Opcodes.ACC_PROTECTED) {
	AstVisibility.PROTECTED
} else {
	AstVisibility.PRIVATE
}

fun MethodNode.astRef(clazz: AstType.REF) = AstMethodRef(clazz.name, this.name, AstType.demangleMethod(this.desc))

class AsmToAst : AstClassGenerator {
	override fun generateClass(program: AstProgram, fqname: FqName): AstClass {
		val cr = ClassReader(program.getClassBytes(fqname))
		val classNode = ClassNode()
		cr.accept(classNode, ClassReader.SKIP_FRAMES)

		//val cw = ClassWriter(cr, ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES);
		//classNode.accept(cw);

		val astClass = AstClass(
			program = program,
			name = FqName.fromInternal(classNode.name),
			modifiers = AstModifiers(classNode.access),
			annotations = classNode.getAnnotations(),
			extending = if (classNode.hasSuperclass() && !classNode.isInterface()) FqName.fromInternal(classNode.superName) else null,
			implementing = classNode.getInterfaces().map { FqName.fromInternal(it) }
		)
		program.add(astClass)

		classNode.getMethods().forEach { astClass.add(generateMethod(astClass, it)) }
		classNode.getFields().forEach { astClass.add(generateField(astClass, it)) }

		return astClass
	}

	fun generateMethod(containingClass: AstClass, method: MethodNode): AstMethod {
		val mods = AstModifiers(method.access)
		val methodRef = method.astRef(containingClass.ref)
		return AstMethod(
			containingClass = containingClass,
			annotations = method.getAnnotations(),
			parameterAnnotations = method.getParameterAnnotations(),
			name = method.name,
			type = methodRef.type,
			signature = methodRef.type.mangle(),
			genericSignature = method.signature,
			defaultTag = AstAnnotationValue(method.annotationDefault),
			modifiers = mods,
			generateBody = {
				if (mods.isConcrete) {
					try {
						Asm2Ast(containingClass.ref, method)
					} catch (e: Throwable) {
						println("Error trying to generate ${containingClass.name}::${method.name} ${method.desc}")
						e.printStackTrace()
						null
					}
				} else {
					null
				}
			}
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
	if (clazz.isArray) {
		return ReflectedArray(value).toList().map { AstAnnotationValue(it) }
	}
	if (value is AnnotationNode) {
		val type = AstType.demangle(value.desc) as AstType.REF
		val fields = hashMapOf<String, Any?>()
		if (value.values != null) {
			val values = value.values
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
	return value
}

fun AstAnnotationBuilder(node: AnnotationNode): AstAnnotation {
	return AstAnnotationValue(node) as AstAnnotation
}

