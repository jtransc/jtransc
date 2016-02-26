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

import com.jtransc.ast.dependency.AstDependencyAnalyzer
import com.jtransc.error.InvalidOperationException
import com.jtransc.error.invalidOp
import com.jtransc.input.SootToAst
import com.jtransc.util.dependencySorter
import com.jtransc.vfs.IUserData
import com.jtransc.vfs.SyncVfsFile
import com.jtransc.vfs.UserData
import jtransc.annotation.*
import java.util.*

data class AstBuildSettings(
	var jtranscVersion: String,
	var title: String = "App Title",
	var name: String = "AppName",
	var version: String = "0.0.0",
	var company: String = "My Company",
	var package_: String = "",
	var libraries: List<AstBuildSettings.Library> = listOf(),
	var assets: List<String> = listOf(),
	var debug: Boolean = true,
	var initialWidth: Int = 1280,
	var initialHeight: Int = 720,
	var vsync: Boolean = true,
	var resizable: Boolean = true,
	var borderless: Boolean = false,
	var fullscreen: Boolean = false,
	var icon: String? = null,
	var orientation: AstBuildSettings.Orientation = AstBuildSettings.Orientation.AUTO
) {
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
	fun isInterface(name: FqName): Boolean
}

class AstProgram(
	val entrypoint: FqName,
	val resourcesVfs: SyncVfsFile,
	val generator: AstClassGenerator
) : IUserData by UserData() {
	private val _classes = arrayListOf<AstClass>()
	private val _classesByFqname = hashMapOf<String, AstClass>()

	val classes: List<AstClass> get() = _classes

	private val classesToGenerate = LinkedList<AstClassRef>()
	private val referencedClasses = hashSetOf<AstClassRef>()

	fun hasClassToGenerate() = classesToGenerate.isNotEmpty()

	fun readClassToGenerate():AstClassRef = classesToGenerate.remove()

	fun addReference(clazz: AstClassRef) {
		if (clazz !in referencedClasses) {
			classesToGenerate += clazz
			referencedClasses += clazz
		}
	}

	operator fun contains(name: FqName) = name.fqname in _classesByFqname
	//operator fun get(name: FqName) = classesByFqname[name.fqname] ?: throw RuntimeException("AstProgram. Can't find class '$name'")
	operator fun get(name: FqName): AstClass {
		val result = _classesByFqname[name.fqname]
		if (result == null) {
			val classFile = name.internalFqname + ".class"
			println("AstProgram. Can't find class '$name'")
			println("AstProgram. ClassFile: $classFile")
			println("AstProgram. File exists: " + resourcesVfs[classFile].exists)
			//println("AstProgram. Soot exists: " + SootToAst.checkIfClassExists(name))

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
			it.annotations + it.methods.flatMap { it.annotations } + it.fields.flatMap { it.annotations }
		}
	}

	operator fun get(ref: AstType.REF): AstClass = this[ref.name]
	operator fun get(ref: AstClassRef): AstClass = this[ref.name]
	operator fun get(ref: AstMethodRef): AstMethod = this[ref.containingClass][ref]
	operator fun get(ref: AstFieldRef): AstField = this[ref.containingClass][ref]

	// @TODO: Cache all this stuff!
	/*
	fun getAncestors(clazz: AstClass): List<AstClass> {
		var out = arrayListOf<AstClass>()
		var current = clazz
		while (current != null) {
			out.add(current)
			val extending = current.extending ?: break
			if (extending.fqname.isNullOrEmpty()) break
			current = this[extending]
		}
		return out
	}
	*/

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
}

enum class AstVisibility { PUBLIC, PROTECTED, PRIVATE }
enum class AstClassType { CLASS, ABSTRACT, INTERFACE }

