package com.jtransc.ast

import com.jtransc.error.InvalidOperationException
import com.jtransc.error.invalidArgument
import com.jtransc.error.invalidOp
import com.jtransc.error.noImpl
import com.jtransc.text.*
import java.io.Reader
import java.io.Serializable
import java.io.StringReader
import java.util.*

interface AstType {
	abstract class Primitive(underlyingClassStr: String, val ch: Char, val shortName:String) : AstType {
		val underlyingClass: FqName = underlyingClassStr.fqname
		val CLASSTYPE = REF(underlyingClassStr)
		val chstring = "$ch"
		override fun hashCode() = ch.toInt()
		override fun equals(that: Any?) = Objects.equals(this.ch, (that as Primitive?)?.ch)
		override fun toString() = shortName
	}

	object UNKNOWN : AstType

	object NULL : AstType

	object VOID : Primitive("java.lang.Void", 'V', "void")

	object BOOL : Primitive("java.lang.Boolean", 'Z', "bool")

	object BYTE : Primitive("java.lang.Byte", 'B', "byte")

	object CHAR : Primitive("java.lang.Character", 'C', "char")

	object SHORT : Primitive("java.lang.Short", 'S', "short")

	object INT : Primitive("java.lang.Integer", 'I', "int")

	object LONG : Primitive("java.lang.Long", 'J', "long")

	object FLOAT : Primitive("java.lang.Float", 'F', "float")

	object DOUBLE : Primitive("java.lang.Double", 'D', "double")

	data class REF(val name: FqName) : AstType {
		constructor(name: String) : this(FqName(name))

		init {
			if (fqname.contains(';') || fqname.contains(']')) {
				invalidOp("AstType.REF containing ; or ] :: $fqname")
			}
		}

		val fqname: String get() = name.fqname

		val classRef: AstClassRef by lazy { AstClassRef(name) }

		override fun hashCode(): Int = name.hashCode()
		override fun equals(other: Any?): Boolean {
			if (other == null || other !is REF) return false
			return Objects.equals(this.name, other.name)
		}
		override fun toString() = name.fqname
	}

	//object OBJECT : REF("java.lang.Object")

	//object NULL : REF("java.lang.Object")

	data class ARRAY(val element: AstType) : AstType {
		override fun toString() = "$element[]"
	}

	data class GENERIC(val type: AstType.REF, val suffixes: List<GENERIC_SUFFIX>, val dummy:Boolean) : AstType {
		constructor(type: AstType.REF, params: List<AstType>) : this(type, listOf(GENERIC_SUFFIX(null, params)), true)
		val params0: List<AstType> get() = suffixes[0].params!!
	}

	data class GENERIC_SUFFIX(val id:String?, val params: List<AstType>?)

	data class TYPE_PARAMETER(val id:String) : AstType

	object GENERIC_STAR : AstType
	object GENERIC_ITEM : AstType

	data class GENERIC_DESCRIPTOR(val element:AstType, val types: List<Pair<String, AstType>>) : AstType

	data class GENERIC_LOWER_BOUND(val element: AstType) : AstType
	data class GENERIC_UPPER_BOUND(val element: AstType) : AstType

	//data class METHOD_TYPE(val args: List<AstArgument>, val ret: AstType) : AstType {
	//	constructor(ret: AstType, args: List<AstType>) : this(args.toArguments(), ret)
	//data class METHOD_TYPE(val ret: AstType, val argTypes: List<AstType>) : AstType {
	data class METHOD_TYPE(val ret: AstType, val args: List<AstArgument>, val dummy: Boolean, val paramTypes: List<Pair<String, AstType>> = listOf()) : AstType {
		val argCount: Int get() = argTypes.size

		constructor(ret: AstType, argTypes: List<AstType>, paramTypes: List<Pair<String, AstType>> = listOf()) : this(ret, argTypes.toArguments(), true, paramTypes)

		constructor(args: List<AstArgument>, ret: AstType, paramTypes: List<Pair<String, AstType>> = listOf()) : this(ret, args, true, paramTypes)

		constructor(ret: AstType, vararg args: AstArgument, paramTypes: List<Pair<String, AstType>> = listOf()) : this(ret, args.toList(), true, paramTypes)

