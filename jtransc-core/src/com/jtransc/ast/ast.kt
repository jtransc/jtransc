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

package com.jtransc.ast

import com.jtransc.BuildBackend
import com.jtransc.JTranscSystem
import com.jtransc.JTranscVersion
import com.jtransc.annotation.*
import com.jtransc.ast.dependency.AstDependencyAnalyzer
import com.jtransc.ds.clearFlags
import com.jtransc.ds.hasFlag
import com.jtransc.error.InvalidOperationException
import com.jtransc.error.invalidOp
import com.jtransc.maven.MavenLocalRepository
import com.jtransc.util.dependencySorter
import com.jtransc.vfs.IUserData
import com.jtransc.vfs.SyncVfsFile
import com.jtransc.vfs.UserData
import java.io.File
import java.io.IOException
import java.util.*

data class AstBuildSettings(
	var jtranscVersion: String = JTranscVersion.getVersion(),
	var title: String = "App Title",
	var name: String = "AppName",
	var version: String = "0.0.0",
	var company: String = "My Company",
	var package_: String = "com.example",
	var embedResources: Boolean = false,
	var libraries: List<AstBuildSettings.Library> = listOf(),
	var assets: List<File> = listOf(),
	var debug: Boolean = true,
	var initialWidth: Int = 1280,
	var initialHeight: Int = 720,
	var vsync: Boolean = true,
	var resizable: Boolean = true,
	var borderless: Boolean = false,
	var fullscreen: Boolean = false,
	var icon: String? = null,
	var orientation: AstBuildSettings.Orientation = AstBuildSettings.Orientation.AUTO,
	val backend: BuildBackend = BuildBackend.ASM,
	val relooper: Boolean = false,
	val minimizeNames: Boolean = false,
	val analyzer: Boolean = false,
	val extra: Map<String?, String?> = mapOf(),
	val rtAndRtCore: List<String> = MavenLocalRepository.locateJars(
		"com.jtransc:jtransc-rt:$jtranscVersion",
		"com.jtransc:jtransc-rt-core:$jtranscVersion"
	)
) {
	companion object {
		val DEFAULT = AstBuildSettings()
	}

	val release: Boolean get() = !debug

	data class Library(val name: String, val version: String?) {
		companion object {
			fun fromInfo(info: String): Library {
				val parts = info.split(':')
				val name = parts.getOrNull(0)
				val version = parts.getOrNull(1)
				return Library(name!!, version)
			}
		}
	}

	enum class Orientation {
		PORTRAIT, LANDSCAPE, AUTO;

		val lowName: String = name.toLowerCase()

		companion object {
			fun fromString(str: String): Orientation = when (str.toLowerCase()) {
				"portrait" -> PORTRAIT
				"landscape" -> LANDSCAPE
				"auto" -> AUTO
				else -> AUTO
			}
		}
	}
}

interface AstClassGenerator {
	fun generateClass(program: AstProgram, fqname: FqName): AstClass
}

interface AstResolver {
	operator fun get(ref: AstMethodRef): AstMethod?
	operator fun get(ref: AstFieldRef): AstField?
	operator fun get(name: FqName): AstClass?
	operator fun contains(name: FqName): Boolean
}

fun AstResolver.get3(ref: AstType.REF): AstClass = this[ref.name]!!

interface LocateRightClass {
	fun locateRightClass(field: AstFieldRef): AstType.REF
	fun locateRightClass(method: AstMethodRef): AstType.REF
}

fun LocateRightClass.locateRightField(field: AstFieldRef): AstFieldRef {
	val clazz = locateRightClass(field)
	return if (clazz == field.classRef) field else AstFieldRef(clazz.name, field.name, field.type)
}

fun LocateRightClass.locateRightMethod(method: AstMethodRef): AstMethodRef {
	val clazz = locateRightClass(method)
	return if (clazz == method.classRef) method else AstMethodRef(clazz.name, method.name, method.type)
}

class AstGenContext {
	lateinit var clazz: AstClass
	lateinit var method: AstMethod

	override fun toString() = try {
		"${clazz.name}::${method.name}"
	} catch (e: Throwable) {
		"${clazz.name}"
	} catch (e: Throwable) {
		"NO_CONTEXT"
	}
}

