package com.jtransc.ast

import com.jtransc.error.InvalidOperationException
import com.jtransc.error.invalidArgument
import com.jtransc.error.invalidOp
import com.jtransc.error.noImpl
import com.jtransc.injector.Singleton
import com.jtransc.lang.toBool
import com.jtransc.lang.toInt
import com.jtransc.text.StrReader
import com.jtransc.text.readUntil
import java.io.Serializable
import java.util.*

open class AstType {
	abstract class Primitive(
		underlyingClassStr: String, val ch: Char, val shortName: String, val byteSize: Int, val priority: Int,
		val subsets: Set<Primitive> = setOf()
	) : AstType() {
		//val underlyingClass: FqName = underlyingClassStr.fqname
		val CLASSTYPE = REF(underlyingClassStr)
		val chstring = "$ch"
		override fun hashCode() = ch.toInt()
		override fun equals(other: Any?): Boolean {
			if (other == null) return false;
			if (other !is Primitive) return false
			return Objects.equals(this.ch, (other as Primitive?)?.ch)
		}

		override fun toString() = shortName
		fun canHold(other: Primitive) = other in subsets
	}

	open class Reference : AstType()

	data class UNKNOWN(val reason: String) : Reference()

	object NULL : Reference()

	object VOID : Primitive("java.lang.Void", 'V', "void", 0, priority = 8, subsets = setOf())

	object BOOL : Primitive("java.lang.Boolean", 'Z', "bool", 1, priority = 7, subsets = setOf())

	object BYTE : Primitive("java.lang.Byte", 'B', "byte", 1, priority = 6, subsets = setOf(BOOL))

	object CHAR : Primitive("java.lang.Character", 'C', "char", 2, priority = 5, subsets = setOf(BOOL))

	object SHORT : Primitive("java.lang.Short", 'S', "short", 2, priority = 4, subsets = setOf(BOOL, BYTE))

	object INT : Primitive("java.lang.Integer", 'I', "int", 4, priority = 3, subsets = setOf(BOOL, BYTE, CHAR, SHORT))

	object LONG : Primitive("java.lang.Long", 'J', "long", 8, priority = 2, subsets = setOf(BOOL, BYTE, CHAR, SHORT, INT))

	object FLOAT : Primitive("java.lang.Float", 'F', "float", 4, priority = 1, subsets = setOf(BOOL, BYTE, CHAR, SHORT))

	object DOUBLE : Primitive("java.lang.Double", 'D', "double", 8, priority = 0, subsets = setOf(BOOL, BYTE, CHAR, SHORT, INT))

	data class REF(val name: FqName) : Reference(), AstRef {
		constructor(name: String) : this(FqName(name))

		init {
			if (fqname.contains(';') || fqname.contains(']')) {
				invalidOp("AstType.REF containing ; or ] :: $fqname")
			}
		}

		val fqname: String get() = name.fqname

		override fun hashCode(): Int = name.hashCode()
		override fun equals(other: Any?): Boolean {
			if (other == null || other !is REF) return false
			return Objects.equals(this.name, other.name)
		}

		override fun toString() = name.fqname
	}

	//object OBJECT : REF("java.lang.Object")

	//object NULL : REF("java.lang.Object")

	data class ARRAY(val element: AstType) : Reference() {
		override fun toString() = "$element[]"
	}

	data class COMMON(val elements: HashSet<AstType>) : AstType() {
		constructor(first: AstType) : this(HashSet()) {
			add(first)
		}

		val single: AstType? get() = if (elements.size == 1) elements.first() else null
		val singleOrInvalid: AstType get() = single ?: invalidArgument("Common type not resolved $elements")

		fun add(type: AstType) {
			if (type != this) {
				if (type is AstType.COMMON) {
					for (e in type.elements) add(e)
				} else {
					elements += type
				}
			}
		}

		override fun toString() = "COMMON($elements)"
	}

	data class MUTABLE(var ref: AstType) : AstType() {
		override fun toString() = "MUTABLE($ref)"
	}

	data class GENERIC(val type: AstType.REF, val suffixes: List<GENERIC_SUFFIX>, val dummy: Boolean) : Reference() {
		constructor(type: AstType.REF, params: List<AstType>) : this(type, listOf(GENERIC_SUFFIX(null, params)), true)

