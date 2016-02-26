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

package com.jtransc.input

import com.jtransc.ast.*
import com.jtransc.ds.cast
import com.jtransc.ds.createPairs
import com.jtransc.ds.toHashMap
import com.jtransc.error.InvalidOperationException
import com.jtransc.util.recursiveExploration
import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.MergeVfs
import com.jtransc.vfs.SyncVfsFile
import com.jtransc.vfs.ZipVfs
import jtransc.annotation.JTranscKeep
import jtransc.annotation.JTranscReferenceClass
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import java.util.*

interface AnaMember {
	val project: AnaProject
	val clazz: AnaType
	val name: String
	val type: AstType
	val access: Int
	val references: Set<AstRef>
	val referencedBy: HashSet<AstRef>
	val keep: Boolean
	val ref: AstRef
}

val AnaMember.isStatic: Boolean get() = (access and Opcodes.ACC_STATIC) != 0

private fun String.normalizeType(): String {
	var str = this.trimEnd(';')
	while (str.startsWith("[L")) str = str.drop(2)
	while (str.startsWith("[")) str = str.drop(1)

	return if (str.length == 1) {
		(AstType.demangle(str) as AstType.Primitive).underlyingClass.fqname
	} else {
		str.replace('/', '.')
	}
}

fun MethodNode.getRefs(): Set<AstRef> {
	val out = hashSetOf<AstRef>()
	// Signature
	out.addAll(AstType.demangle(this.desc).getRefClasses())
	// Exceptions
	this.tryCatchBlocks.cast<TryCatchBlockNode>().forEach {
		if (it.type != null) {
			out.add(AstClassRef(it.type.normalizeType()))
		}
	}
	// Instructions

	for (ins in this.instructions.toArray()) {
		val i = ins
		when (i) {
			is TypeInsnNode -> out.add(AstClassRef(i.desc.normalizeType()))
			is MethodInsnNode -> {
				// Skip arrays
				if (!i.owner.startsWith("[")) {
					out.add(AstMethodRef(FqName(i.owner.normalizeType()), i.name, AstType.demangleMethod(i.desc)))
				}
			}
			is MultiANewArrayInsnNode -> out.add(AstClassRef(i.desc.normalizeType()))
			is FieldInsnNode -> out.add(AstFieldRef(FqName(i.owner.normalizeType()), i.name, AstType.demangle(i.desc)))
			else -> {
				//throw InvalidOperationException("Not handled: $i")
			}
		}
	}
	return out
}

fun FieldNode.getRefs(): Set<AstRef> {
	val out = hashSetOf<AstRef>()
	out.addAll(AstType.demangle(this.desc).getRefClasses())
	return out
}

//val KEEP_NAME = JTranscKeep::class.java.name.fqname
//val KEEP_NAME_INTERNAL = "L" + KEEP_NAME.internalFqname + ";"

class AnaMethod(override val clazz: AnaType, val node: MethodNode) : AnaMember {
	override val project: AnaProject get() = clazz.project
	val methodType = AstType.demangleMethod(node.desc)
	override val name: String = node.name
	override val type: AstType = methodType
	override val access: Int = node.access
	val isAbstract: Boolean get() = (node.access and Opcodes.ACC_ABSTRACT) != 0
	val isPartOfInterface: Boolean = clazz.isInterface
	override val ref by lazy { AstMethodRef(clazz.name, name, methodType) }
	val annotations by lazy { node.invisibleAnnotations?.filterIsInstance<AnnotationNode>()?.toAstAnnotations() ?: listOf() }
	override val keep by lazy { annotations.contains<JTranscKeep>() || node.name == "<clinit>" || (node.name == "values" && clazz.isEnum) }

	override val references by lazy {
		(listOf(clazz.ref) + node.getRefs()).toSet()
	}
	override val referencedBy = hashSetOf<AstRef>()
}

