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

package com.jtransc.input.soot

import com.jtransc.ast.*
import com.jtransc.error.InvalidOperationException
import com.jtransc.time.measureTime
import com.jtransc.vfs.SyncVfsFile
import jtransc.annotation.*
import soot.*
import soot.tagkit.*
import java.nio.charset.Charset
import java.util.*

class SootToAst {
	//registerAbstract("java.lang.Object", "Object", "ObjectTools")
	//registerAbstract("java.lang.String", "String", "StringTools")

	companion object {
		fun createProgramAst(classNames: List<String>, mainClass: String, classPaths: List<String>, outputPath: SyncVfsFile, deps2: Set<AstRef>? = null): AstProgram {
			SootUtils.init(classPaths)
			return SootToAst().generateProgram(BaseProjectContext(classNames, mainClass, classPaths, outputPath, deps2?.toHashSet()))
		}
	}

	protected val cl = this.javaClass.classLoader
	protected val utf8 = Charset.forName("UTF-8")
	protected val file_separator = System.getProperty("file.separator")
	protected val tree = TargetTree()


	protected fun classNameToPath(name: String): String = name.replace(".", "/")

	fun generateProgram(projectContext: BaseProjectContext): AstProgram {

		// Load classes into stage
		projectContext.classNames.forEach {
			Scene.v().loadClassAndSupport(it)
		}

		// Preprocesses classes
		projectContext.classNames.forEach { tree.getTargetClass(it) }

		print("Processing classes...")
		val context = SootContext(projectContext)

		projectContext.classNames.forEach { context.addClassToGenerate(it) }

		val (elapsed) = measureTime {
			while (context.hasClassToGenerate()) {
				val className = context.getClassToGenerate()
				val clazz = tree.getTargetClass(className)
				//val nativeClassTag = clazz.clazz.getTag("libcore.NativeClass", "")

				//print("Processing class: " + clazz.clazz.name + "...")

				val generatedClass = generateClass(clazz.clazz, context)
				context.addGeneratedClass(generatedClass)

				// Add dependencies for annotations
				// @TODO: Do this better!
				// @TODO: This should be recursive. But anyway it shouldn't be there!
				// @TODO: Analyzer should detect annotations and reference these ones
				generatedClass.classAndFieldAndMethodAnnotations.forEach {
					context.addClassToGenerate(it.type.name.fqname)
					val clazz2 = context.getClass(it.type.name)
					context.projectContext.addDep(clazz2.clazz.astRef)
					for (m in clazz2.clazz.methods) {
						context.projectContext.addDep(m.astRef)
						context.projectContext.addDep(m.returnType.astType.getRefClasses())
						for (clazz in m.returnType.astType.getRefClasses()) {
							context.addClassToGenerate(clazz.fqname)
						}
					}
				}
				//println("Ok($elapsed):$nativeClassTag")
			}
		}
		println("Ok classes=${projectContext.classNames.size}, time=$elapsed")

		return AstProgram(
			entrypoint = FqName(projectContext.mainClass),
			classes = context.generatedClassesList
		)
	}

	class SootContext(val projectContext: BaseProjectContext) {
		private val classesToGenerateSet = hashSetOf<String>()
		private val classesToGenerateOnce: Queue<String> = LinkedList()
		val generatedClassesList = arrayListOf<AstClass>()

		fun hasClassToGenerate() = classesToGenerateOnce.isNotEmpty()

		fun getClassToGenerate() = classesToGenerateOnce.remove()

		fun addClassToGenerate(className: String) {
			if (classesToGenerateSet.contains(className)) return
			classesToGenerateSet.add(className)
			classesToGenerateOnce.add(className)
		}

		fun addGeneratedClass(clazz: AstClass) {
			generatedClassesList.add(clazz)
		}