		val params0: List<AstType> get() = suffixes[0].params!!
	}

	data class GENERIC_SUFFIX(val id: String?, val params: List<AstType>?)

	data class TYPE_PARAMETER(val id: String) : AstType()

	object GENERIC_STAR : AstType()

	object GENERIC_ITEM : AstType()

	data class GENERIC_DESCRIPTOR(val element: AstType, val types: List<Pair<String, AstType>>) : AstType()

	data class GENERIC_LOWER_BOUND(val element: AstType) : AstType()
	data class GENERIC_UPPER_BOUND(val element: AstType) : AstType()

	data class METHOD(val ret: AstType, val args: List<AstArgument>, val dummy: Boolean, val paramTypes: List<Pair<String, AstType>> = listOf()) : AstType() {
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
		val withoutRetval: AstType.METHOD get() = AstType.METHOD(AstType.UNKNOWN("No retval"), argTypes, paramTypes)

		override fun hashCode() = desc.hashCode();
		override fun equals(other: Any?) = Objects.equals(this.desc, (other as METHOD?)?.desc)
		override fun toString() = ret.toString() + " (" + args.joinToString(", ") + ")"
	}

	companion object {
		val THROWABLE = AstType.REF("java.lang.Throwable")
		val STRING = AstType.REF("java.lang.String")
		val STRINGBUILDER = AstType.REF("java.lang.StringBuilder")
		val OBJECT = AstType.REF("java.lang.Object")
		val CLASS = AstType.REF("java.lang.Class")

		fun fromConstant(value: Any?): AstType = when (value) {
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
			is AstType.ARRAY -> AstType.CLASS
			is AstType.REF -> AstType.CLASS
			is AstType.METHOD -> AstType.CLASS // @TODO: Probably java.lang...MethodHandle or something like this!
			is AstMethodHandle -> AstType.CLASS // @TODO: Probably java.lang...MethodHandle or something like this!
			else -> invalidOp("Literal type: ${value.javaClass} : $value")
		}
	}
}

fun AstType.asArray(): AstType.ARRAY {
	if (this !is AstType.ARRAY) invalidOp("$this is not AstType.ARRAY")
	return this
}

fun ARRAY(type: AstType) = AstType.ARRAY(type)

@Singleton
class AstTypes {
	private val AstTypeDemangleCache = hashMapOf<String, AstType>()

	fun fromConstant(value: Any?): AstType = AstType.fromConstant(value)

	fun ARRAY(element: AstType, count: Int): AstType.ARRAY = if (count <= 1) AstType.ARRAY(element) else ARRAY(AstType.ARRAY(element), count - 1)

	fun REF_INT(internalName: String): AstType = if (internalName.startsWith("[")) demangle(internalName) else REF_INT2(internalName)

	fun REF_INT2(internalName: String): AstType.REF = AstType.REF(internalName.replace('/', '.'))
	fun REF_INT3(internalName: String?): AstType.REF? = if (internalName != null) REF_INT2(internalName) else null

	fun demangle(desc: String): AstType = AstTypeDemangleCache.getOrPut(desc) { this.readOne(StrReader(desc)) }
	fun demangleMethod(text: String): AstType.METHOD = demangle(text) as AstType.METHOD

	fun <T : AstType> build(init: AstTypeBuilder.() -> T): T = AstTypeBuilder.init()

	// @TODO: implement unification
	fun unify(a: AstType, b: AstType): AstType = a

	// http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-TypeVariableSignature
	fun readOne(reader: StrReader): AstType {
		if (reader.eof) return AstType.UNKNOWN("demangling eof")
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
			'[' -> AstType.ARRAY(readOne(reader))
			'*' -> AstType.GENERIC_STAR
			'-' -> AstType.GENERIC_LOWER_BOUND(readOne(reader))
			'+' -> AstType.GENERIC_UPPER_BOUND(readOne(reader))
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
						mainGenerics@ while (reader.hasMore) {
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
							mainGeneric@ while (reader.hasMore) {
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
				if (item is AstType.METHOD) {
					AstType.METHOD(item.ret, item.argTypes, types)
				} else {
					AstType.GENERIC_DESCRIPTOR(item, types)
				}
			}
			'(' -> {
				val args = arrayListOf<AstType>()
				while (reader.peekch() != ')') {
					args.add(readOne(reader))
				}
				assert(reader.readch() == ')')
				val ret = readOne(reader)
				AstType.METHOD(ret, args)
			}
			else -> {
				throw NotImplementedError("Not implemented type '$typech' @ $reader")
			}
		}
	}

}

