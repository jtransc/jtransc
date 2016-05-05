package com.jtransc.gen.haxe

import com.jtransc.ast.*
import com.jtransc.error.invalidOp
import com.jtransc.error.noImpl
import com.jtransc.error.unexpected
import com.jtransc.text.escape
import com.jtransc.text.quote


val HaxeKeywords = setOf(
	"java",
	"package",
	"import",
	"class", "interface", "extends", "implements",
	"internal", "private", "protected", "final",
	"function", "var", "const",
	"if", "else",
	"switch", "case", "default",
	"do", "while", "for", "each", "in",
	"try", "catch", "finally",
	"break", "continue",
	"int", "uint", "void",
	"goto"
)

val HaxeSpecial = setOf(
	"N", // used for HaxeNatives
	"SI", // STATIC INIT
	"SII", // STATIC INIT INITIALIZED
	"HAXE_CLASS_INIT", // Information about the class
	"HAXE_CLASS_NAME", // Information about the class
	"HaxeNatives", // used for HaxeNatives
	"unix"
)

val HaxeKeywordsWithToStringAndHashCode: Set<String> = HaxeKeywords + HaxeSpecial + setOf("toString", "hashCode")

class HaxeNames(
	val program: AstResolver,
	val minimize: Boolean = false
) {
	fun getHaxeMethodName(method: AstMethod): String = getHaxeMethodName(method.ref)
	fun getHaxeMethodName(method: AstMethodRef): String {
		val realmethod = program[method] ?: invalidOp("Can't find method $method")
		if (realmethod.nativeMethod != null) {
			return realmethod.nativeMethod!!
		} else {
			val name2 = "${method.name}${method.desc}"
			val name = when (method.name) {
				"<init>", "<clinit>" -> "${method.containingClass}$name2"
				else -> name2
			}
			return name.map {
				if (it.isLetterOrDigit()) "$it" else if (it == '.' || it == '/') "_" else "_"
			}.joinToString("")
		}
	}

	fun getHaxeFunctionalType(type: AstType.METHOD): String {
		return type.argsPlusReturnVoidIsEmpty.map { getHaxeType(it, GenHaxeGen.TypeKind.TYPETAG) }.joinToString(" -> ")
	}

	fun getHaxeDefault(type: AstType): Any? = when (type) {
		is AstType.BOOL -> false
		is AstType.INT, is AstType.SHORT, is AstType.CHAR, is AstType.BYTE -> 0
		is AstType.LONG -> 0L
		is AstType.FLOAT, is AstType.DOUBLE -> 0.0
		is AstType.REF, is AstType.ARRAY, is AstType.NULL -> null
		else -> noImpl("Not supported haxe type $type")
	}

	fun getHaxeFilePath(name: FqName): String {
		return getHaxeGeneratedFqName(name).internalFqname + ".hx"
	}

	fun getHaxeGeneratedFqPackage(name: FqName): String {
		return name.packageParts.map {
			if (it in HaxeKeywords) "${it}_" else it
		}.map { it.decapitalize() }.joinToString(".")
	}

	fun getHaxeGeneratedFqName(name: FqName): FqName {
		return FqName(getHaxeGeneratedFqPackage(name), getHaxeGeneratedSimpleClassName(name))
	}

	fun getHaxeGeneratedSimpleClassName(name: FqName): String {
		return "${name.simpleName.replace('$', '_')}_".capitalize()
	}

	inline fun <reified T : Any> haxeName(): String {
		return getHaxeClassFqName(T::class.java.name.fqname)
	}

	fun getHaxeClassFqName(name: FqName): String {
		val clazz = program[name]
		if (clazz != null && clazz.isNative) {
			return "${clazz.nativeName}"
		} else {
			return getHaxeGeneratedFqName(name).fqname
		}
	}

	private val cachedFieldNames = hashMapOf<AstFieldRef, String>()

	fun getHaxeFieldName(field: AstFieldRef): String {
		if (field !in cachedFieldNames) {
			val fieldName = field.name.replace('$', '_')
			var name = if (fieldName in HaxeKeywordsWithToStringAndHashCode) "${fieldName}_" else fieldName

			val clazz = program[field]?.containingClass
			val clazzAncestors = clazz?.ancestors?.reversed() ?: listOf()
			val names = clazzAncestors.flatMap { it.fields }.filter { it.name == field.name }.map { getHaxeFieldName(it.ref) }.toHashSet()
			val fieldsColliding = clazz?.fields?.filter { it.name == field.name }?.map { it.ref } ?: listOf(field)

			// JTranscBugInnerMethodsWithSameName.kt
			for (f2 in fieldsColliding) {
				while (name in names) name += "_"
				cachedFieldNames[f2] = name
				names += name
			}
			return cachedFieldNames[field] ?:
				unexpected("Unexpected. Not cached: $field")
		}
		return cachedFieldNames[field] ?:
			unexpected("Unexpected. Not cached: $field")
	}

	fun getHaxeFieldName(field: AstField): String {
		return getHaxeFieldName(field.ref)
	}

	fun getStaticFieldText(field: AstFieldRef): String {
		val prefix = getHaxeClassFqNameInt(field.classRef.name)
		return "$prefix.${getHaxeFieldName(field)}"
	}

	fun getHaxeClassFqNameInt(name: FqName): String {
		val clazz = program[name]
		val simpleName = getHaxeGeneratedSimpleClassName(name)
		val suffix = if (clazz?.isInterface ?: false) ".${simpleName}_IFields" else ""
		return getHaxeClassFqName(clazz?.name ?: name) + "$suffix"
	}

	fun getHaxeClassFqNameLambda(name: FqName): String {
		val clazz = program[name]
		val simpleName = getHaxeGeneratedSimpleClassName(name)
		return getHaxeClassFqName(clazz?.name ?: name) + ".${simpleName}_Lambda"
	}

	fun getHaxeClassStaticInit(classRef: AstType.REF): String {
		return "${getHaxeClassFqNameInt(classRef.name)}.SI();"
	}

	fun getHaxeClassStaticClassInit(classRef: AstType.REF): String {
		return "${getHaxeClassFqNameInt(classRef.name)}.HAXE_CLASS_INIT"
	}

	fun getAnnotationProxyName(classRef: AstType.REF): String {
		return "AnnotationProxy_${getHaxeGeneratedFqName(classRef.name).fqname.replace('.', '_')}"
	}

	fun getFullAnnotationProxyName(classRef: AstType.REF): String {
		return getHaxeClassFqName(classRef.name) + ".AnnotationProxy_${getHaxeGeneratedFqName(classRef.name).fqname.replace('.', '_')}"
	}

	fun getHaxeType(type: AstType, typeKind: GenHaxeGen.TypeKind): FqName {
		return FqName(when (type) {
			is AstType.NULL -> "Dynamic"
			is AstType.VOID -> "Void"
			is AstType.BOOL -> "Bool"
			is AstType.GENERIC -> getHaxeType(type.type, typeKind).fqname
			is AstType.INT, is AstType.SHORT, is AstType.CHAR, is AstType.BYTE -> "Int"
			is AstType.FLOAT, is AstType.DOUBLE -> "Float"
			is AstType.LONG -> "haxe.Int64"
			is AstType.REF -> program[type.name]?.nativeName ?: getHaxeClassFqName(type.name)
			is AstType.ARRAY -> when (type.element) {
				is AstType.BOOL -> "HaxeBoolArray"
				is AstType.BYTE -> "HaxeByteArray"
				is AstType.CHAR -> "HaxeCharArray"
				is AstType.SHORT -> "HaxeShortArray"
				is AstType.INT -> "HaxeIntArray"
				is AstType.LONG -> "HaxeLongArray"
				is AstType.FLOAT -> "HaxeFloatArray"
				is AstType.DOUBLE -> "HaxeDoubleArray"
				else -> "HaxeArray"
			}
			else -> throw RuntimeException("Not supported haxe type $type, $typeKind")
		})
	}

	fun escapeConstant(value: Any?, type: AstType): String {
		val result = escapeConstant(value)
		return if (type == AstType.BOOL) {
			if (result != "false" && result != "0") "true" else "false"
		} else {
			result
		}
	}

	fun escapeConstant(value: Any?): String = when (value) {
		null -> "null"
		is Boolean -> if (value) "true" else "false"
		is String -> "N.strLit(\"" + value.escape() + "\")"
		is Short -> "$value"
		is Char -> "$value"
		is Int -> "$value"
		is Byte -> "$value"
		is Long -> "haxe.Int64.make(${((value ushr 32) and 0xFFFFFFFF).toInt()}, ${((value ushr 0) and 0xFFFFFFFF).toInt()})"
		is Float -> escapeConstant(value.toDouble())
		is Double -> if (value.isInfinite()) {
			if (value < 0) "Math.NEGATIVE_INFINITY" else "Math.POSITIVE_INFINITY"
		} else if (value.isNaN()) {
			"Math.NaN"
		} else {
			"$value"
		}
		is AstType.REF -> "HaxeNatives.resolveClass(${value.mangle().quote()})"
		is AstType.ARRAY -> "HaxeNatives.resolveClass(${value.mangle().quote()})"
		else -> throw NotImplementedError("Literal of type $value")
	}

}