		class SootClassContext(val context: SootContext, val clazz: SootClass) {
			val clazzName = FqName(clazz.name)
			val parent = if (clazz.hasSuperclass()) clazz.superclass else null
			val parentContext = if (parent != null) context[parent] else null
			val clazzAbstract = clazz.isClassAbstract
			val allImplementedMethods by lazy { clazz.getAllImplementedMethods() }
			val allMethodsToImplement by lazy { clazz.getAllMethodsToImplement() }
			val missingMethods by lazy {
				val list = allMethodsToImplement.map { it.withoutClass } - allImplementedMethods.map { it.withoutClass }
				list.map { it.withClass(clazzName).toEmptyMethod(isOverriding = false) }
			}
			val ancestorsWithThis = clazz.getAncestors(includeThis = true)
			val ancestorsWithoutThis = clazz.getAncestors(includeThis = false)
			val abstractAncestorsWithThis by lazy { ancestorsWithThis.filter { it.isClassAbstract } }
			val abstractAncestorsWithoutThis by lazy { ancestorsWithoutThis.filter { it.isClassAbstract } }
			val allMissingMethods by lazy { abstractAncestorsWithoutThis.flatMap { context[it].missingMethods } }
		}

		class SootMethodContext(val context: SootContext, val method: SootMethod) {
			val clazz = method.declaringClass
			val clazzContext = context[clazz]
			val methodName = method.name
			val overridingMethod by lazy {
				var override = if (clazz.isInterface) {
					null
				} else {
					method.overridingMethod
				}
				if (override == null) {
					val allMissingMethods = clazzContext.allMissingMethods
					if (allMissingMethods.isNotEmpty()) {
						override = allMissingMethods.firstOrNull { it.ref.withoutClass == method.astRef.withoutClass }?.ref
					}
				}
				override
			}
		}

		public val classes = hashMapOf<String, SootClassContext>()
		public val methods = hashMapOf<SootMethod, SootMethodContext>()

		operator fun get(clazz: SootClass): SootClassContext {
			val clazzName = clazz.name
			if (clazzName !in classes) classes[clazzName] = SootClassContext(this, clazz)
			return classes[clazzName]!!
		}

		operator fun get(method: SootMethod): SootMethodContext {
			if (method !in methods) methods[method] = SootMethodContext(this, method)
			return methods[method]!!
		}

		fun mustInclude(method: SootMethod): Boolean {
			//if (method.declaringClass.isInterface) return true
			//if (method.isMethodOverriding && method.name != "<init>") return true
			//if (method.name == "toString") return true
			return projectContext.mustInclude(method.astRef)
			//return true
		}

		fun mustInclude(method: SootField): Boolean {
			return true
		}

		fun getClass(name: FqName): SootClassContext {
			//return this[Scene.v().getSootClass(name.fqname)]
			return this[Scene.v().loadClassAndSupport(name.fqname)]
		}
	}

	fun generateMethod(method: SootMethod, context: SootContext): AstMethod {
		val methodRef = method.astRef
		val methodContext = context[method]
		val body = if (method.isConcrete) AstMethodProcessor.processBody(method, context) else null

		return AstMethod(
			containingClass = FqName(method.declaringClass.name),
			annotations = readAnnotations(context, method.tags),
			name = methodRef.name,
			type = methodRef.type,
			body = body,
			signature = method.astType.mangle(),
			genericSignature = method.tags.filterIsInstance<SignatureTag>().firstOrNull()?.signature,
			defaultTag = method.tags.filterIsInstance<AnnotationDefaultTag>().firstOrNull()?.toAstAnnotation(),
			modifiers = method.modifiers,
			isExtraAdded = false,
			isStatic = method.isStatic,
			visibility = method.astVisibility,
			overridingMethod = methodContext.overridingMethod,
			isImplementing = method.isMethodImplementing,
			isNative = method.isNative,
			getterField = method.getAnnotation(JTranscGetter::class.java.name, "value") as String?,
			setterField = method.getAnnotation(JTranscSetter::class.java.name, "value") as String?,
			nativeMethod = method.getAnnotation(JTranscMethod::class.java.name, "value") as String?,
			isInline = method.hasAnnotation(JTranscInline::class.java.name)
		)
	}