class AstProgram(
	val entrypoint: FqName,
	val resourcesVfs: SyncVfsFile,
	val generator: AstClassGenerator
) : IUserData by UserData(), AstResolver, LocateRightClass {
	private val _classes = arrayListOf<AstClass>()
	private val _classesByFqname = hashMapOf<String, AstClass>()

	val classes: List<AstClass> get() = _classes

	private val classesToGenerate = LinkedList<AstType.REF>()
	private val referencedClasses = hashSetOf<AstType.REF>()
	private val referencedClassBy = hashMapOf<AstType.REF, AstType.REF>()

	fun hasClassToGenerate() = classesToGenerate.isNotEmpty()

	fun getClassBytes(clazz: FqName): ByteArray {
		try {
			return resourcesVfs[clazz.internalFqname + ".class"].readBytes()
		} catch (e: Throwable) {
			throw IOException(e.message + " referenced by " + referencedClassBy[AstType.REF(clazz)], e)
		}
	}

	fun readClassToGenerate(): AstType.REF = classesToGenerate.remove()

	fun addReference(clazz: AstType.REF, referencedBy: AstType.REF) {
		if (clazz !in referencedClasses) {
			classesToGenerate += clazz
			referencedClasses += clazz
			referencedClassBy[clazz] = referencedBy
		}
	}

	override operator fun contains(name: FqName) = name.fqname in _classesByFqname
	//operator fun get(name: FqName) = classesByFqname[name.fqname] ?: throw RuntimeException("AstProgram. Can't find class '$name'")
	override operator fun get(name: FqName): AstClass {
		val result = _classesByFqname[name.fqname]
		if (result == null) {
			val classFile = name.internalFqname + ".class"
			println("AstProgram. Can't find class '$name'")
			println("AstProgram. ClassFile: $classFile")
			println("AstProgram. File exists: " + resourcesVfs[classFile].exists)

			throw RuntimeException("AstProgram. Can't find class '$name'")
		} else {
			return result
		}
	}

	private var finished = false

	fun add(clazz: AstClass) {
		if (finished) invalidOp("Can't add more classes to a finished program")
		_classes.add(clazz)
		_classesByFqname[clazz.fqname] = clazz
	}

	fun finish() {
		finished = true
	}

	val allAnnotations by lazy {
		classes.flatMap {
			it.annotations + it.methods.flatMap { it.annotations + it.parameterAnnotations.flatMap { it } } + it.fields.flatMap { it.annotations }
		}
	}

	val allAnnotationsList by lazy { AstAnnotationList(allAnnotations) }

	override operator fun get(ref: AstMethodRef): AstMethod? = this[ref.containingClass].getMethodInAncestorsAndInterfaces(ref.nameDesc)
	//override operator fun get(ref: AstFieldRef): AstField = this[ref.containingClass][ref]
	override operator fun get(ref: AstFieldRef): AstField = this[ref.containingClass].get(ref.withoutClass)

	operator fun get(ref: AstFieldWithoutTypeRef): AstField = this[ref.containingClass].get(ref)

	fun getInterfaces(clazz: AstClass) = clazz.implementing.map { this[it] }

	fun getAllInterfaces(clazz: AstClass): Set<AstClass> {
		val thisInterfaces = getInterfaces(clazz).flatMap { listOf(it) + getAllInterfaces(it) }.toSet()
		if (clazz.extending == null) {
			return thisInterfaces
		} else {
			return getAllInterfaces(this[clazz.extending]) + thisInterfaces
		}
	}

	fun isImplementing(clazz: AstClass, implementingClazz: String): Boolean {
		return (implementingClazz.fqname in this) && (this[implementingClazz.fqname] in getAllInterfaces(clazz))
	}

	override fun locateRightClass(field: AstFieldRef): AstType.REF {
		return field.classRef
	}

	override fun locateRightClass(method: AstMethodRef): AstType.REF {
		return method.classRef
	}
}

enum class AstVisibility { PUBLIC, PROTECTED, PRIVATE }
enum class AstClassType { CLASS, ABSTRACT, INTERFACE }

