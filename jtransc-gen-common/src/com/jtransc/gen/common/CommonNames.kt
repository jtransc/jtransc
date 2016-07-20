package com.jtransc.gen.common

import com.jtransc.ast.*
import com.jtransc.text.isLetterDigitOrUnderscore

abstract class CommonNames(val program: AstResolver) {
	abstract fun buildTemplateClass(clazz: FqName): String
	abstract fun buildTemplateClass(clazz: AstClass): String
	fun buildField(field: AstField, static: Boolean): String {
		return if (static) buildStaticField(field) else getNativeName(field)
	}

	abstract fun buildMethod(method: AstMethod, static: Boolean): String
	abstract fun buildStaticInit(clazz: AstClass): String
	abstract fun buildConstructor(method: AstMethod): String
	abstract fun escapeConstant(value: Any?): String
	abstract fun escapeConstant(value: Any?, type: AstType): String

	open fun buildInstanceField(expr: String, field: AstField): String = expr + buildAccessName(field)
	open fun buildStaticField(field: AstField): String = getNativeName(field.ref.containingTypeRef.name) + buildAccessName(field)

	fun buildStaticField(field: FieldRef): String = buildStaticField(program[field.ref]!!)
	fun buildInstanceField(expr: String, field: FieldRef): String = buildInstanceField(expr, program[field.ref]!!)

	open fun buildAccessName(field: AstField): String = buildAccessName(getNativeName(field))
	open fun buildAccessName(name: String): String = ".$name"

	fun normalizeName(name: String): String {
		if (name.isNullOrEmpty()) return ""
		val chars = name.toCharArray()
		for (i in chars.indices) {
			var c = chars[i]
			if (!c.isLetterDigitOrUnderscore() || c == '$') c = '_'
			chars[i] = c
		}
		if (chars[0].isDigit()) chars[0] = '_'
		return String(chars)
	}

	open fun getNativeName(local: LocalParamRef): String = normalizeName(local.name)
	open fun getNativeName(field: FieldRef): String = normalizeName(field.ref.name)
	open fun getNativeName(clazz: FqName): String = clazz.fqname

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
}