package com.jtransc.input

import com.jtransc.ast.*
import com.jtransc.ds.Concat
import com.jtransc.ds.cast
import com.jtransc.ds.createPairs
import com.jtransc.ds.hasFlag
import com.jtransc.error.invalidOp
import com.jtransc.injector.Singleton
import com.jtransc.lang.ReflectedArray
import com.jtransc.org.objectweb.asm.ClassReader
import com.jtransc.org.objectweb.asm.Opcodes
import com.jtransc.org.objectweb.asm.tree.AnnotationNode
import com.jtransc.org.objectweb.asm.tree.ClassNode
import com.jtransc.org.objectweb.asm.tree.FieldNode
import com.jtransc.org.objectweb.asm.tree.MethodNode
import com.jtransc.types.Asm2Ast
import java.io.IOException
import java.util.*

fun AnnotationNode.toAst(types:AstTypes): AstAnnotation {
	val ref = types.demangle(this.desc) as AstType.REF
	return AstAnnotation(ref, this.values.createPairs().map { Pair(it.first as String, it.second as String) }.toMap(), true)
}

fun ClassNode.isInterface() = this.access hasFlag Opcodes.ACC_INTERFACE
fun ClassNode.isAbstract() = this.access hasFlag Opcodes.ACC_ABSTRACT
fun ClassNode.hasSuperclass() = this.superName != null
fun ClassNode.getInterfaces() = this.interfaces.cast<String>()
fun ClassNode.getMethods() = this.methods.cast<MethodNode>()
fun ClassNode.getFields() = this.fields.cast<FieldNode>()

private fun getAnnotations(visibleAnnotations: List<AnnotationNode>?, invisibleAnnotations: List<AnnotationNode>?, types: AstTypes): List<AstAnnotation> {
	val visible = Concat(visibleAnnotations).filterNotNull().filterIsInstance<AnnotationNode>().map { AstAnnotationBuilder(it, visible = true, types = types) }.filterBlackList()
	val invisible = Concat(invisibleAnnotations).filterNotNull().filterIsInstance<AnnotationNode>().map { AstAnnotationBuilder(it, visible = false, types = types) }.filterBlackList()
	return visible + invisible
}

fun ClassNode.getAnnotations(types: AstTypes) = getAnnotations(this.visibleAnnotations, this.invisibleAnnotations, types)
fun MethodNode.getAnnotations(types: AstTypes) = getAnnotations(this.visibleAnnotations, this.invisibleAnnotations, types)
fun FieldNode.getAnnotations(types: AstTypes) = getAnnotations(this.visibleAnnotations, this.invisibleAnnotations, types)

fun MethodNode.getParameterAnnotations(types:AstTypes): List<List<AstAnnotation>> {
	val type = types.demangleMethod(this.desc)

	return this.visibleParameterAnnotations?.toList()
		?.map {
			it?.filterNotNull()?.filterIsInstance<AnnotationNode>()
				?.map { AstAnnotationBuilder(it, visible = true, types = types) }
				?.filterBlackList()
				?: listOf()
		}
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

fun MethodNode.astRef(clazz: AstType.REF, types:AstTypes) = AstMethodRef(clazz.name, this.name, types.demangleMethod(this.desc))

@Singleton
class AsmToAst(val types: AstTypes) : AstClassGenerator {
	override fun generateClass(program: AstProgram, fqname: FqName): AstClass {
		val cr = try {
			ClassReader(program.getClassBytes(fqname))
		} catch (e: IOException) {
			invalidOp("Can't find class $fqname")
		}
		val classNode = ClassNode()
		cr.accept(classNode, ClassReader.SKIP_FRAMES)

		// SourceFile

		//val cw = ClassWriter(cr, ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES);
		//classNode.accept(cw);

		val astClass = AstClass(
			source = classNode.sourceDebug ?: "${classNode.name}.java",
			program = program,
			name = FqName.fromInternal(classNode.name),
			modifiers = AstModifiers(classNode.access),
			annotations = classNode.getAnnotations(types),
			extending = if (classNode.hasSuperclass() && !classNode.isInterface()) FqName.fromInternal(classNode.superName) else null,
			implementing = classNode.getInterfaces().map { FqName.fromInternal(it) }
		)
		program.add(astClass)


		classNode.getMethods().withIndex().forEach { astClass.add(generateMethod(astClass, it.value)) }
		classNode.getFields().withIndex().forEach { astClass.add(generateField(astClass, it.value)) }

		return astClass
	}

	fun generateMethod(containingClass: AstClass, method: MethodNode): AstMethod {
		val mods = AstModifiers(method.access)
		val methodRef = method.astRef(containingClass.ref, types)
		return AstMethod(
			containingClass = containingClass,
			annotations = method.getAnnotations(types),
			parameterAnnotations = method.getParameterAnnotations(types),
			name = method.name,
			methodType = methodRef.type,
			signature = methodRef.type.mangle(),
			genericSignature = method.signature,
			defaultTag = AstAnnotationValue(method.annotationDefault, visible = true, types = types),
			modifiers = mods,
			types = types,
			generateBody = {
				if (mods.isConcrete) {
					try {
						Asm2Ast(containingClass.ref, method, types, containingClass.source)
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
			annotations = field.getAnnotations(types),
			type = types.demangle(field.desc),
			desc = field.desc,
			genericSignature = field.signature,
			modifiers = mods,
			constantValue = field.value,
			types = types
		)
	}
}

fun AstAnnotationValue(value: Any?, visible:Boolean, types: AstTypes): Any? {
	if (value == null) return null
	val clazz = value.javaClass
	if (clazz.isArray && clazz.componentType == java.lang.String::class.java) {
		val array = value as Array<String>
		return AstFieldWithoutTypeRef((types.demangle(array[0]) as AstType.REF).name, array[1])
	}
	if (value is ArrayList<*>) {
		return value.map { AstAnnotationValue(it, visible, types) }
	}
	if (clazz.isArray) {
		return ReflectedArray(value).toList().map { AstAnnotationValue(it, visible, types) }
	}
	if (value is AnnotationNode) {
		val type = types.demangle(value.desc) as AstType.REF
		val fields = hashMapOf<String, Any?>()
		if (value.values != null) {
			val values = value.values
			var n = 0
			while (n < values.size) {
				val name = values[n++] as String
				val value = values[n++]
				fields[name] = AstAnnotationValue(value, visible, types)
			}
			//println(node.values)
			//println(node.values)
		}
		return AstAnnotation(type, fields, visible)
	}
	return value
}

fun AstAnnotationBuilder(node: AnnotationNode, visible:Boolean, types: AstTypes): AstAnnotation {
	return AstAnnotationValue(node, visible, types) as AstAnnotation
}