fun _castLiteral(value: Int, to: AstType): Any = when (to) {
	AstType.BOOL -> value.toBool()
	AstType.BYTE -> value.toByte()
	AstType.CHAR -> value.toChar()
	AstType.SHORT -> value.toShort()
	AstType.INT -> value.toInt()
	AstType.LONG -> value.toLong()
	AstType.FLOAT -> value.toFloat()
	AstType.DOUBLE -> value.toDouble()
//is AstType.Reference -> null
	else -> invalidOp("Can't cast $value to $to")
}

fun <T> Class<T>.ref() = AstType.REF(this.name)

fun _castLiteral(value: Long, to: AstType): Any = when (to) {
	AstType.BOOL -> value.toBool()
	AstType.BYTE -> value.toByte()
	AstType.CHAR -> value.toChar()
	AstType.SHORT -> value.toShort()
	AstType.INT -> value.toInt()
	AstType.LONG -> value.toLong()
	AstType.FLOAT -> value.toFloat()
	AstType.DOUBLE -> value.toDouble()
	else -> invalidOp("Can't cast $value to $to")
}

fun _castLiteral(value: Float, to: AstType): Any = when (to) {
	AstType.BOOL -> value.toBool()
	AstType.BYTE -> value.toByte()
	AstType.CHAR -> value.toChar()
	AstType.SHORT -> value.toShort()
	AstType.INT -> value.toInt()
	AstType.LONG -> value.toLong()
	AstType.FLOAT -> value.toFloat()
	AstType.DOUBLE -> value.toDouble()
	else -> invalidOp("Can't cast $value to $to")
}

fun _castLiteral(value: Double, to: AstType): Any = when (to) {
	AstType.BOOL -> value.toBool()
	AstType.BYTE -> value.toByte()
	AstType.CHAR -> value.toChar()
	AstType.SHORT -> value.toShort()
	AstType.INT -> value.toInt()
	AstType.LONG -> value.toLong()
	AstType.FLOAT -> value.toFloat()
	AstType.DOUBLE -> value.toDouble()
	else -> invalidOp("Can't cast $value to $to")
}

fun Boolean.castTo(to: AstType) = _castLiteral(this.toInt(), to)
fun Byte.castTo(to: AstType) = _castLiteral(this.toInt(), to)
fun Char.castTo(to: AstType) = _castLiteral(this.toInt(), to)
fun Short.castTo(to: AstType) = _castLiteral(this.toInt(), to)
fun Int.castTo(to: AstType) = _castLiteral(this, to)
fun Long.castTo(to: AstType) = _castLiteral(this, to)
fun Float.castTo(to: AstType) = _castLiteral(this, to)
fun Double.castTo(to: AstType) = _castLiteral(this, to)

fun Iterable<AstType>.toArguments(): List<AstArgument> = this.mapIndexed { i, v -> AstArgument(i, v) }

fun AstType.getNull(): Any? = when (this) {
	is AstType.VOID -> null
	is AstType.BOOL -> false
	is AstType.INT -> 0.toInt()
	is AstType.SHORT -> 0.toShort()
	is AstType.CHAR -> 0.toChar()
	is AstType.BYTE -> 0.toByte()
	is AstType.LONG -> 0.toLong()
	is AstType.FLOAT -> 0f.toFloat()
	is AstType.DOUBLE -> 0.0.toDouble()
	is AstType.UNKNOWN -> {
		println("Referenced UNKNOWN")
		null
	}
	is AstType.REF, is AstType.ARRAY, is AstType.NULL -> null
	is AstType.COMMON -> this.elements.firstOrNull()?.getNull()
	else -> noImpl("Not supported type $this")
}

//fun AstType.getNullCompact(): Any? = when(this) {
//	is AstType.BOOL -> false
//	is AstType.INT, is AstType.SHORT, is AstType.CHAR, is AstType.BYTE -> 0
//	is AstType.LONG -> 0L
//	is AstType.FLOAT, is AstType.DOUBLE -> 0.0
//	is AstType.REF, is AstType.ARRAY, is AstType.NULL -> null
//	else -> noImpl("Not supported type $this")
//}

