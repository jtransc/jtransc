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
import com.jtransc.ds.flatMapInChunks
import com.jtransc.ds.flatMapInChunks2
import com.jtransc.error.InvalidOperationException
import com.jtransc.error.invalidOp
import com.jtransc.text.*
import com.jtransc.util.dependencySorter
import java.io.Reader
import java.io.Serializable
import java.io.StringReader
import java.util.*

data class FqName(val fqname: String) : Serializable {
	constructor(packagePath: String, simpleName: String) : this("$packagePath.$simpleName".trim('.'))

	constructor(packageParts: List<String>, simpleName: String) : this("${packageParts.joinToString(".")}.$simpleName".trim('.'))

	constructor(parts: List<String>) : this(parts.joinToString(".").trim('.'))

	companion object {
		fun fromInternal(internalName:String):FqName {
			return FqName(internalName.replace('/', '.'))
		}
	}

	init {
		//if (fqname.isNullOrBlank()) invalidOp("Fqname is empty!")

		if (!fqname.isEmpty()) {
			val f = fqname.first()
			//if (!f.isLetterOrUnderscore()) {
			if ((f == '(') || (f == '[') || ('/' in fqname)) {
				throw InvalidOperationException("Invalid classname '$fqname'")
			}
		}
	}

	val parts: List<String> get() = fqname.split('.')
	val packageParts: List<String> get() = packagePath.split('.')

	val packagePath by lazy { fqname.substringBeforeLast('.', "") }
	val simpleName by lazy { fqname.substringAfterLast('.') }
	val internalFqname by lazy { fqname.replace('.', '/') }

	fun withPackagePath(packagePath: String) = FqName(packagePath, simpleName)
	fun withPackageParts(packageParts: List<String>) = FqName(packageParts, simpleName)
	fun withSimpleName(simpleName: String) = FqName(packagePath, simpleName)

	override fun toString() = "$fqname"

	fun append(s: String): FqName = FqName(this.fqname + s)
}

val String.fqname: FqName get() = FqName(this)

interface AstType {
	open class Primitive(underlyingClassStr: String) : AstType {
		val underlyingClass: FqName = underlyingClassStr.fqname
	}

	object UNKNOWN : AstType

	object NULL : AstType

	object VOID : Primitive("java.lang.Void")

	object BOOL : Primitive("java.lang.Boolean")

	object BYTE : Primitive("java.lang.Byte")

	object CHAR : Primitive("java.lang.Character")

	object SHORT : Primitive("java.lang.Short")

	object INT : Primitive("java.lang.Integer")

	object LONG : Primitive("java.lang.Long")

	object FLOAT : Primitive("java.lang.Float")

	object DOUBLE : Primitive("java.lang.Double")

	data class REF(val name: FqName) : AstType {
		constructor(name: String) : this(FqName(name))

		val fqname: String get() = name.fqname

		val classRef: AstClassRef by lazy { AstClassRef(name) }
	}

	data class ARRAY(val element: AstType, val dimensions: Int = 1) : AstType

	data class GENERIC(val type: AstType.REF, val params: List<AstType>) : AstType

	//data class METHOD_TYPE(val args: List<AstArgument>, val ret: AstType) : AstType {
	//	constructor(ret: AstType, args: List<AstType>) : this(args.toArguments(), ret)
	//data class METHOD_TYPE(val ret: AstType, val argTypes: List<AstType>) : AstType {
	data class METHOD_TYPE(val ret: AstType, val args: List<AstArgument>, val dummy: Boolean) : AstType {
		val argCount: Int get() = argTypes.size
		constructor(ret: AstType, argTypes: List<AstType>) : this(ret, argTypes.toArguments(), true)
		constructor(args: List<AstArgument>, ret: AstType) : this(ret, args, true)
		constructor(ret: AstType, vararg args: AstArgument) : this(ret, args.toList(), true)
		constructor(ret: AstType, vararg args: AstType) : this(args.withIndex().map { AstArgument(it.index, it.value) }, ret)

		val argNames by lazy { args.map { it.name } }
		val argTypes by lazy { args.map { it.type } }
		val desc by lazy { this.mangle(true) }
		val desc2 by lazy { this.mangle(false) }
		val retVoid by lazy { ret == AstType.VOID }
		val argsPlusReturn by lazy { argTypes + listOf(ret) }
	}

