package com.jtransc.gen.js

import com.jtransc.ast.*
import com.jtransc.ds.getOrPut2
import com.jtransc.error.unexpected
import com.jtransc.gen.common.CommonNames
import com.jtransc.text.escape
import com.jtransc.text.quote

class JsNames(
	program: AstResolver,
	val minimize: Boolean
) : CommonNames(program) {
	companion object {
		val JsKeywordsWithToStringAndHashCode = setOf(
			"name", "constructor", "prototype", "__proto__"
		)
	}

	override fun buildTemplateClass(clazz: FqName): String {
		return getJsClassFqNameForCalling(clazz)
	}

	override fun buildTemplateClass(clazz: AstClass): String {
		return getJsClassFqNameForCalling(clazz.name)
	}

	override fun buildField(field: AstField, static: Boolean): String {
		return if (static) getStaticFieldText(field.ref) else getJsFieldName(field)
	}

	override fun buildMethod(method: AstMethod, static: Boolean): String {
		val clazz = getJsClassFqNameForCalling(method.containingClass.name)
		val name = getJsMethodName(method)
		return if (static) "$clazz[${name.quote()}]" else name
	}

	override fun buildStaticInit(clazz: AstClass): String {
		return getJsClassStaticInit(clazz.ref, "template sinit")
	}

	override fun buildConstructor(method: AstMethod): String {
		val clazz = getJsClassFqNameForCalling(method.containingClass.name)
		val methodName = getJsMethodName(method)
		return "new $clazz()[${methodName.quote()}]"
	}

	private var lastStringId = 0;
	val allocatedStrings = hashMapOf<String, Int>()

	fun allocString(str: String): Int {
		return allocatedStrings.getOrPut(str) {
			val id = lastStringId++
			allocatedStrings[str] = id
			id
		}
	}

	inline fun <reified T : Any> jsName(): String = T::class.java.name

	private val fieldNames = hashMapOf<Any?, String>()
	private val cachedFieldNames = hashMapOf<AstFieldRef, String>()

	fun getJsFieldName(field: AstField): String {
		//"_" + field.uniqueName

		val fieldRef = field.ref
		val keyToUse = field.ref

		return fieldNames.getOrPut2(keyToUse) {
			if (fieldRef !in cachedFieldNames) {
				val fieldName = field.name.replace('$', '_')
				//var name = if (fieldName in JsKeywordsWithToStringAndHashCode) "${fieldName}_" else fieldName
				var name = "_$fieldName"

				val clazz = program[fieldRef]?.containingClass
				val clazzAncestors = clazz?.ancestors?.reversed() ?: listOf()
				val names = clazzAncestors.flatMap { it.fields }.filter { it.name == field.name }.map { getJsFieldName(it.ref) }.toHashSet()
				val fieldsColliding = clazz?.fields?.filter { it.name == field.name }?.map { it.ref } ?: listOf(field.ref)

				// JTranscBugInnerMethodsWithSameName.kt
				for (f2 in fieldsColliding) {
					while (name in names) name += "_"
					cachedFieldNames[f2] = name
					names += name
				}
				cachedFieldNames[field.ref] ?: unexpected("Unexpected. Not cached: $field")
			}
			cachedFieldNames[field.ref] ?: unexpected("Unexpected. Not cached: $field")
		}
	}

	fun getJsFieldName(field: AstFieldRef): String = getJsFieldName(program[field]!!)

	override fun escapeConstant(value: Any?, type: AstType): String {
		val result = escapeConstant(value)
		return if (type != AstType.BOOL) result else if (result != "false" && result != "0") "true" else "false"
	}

	fun genAccess(name: String): String {
		// @TODO: improve direct access with .name when possible.
		return "[${name.quote()}]";
	}

	val JsArrayAny = "JA_L"

	override fun escapeConstant(value: Any?): String = when (value) {
		null -> "null"
		is Boolean -> if (value) "true" else "false"
		is String -> "\"" + value.escape() + "\""
		is Long -> "N.lnew(${((value ushr 32) and 0xFFFFFFFF).toInt()}, ${((value ushr 0) and 0xFFFFFFFF).toInt()})"
		is Float -> escapeConstant(value.toDouble())
		is Double -> if (value.isInfinite()) if (value < 0) "-Infinity" else "Infinity" else if (value.isNaN()) "NaN" else "$value"
		is Int -> "$value"
		is Number -> "${value.toInt()}"
		is Char -> "${value.toInt()}"
		is AstType.REF, is AstType.ARRAY -> "N.resolveClass(${(value as AstType).mangle().quote()})"
		else -> throw NotImplementedError("Literal of type $value")
	}

	fun getJsType(type: AstType, typeKind: GenJsGen.TypeKind): FqName {
		return FqName(when (type) {
			is AstType.NULL -> "Dynamic"
			is AstType.VOID -> "Void"
			is AstType.BOOL -> "Bool"
			is AstType.GENERIC -> getJsType(type.type, typeKind).fqname
			is AstType.INT, is AstType.SHORT, is AstType.CHAR, is AstType.BYTE -> "Int"
			is AstType.FLOAT -> "Float32"
			is AstType.DOUBLE -> "Float64"
			is AstType.LONG -> "haxe.Int64"
			is AstType.REF -> program[type.name]?.nativeName ?: getJsClassFqNameForCalling(type.name)
			is AstType.ARRAY -> when (type.element) {
				is AstType.BOOL -> "JA_Z"
				is AstType.BYTE -> "JA_B"
				is AstType.CHAR -> "JA_C"
				is AstType.SHORT -> "JA_S"
				is AstType.INT -> "JA_I"
				is AstType.LONG -> "JA_J"
				is AstType.FLOAT -> "JA_F"
				is AstType.DOUBLE -> "JA_D"
				else -> JsArrayAny
			}
			else -> throw RuntimeException("Not supported js type $type, $typeKind")
		})
	}

	fun getJsDefault(type: AstType): Any? = type.getNull()

	fun getStaticFieldText(field: AstFieldRef): String {
		val clazz = getJsClassFqNameForCalling(field.containingClass)
		return clazz + "[" + getJsFieldName(field).quote() + "]"
	}

	fun getJsMethodName(method: AstMethod): String = getJsMethodName(method.ref)

	fun getJsMethodName(method: AstMethodRef): String {
		return if (method.isInstanceInit) {
			"${method.classRef.fqname}${method.name}${method.desc}"
		} else {
			"${method.name}${method.desc}"
		}
	}

	fun getJsClassStaticInit(clazzRef: AstType.REF, joinToString: String): String {
		//return "throw 'Not implemented getJsClassStaticInit';"
		return getJsClassFqNameForCalling(clazzRef.name) + ".SI();"
	}

	fun getJsClassFqName(fqName: FqName): String {
		return fqName.fqname
	}

	fun getJsClassFqNameForCalling(fqName: FqName): String {
		return fqName.fqname.replace('.', '_')
	}

	fun getJsClassFqNameInt(fqName: FqName): String {
		return fqName.simpleName
	}

	fun getJsFilePath(fqName: FqName): String {
		return fqName.simpleName
	}

	fun getJsGeneratedFqPackage(fqName: FqName): String = fqName.fqname
	fun getJsGeneratedFqName(fqName: FqName): FqName = fqName
	fun getJsGeneratedSimpleClassName(fqName: FqName): String = fqName.fqname
}