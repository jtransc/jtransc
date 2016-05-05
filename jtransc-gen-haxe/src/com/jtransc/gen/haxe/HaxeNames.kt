package com.jtransc.gen.haxe

import com.jtransc.annotation.JTranscKeep
import com.jtransc.annotation.JTranscKeepName
import com.jtransc.ast.*
import com.jtransc.error.invalidOp
import com.jtransc.error.noImpl
import com.jtransc.error.unexpected
import com.jtransc.gen.MinimizedNames
import com.jtransc.text.escape
import com.jtransc.text.quote


val HaxeKeywords = setOf(
	"haxe",
	"Dynamic",
	"Void",
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
	private val cachedFieldNames = hashMapOf<AstFieldRef, String>()

	val ENABLED_MINIFY = false
	//val ENABLED_MINIFY = true
	private val ENABLED_MINIFY_METHODS = ENABLED_MINIFY
	private val ENABLED_MINIFY_FIELDS = ENABLED_MINIFY

	private var minClassLastId:Int = 0
	private var minMemberLastId:Int = 0
	private val methodNmaes = hashMapOf<Any?, String>()
	private val fieldNames = hashMapOf<String, String>()

	private fun <T> Set<T>.runUntilNotInSet(callback: () -> T): T {
		while (true) {
			val result = callback()
			if (result !in this) return result
		}
	}

	fun allocClassName(obj: Any?): String = HaxeKeywordsWithToStringAndHashCode.runUntilNotInSet { MinimizedNames.getTypeNameById(minClassLastId++) }
	fun allocMemberName(obj: Any?): String = HaxeKeywordsWithToStringAndHashCode.runUntilNotInSet { MinimizedNames.getIdNameById(minMemberLastId++) }

	fun setClassName(obj: Any?): String = methodNmaes.getOrPut(obj) { HaxeKeywordsWithToStringAndHashCode.runUntilNotInSet { MinimizedNames.getTypeNameById(minClassLastId++) } }
	fun setMemberName(obj: Any?): String = methodNmaes.getOrPut(obj) { HaxeKeywordsWithToStringAndHashCode.runUntilNotInSet { MinimizedNames.getIdNameById(minMemberLastId++) } }

	fun getHaxeMethodName(method: AstMethod): String = getHaxeMethodName(method.ref)
	fun getHaxeMethodName(method: AstMethodRef): String {
		val realmethod = program[method] ?: invalidOp("Can't find method $method")
		val methodWithoutClass = method.withoutClass

		val objectToCache: Any = if (method.isClassOrInstanceInit) method else methodWithoutClass

		return methodNmaes.getOrPut(objectToCache) {
			if (ENABLED_MINIFY_METHODS && !realmethod.keepName && minimize) {
				allocMemberName(objectToCache)
			} else {
				if (realmethod.nativeMethod != null) {
					realmethod.nativeMethod!!
				} else {
					val name2 = "${method.name}${method.desc}"
					val name = when (method.name) {
						"<init>", "<clinit>" -> "${method.containingClass}$name2"
						else -> name2
					}
					cleanName(name)
				}
			}
		}
	}

	private fun cleanName(name:String):String {
		val out = CharArray(name.length)
		for (n in 0 until name.length) out[n] = if (name[n].isLetterOrDigit()) name[n] else '_'
		return String(out)
	}

	fun getHaxeFunctionalType(type: AstType.METHOD): String {
		return type.argsPlusReturnVoidIsEmpty.map { getHaxeType(it, GenHaxeGen.TypeKind.TYPETAG) }.joinToString(" -> ")
	}

	fun getHaxeDefault(type: AstType): Any? = type.getNull()

	fun getHaxeFilePath(name: FqName): String = getHaxeGeneratedFqName(name).internalFqname + ".hx"

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
		return if (clazz != null && clazz.isNative) "${clazz.nativeName}" else getHaxeGeneratedFqName(name).fqname
	}

	data class FieldName(val name:String)

	fun getHaxeFieldName(field: AstFieldRef): String {
		val realfield = program[field]
		val fieldWithoutClass = field.withoutClass
		val keyToUse = field.name

		return fieldNames.getOrPut(keyToUse) {
			if (ENABLED_MINIFY_FIELDS && !realfield.keepName && minimize) {
				return allocMemberName(keyToUse)
			} else {
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
		}
	}

	fun getHaxeFieldName(field: AstField): String {
		//field.annotations.contains<JTranscKeepName>()
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
		return if (type != AstType.BOOL) result else if (result != "false" && result != "0") "true" else "false"
	}

	fun escapeConstant(value: Any?): String = when (value) {
		null -> "null"
		is Boolean -> if (value) "true" else "false"
		is String -> "N.strLit(\"" + value.escape() + "\")"
		is Long -> "N.lnew(${((value ushr 32) and 0xFFFFFFFF).toInt()}, ${((value ushr 0) and 0xFFFFFFFF).toInt()})"
		is Float -> escapeConstant(value.toDouble())
		is Double -> if (value.isInfinite()) if (value < 0) "Math.NEGATIVE_INFINITY" else "Math.POSITIVE_INFINITY" else if (value.isNaN()) "Math.NaN" else "$value"
		is Number -> "${value.toInt()}"
		is Char -> "${value.toInt()}"
		is AstType.REF -> "HaxeNatives.resolveClass(${value.mangle().quote()})"
		is AstType.ARRAY -> "HaxeNatives.resolveClass(${value.mangle().quote()})"
		else -> throw NotImplementedError("Literal of type $value")
	}

}