	fun AnnotationElem.unboxAnnotationElement(): Pair<String, Any?> {
		val it = this
		return it.name to when (it) {
			is AnnotationBooleanElem -> it.value
			is AnnotationClassElem -> throw NotImplementedError("Unhandled annotation element type $it")
			is AnnotationAnnotationElem -> throw NotImplementedError("Unhandled annotation element type $it")
			is AnnotationEnumElem -> {
				val type = AstType.demangle(it.typeName) as AstType.REF
				AstFieldRef(type.name, it.constantName, type)
			}
			is AnnotationArrayElem -> it.values.map { it.unboxAnnotationElement() }
			is AnnotationFloatElem -> it.value
			is AnnotationDoubleElem -> it.value
			is AnnotationIntElem -> it.value
			is AnnotationLongElem -> it.value
			is AnnotationStringElem -> it.value
			else -> throw NotImplementedError("Unhandled annotation element type $it")
		}
	}

	fun AnnotationDefaultTag.toAstAnnotation() = this.defaultVal.unboxAnnotationElement()

	fun readAnnotations(context: SootContext, tags: List<soot.tagkit.Tag>): List<AstAnnotation> {
		val runtimeAnnotations: List<AnnotationTag> = tags.filterIsInstance<VisibilityAnnotationTag>().filter { it.visibility == 0 }.flatMap { it.annotations }
		//val decAnnotations = MyTest::class.java.declaredAnnotations


		return runtimeAnnotations.flatMap { annotation ->
			// @TODO: Move this list outside!
			val blacklist = setOf(
				"Ljava/lang/annotation/Documented;",
				"Ljava/lang/Deprecated;",
				"Ljava/lang/annotation/Target;",
				"Ljava/lang/annotation/Retention;",
				"Lkotlin/jvm/internal/KotlinLocalClass;",
				"Lkotlin/jvm/internal/KotlinSyntheticClass;",
				"Lkotlin/jvm/internal/KotlinClass;",
				"Lkotlin/jvm/internal/KotlinFunction;",
				"Lkotlin/jvm/internal/KotlinFileFacade;",
				"Lkotlin/jvm/internal/KotlinMultifileClassPart;",
				"Lkotlin/jvm/internal/KotlinMultifileClass;",
				"Lkotlin/annotation/MustBeDocumented;",
				"Lkotlin/annotation/Target;",
				"Lkotlin/annotation/Retention;",
				"Lkotlin/jvm/JvmStatic;",
				"Lkotlin/Deprecated;"
			)
			if (annotation.type !in blacklist) {
				val elems = (0 until annotation.numElems).map { annotation.getElemAt(it) }
				try {
					listOf(AstAnnotation(AstType.demangle(annotation.type) as AstType.REF, elems.map { it.unboxAnnotationElement() }.toMap()))
				} catch (e: Throwable) {
					System.err.println("Exception.readAnnotations: ${e.message} : " + annotation.info + ", " + annotation)
					listOf<AstAnnotation>()
				}
			} else {
				listOf<AstAnnotation>()
			}
		}
	}

	fun generateField(field: SootField, context: SootContext): AstField {
		val it = field
		val constantValue = it.tags.filterIsInstance<ConstantValueTag>().firstOrNull()
		val hasConstantValue = constantValue != null
		val finalConstantValue: Any? = if (constantValue != null) {
			when (constantValue) {
				is IntegerConstantValueTag -> constantValue.intValue
				is LongConstantValueTag -> constantValue.longValue
				is DoubleConstantValueTag -> constantValue.doubleValue
				is FloatConstantValueTag -> constantValue.floatValue
				is StringConstantValueTag -> constantValue.stringValue
				else -> throw InvalidOperationException("Not a valid constant")
			}
		} else {
			null
		}

		return AstField(
			containingClass = FqName(field.declaringClass.name),
			name = it.name,
			annotations = readAnnotations(context, it.tags),
			type = it.type.astType,
			descriptor = it.type.astType.mangle(),
			genericSignature = it.tags.filterIsInstance<SignatureTag>().firstOrNull()?.signature,
			modifiers = it.modifiers,
			isStatic = it.isStatic,
			isFinal = it.isFinal,
			hasConstantValue = hasConstantValue,
			constantValue = finalConstantValue,
			visibility = if (it.isPublic) AstVisibility.PUBLIC else if (it.isProtected) AstVisibility.PROTECTED else AstVisibility.PRIVATE
		)
	}