class AstClass(
	val source: String,
	val program: AstProgram,
	val name: FqName,
	val modifiers: AstModifiers,
	val extending: FqName? = null,
	val implementing: List<FqName> = listOf(),
	override val annotations: List<AstAnnotation> = listOf()
) : IUserData by UserData(), AstAnnotated {
	override val annotationsList = AstAnnotationList(annotations)
	val ref = AstType.REF(name)
	val astType = AstType.REF(this.name)
	val classType: AstClassType = modifiers.classType
	val visibility: AstVisibility = modifiers.visibility
	val fields = arrayListOf<AstField>()
	val methods = arrayListOf<AstMethod>()
	val methodsByName = hashMapOf<String, ArrayList<AstMethod>>()
	val methodsByNameDescInterfaces = hashMapOf<AstMethodWithoutClassRef, AstMethod?>()
	val methodsByNameDesc = hashMapOf<AstMethodWithoutClassRef, AstMethod?>()
	//val fieldsByName = hashMapOf<String, AstField>()
	val fieldsByInfo = hashMapOf<AstFieldWithoutClassRef, AstField>()
	val fieldsByName = hashMapOf<String, AstField>()
	val runtimeAnnotations = annotations.filter { it.runtimeVisible }
	val hasFFI = implementing.contains(FqName("com.sun.jna.Library"))

	//fun getDirectInterfaces(): List<AstClass> = implementing.map { program[it] }
	val directInterfaces: List<AstClass> by lazy { implementing.map { program[it] } }

	val parentClass: AstClass? by lazy { if (extending != null) program[extending] else null }
	//fun getParentClass(): AstClass? = if (extending != null) program[extending] else null

	val allInterfaces: List<AstClass> by lazy {
		val out = arrayListOf<AstClass>()
		val sets = hashSetOf<AstClass>()
		val queue: Queue<AstClass> = LinkedList<AstClass>()
		queue += this
		while (queue.isNotEmpty()) {
			val item = queue.remove()
			if (item.isInterface && item != this) out += item
			for (i in item.directInterfaces) {
				if (i !in sets) {
					sets += i
					queue += i
				}
			}
		}
		out.distinct()
	}

	val allMethodsToImplement: List<AstMethodWithoutClassRef> by lazy {
		val allInterfacesIncludingThis = (if (isInterface) listOf(this) else listOf<AstClass>()) + this.allInterfaces
		allInterfacesIncludingThis.distinct().flatMap { it.methods }.filter { !it.isStatic }.distinct().map { it.ref.withoutClass }.distinct()
	}

	fun add(field: AstField) {
		if (finished) invalidOp("Finished class")
		fields.add(field)
		fieldsByInfo[field.refWithoutClass] = field
		fieldsByName[field.name] = field
	}

	fun add(method: AstMethod) {
		if (finished) invalidOp("Finished class")
		methods.add(method)
		if (method.name !in methodsByName) methodsByName[method.name] = arrayListOf()
		val methodDesc = AstMethodWithoutClassRef(method.name, method.methodType)
		methodsByName[method.name]?.add(method)
		methodsByNameDescInterfaces[methodDesc] = method
		methodsByNameDesc[methodDesc] = method
	}

	private var finished = false

	fun finish() {
		finished = true
	}

	//val dependencies: AstReferences = AstReferences()
	val implCode by lazy { annotationsList.getTyped<JTranscNativeClassImpl>()?.value }
	val nativeName: String? by lazy {
		annotationsList.getTyped<JTranscNativeClass>()?.value ?: annotationsList.getTyped<JTranscNativeName>()?.value
	}

	val isInterface: Boolean get() = classType == AstClassType.INTERFACE
	val isAbstract: Boolean get() = classType == AstClassType.ABSTRACT
	val fqname = name.fqname

	val classAndFieldAndMethodAnnotations by lazy {
		annotations + methods.flatMap { it.annotations } + fields.flatMap { it.annotations }
	}

	fun getMethods(name: String): List<AstMethod> = methodsByName[name]!!
	fun getMethod(name: String, desc: String): AstMethod? = methodsByName[name]?.firstOrNull { it.desc == desc }

	fun getMethodInAncestors(nameDesc: AstMethodWithoutClassRef): AstMethod? {
		var result = methodsByNameDesc[nameDesc]
		if (result == null) {
			result = parentClass?.getMethodInAncestorsAndInterfaces(nameDesc)
		}
		methodsByNameDesc[nameDesc] = result
		return result
	}

	fun getMethodInAncestorsAndInterfaces(nameDesc: AstMethodWithoutClassRef): AstMethod? {
		var result = methodsByNameDescInterfaces[nameDesc]
		if (result == null) {
			result = parentClass?.getMethodInAncestorsAndInterfaces(nameDesc)
		}
		if (result == null) {
			for (it in directInterfaces) {
				result = it.getMethodInAncestorsAndInterfaces(nameDesc)
				if (result != null) break
			}
		}
		methodsByNameDescInterfaces[nameDesc] = result
		return result
		//return methodsByNameDesc[nameDesc] ?: parentClass?.getMethodInAncestors(nameDesc)
	}

	fun getMethodSure(name: String, desc: String): AstMethod {
		return getMethod(name, desc) ?: throw InvalidOperationException("Can't find method ${this.name}:$name:$desc")
	}

	// Methods
	operator fun get(ref: AstMethodRef): AstMethod = getMethodSure(ref.name, ref.desc)

	operator fun get(ref: AstMethodWithoutClassRef): AstMethod? = getMethod(ref.name, ref.desc)

	// Fields
	operator fun get(ref: AstFieldRef): AstField = fieldsByInfo[ref.withoutClass] ?:
		invalidOp("Can't find field $ref")

	operator fun get(ref: AstFieldWithoutClassRef): AstField = fieldsByInfo[ref] ?: parentClass?.get(ref) ?:
		invalidOp("Can't find field $ref on ancestors")

	operator fun get(ref: AstFieldWithoutTypeRef): AstField = fieldsByName[ref.name] ?: parentClass?.get(ref) ?:
		invalidOp("Can't find field $ref on ancestors")


	val hasStaticInit: Boolean get() = staticInitMethod != null
	val staticInitMethod: AstMethod? by lazy { methodsByName["<clinit>"]?.firstOrNull() }

	val allDependencies: Set<AstRef> by lazy {
		var out = hashSetOf<AstRef>()
		if (extending != null) out.add(AstType.REF(extending))
		for (i in implementing) out.add(AstType.REF(i))
		for (f in fields) for (ref in f.type.getRefClasses()) out.add(ref)
		for (m in methods) {
			for (dep in m.dependencies.methods) {
				out.add(dep.classRef)
				out.add(dep)
			}
			for (dep in m.dependencies.fields) {
				out.add(dep.classRef)
				out.add(dep)
			}
			for (dep in m.dependencies.classes) {
				out.add(dep)
			}
		}
		out.toSet()
	}

	val classDependencies: Set<AstType.REF> by lazy {
		allDependencies.filterIsInstance<AstType.REF>().toSet()
	}

	//override fun toString() = "AstClass($name)"
	override fun toString() = "$name"

	val thisAndAncestors: List<AstClass> by lazy {
		if (extending == null) {
			listOf(this)
		} else {
			listOf(this) + program[extending].thisAndAncestors
		}
	}

	val ancestors: List<AstClass> by lazy { thisAndAncestors.drop(1) }
}