	companion object {
		val STRING = REF(FqName("java.lang.String"))
		val OBJECT = REF(FqName("java.lang.Object"))
		val CLASS = REF(FqName("java.lang.Class"))
		fun REF_INT(internalName: String): AstType {
			if (internalName.startsWith("[")) {
				return ARRAY(REF_INT(internalName.substring(1)))
			} else {
				return REF_INT2(internalName)
			}
		}
		fun REF_INT2(internalName: String): AstType.REF {
			return REF(internalName.replace('/', '.'))
		}
		fun fromConstant(value: Any?): AstType = when (value) {
			null -> OBJECT
			is Int -> INT
			is Long -> LONG
			is Float -> FLOAT
			is Double -> DOUBLE
			is Byte -> BYTE
			is Short -> SHORT
			is Char -> CHAR
			is String -> STRING
			else -> invalidOp // @TODO: custom type!
		}
	}
}

fun AstType.isFloating() = (this == AstType.FLOAT) || (this == AstType.DOUBLE)

object AstTypeBuilder {
	val UNKNOWN = AstType.UNKNOWN
	val STRING = AstType.STRING
	val OBJECT = AstType.OBJECT
	val NULL = AstType.NULL
	val VOID = AstType.VOID
	val BOOL = AstType.BOOL
	val BYTE = AstType.BYTE
	val CHAR = AstType.CHAR
	val SHORT = AstType.SHORT
	val INT = AstType.INT
	val LONG = AstType.LONG
	val FLOAT = AstType.FLOAT
	val DOUBLE = AstType.DOUBLE
	fun REF(name: FqName) = AstType.REF(name)
	fun ARRAY(element: AstType, dimensions: Int = 1) = AstType.ARRAY(element, dimensions)
	fun GENERIC(type: AstType.REF, params: List<AstType>) = AstType.GENERIC(type, params)
	fun METHOD(args: List<AstArgument>, ret: AstType) = AstType.METHOD_TYPE(args, ret)
	fun METHOD(ret: AstType, vararg args: AstType) = AstType.METHOD_TYPE(ret, args.toList())
}

fun <T : AstType> AstTypeBuild(init: AstTypeBuilder.() -> T): T = AstTypeBuilder.init()

fun AstType.Companion.demangleMethod(text: String): AstType.METHOD_TYPE {
	return AstType.demangle(text) as AstType.METHOD_TYPE
}

val AstTypeDemangleCache = hashMapOf<String, AstType>()

fun AstType.Companion.demangle(desc: String): AstType {
	if (desc !in AstTypeDemangleCache) {
		AstTypeDemangleCache[desc] = this.readOne(StringReader(desc))
	}
	return AstTypeDemangleCache[desc]!!
}

val REF_DELIMITER = setOf(';', '<')

fun AstType.Companion.readOne(reader: Reader): AstType {
	if (reader.eof) return AstType.UNKNOWN
	val typech = reader.readch()
	return when (typech) {
		'V' -> AstType.VOID
		'Z' -> AstType.BOOL
		'B' -> AstType.BYTE
		'C' -> AstType.CHAR
		'S' -> AstType.SHORT
		'D' -> AstType.DOUBLE
		'F' -> AstType.FLOAT
		'I' -> AstType.INT
		'J' -> AstType.LONG
		'[' -> AstType.ARRAY(AstType.readOne(reader))
		'L' -> {
			val base = reader.readUntil(REF_DELIMITER, including = false, readDelimiter = false)
			val delim = reader.readch()
			val ref = AstType.REF(base.replace('/', '.'))
			when (delim) {
				';' -> ref
				'<' -> {
					val generic = arrayListOf<AstType>()
					while (reader.hasMore) {
						val ch = reader.peekch()
						if (ch == '>') {
							reader.expect(">;")
							break
						} else {
							generic.add(readOne(reader))
						}
					}
					AstType.GENERIC(ref, generic)
				}
				else -> throw InvalidOperationException()
			}
		}
		'(' -> {
			val args = arrayListOf<AstType>()
			while (reader.peekch() != ')') {
				args.add(AstType.readOne(reader))
			}
			assert(reader.readch() == ')')
			val ret = AstType.readOne(reader)
			AstType.METHOD_TYPE(ret, args)
		}
		else -> throw NotImplementedError("Not implemented type '$typech'")
	}
}