data class AstArgument(val index: Int, val type: AstType, override val name: String = "p$index", val optional: Boolean = false) : ArgumentRef {
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

	override fun toString() = fqname

	override fun hashCode(): Int = fqname.hashCode()

	override fun equals(other: Any?): Boolean = this.fqname == (other as? FqName)?.fqname

	fun append(s: String): FqName = FqName(this.fqname + s)
}

val Class<*>.fqname: FqName get() = FqName(this.name)
val String.fqname: FqName get() = FqName(this)
val FqName.ref: AstType.REF get() = AstType.REF(this)

fun AstType.isPrimitive() = (this is AstType.Primitive)
fun AstType.isNotPrimitive() = (this !is AstType.Primitive)
fun AstType.isReference() = (this is AstType.Reference)
fun AstType.isFloating() = (this == AstType.FLOAT) || (this == AstType.DOUBLE)
fun AstType.isLongOrDouble() = (this == AstType.LONG) || (this == AstType.DOUBLE)

object AstTypeBuilder {
	val STRING = AstType.STRING
	val OBJECT = AstType.OBJECT
	val CLASS = AstType.CLASS
	val METHOD = AstType.REF("java.lang.reflect.Method")
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
	fun ARRAY(element: AstType) = AstType.ARRAY(element)
	fun ARRAY(element: AstClass) = AstType.ARRAY(element.ref)
	//fun ARRAY(element: AstType, dimensions: Int = 1) = AstType.ARRAY(element, dimensions)
	//fun GENERIC(type: AstType.REF, params: List<AstType>) = AstType.GENERIC(type, params)
	fun METHOD(args: List<AstArgument>, ret: AstType) = AstType.METHOD(args, ret)

	fun METHOD(ret: AstType, vararg args: AstType) = AstType.METHOD(ret, args.toList())
}

fun <T : AstType> AstTypeBuild(init: AstTypeBuilder.() -> T): T = AstTypeBuilder.init()


val REF_DELIMITER = setOf(';', '<')
val REF_DELIMITER2 = setOf(';', '<', '.')
val T_DELIMITER = setOf(';')
val TYPE_DELIMITER = setOf(':', '>')

val AstType.elementType: AstType get() = when (this) {
	is AstType.ARRAY -> this.element
	is AstType.GENERIC -> this.suffixes[0].params!![0]
	is AstType.COMMON -> this.singleOrInvalid.elementType
	else -> invalidArgument("Type is not an array: $this")
}

fun AstType.mangleExt(retval: Boolean = true): String = when (this) {
	is AstType.COMMON -> {
		if (this.elements.size == 1) {
			this.elements.first().mangleExt(retval)
		} else {
			"COMMON(" + this.elements.map { it.mangleExt(retval) } + ")"
		}
	}
	is AstType.MUTABLE -> this.ref.mangleExt(retval)
	else -> mangle(retval)
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
	is AstType.METHOD -> {
		val param = if (this.paramTypes.isNotEmpty()) {
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
	is AstType.COMMON -> {
		if (this.elements.size == 1) {
			this.elements.first().mangle(retval)
		} else {
			throw RuntimeException("Can't mangle common with several types: ${this.elements}. Resolve COMMONS first.")
		}
	}
	is AstType.UNKNOWN -> {
		//throw RuntimeException("Can't mangle unknown")
		"!!UNKNOWN!!"
	}
	else -> throw RuntimeException("Don't know how to mangle $this")
}

fun AstType.getRefTypes(): List<AstType.REF> = this.getRefTypesFqName().map { AstType.REF(it) }

fun AstType.getRefTypesFqName(): List<FqName> = when (this) {
	is AstType.REF -> listOf(this.name)
	is AstType.ARRAY -> this.element.getRefTypesFqName()
	is AstType.METHOD -> {
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
	is AstType.COMMON -> this.elements.flatMap { it.getRefTypesFqName() }
	is AstType.MUTABLE -> this.ref.getRefTypesFqName()
	else -> noImpl("AstType.getRefTypesFqName: $this")
}