val AstClass?.isNative: Boolean get() = (this?.nativeName != null)

fun List<AstClass>.sortedByDependencies(): List<AstClass> {
	val classes = this.associateBy { it.name.fqname }
	fun resolveClassRef(ref: AstType.REF) = classes[ref.fqname]!!
	fun resolveMethodRef(ref: AstMethodRef) = resolveClassRef(ref.classRef)[ref]

	return this.dependencySorter(allowCycles = true) {
		val extending = it.extending?.fqname
		val implementing = it.implementing.map { classes[it.fqname]!! }
		val ext = if (extending != null) classes[extending] else null
		val ext2 = if (ext != null) listOf(ext) else listOf()
		val ext3 = it.staticInitMethod?.dependencies?.allClasses?.map { classes[it.fqname]!! } ?: listOf()

		fun checkMethodsRecursive(method: AstMethod, explored: MutableSet<AstMethod> = hashSetOf()): MutableSet<AstMethod> {
			if (method !in explored) {
				explored.add(method)
				for (m in method.dependencies.methods) {
					checkMethodsRecursive(resolveMethodRef(m), explored)
				}
			}
			return explored
		}

		val ext4 = if (it.staticInitMethod != null) {
			checkMethodsRecursive(it.staticInitMethod!!).toList().flatMap { it.dependencies.allClasses }.map { classes[it.fqname]!! }
		} else {
			listOf()
		}
		//val ext3 = it.methods.flatMap { it.dependencies.allClasses.map { classes[it.fqname]!! } }
		// @TODO: Check fields too!
		ext2 + implementing + ext3 + ext4
	}
}