interface AstRef
interface AstMemberRef : AstRef {
	val classRef: AstClassRef
	val containingClass: FqName
	val name: String
	val memberType: AstType
}

data class AstClassRef(val name: FqName) : AstRef {
	constructor(name: String) : this(FqName(name))

	val fqname: String get() = name.fqname

	val type: AstType get() = AstType.REF(name)
}

data class AstFieldRef(override val containingClass: FqName, override val name: String, val type: AstType, val isStatic: Boolean? = null) : AstMemberRef {
	override val classRef: AstClassRef by lazy { AstClassRef(containingClass) }
	override val memberType: AstType = type
	val containingTypeRef = AstType.REF(containingClass)
	override fun toString() = "AstFieldRef(${containingClass.fqname},$name,${type.mangle()})"
}

data class AstMethodRef(override val containingClass: FqName, override val name: String, val type: AstType.METHOD_TYPE, val isStatic:Boolean? = null) : AstMemberRef {
	override val classRef: AstClassRef by lazy { AstClassRef(containingClass) }
	override val memberType: AstType = type
	val fid: String get() = "${containingClass.fqname}:$name:$desc"
	val fidWildcard: String get() = "${containingClass.fqname}:$name:*"
	val desc by lazy { type.desc }
	val descWithoutRetval by lazy { type.desc2 }

	val allClassRefs: List<AstClassRef> by lazy { type.getRefClasses() + classRef }

	override fun toString() = "AstMethodRef(${containingClass.fqname},$name,${type.desc})"
}

fun FqName.ref() = AstType.REF(this)

data class AstMethodDesc(val name: String, val args: List<AstType>)

val AstMethodRef.methodDesc: AstMethodDesc get() = AstMethodDesc(this.name, this.type.argTypes)


val AstMethodRef.withoutRetval: AstMethodRef get() {
	return if (this.type.ret == AstType.UNKNOWN) this else AstMethodRef(containingClass, name, type.withoutRetval)
}
val AstType.METHOD_TYPE.withoutRetval: AstType.METHOD_TYPE get() = AstType.METHOD_TYPE(AstType.UNKNOWN, argTypes)

fun AstType.getRefClasses(): List<AstClassRef> = this.getRefTypesFqName().map { AstClassRef(it) }
fun AstType.getRefTypes(): List<AstType> = this.getRefTypesFqName().map { AstType.REF(it) }

fun AstType.getRefTypesFqName(): List<FqName> = when (this) {
	is AstType.REF -> listOf(this.name)
	is AstType.ARRAY -> this.element.getRefTypesFqName()
	is AstType.METHOD_TYPE -> this.argTypes.flatMap { it.getRefTypesFqName() } + this.ret.getRefTypesFqName()
	is AstType.GENERIC -> this.type.getRefTypesFqName()
	else -> listOf()
}

fun Iterable<AstType>.toArguments(): List<AstArgument> {
	return this.mapIndexed { i, v -> AstArgument(i, v) }
}

data class AstArgument(val index: Int, val type: AstType, val name: String = "p$index", val optional: Boolean = false) {
}

val AstType.elementType: AstType get() = when (this) {
	is AstType.ARRAY -> this.element
	is AstType.GENERIC -> this.params[0]
	else -> AstType.UNKNOWN
}

fun AstType.mangle(retval: Boolean = true): String = when (this) {
	is AstType.VOID -> "V"
	is AstType.BOOL -> "Z"
	is AstType.BYTE -> "B"
	is AstType.CHAR -> "C"
	is AstType.SHORT -> "S"
	is AstType.DOUBLE -> "D"
	is AstType.FLOAT -> "F"
	is AstType.INT -> "I"
	is AstType.LONG -> "J"
	is AstType.GENERIC -> "L" + type.name.internalFqname + "<" + this.params.map { it.mangle(retval) }.joinToString("") + ">;"
	is AstType.REF -> "L" + name.internalFqname + ";"
	is AstType.ARRAY -> "[" + element.mangle(retval)
	is AstType.METHOD_TYPE -> {
		val args = "(" + argTypes.map { it.mangle(retval) }.joinToString("") + ")"
		if (retval) {
			args + ret.mangle(retval)
		} else {
			args
		}
	}
	is AstType.UNKNOWN -> throw RuntimeException("Can't mangle unknown")
	else -> throw RuntimeException("Don't know how to mangle $this")
}