		constructor(ret: AstType, vararg args: AstType, paramTypes: List<Pair<String, AstType>> = listOf()) : this(args.withIndex().map { AstArgument(it.index, it.value) }, ret, paramTypes)

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
		val withoutRetval: AstType.METHOD_TYPE get() = AstType.METHOD_TYPE(AstType.UNKNOWN, argTypes, paramTypes)

		override fun hashCode() = desc.hashCode();
		override fun equals(other: Any?) = Objects.equals(this.desc, (other as METHOD_TYPE?)?.desc)
		override fun toString() = ret.toString() + " (" + args.joinToString(", ") + ")"
	}

	companion object {
		val STRING = REF("java.lang.String")
		val OBJECT = REF("java.lang.Object")
		val CLASS = REF("java.lang.Class")

		fun ARRAY(element: AstType, count:Int): AstType.ARRAY = if (count <= 1) ARRAY(element) else ARRAY(ARRAY(element), count - 1)

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
	override fun toString() = "$type $name"
}

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
				invalidOp("Invalid classname '$fqname'")
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

	override fun hashCode(): Int = fqname.hashCode()

	override fun equals(that:Any?): Boolean = this.fqname == (that as? FqName)?.fqname

	fun append(s: String): FqName = FqName(this.fqname + s)
}

val String.fqname: FqName get() = FqName(this)
fun FqName.ref() = AstType.REF(this)

fun AstType.isFloating() = (this == AstType.FLOAT) || (this == AstType.DOUBLE)
fun AstType.isLongOrDouble() = (this == AstType.LONG) || (this == AstType.DOUBLE)

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
	//fun ARRAY(element: AstType, dimensions: Int = 1) = AstType.ARRAY(element, dimensions)
	//fun GENERIC(type: AstType.REF, params: List<AstType>) = AstType.GENERIC(type, params)
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
		AstTypeDemangleCache[desc] = this.readOne(StrReader(desc))
	}
	return AstTypeDemangleCache[desc]!!
}

val REF_DELIMITER = setOf(';', '<')
val REF_DELIMITER2 = setOf(';', '<', '.')
val T_DELIMITER = setOf(';')
val TYPE_DELIMITER = setOf(':', '>')

// http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-TypeVariableSignature
fun AstType.Companion.readOne(reader: StrReader): AstType {
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
		'*' -> AstType.GENERIC_STAR
		'-' -> AstType.GENERIC_LOWER_BOUND(AstType.readOne(reader))
		'+' -> AstType.GENERIC_UPPER_BOUND(AstType.readOne(reader))
		'T' -> {
			val id = reader.readUntil(T_DELIMITER, including = false, readDelimiter = true)
			AstType.TYPE_PARAMETER(id)
		}
		'L' -> {
			val base = reader.readUntil(REF_DELIMITER, including = false, readDelimiter = false)
			val delim = reader.readch()
			val ref = AstType.REF(base.replace('/', '.'))
			when (delim) {
				';' -> ref
				'<' -> {
					var index = 0
					val suffixes = arrayListOf<AstType.GENERIC_SUFFIX>();
					mainGenerics@while (reader.hasMore) {
						val id = if (reader.peekch() == '.') {
							reader.readch()
							val id = reader.readUntil(REF_DELIMITER, including = false, readDelimiter = false)
							val ch = reader.readch()
							when (ch) {
								'<' -> id
								';' -> {
									suffixes += AstType.GENERIC_SUFFIX(id, null)
									break@mainGenerics
								}
								else -> invalidOp("Expected > or ; but found $ch on reader $reader")
							}
						} else {
							null
						}
						val generic = arrayListOf<AstType>()
						mainGeneric@while (reader.hasMore) {
							val ch = reader.peekch()
							if (ch == '>') {
								reader.expect(">")
								when (reader.peekch()) {
									'.' -> break@mainGeneric
									';' -> break@mainGeneric
								}

								break
							} else {
								generic.add(readOne(reader))
							}
						}
						index++
						suffixes += AstType.GENERIC_SUFFIX(id, generic)
						if (reader.peekch() == ';') {
							reader.expect(";")
							break
						}
					}
					AstType.GENERIC(ref, suffixes, true)
				}
				else -> throw InvalidOperationException()
			}
		}
		// PARAMETRIZED TYPE
		'<' -> {
			val types = arrayListOf<Pair<String, AstType>>()
			while (reader.peekch() != '>') {
				val id = reader.readUntil(TYPE_DELIMITER, including = false, readDelimiter = false)
				reader.expect(":")
				if (reader.peekch() == ':') {
					reader.readch()
				}
				types += Pair(id, this.readOne(reader))
			}
			reader.expect(">")
			val item = this.readOne(reader)
			if (item is AstType.METHOD_TYPE) {
				AstType.METHOD_TYPE(item.ret, item.argTypes, types)
			} else {
				AstType.GENERIC_DESCRIPTOR(item, types)
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
			throw NotImplementedError("Not implemented type '$typech' @ $reader")
		}
	}
}