fun AstType.getRefClasses(): List<AstType.REF> = this.getRefTypesFqName().map { AstType.REF(it) }

data class AstReferences(
	val program: AstProgram?,
	val classes: Set<AstType.REF> = setOf(),
	val methods: Set<AstMethodRef> = setOf(),
	val fields: Set<AstFieldRef> = setOf()
) {
	val allClasses: Set<AstType.REF> by lazy {
		classes + methods.flatMap { it.allClassRefs } + fields.flatMap { listOf(it.classRef) }
	}

	val fields2 by lazy { fields.map { program!![it] } }
}

interface AstAnnotated {
	val annotations: List<AstAnnotation>
	val annotationsList: AstAnnotationList
}

val AstAnnotated?.keepName: Boolean get() = this?.annotationsList?.contains<JTranscKeepName>() ?: false

open class AstMember(
	val containingClass: AstClass,
	val name: String,
	val type: AstType,
	val genericType: AstType,
	val isStatic: Boolean = false,
	val visibility: AstVisibility = AstVisibility.PUBLIC,
	override val annotations: List<AstAnnotation> = listOf()
) : IUserData by UserData(), AstAnnotated {
	override val annotationsList = AstAnnotationList(annotations)
	val program = containingClass.program

	val nativeName: String? by lazy {
		annotationsList.getTyped<JTranscNativeName>()?.value
	}
}

class AstField(
	id: Int,
	containingClass: AstClass,
	name: String,
	type: AstType,
	val modifiers: AstModifiers,
	val descriptor: String,
	annotations: List<AstAnnotation>,
	val genericSignature: String?,
	val constantValue: Any? = null
) : AstMember(containingClass, name, type, if (genericSignature != null) AstType.demangle(genericSignature) else type, modifiers.isStatic, modifiers.visibility, annotations) {
	val isFinal: Boolean = modifiers.isFinal
	val ref: AstFieldRef by lazy { AstFieldRef(this.containingClass.name, this.name, this.type) }
	val refWithoutClass: AstFieldWithoutClassRef by lazy { AstFieldWithoutClassRef(this.name, this.type) }
	val hasConstantValue = constantValue != null
}

class AstMethod(
	containingClass: AstClass,
	val id: Int,
	name: String,
	type: AstType.METHOD,
	annotations: List<AstAnnotation>,
	val signature: String,
	val genericSignature: String?,
	val defaultTag: Any?,
	val modifiers: AstModifiers,
	val generateBody: () -> AstBody?,
	val bodyRef: AstMethodRef? = null,
	val parameterAnnotations: List<List<AstAnnotation>> = listOf()
	//val isOverriding: Boolean = overridingMethod != null,
) : AstMember(containingClass, name, type, if (genericSignature != null) AstType.demangleMethod(genericSignature) else type, modifiers.isStatic, modifiers.visibility, annotations) {
	val isNative: Boolean = modifiers.isNative

	val body: AstBody? by lazy { generateBody() }
	val hasBody: Boolean get() = body != null

	val methodType: AstType.METHOD = type
	val genericMethodType: AstType.METHOD = genericType as AstType.METHOD
	val desc = methodType.desc
	val ref: AstMethodRef by lazy { AstMethodRef(containingClass.name, name, methodType) }
	val dependencies by lazy { AstDependencyAnalyzer.analyze(containingClass.program, body) }

	val getterField: String? by lazy { annotationsList.getTyped<JTranscGetter>()?.value }
	val setterField: String? by lazy { annotationsList.getTyped<JTranscSetter>()?.value }
	val nativeMethod: String? by lazy { annotationsList.getTyped<JTranscMethod>()?.value }
	val isInline: Boolean by lazy { annotationsList.contains<JTranscInline>() }

	val isInstanceInit: Boolean get() = name == "<init>"
	val isClassInit: Boolean get() = name == "<clinit>"
	val isClassOrInstanceInit: Boolean get() = isInstanceInit || isClassInit
	val methodVoidReturnThis: Boolean get() = isInstanceInit

	val isOverriding: Boolean by lazy { containingClass.ancestors.any { it[ref.withoutClass] != null } }
	val isImplementing: Boolean by lazy { containingClass.allInterfaces.any { it.getMethod(this.name, this.desc) != null } }

	override fun toString(): String = "AstMethod(${containingClass.fqname}:$name:$desc)"
}