enum class AstBinop(val symbol: String, val str: String) {
	ADD("+", "add"), SUB("-", "sub"), MUL("*", "mul"), DIV("/", "div"), REM("%", "rem"),
	AND("&", "and"), OR("|", "or"), XOR("^", "xor"),
	SHL("<<", "shl"), SHR(">>", "shr"), USHR(">>>", "ushr"),
	EQ("==", "eq"), NE("!=", "ne"), GE(">=", "ge"), LE("<=", "le"), LT("<", "lt"), GT(">", "gt"),
	LCMP("lcmp", "lcmp"), CMP("cmp", "cmp"), CMPL("cmpl", "cml"), CMPG("cmpg", "cmpg");

	companion object {
		//val operators = values.flatMap { listOf(Pair(it.symbol, it), Pair(it.str, it)) }.toMap()
	}
}

enum class AstUnop(val symbol: String, val str: String) {
	NEG("-", "neg"),
	NOT("!", "not"),
	INV("~", "inv");

	companion object {
		//val operators = values.flatMap { listOf(Pair(it.symbol, it), Pair(it.str, it)) }.toMap()
	}
}

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
	var orientation: AstBuildSettings.Orientation = AstBuildSettings.Orientation.AUTO,
	var extraRefs: List<AstRef> = listOf()
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
		val lowName:String = name.toLowerCase()
		companion object {
			fun fromString(str:String):Orientation = when (str.toLowerCase()) {
				"portrait" -> PORTRAIT
				"landscape" -> LANDSCAPE
				"auto" -> AUTO
				else -> AUTO
			}
		}
	}
}

