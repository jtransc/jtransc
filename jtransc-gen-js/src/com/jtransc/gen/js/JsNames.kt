package com.jtransc.gen.js

import com.jtransc.ast.*
import com.jtransc.ds.getOrPut2
import com.jtransc.error.unexpected
import com.jtransc.gen.common.CommonGenGen
import com.jtransc.gen.common.CommonNames
import com.jtransc.text.escape
import com.jtransc.text.quote

class JsNames(
	program: AstResolver,
	val minimize: Boolean
) : CommonNames(program) {
	companion object {
		val JsKeywordsWithToStringAndHashCode = setOf("name", "constructor", "prototype", "__proto__")
	}

	override fun buildTemplateClass(clazz: FqName): String = getJsClassFqNameForCalling(clazz)
	override fun buildTemplateClass(clazz: AstClass): String = getJsClassFqNameForCalling(clazz.name)
	override fun buildMethod(method: AstMethod, static: Boolean): String {
		val clazz = getJsClassFqNameForCalling(method.containingClass.name)
		val name = getJsMethodName(method)
		return if (static) "$clazz[${name.quote()}]" else name
	}

	override fun buildStaticInit(clazz: AstClass): String = getJsClassStaticInit(clazz.ref, "template sinit")

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

	fun getNativeName(field: AstField): String {
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
				val names = clazzAncestors.flatMap { it.fields }.filter { it.name == field.name }.map { getNativeName(it.ref) }.toHashSet()
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

	override fun getNativeName(field: FieldRef): String = getNativeName(program[field.ref]!!)

	override fun getNativeName(local: LocalParamRef): String {
		return super.getNativeName(local)
	}

	override fun getNativeName(clazz: FqName): String {
		return getJsClassFqNameForCalling(clazz)
	}

	override fun escapeConstant(value: Any?, type: AstType): String {
		val result = escapeConstant(value)
		return if (type != AstType.BOOL) result else if (result != "false" && result != "0") "true" else "false"
	}

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

	fun getJsDefault(type: AstType): Any? = type.getNull()

	override fun buildAccessName(name: String): String = "[" + name.quote() + "]"

	fun getJsMethodName(method: MethodRef): String = getJsMethodName(method.ref)

	fun getJsMethodName(method: AstMethodRef): String {
		return if (method.isInstanceInit) {
			"${method.classRef.fqname}${method.name}${method.desc}"
		} else {
			"${method.name}${method.desc}"
		}
	}

	fun getJsClassStaticInit(clazzRef: AstType.REF, joinToString: String): String = getJsClassFqNameForCalling(clazzRef.name) + ".SI();"
	fun getJsClassFqName(fqName: FqName): String = fqName.fqname
	fun getJsClassFqNameForCalling(fqName: FqName): String = fqName.fqname.replace('.', '_')
	fun getJsClassFqNameInt(fqName: FqName): String = fqName.simpleName
	fun getJsFilePath(fqName: FqName): String = fqName.simpleName
	fun getJsGeneratedFqPackage(fqName: FqName): String = fqName.fqname
	fun getJsGeneratedFqName(fqName: FqName): FqName = fqName
	fun getJsGeneratedSimpleClassName(fqName: FqName): String = fqName.fqname
}