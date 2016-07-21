package com.jtransc.gen.common

import com.jtransc.ast.*
import com.jtransc.text.escape
import com.jtransc.text.isLetterDigitOrUnderscore
import com.jtransc.text.quote
import kotlin.reflect.KMutableProperty1

abstract class CommonNames(val program: AstResolver) {
	abstract fun buildTemplateClass(clazz: FqName): String
	abstract fun buildTemplateClass(clazz: AstClass): String
	fun buildField(field: AstField, static: Boolean): String {
		return if (static) buildStaticField(field) else getNativeName(field)
	}

	abstract fun buildMethod(method: AstMethod, static: Boolean): String
	abstract fun buildStaticInit(clazz: AstClass): String
	abstract fun buildConstructor(method: AstMethod): String

	open fun buildInstanceField(expr: String, field: AstField): String = expr + buildAccessName(field)
	open fun buildStaticField(field: AstField): String = getNativeNameForFields(field.ref.containingTypeRef.name) + buildAccessName(field)

	fun buildStaticField(field: FieldRef): String = buildStaticField(program[field.ref]!!)
	fun buildInstanceField(expr: String, field: FieldRef): String = buildInstanceField(expr, program[field.ref]!!)

	open fun buildAccessName(field: AstField): String = buildAccessName(getNativeName(field))
	open fun buildAccessName(name: String): String = ".$name"

	val normalizeNameCache = hashMapOf<String, String>()

	fun normalizeName(name: String): String {
		if (name.isNullOrEmpty()) return ""
		if (name !in normalizeNameCache) {
			val chars = name.toCharArray()
			for (i in chars.indices) {
				var c = chars[i]
				if (!c.isLetterDigitOrUnderscore() || c == '$') c = '_'
				chars[i] = c
			}
			if (chars[0].isDigit()) chars[0] = '_'
			normalizeNameCache[name] = String(chars)
		}
		return normalizeNameCache[name]!!
	}

	open fun getNativeName(local: LocalParamRef): String = normalizeName(local.name)
	open fun getNativeName(field: FieldRef): String = normalizeName(field.ref.name)
	open fun getNativeName(method: MethodRef): String = normalizeName(method.ref.name)
	open fun getNativeName(clazz: FqName): String = clazz.fqname
	inline fun <reified T : Any> nativeName(): String = getNativeName(T::class.java.name.fqname)

	fun getNativeFieldName(clazz: Class<*>, name: String): String {
		return getNativeName(program[clazz.name.fqname]!!.fieldsByName[name]!!)
	}

	inline fun <reified T : Any, R> getNativeFieldName(prop: KMutableProperty1<T, R>): String {
		return getNativeFieldName(T::class.java, prop.name)
	}

	open fun getNativeNameForMethods(clazz: FqName): String = getNativeName(clazz)
	open fun getNativeNameForFields(clazz: FqName): String = getNativeName(clazz)

	open val NullType = FqName("Dynamic")
	open val VoidType = FqName("Void")
	open val BoolType = FqName("Bool")
	open val IntType = FqName("Int")
	open val FloatType = FqName("Float32")
	open val DoubleType = FqName("Float64")
	open val LongType = FqName("haxe.Int64")
	open val BaseArrayType = FqName("JA_0")
	open val BoolArrayType = FqName("JA_Z")
	open val ByteArrayType = FqName("JA_B")
	open val CharArrayType = FqName("JA_C")
	open val ShortArrayType = FqName("JA_S")
	open val IntArrayType = FqName("JA_I")
	open val LongArrayType = FqName("JA_J")
	open val FloatArrayType = FqName("JA_F")
	open val DoubleArrayType = FqName("JA_D")
	open val ObjectArrayType = FqName("JA_L")

	open fun getDefault(type: AstType): Any? = type.getNull()

	open fun getNativeType(type: AstType, typeKind: CommonGenGen.TypeKind): FqName {
		return when (type) {
			is AstType.NULL -> NullType
			is AstType.VOID -> VoidType
			is AstType.BOOL -> BoolType
			is AstType.GENERIC -> getNativeType(type.type, typeKind)
			is AstType.INT, is AstType.SHORT, is AstType.CHAR, is AstType.BYTE -> IntType
			is AstType.FLOAT -> FloatType
			is AstType.DOUBLE -> DoubleType
			is AstType.LONG -> LongType
			is AstType.REF -> FqName(program[type.name]?.nativeName ?: getNativeName(type.name))
			is AstType.ARRAY -> when (type.element) {
				is AstType.BOOL -> BoolArrayType
				is AstType.BYTE -> ByteArrayType
				is AstType.CHAR -> CharArrayType
				is AstType.SHORT -> ShortArrayType
				is AstType.INT -> IntArrayType
				is AstType.LONG -> LongArrayType
				is AstType.FLOAT -> FloatArrayType
				is AstType.DOUBLE -> DoubleArrayType
				else -> ObjectArrayType
			}
			else -> throw RuntimeException("Not supported native type $type, $typeKind")
		}
	}

	open fun escapeConstant(value: Any?, type: AstType): String {
		val result = escapeConstant(value)
		return if (type != AstType.BOOL) result else if (result != "false" && result != "0") "true" else "false"
	}

	open fun escapeConstant(value: Any?): String = when (value) {
		null -> "null"
		is Boolean -> if (value) "true" else "false"
		is String -> "N.strLitEscape(\"" + value.escape() + "\")"
		is Long -> "N.lnew(${((value ushr 32) and 0xFFFFFFFF).toInt()}, ${((value ushr 0) and 0xFFFFFFFF).toInt()})"
		is Float -> escapeConstant(value.toDouble())
		is Double -> if (value.isInfinite()) if (value < 0) NegativeInfinityString else PositiveInfinityString else if (value.isNaN()) NanString else "$value"
		is Int -> when (value) {
			Int.MIN_VALUE -> "N.MIN_INT32"
			else -> "$value"
		}
		is Number -> "${value.toInt()}"
		is Char -> "${value.toInt()}"
		is AstType.REF -> "N.resolveClass(${value.mangle().quote()})"
		is AstType.ARRAY -> "N.resolveClass(${value.mangle().quote()})"
		else -> throw NotImplementedError("Literal of type $value")
	}

	open val NegativeInfinityString = "-Infinity"
	open val PositiveInfinityString = "Infinity"
	open val NanString = "NaN"
}