// @TODO: Check class delegation to see if we can avoid the duplicated code in AnaMethod and AnaField
class AnaField(override val clazz: AnaType, val node: FieldNode) : AnaMember {
	override val project: AnaProject get() = clazz.project
	override val name: String = node.name
	override val type: AstType = AstType.demangle(node.desc)
	override val access: Int = node.access
	override val ref by lazy { AstFieldRef(clazz.name, name, type) }
	//val annotations = node.invisibleAnnotations?.cast<AnnotationNode>() ?: listOf()
	val annotations by lazy { node.invisibleAnnotations?.filterIsInstance<AnnotationNode>()?.toAstAnnotations() ?: listOf() }
	override val keep by lazy { annotations.contains<JTranscKeep>() }

	override val references: Set<AstRef> by lazy {
		(listOf(clazz.ref) + node.getRefs()).toSet()
	}
	override val referencedBy = hashSetOf<AstRef>()
}

fun List<AnnotationNode>?.toAstAnnotations(): List<AstAnnotation> {
	return this?.filterNotNull()?.map {
		AstAnnotation(
			AstType.demangle(it.desc) as AstType.REF,
			it.values.createPairs().map { Pair(it.first as String, it.second) }.toMap()
		)
	} ?: listOf()
}

class AnaType(val project: AnaProject, val node: ClassNode) {
	val fqname = node.name.normalizeType()
	val name = FqName(fqname)
	val ref = AstClassRef(name)
	val isAbstract: Boolean = (node.access and Opcodes.ACC_ABSTRACT) != 0
	val isInterface: Boolean = (node.access and Opcodes.ACC_INTERFACE) != 0
	val isEnum: Boolean = (node.access and Opcodes.ACC_ENUM) != 0

	// Dependencies + members
	var parent = if (node.superName != null) project.getOrCreate(AstClassRef(node.superName.normalizeType())) else null
	val interfaces = node.interfaces.cast<String>().map { project.getOrCreate(AstClassRef(it.normalizeType())) }.toSet()
	val methods: HashMap<AstMethodWithoutClassRef, AnaMethod> = node.methods.cast<MethodNode>().map { AnaMethod(this, it) }.associateBy { AstMethodRef(name, it.name, it.methodType).withoutClass }.toHashMap()
	val fields: HashMap<AstFieldWithoutClassRef, AnaField> = node.fields.cast<FieldNode>().map { AnaField(this, it) }.associateBy { AstFieldRef(name, it.name, it.type).withoutClass }.toHashMap()
	val members: List<AnaMember> = methods.values + fields.values

	// Flattened ascendence
	val ascendancy = hashSetOf<AnaType>()
	val descendants = hashSetOf<AnaType>()

	val annotations by lazy { node.invisibleAnnotations?.filterIsInstance<AnnotationNode>()?.toAstAnnotations() ?: listOf() }

	companion object {
		fun createRelationship(ancestor: AnaType, descendant: AnaType) {
			ancestor.descendants += descendant
			ancestor.descendants += descendant.descendants

			descendant.ascendancy += ancestor
			descendant.ascendancy += ancestor.ascendancy
		}
	}

	init {
		val ancestors = (listOf(parent) + interfaces).filterNotNull().flatMap { listOf(it) + it.ascendancy }.toSet()
		for (ancestor in ancestors) createRelationship(ancestor, this)
	}

	fun getMethods(name: String) = methods.values.filter { it.name == name }
	fun getMethod(name: String, args: List<AstType>): AnaMethod? = methods.values.firstOrNull { it.name == name && it.methodType.argTypes == args }

	fun ensure(ref: AstMethodRef) = ensure(ref.withoutClass)

	fun _ensure(ref: AstMethodWithoutClassRef): AnaMethod? {
		if (ref !in methods) {
			var res = parent?._ensure(ref)
			if (res == null) {
				for (i in interfaces) {
					res = i._ensure(ref)
					if (res != null) break
				}
			}
			if (res != null) methods[ref] = res
		}
		return methods[ref]
	}

	fun ensure(ref: AstMethodWithoutClassRef): AnaMethod {
		val result = _ensure(ref)
		if (result == null) {
			println("Class: $fqname")
			for (method in methods) println("method: $method")
			for (field in fields) println("field: $field")
			throw InvalidOperationException("Can't resolve method $ref in ${this.fqname}")
		}
		return result
	}

	fun ensure(ref: AstFieldWithoutClassRef): AnaField {
		if (ref !in fields) {
			var res = parent?.ensure(ref)
			if (res == null) {
				throw InvalidOperationException("Can't resolve field $ref in ${this.fqname}")
			}
			fields[ref] = res
		}
		return fields[ref]!!
	}