val AstMethodRef.isInstanceInit: Boolean get() = name == "<init>"
val AstMethodRef.isClassInit: Boolean get() = name == "<clinit>"
val AstMethodRef.isClassOrInstanceInit: Boolean get() = isInstanceInit || isClassInit


data class AstModifiers(val acc: Int) {
	companion object {
		fun withFlags(vararg flags: Int): AstModifiers {
			var out = 0
			for (f in flags) out = out or f
			return AstModifiers(out)
		}

		const val ACC_PUBLIC = 0x0001; // class, field, method
		const val ACC_PRIVATE = 0x0002; // class, field, method
		const val ACC_PROTECTED = 0x0004; // class, field, method
		const val ACC_STATIC = 0x0008; // field, method
		const val ACC_FINAL = 0x0010; // class, field, method, parameter
		const val ACC_SUPER = 0x0020; // class
		const val ACC_SYNCHRONIZED = 0x0020; // method
		const val ACC_VOLATILE = 0x0040; // field
		const val ACC_BRIDGE = 0x0040; // method
		const val ACC_VARARGS = 0x0080; // method
		const val ACC_TRANSIENT = 0x0080; // field
		const val ACC_NATIVE = 0x0100; // method
		const val ACC_INTERFACE = 0x0200; // class
		const val ACC_ABSTRACT = 0x0400; // class, method
		const val ACC_STRICT = 0x0800; // method
		const val ACC_SYNTHETIC = 0x1000; // class, field, method, parameter
		const val ACC_ANNOTATION = 0x2000; // class
		const val ACC_ENUM = 0x4000; // class(?) field inner
		const val ACC_MANDATED = 0x8000; // parameter
	}

	val isPublic: Boolean get() = acc hasFlag ACC_PUBLIC
	val isPrivate: Boolean get() = acc hasFlag ACC_PRIVATE
	val isProtected: Boolean get() = acc hasFlag ACC_PROTECTED
	val isStatic: Boolean get() = acc hasFlag ACC_STATIC
	val isFinal: Boolean get() = acc hasFlag ACC_FINAL
	val isSuper: Boolean get() = acc hasFlag ACC_SUPER
	val isSynchronized: Boolean get() = acc hasFlag ACC_SYNCHRONIZED
	val isVolatile: Boolean get() = acc hasFlag ACC_VOLATILE
	val isBridge: Boolean get() = acc hasFlag ACC_BRIDGE
	val isVarargs: Boolean get() = acc hasFlag ACC_VARARGS
	val isTransient: Boolean get() = acc hasFlag ACC_TRANSIENT
	val isNative: Boolean get() = acc hasFlag ACC_NATIVE
	val isInterface: Boolean get() = acc hasFlag ACC_INTERFACE
	val isAbstract: Boolean get() = acc hasFlag ACC_ABSTRACT
	val isStrict: Boolean get() = acc hasFlag ACC_STRICT
	val isSynthetic: Boolean get() = acc hasFlag ACC_SYNTHETIC
	val isAnnotation: Boolean get() = acc hasFlag ACC_ANNOTATION
	val isEnum: Boolean get() = acc hasFlag ACC_ENUM
	val isMandated: Boolean get() = acc hasFlag ACC_MANDATED
	val isConcrete: Boolean get() = !isNative && !isAbstract

	val visibility: AstVisibility get() = if (isPublic) {
		AstVisibility.PUBLIC
	} else if (isProtected) {
		AstVisibility.PROTECTED
	} else {
		AstVisibility.PRIVATE
	}

	val classType: AstClassType get() = if (isInterface) {
		AstClassType.INTERFACE
	} else if (isAbstract) {
		AstClassType.ABSTRACT
	} else {
		AstClassType.CLASS
	}

	fun withVisibility(visibility: AstVisibility) = AstModifiers(
		(acc clearFlags (ACC_PUBLIC or ACC_PROTECTED or ACC_PRIVATE)) or when (visibility) {
			AstVisibility.PUBLIC -> ACC_PUBLIC
			AstVisibility.PROTECTED -> ACC_PROTECTED
			AstVisibility.PRIVATE -> ACC_PRIVATE
			else -> invalidOp
		}
	)

	override fun toString(): String = "$acc"
}