val AstType.elementType: AstType get() = when (this) {
	is AstType.ARRAY -> this.element
	is AstType.GENERIC -> this.suffixes[0].params!![0]
	else -> invalidArgument("Type is not an array: $this")
}

fun AstType.mangle(retval: Boolean = true): String = when (this) {
	is AstType.Primitive -> this.chstring
	is AstType.GENERIC -> "L" + type.name.internalFqname + this.suffixes.map {
		(it.id ?: "") + (if (it.params != null) "<" + it.params.map { it.mangle(retval) }.joinToString("") + ">" else "")
	}.joinToString(".") + ";"
	is AstType.REF -> "L" + name.internalFqname + ";"
	is AstType.ARRAY -> "[" + element.mangle(retval)
	is AstType.GENERIC_STAR -> "*"
	is AstType.GENERIC_LOWER_BOUND -> "-" + this.element.mangle(retval)
	is AstType.GENERIC_UPPER_BOUND -> "+" + this.element.mangle(retval)
	is AstType.TYPE_PARAMETER -> "T" + this.id + ";"
	is AstType.GENERIC_DESCRIPTOR -> {
		"<" + this.types.map { it.first + ":" + it.second.mangle(retval) }.joinToString("") + ">" + this.element.mangle(retval)
	}
	is AstType.METHOD_TYPE -> {
		var param = if (this.paramTypes.size > 0) {
			"<" + this.paramTypes.map { it.first + ":" + it.second.mangle(retval) }.joinToString("") + ">"
		} else {
			""
		}
		val args = "(" + argTypes.map { it.mangle(retval) }.joinToString("") + ")"
		if (retval) {
			param + args + ret.mangle(retval)
		} else {
			param + args
		}
	}
	is AstType.UNKNOWN -> throw RuntimeException("Can't mangle unknown")
	else -> throw RuntimeException("Don't know how to mangle $this")
}

fun AstType.getRefTypes(): List<AstType> = this.getRefTypesFqName().map { AstType.REF(it) }

fun AstType.getRefTypesFqName(): List<FqName> = when (this) {
	is AstType.REF -> listOf(this.name)
	is AstType.ARRAY -> this.element.getRefTypesFqName()
	is AstType.METHOD_TYPE -> {
		//if (this.paramTypes.isNotEmpty()) println(":::::::: " + this.paramTypes)
		this.argTypes.flatMap { it.getRefTypesFqName() } + this.ret.getRefTypesFqName() + this.paramTypes.flatMap { it.second.getRefTypesFqName() }
	}
	is AstType.GENERIC -> {
		this.type.getRefTypesFqName() + this.suffixes.flatMap { it.params ?: listOf() }.flatMap { it.getRefTypesFqName() }
	}
	is AstType.Primitive, is AstType.UNKNOWN, is AstType.NULL -> listOf()
	is AstType.TYPE_PARAMETER -> listOf()
	is AstType.GENERIC_STAR -> listOf()
	is AstType.GENERIC_LOWER_BOUND -> this.element.getRefTypesFqName()
	is AstType.GENERIC_UPPER_BOUND -> this.element.getRefTypesFqName()
	else -> noImpl("AstType.getRefTypesFqName: $this")
}