class AstProgram(
	val entrypoint: FqName,
	val classes: List<AstClass>
) {
	private val classesByFqname = classes.associateBy { it.name.fqname }

	operator fun contains(name: FqName) = name.fqname in classesByFqname
	//operator fun get(name: FqName) = classesByFqname[name.fqname] ?: throw RuntimeException("AstProgram. Can't find class '$name'")
	operator fun get(name: FqName): AstClass {
		val result = classesByFqname[name.fqname]
		if (result == null) {
			println("AstProgram. Can't find class '$name'")
			throw RuntimeException("AstProgram. Can't find class '$name'")
		} else {
			return result
		}
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

	fun getInterfaces(clazz: AstClass): List<AstClass> {
		return clazz.implementing.map { this[it] }
	}

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

data class AstAnnotation(
	val type: AstType.REF,
	val elements: Map<String, Any?>
)

class AstClass(
	val name: FqName,
	val implCode: String?,
	val modifiers: Int,
	val nativeName: String? = null,
	val annotations: List<AstAnnotation>,
	val classType: AstClassType = AstClassType.CLASS,
	val visibility: AstVisibility = AstVisibility.PUBLIC,
	val extending: FqName? = null,
	val implementing: List<FqName> = listOf(),
	val fields: List<AstField> = listOf(),
	val methods: List<AstMethod> = listOf()
) {
	//val dependencies: AstReferences = AstReferences()
	val isInterface: Boolean = classType == AstClassType.INTERFACE
	val fqname = name.fqname
	val isNative = (nativeName != null)
	val methodsByName: Map<String, List<AstMethod>> = methods.groupBy { it.name }
	val fieldsByName: Map<String, AstField> = fields.associateBy { it.name }

	val classAndFieldAndMethodAnnotations by lazy {
		annotations + methods.flatMap { it.annotations } + fields.flatMap { it.annotations }
	}

	fun getMethods(name: String): List<AstMethod> = methodsByName[name]!!
	fun getMethod(name: String, desc: String): AstMethod? = methodsByName[name]?.firstOrNull { it.desc == desc }
	fun getMethodSure(name: String, desc: String): AstMethod {
		return getMethod(name, desc) ?: throw InvalidOperationException("Can't find method ${this.name}:$name:$desc")
	}

	operator fun get(ref: AstMethodRef) = getMethodSure(ref.name, ref.desc)
	operator fun get(ref: AstFieldRef) = fieldsByName[ref.name]!!

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

	fun getThisAndAncestors(program: AstProgram):List<AstClass> {
		if (extending == null) {
			return listOf(this)
		} else {
			return listOf(this) + program[extending].getThisAndAncestors(program)
		}
	}
	fun getAncestors(program: AstProgram):List<AstClass> {
		return getThisAndAncestors(program).drop(1)
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

data class AstDependencies(val dependencies: HashSet<AstRef>)

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
	val containingClass: FqName,
	val name: String,
	val type: AstType,
	val isStatic: Boolean = false,
	val visibility: AstVisibility = AstVisibility.PUBLIC
) {
	fun getContainingClass2(program: AstProgram) = program[containingClass]
}

class AstField(
	containingClass: FqName,
	name: String,
	type: AstType,
	val modifiers: Int,
	val descriptor: String,
	val annotations: List<AstAnnotation>,
	val genericSignature: String?,
	isStatic: Boolean = false,
	val isFinal: Boolean = false,
	visibility: AstVisibility = AstVisibility.PUBLIC,
	val hasConstantValue: Boolean = false,
	val constantValue: Any? = null
) : AstMember(containingClass, name, type, isStatic, visibility)

val AstField.ref: AstFieldRef get() = AstFieldRef(this.containingClass, this.name, this.type)

class AstMethod(
	containingClass: FqName,
	name: String,
	type: AstType.METHOD_TYPE,
	val annotations: List<AstAnnotation>,
	val isExtraAdded: Boolean,
	val signature: String,
	val genericSignature: String?,
	val defaultTag: Any?,
	val modifiers: Int,
	val body: AstBody? = null,
	isStatic: Boolean = false,
	visibility: AstVisibility = AstVisibility.PUBLIC,
	val getterField: String? = null,
	val setterField: String? = null,
	val nativeMethod: String? = null,
	val nativeMethodBody: Array<String>? = null,
	val overridingMethod: AstMethodRef? = null,
	val isImplementing: Boolean = false,
	val isNative: Boolean = false,
	val isInline: Boolean = false,
	val isOverriding: Boolean = overridingMethod != null
) : AstMember(containingClass, name, type, isStatic, visibility) {
	val methodType: AstType.METHOD_TYPE = type
	val desc = methodType.desc
	val ref: AstMethodRef get() = AstMethodRef(containingClass, name, methodType)
	val dependencies by lazy { AstDependencyAnalyzer.analyze(body) }

	private val nativeMethodBody2 = nativeMethodBody?.toList() ?: listOf()
	private val nativeBodies = nativeMethodBody2.flatMapInChunks2(2) {
		listOf(Pair(it[0], it[1]))
	}.toMap()

	fun getNativeBody(lang:String):String? = nativeBodies[lang]

	override fun toString(): String = "AstMethod(${containingClass.fqname}:$name:$desc)"
}

fun AstMethod.isAsync(astProgram: AstProgram): Boolean =
	name.startsWith("async")
		|| name.startsWith("access\$async")
		|| !dependencies.methods.filter { astProgram.get(it).isAsync(astProgram) }.isEmpty()

val AstFieldRef.withoutClass: AstFieldWithoutClassRef get() = AstFieldWithoutClassRef(this.name, this.type)
val AstMethodRef.withoutClass: AstMethodWithoutClassRef get() = AstMethodWithoutClassRef(this.name, this.type)
fun AstMethodRef.withClass(other: AstClassRef) = AstMethodRef(other.name, this.name, this.type)
fun AstMethodWithoutClassRef.withClass(containingClass: FqName): AstMethodRef = AstMethodRef(containingClass, this.name, this.type)
fun AstMethodRef.toEmptyMethod(isStatic: Boolean = false, visibility: AstVisibility = AstVisibility.PUBLIC, isOverriding: Boolean = false): AstMethod {
	return AstMethod(
		this.containingClass,
		this.name,
		this.type,
		annotations = listOf(),
		defaultTag = null,
		signature = "()V",
		genericSignature = null,
		modifiers = 0,
		body = null,
		isStatic = isStatic,
		visibility = AstVisibility.PUBLIC,
		isOverriding = isOverriding,
		isExtraAdded = true
	)
}

fun AstMethodRef.isAsync(astProgram: AstProgram): Boolean = astProgram.get(this).isAsync(astProgram)

data class AstFieldWithoutClassRef(val name: String, val type: AstType)

data class AstMethodWithoutClassRef(val name: String, val type: AstType.METHOD_TYPE) {
	val fid2: String get() = "$name:${type.mangle()}"
	val fid2Wildcard: String get() = "$name:*"
	val desc = type.desc
	val descWithoutRetval = type.desc2

	override fun toString() = "AstMethodWithoutClassRef($name,${type.desc})"
}

data class AstLocal(val index: Int, val name: String, val type: AstType) {
	override fun toString() = "AstLocal:$name:$type"
}

val AstLocal.expr: AstExpr.LOCAL get() = AstExpr.LOCAL(this)

data class AstBody(
	val stm: AstStm,
	val locals: List<AstLocal>,
	val traps: List<AstTrap>
)

data class AstTrap(val start: AstLabel, val end: AstLabel, val handler: AstLabel, val exception: AstType.REF)

data class AstLabel(val name: String)

class Box<T>(var value: T)

val Iterable<AstStm>.stms: AstStm get() = AstStm.STMS(this.toList())

interface AstStm {
	data class STMS(val stms: List<AstStm>) : AstStm {
		constructor(vararg stms: AstStm) : this(stms.toList())
	}

	data class NOP(val dummy: Any? = null) : AstStm
	data class STM_EXPR(val expr: AstExpr) : AstStm
	data class SET(val local: AstLocal, val expr: AstExpr) : AstStm
	data class SET_ARRAY(val local: AstLocal, val index: AstExpr, val expr: AstExpr) : AstStm
	data class SET_FIELD_STATIC(val clazz: AstType.REF, val field: AstFieldRef, val expr: AstExpr, val isInterface: Boolean) : AstStm
	data class SET_FIELD_INSTANCE(val left: AstExpr, val field: AstFieldRef, val expr: AstExpr) : AstStm
	data class SET_NEW_WITH_CONSTRUCTOR(val local: AstLocal, val target: AstType.REF, val method: AstMethodRef, val args: List<AstExpr>) : AstStm

	data class IF(val cond: AstExpr, val strue: AstStm, val sfalse: AstStm? = null) : AstStm
	data class WHILE(val cond: AstExpr, val iter: AstStm) : AstStm
	data class RETURN(val retval: AstExpr?) : AstStm
	data class THROW(val value: AstExpr) : AstStm

	object RETHROW : AstStm

	//data class TRY_CATCH(val trystm: AstStm, val catches: List<Pair<AstType, AstStm>>) : AstStm
	data class TRY_CATCH(val trystm: AstStm, val catch: AstStm) : AstStm

	class BREAK() : AstStm
	class CONTINUE() : AstStm

	// SwitchFeature
	data class SWITCH(val subject: AstExpr, val default: AstStm, val cases: List<Pair<Int, AstStm>>) : AstStm

	// GotoFeature

	data class STM_LABEL(val label: AstLabel) : AstStm
	data class IF_GOTO(val cond: AstExpr, val label: AstLabel) : AstStm
	data class SWITCH_GOTO(val subject: AstExpr, val default: AstLabel, val cases: List<Pair<Int, AstLabel>>) : AstStm
	data class GOTO(val label: AstLabel) : AstStm

	data class MONITOR_ENTER(val expr: AstExpr) : AstStm
	data class MONITOR_EXIT(val expr: AstExpr) : AstStm
}

val Any?.lit: AstExpr get() = AstExpr.LITERAL(this)

interface AstExpr {
	open val type: AstType

	interface ImmutableRef : AstExpr
	interface LValueExpr : AstExpr

	data class THIS(val ref: FqName) : LValueExpr {
		override val type: AstType = AstType.REF(ref)
	}

	data class CLASS_CONSTANT(val classType: AstType) : AstExpr {
		override val type: AstType = AstType.GENERIC(AstType.REF("java.lang.Class"), listOf(classType))
	}

	data class LITERAL(val value: Any?) : AstExpr {
		override val type: AstType = when (value) {
			null -> AstType.NULL
			is Boolean -> AstType.BOOL
			is Byte -> AstType.BYTE
			is Char -> AstType.CHAR
			is Short -> AstType.SHORT
			is Int -> AstType.INT
			is Long -> AstType.LONG
			is Float -> AstType.FLOAT
			is Double -> AstType.DOUBLE
			is String -> AstType.STRING
			else -> throw NotImplementedError("Literal type: $value")
		}
	}

	data class LOCAL(val local: AstLocal) : LValueExpr {
		override val type = local.type
	}

	data class PARAM(val argument: AstArgument) : LValueExpr {
		override val type = argument.type
	}

	data class CAUGHT_EXCEPTION(override val type: AstType = AstType.OBJECT) : AstExpr
	data class BINOP(override val type: AstType, val left: AstExpr, val op: AstBinop, val right: AstExpr) : AstExpr

	data class UNOP(val op: AstUnop, val right: AstExpr) : AstExpr {
		override val type = right.type
	}

	interface CALL_BASE : AstExpr {
		//override val type = method.type.ret
		val method: AstMethodRef
		val args: List<AstExpr>
		val isSpecial: Boolean
	}

	data class CALL_INSTANCE(val obj: AstExpr, override val method: AstMethodRef, override val args: List<AstExpr>, override val isSpecial: Boolean = false) : CALL_BASE {
		override val type = method.type.ret
	}

	data class CALL_SUPER(val obj: AstExpr, val target: FqName, override val method: AstMethodRef, override val args: List<AstExpr>, override val isSpecial: Boolean = false) : CALL_BASE {
		override val type = method.type.ret
	}

	data class CALL_STATIC(val clazz: AstType.REF, override val method: AstMethodRef, override val args: List<AstExpr>, override val isSpecial: Boolean = false) : CALL_BASE {
		override val type = method.type.ret
	}

	data class ARRAY_LENGTH(val array: AstExpr) : AstExpr {
		override val type = AstType.INT
	}

	data class ARRAY_ACCESS(val array: AstExpr, val index: AstExpr) : LValueExpr {
		override val type = array.type.elementType
	}

	data class INSTANCE_FIELD_ACCESS(val expr: AstExpr, val field: AstFieldRef, override val type: AstType) : LValueExpr
	data class STATIC_FIELD_ACCESS(val clazzName: AstType.REF, val field: AstFieldRef, override val type: AstType, val isInterface: Boolean) : LValueExpr

	data class INSTANCE_OF(val expr: AstExpr, val checkType: AstType) : AstExpr {
		override val type = AstType.BOOL
	}

	data class CAST(val from: AstType, val to: AstType, val expr: AstExpr) : AstExpr {
		constructor(to: AstType, expr: AstExpr) : this(expr.type, to, expr)

		override val type = to
	}

	data class NEW(val target: AstType.REF) : AstExpr {
		override val type = target
	}

	data class NEW_WITH_CONSTRUCTOR(val target: AstType.REF, val method: AstMethodRef, val args: List<AstExpr>) : AstExpr {
		override val type = target
	}

	data class NEW_ARRAY(val element: AstType, val counts: List<AstExpr>) : AstExpr {
		override val type = AstType.ARRAY(element, counts.size)
	}

	infix fun ge(that: AstExpr) = AstExpr.BINOP(AstType.BOOL, this, AstBinop.GE, that)
	infix fun le(that: AstExpr) = AstExpr.BINOP(AstType.BOOL, this, AstBinop.LE, that)
	infix fun and(that: AstExpr) = AstExpr.BINOP(this.type, this, AstBinop.AND, that)
	infix fun instanceof(that: AstType) = AstExpr.INSTANCE_OF(this, that)
}

open class AstTransformer {
	open fun visit(type: AstType) {
	}

	open fun transform(body: AstBody): AstBody {
		for (local in body.locals) visit(local.type)
		return AstBody(
			stm = transform(body.stm),
			locals = body.locals,
			traps = body.traps
		)
	}

	open fun transform(stm: AstStm): AstStm = when (stm) {
		is AstStm.STMS -> transform(stm)
		else -> throw NotImplementedError("Unhandled statement $stm")
	}

	open fun transform(stm: AstStm.STMS): AstStm = AstStm.STMS(stm.stms.map { transform(stm) })

	open fun transform(expr: AstExpr): AstExpr = when (expr) {
	//else -> expr
		else -> throw NotImplementedError("Unhandled expression $expr")
	}
}