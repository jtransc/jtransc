package com.jtransc.backend

import com.jtransc.ast.*
import com.jtransc.ds.Concat
import com.jtransc.ds.cast
import com.jtransc.ds.createPairs
import com.jtransc.ds.hasFlag
import com.jtransc.error.invalidOp
import com.jtransc.injector.Singleton
import com.jtransc.lang.ReflectedArray
import com.jtransc.org.objectweb.asm.ClassReader
import com.jtransc.org.objectweb.asm.Handle
import com.jtransc.org.objectweb.asm.Opcodes
import com.jtransc.org.objectweb.asm.tree.*
import java.io.IOException
import java.util.*

@Singleton
abstract class BaseAsmToAst(val types: AstTypes, val settings: AstBuildSettings) : AstClassGenerator {
	open val expandFrames = false

	override fun generateClass(program: AstProgram, fqname: FqName): AstClass {
		val cr = try {
			ClassReader(program.getClassBytes(fqname))
		} catch (e: IOException) {
			invalidOp("generateClass: Can't find class $fqname")
		}
		val classNode = ClassNode()
		cr.accept(classNode, if (expandFrames) ClassReader.EXPAND_FRAMES else ClassReader.SKIP_FRAMES)

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
			generateBody = {
				if (mods.isConcrete) {
					try {
						genBody(containingClass.ref, method, types, containingClass.source)
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

	abstract fun genBody(classRef: AstType.REF, methodNode: MethodNode, types: AstTypes, source: String): AstBody

	fun generateField(containingClass: AstClass, field: FieldNode): AstField = AstField(
		containingClass = containingClass,
		name = field.name,
		annotations = field.getAnnotations(types),
		type = types.demangle(field.desc),
		desc = field.desc,
		genericSignature = field.signature,
		modifiers = AstModifiers(field.access),
		constantValue = field.value,
		types = types
	)
}

fun AstAnnotationValue(value: Any?, visible: Boolean, types: AstTypes): Any? {
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

fun AstAnnotationBuilder(node: AnnotationNode, visible: Boolean, types: AstTypes): AstAnnotation {
	return AstAnnotationValue(node, visible, types) as AstAnnotation
}

val ANNOTATIONS_BLACKLIST = listOf(
	"java.lang.annotation.Documented", "java.lang.Deprecated",
	"java.lang.annotation.Target", "java.lang.annotation.Retention",
	"kotlin.jvm.internal.KotlinLocalClass", "kotlin.jvm.internal.KotlinSyntheticClass",
	"kotlin.jvm.internal.KotlinClass", "kotlin.jvm.internal.KotlinFunction",
	"kotlin.jvm.internal.KotlinFileFacade", "kotlin.jvm.internal.KotlinMultifileClassPart",
	"kotlin.jvm.internal.KotlinMultifileClass", "kotlin.annotation.MustBeDocumented",
	"kotlin.annotation.Target", "kotlin.annotation.Retention",
	"kotlin.jvm.JvmStatic", "kotlin.Deprecated", "kotlin.Metadata", "org.jetbrains.annotations.NotNull",
	"kotlin.internal.InlineExposed"
).map { AstType.REF(it) }.toSet()

fun List<AstAnnotation>.filterBlackList(): List<AstAnnotation> {
	return this.filter { it.type !in com.jtransc.backend.ANNOTATIONS_BLACKLIST }
}

fun Handle.ast(types: AstTypes): AstMethodRef = AstMethodRef(FqName.fromInternal(this.owner), this.name, types.demangleMethod(this.desc))

fun AnnotationNode.toAst(types: AstTypes): AstAnnotation {
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

@Suppress("UNNECESSARY_SAFE_CALL")
private fun MethodNode._getParameterAnnotations(type: AstType.METHOD, annotations: Array<List<AnnotationNode>>?, types: AstTypes, visible: Boolean): List<List<AstAnnotation>> {
	return annotations?.toList()
		?.map {
			it?.filterNotNull()?.filterIsInstance<AnnotationNode>()
				?.map { AstAnnotationBuilder(it, visible = visible, types = types) }
				?.filterBlackList()
				?: listOf()
		}
		?: (0 until type.argCount).map { listOf<AstAnnotation>() }
}

fun MethodNode.getParameterAnnotations(types: AstTypes): List<List<AstAnnotation>> {
	val type = types.demangleMethod(this.desc)
	val visible = this._getParameterAnnotations(type, this.visibleParameterAnnotations, types, visible = true)
	val invisible = this._getParameterAnnotations(type, this.invisibleParameterAnnotations, types, visible = false)
	return (0 until type.argCount).map { visible[it] + invisible[it] }
}

fun MethodNode.isStatic() = this.access hasFlag Opcodes.ACC_STATIC
fun MethodNode.isNative() = this.access hasFlag Opcodes.ACC_NATIVE
fun MethodNode.hasBody() = this.instructions.first != null
fun MethodNode.visibility() = if (this.access hasFlag Opcodes.ACC_PUBLIC) {
	AstVisibility.PUBLIC
} else if (this.access hasFlag Opcodes.ACC_PROTECTED) {
	AstVisibility.PROTECTED
} else {
	AstVisibility.PRIVATE
}

fun MethodNode.astRef(clazz: AstType.REF, types: AstTypes) = AstMethodRef(clazz.name, this.name, types.demangleMethod(this.desc))

fun AbstractInsnNode.isEndOfBasicBlock(): Boolean = when (this.opcode) {
	in Opcodes.IFEQ..Opcodes.IF_ACMPNE -> true
	else -> isEnd()
}

fun AbstractInsnNode.isEnd(): Boolean = when (this.opcode) {
	in Opcodes.TABLESWITCH..Opcodes.LOOKUPSWITCH -> true
	Opcodes.GOTO -> true
	Opcodes.ATHROW -> true
	in Opcodes.IRETURN..Opcodes.RETURN -> true
	else -> false
}