	operator fun get(ref: AstMethodRef): AnaMethod? = methods[ref.withoutClass]
	operator fun get(desc: AstMethodWithoutClassRef): AnaMethod? = methods[desc]
	operator fun get(ref: AstFieldRef): AnaField? = ensure(ref.withoutClass)
}

class AnaProject(val resolver: SyncVfsFile) {
	constructor(classPaths: List<String>) : this(MergeVfs(classPaths.map { if (it.endsWith(".jar")) ZipVfs(it) else LocalVfs(it) })) {
		/*
		println("ClassPaths:")
		println(classPaths.joinToString("\n"))
		println("-------------")
		*/
	}

	private val classes = hashMapOf<String, AnaType>()

	fun getOrCreate(ref: AstClassRef): AnaType {
		val name = ref.fqname
		val path = ref.name.pathToClass
		if (name !in classes) {
			val clazz = AnaType(this, ClassNode(Opcodes.ASM5).apply {
				ClassReader(resolver[path].readBytes()).accept(this, ClassReader.EXPAND_FRAMES)
			})

			classes[name] = clazz
		}
		return classes[name]!!
	}

	operator fun get(ref: AstClassRef): AnaType = getOrCreate(ref)

	operator fun get(ref: String): AnaType = this[AstClassRef(ref)]
	operator fun get(ref: AstMethodRef): AnaMethod? = this[ref.classRef][ref]
	fun ensure(ref: AstMethodRef): AnaMethod = this[ref.classRef].ensure(ref)
	operator fun get(ref: AstFieldRef): AnaField = this[ref.classRef].ensure(ref.withoutClass)

	fun explore(vararg refs: AstRef) = explore(refs.toSet())

	fun exploreClasses(vararg refs: AstRef) = explore(refs.toSet(), exploreFullClasses = true)

	fun explore(refs: Set<AstRef>, exploreFullClasses: Boolean = false): Set<AstRef> {
		val result = recursiveExploration(
			initialItems = refs.filterNotNull().toSet(),
			extra = { ref ->
				when (ref) {
				// Propagate methods!
					is AstMethodRef -> {
						val method = this[ref]!!
						val relatedClasses = method.clazz.ascendancy + method.clazz.descendants
						listOf(ref) + relatedClasses.map { it[method.ref.withoutClass]?.ref }.filterNotNull()
					}
					else -> {
						listOf(ref)
					}
				}
			}, performExploration = { ref ->
			//println("Processing ref: $ref")
			when (ref) {
				is AstMethodRef -> {
					// Must reference all related methods in related types
					val method = this.ensure(ref)
					listOf(ref, method.clazz.ref) + method.references
				}
				is AstFieldRef -> {
					listOf(ref.classRef) + this[ref].references
				}
				is AstClassRef -> {
					val clazz = this[ref]
					val keepMembers = clazz.members.filter { it.keep }.map { it.ref }
					val fields = clazz.fields.values.map { it.ref }
					//val allMethods = clazz.methods.values.map { it.ref }
					val allMembers = clazz.members.map { it.ref }
					val list2 = listOf(ref, clazz.parent?.ref) + clazz.interfaces.map { it.ref }
					//if (keepMembers.isNotEmpty()) println("keepMembers: $keepMembers")

					val referencedClasses = clazz.annotations[JTranscReferenceClass::value]
					val extraReferencedClasses = referencedClasses?.map { AstClassRef(FqName(it)) } ?: listOf()

					//val isAbstract = clazz.isAbstract && !clazz.isInterface
					//val members2 = if (isAbstract) clazz.methods.values.filter { !it.isStatic && it.isAbstract }.map { it.ref } else listOf()
					val members2 = listOf<AstRef>()
					if (exploreFullClasses) {
						fields + list2 + keepMembers + members2 + extraReferencedClasses + allMembers
					} else {
						fields + list2 + keepMembers + members2 + extraReferencedClasses
					}
				}
				else -> throw InvalidOperationException()
			}
		}
		)

		//println("ANALYSYS RESULT:\n" + result.joinToString("\n"))

		return result
	}
}
