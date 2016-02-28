package com.jtransc.ast

import com.jtransc.error.InvalidOperationException
import com.jtransc.error.invalidOp
import com.jtransc.text.*
import java.io.Reader
import java.io.Serializable
import java.io.StringReader
import java.util.*

interface AstType {
	open class Primitive(underlyingClassStr: String) : AstType {
		val underlyingClass: FqName = underlyingClassStr.fqname
		val CLASSTYPE = REF(underlyingClassStr)
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

	/*
	object TYPECLASS {
		val VOID = REF("java.lang.Void")
		val BOOL = REF("java.lang.Boolean")
		val BYTE = REF("java.lang.Byte")
		val CHAR = REF("java.lang.Character")
		val SHORT = REF("java.lang.Short")
		val INT = REF("java.lang.Integer")
		val LONG = REF("java.lang.Long")
		val FLOAT = REF("java.lang.Float")
		val DOUBLE = REF("java.lang.Double")
	}
	*/

	data class REF(val name: FqName) : AstType {
		constructor(name: String) : this(FqName(name))

		init {
			if (fqname.contains(';') || fqname.contains(']')) {
				invalidOp("AstType.REF containing ; or ] :: $fqname")
			}
		}

		val fqname: String get() = name.fqname

		val classRef: AstClassRef by lazy { AstClassRef(name) }
	}

	data class ARRAY(val element: AstType) : AstType

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
		val argsPlusReturnVoidIsEmpty by lazy {
			if (argTypes.isEmpty()) {
				listOf(AstType.VOID, ret)
			} else {
				argTypes + listOf(ret)
			}
		}
		val withoutRetval: AstType.METHOD_TYPE get() = AstType.METHOD_TYPE(AstType.UNKNOWN, argTypes)

		override fun hashCode() = desc.hashCode();
		override fun equals(other: Any?) = Objects.equals(this.desc, (other as METHOD_TYPE?)?.desc)
	}

	companion object {
		val STRING = REF(FqName("java.lang.String"))
		val OBJECT = REF(FqName("java.lang.Object"))
		val CLASS = REF(FqName("java.lang.Class"))
		fun ARRAY(element: AstType, dimensions: Int): AstType {
			return if (dimensions == 1) {
				ARRAY(element)
			} else {
				ARRAY(ARRAY(element, dimensions - 1))
			}
		}

		fun REF_INT(internalName: String): AstType {
			if (internalName.startsWith("[")) {
				return demangle(internalName)
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

fun Iterable<AstType>.toArguments(): List<AstArgument> {
	return this.mapIndexed { i, v -> AstArgument(i, v) }
}

data class AstArgument(val index: Int, val type: AstType, val name: String = "p$index", val optional: Boolean = false) {
}

data class AstMethodDesc(val name: String, val type: AstType.METHOD_TYPE)

data class FqName(val fqname: String) : Serializable {
	constructor(packagePath: String, simpleName: String) : this("$packagePath.$simpleName".trim('.'))

	constructor(packageParts: List<String>, simpleName: String) : this("${packageParts.joinToString(".")}.$simpleName".trim('.'))

	constructor(parts: List<String>) : this(parts.joinToString(".").trim('.'))

	companion object {
		fun fromInternal(internalName: String): FqName {
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
	val pathToClass by lazy { "$internalFqname.class" }

	fun withPackagePath(packagePath: String) = FqName(packagePath, simpleName)
	fun withPackageParts(packageParts: List<String>) = FqName(packageParts, simpleName)
	fun withSimpleName(simpleName: String) = FqName(packagePath, simpleName)

	override fun toString() = "$fqname"

	fun append(s: String): FqName = FqName(this.fqname + s)
}

val String.fqname: FqName get() = FqName(this)
fun FqName.ref() = AstType.REF(this)

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
		else -> {
			throw NotImplementedError("Not implemented type '$typech'")
		}
	}
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

fun AstType.getRefTypes(): List<AstType> = this.getRefTypesFqName().map { AstType.REF(it) }

fun AstType.getRefTypesFqName(): List<FqName> = when (this) {
	is AstType.REF -> listOf(this.name)
	is AstType.ARRAY -> this.element.getRefTypesFqName()
	is AstType.METHOD_TYPE -> this.argTypes.flatMap { it.getRefTypesFqName() } + this.ret.getRefTypesFqName()
	is AstType.GENERIC -> this.type.getRefTypesFqName()
	else -> listOf()
}