class AstClass(
	val program: AstProgram,
	val name: FqName,
	val modifiers: Int,
	val classType: AstClassType = AstClassType.CLASS,
	val visibility: AstVisibility = AstVisibility.PUBLIC,
	val extending: FqName? = null,
	val implementing: List<FqName> = listOf(),
	val annotations: List<AstAnnotation> = listOf()
) {
	val fields = arrayListOf<AstField>()
	val methods = arrayListOf<AstMethod>()
	val methodsByName = hashMapOf<String, ArrayList<AstMethod>>()
	val fieldsByName = hashMapOf<String, AstField>()

	fun getDirectInterfaces(): List<AstClass> = implementing.map { program[it] }

	fun getParentClass(): AstClass? = if (extending != null) program[extending] else null

	fun getAllInterfaces(): List<AstClass> {
		val out = arrayListOf<AstClass>()
		val sets = hashSetOf<AstClass>()
		val queue: Queue<AstClass> = LinkedList<AstClass>()
		queue += this
		while (queue.isNotEmpty()) {
			val item = queue.remove()
			if (item.isInterface && item != this) out += item
			for (i in item.getDirectInterfaces()) {
				if (i !in sets) {
					sets += i
					queue += i
				}
			}
		}
		//return (getParentClass()?.getAllInterfaces() ?: listOf()) + getAllInterfaces().flatMap { it.getAllInterfaces() }
		return out
	}

	fun getAllMethodsToImplement(): List<AstMethodWithoutClassRef> {
		return this.getAllInterfaces().flatMap { it.methods }.filter { !it.isStatic }.map { it.ref.withoutClass }
	}

	fun add(field: AstField) {
		if (finished) invalidOp("Finished class")
		fields.add(field)
		fieldsByName[field.name] = field
	}

	fun add(method: AstMethod) {
		if (finished) invalidOp("Finished class")
		methods.add(method)
		if (method.name !in methodsByName) methodsByName[method.name] = arrayListOf()
		methodsByName[method.name]?.add(method)
	}

	private var finished = false

	fun finish() {
		finished = true
	}

	//val dependencies: AstReferences = AstReferences()
	val implCode by lazy { annotations.get(JTranscNativeClassImpl::value) }
	val nativeName by lazy { annotations.get(JTranscNativeClass::value) }

	val isInterface: Boolean get() = classType == AstClassType.INTERFACE
	val isAbstract: Boolean get() = classType == AstClassType.ABSTRACT
	val fqname = name.fqname
	val isNative by lazy { (nativeName != null) }

	val classAndFieldAndMethodAnnotations by lazy {
		annotations + methods.flatMap { it.annotations } + fields.flatMap { it.annotations }
	}

	fun getMethods(name: String): List<AstMethod> = methodsByName[name]!!
	fun getMethod(name: String, desc: String): AstMethod? = methodsByName[name]?.firstOrNull { it.desc == desc }
	fun getMethodSure(name: String, desc: String): AstMethod {
		return getMethod(name, desc) ?: throw InvalidOperationException("Can't find method ${this.name}:$name:$desc")
	}

	operator fun get(ref: AstMethodRef) = getMethodSure(ref.name, ref.desc)
	operator fun get(ref: AstMethodWithoutClassRef) = getMethod(ref.name, ref.desc)
	operator fun get(ref: AstFieldRef) = fieldsByName[ref.name] ?: invalidOp("Can't find field $ref")

	val hasStaticInit: Boolean get() = staticInitMethod != null
	val staticInitMethod: AstMethod? get() = methodsByName["<clinit>"]?.firstOrNull()

	val allDependencies: Set<AstRef> by lazy {
		var out = hashSetOf<AstRef>()
		if (extending != null) out.add(AstClassRef(extending))
		for (i in implementing) out.add(AstClassRef(i))
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

	val classDependencies: Set<AstClassRef> by lazy {
		allDependencies.filterIsInstance<AstClassRef>().toSet()
	}

	override fun toString() = "AstClass($name)"

	fun getThisAndAncestors(program: AstProgram): List<AstClass> {
		if (extending == null) {
			return listOf(this)
		} else {
			return listOf(this) + program[extending].getThisAndAncestors(program)
		}
	}

	fun getAncestors(program: AstProgram): List<AstClass> {
		return getThisAndAncestors(program).drop(1)
	}

	fun hasMethod(method: AstMethodWithoutClassRef): Boolean {
		return return methods.any { it.ref.withoutClass == method }
	}
}

fun List<AstClass>.sortedByDependencies(): List<AstClass> {
	val classes = this.associateBy { it.name.fqname }
	fun resolveClassRef(ref: AstClassRef) = classes[ref.fqname]!!
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

val AstClass.ref: AstClassRef get() = AstClassRef(this.name)
val AstClass.astType: AstType.REF get() = AstType.REF(this.name)

fun AstType.getRefClasses(): List<AstClassRef> = this.getRefTypesFqName().map { AstClassRef(it) }

data class AstReferences(
	val classes: Set<AstClassRef> = setOf(),
	val methods: Set<AstMethodRef> = setOf(),
	val fields: Set<AstFieldRef> = setOf()
) {
	val allClasses: Set<AstClassRef> by lazy {
		classes + methods.flatMap { it.allClassRefs } + fields.flatMap { listOf(it.classRef) }
	}

	fun getFields2(program: AstProgram) = fields.map { program[it] }
}

open class AstMember(
	val containingClass: AstClass,
	val name: String,
	val type: AstType,
	val isStatic: Boolean = false,
	val visibility: AstVisibility = AstVisibility.PUBLIC,
	val annotations: List<AstAnnotation> = listOf()
) {
	val program = containingClass.program
}

class AstField(
	containingClass: AstClass,
	name: String,
	type: AstType,
	val modifiers: Int,
	val descriptor: String,
	annotations: List<AstAnnotation>,
	val genericSignature: String?,
	isStatic: Boolean = false,
	val isFinal: Boolean = false,
	visibility: AstVisibility = AstVisibility.PUBLIC,
	val constantValue: Any? = null
) : AstMember(containingClass, name, type, isStatic, visibility, annotations) {
	val ref: AstFieldRef get() = AstFieldRef(this.containingClass.name, this.name, this.type)
	val hasConstantValue = constantValue != null
}

class AstMethod(
	containingClass: AstClass,
	name: String,
	type: AstType.METHOD_TYPE,
	annotations: List<AstAnnotation>,
	val signature: String,
	val genericSignature: String?,
	val defaultTag: Any?,
	val modifiers: Int,
	val body: AstBody? = null,
	isStatic: Boolean = false,
	visibility: AstVisibility = AstVisibility.PUBLIC,
	val isNative: Boolean = false
	//val isOverriding: Boolean = overridingMethod != null,
) : AstMember(containingClass, name, type, isStatic, visibility, annotations) {

	val methodType: AstType.METHOD_TYPE = type
	val desc = methodType.desc
	val ref: AstMethodRef get() = AstMethodRef(containingClass.name, name, methodType)
	val dependencies by lazy { AstDependencyAnalyzer.analyze(body) }

	val getterField: String? by lazy { annotations.get(JTranscGetter::value) }
	val setterField: String? by lazy { annotations.get(JTranscSetter::value) }
	val nativeMethod: String? by lazy { annotations.get(JTranscMethod::value) }
	val isInline: Boolean by lazy { annotations.contains<JTranscInline>() }

	val isOverriding: Boolean get() {
		for (ancestor in containingClass.getAncestors(program)) {
			if (ancestor[ref.withoutClass] != null) return true
		}
		return false
	}

	val isImplementing: Boolean get() {
		for (i in containingClass.getAllInterfaces()) {
			if (i.getMethod(this.name, this.desc) != null) return true
		}
		return false
	}

	override fun toString(): String = "AstMethod(${containingClass.fqname}:$name:$desc)"
}

fun AstMethodRef.toEmptyMethod(program: AstProgram, isStatic: Boolean = false, visibility: AstVisibility = AstVisibility.PUBLIC): AstMethod {
	return AstMethod(
		program[this.containingClass],
		this.name,
		this.type,
		annotations = listOf(),
		defaultTag = null,
		signature = "()V",
		genericSignature = null,
		modifiers = 0,
		body = null,
		isStatic = isStatic,
		visibility = AstVisibility.PUBLIC
	)
}