	fun generateClass(clazz: SootClass, context: SootContext): AstClass {
		val clazzName = FqName(clazz.name)
		val contextClass = context[clazz]
		val missingMethods = if (contextClass.clazzAbstract) contextClass.missingMethods else listOf()
		val generatedMethods = clazz.methods.filter { context.mustInclude(it) }.map { generateMethod(it, context) }
		val generatedFields = clazz.fields.filter { context.mustInclude(it) }.map { generateField(it, context) }

		return AstClass(
			name = clazzName,
			modifiers = clazz.modifiers,
			annotations = readAnnotations(context, clazz.tags),
			implCode = clazz.getAnnotation(JTranscNativeClassImpl::class.java.name, "value") as String?,
			nativeName = clazz.getAnnotation(JTranscNativeClass::class.java.name, "value") as String?,
			classType = if (clazz.isInterface) AstClassType.INTERFACE else if (clazz.isAbstract) AstClassType.ABSTRACT else AstClassType.CLASS,
			visibility = AstVisibility.PUBLIC,
			extending = if (clazz.hasSuperclass() && !clazz.isInterface) FqName(clazz.superclass.name) else null,
			implementing = clazz.interfaces.map { FqName(it.name) },
			fields = generatedFields,
			methods = (missingMethods + generatedMethods).distinctBy { it.name + ":" + it.desc }
		)
	}
}

class TargetTree {
	private val targetClasses = hashMapOf<SootClass, TargetClass>()

	fun getTargetClass(name: String): TargetClass {
		//return getTargetClass(Scene.v().getSootClass(name))
		return getTargetClass(Scene.v().loadClassAndSupport(name))
	}

	fun getTargetClass(clazz: SootClass): TargetClass {
		//if (!targetClasses.contains(clazz)) targetClasses.put(clazz, new TargetClass(this, clazz))
		//targetClasses.get(clazz).orNull
		return TargetClass(this, clazz)
	}

}

class TargetMethod(val tree: TargetTree, val clazz: TargetClass, val method: SootMethod) {
	val signature by lazy {
		method.name + "_" + method.parameterTypes.map { (it as Type).toString() }.joinToString(",") + method.returnType.toString()
	}
}

class TargetField(val tree: TargetTree, val clazz: TargetClass, val field: SootField) {

}

class TargetClass(val tree: TargetTree, val clazz: SootClass) {
	val methods = hashMapOf<String, TargetMethod>()
	val fields = hashMapOf<String, TargetField>()

	init {
		for (sootMethod in clazz.methods) {
			val targetMethod = TargetMethod(tree, this, sootMethod)
			methods[targetMethod.signature] = targetMethod
		}
	}
}

open class BaseProjectContext(val classNames: List<String>, val mainClass: String, val classPaths: List<String>, val output: SyncVfsFile, val deps2: HashSet<AstRef>?) {
	val classes = arrayListOf<AstClass>()
	val preInitLines = arrayListOf<String>()
	val bootImports = arrayListOf<String>()

	fun mustInclude(ref: AstRef): Boolean = if (deps2 == null) true else ref in deps2
	fun addDep(dep: AstRef) {
		this.deps2?.add(dep)
	}

	fun addDep(dep: Iterable<AstRef>) {
		this.deps2?.addAll(dep)
	}
	//def getClassesWithStaticConstructor
}

/*
@Retention(AnnotationRetention.RUNTIME)
annotation class DemoAnnotation(val a:String)

@DemoAnnotation("test")
class MyTest {

}
*/