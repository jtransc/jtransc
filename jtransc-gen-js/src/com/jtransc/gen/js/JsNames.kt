package com.jtransc.gen.js

import com.jtransc.ast.*
import com.jtransc.text.Indenter
import com.jtransc.text.escape
import com.jtransc.text.quote

class JsNames(val program: AstProgram, val minimize: Boolean) {
	private var lastStringId = 0;
	val allocatedStrings = hashMapOf<String, Int>()

	fun allocString(str:String):Int {
		return allocatedStrings.getOrPut(str) {
			val id = lastStringId++
			allocatedStrings[str] = id
			id
		}
	}

	inline fun <reified T : Any> jsName(): String {
		return T::class.java.name
	}

	fun getJsFieldName(java: Class<Class<*>>, name: String): String = name
	fun getJsFieldName(field: AstFieldRef): String = field.name
	fun getJsFieldName(field: AstField): String = field.name

	fun escapeConstant(value: Any?, type: AstType): String {
		val result = escapeConstant(value)
		return if (type != AstType.BOOL) result else if (result != "false" && result != "0") "true" else "false"
	}

	fun genAccess(name: String): String {
		// @TODO: improve direct access with .name when possible.
		return "[${name.quote()}]";
	}

	val JsArrayAny = "JA_L"
	val JsArrayBase = "JA_0"

	fun escapeConstant(value: Any?): String = when (value) {
		null -> "null"
		is Boolean -> if (value) "true" else "false"
		is String -> "N.strLit(\"" + value.escape() + "\")"
		is Long -> "N.lnew(${((value ushr 32) and 0xFFFFFFFF).toInt()}, ${((value ushr 0) and 0xFFFFFFFF).toInt()})"
		is Float -> escapeConstant(value.toDouble())
		is Double -> if (value.isInfinite()) if (value < 0) "Math.NEGATIVE_INFINITY" else "Math.POSITIVE_INFINITY" else if (value.isNaN()) "Math.NaN" else "$value"
		is Int -> "$value"
		is Number -> "${value.toInt()}"
		is Char -> "${value.toInt()}"
		is AstType.REF -> "HaxeNatives.resolveClass(${value.mangle().quote()})"
		is AstType.ARRAY -> "HaxeNatives.resolveClass(${value.mangle().quote()})"
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

	fun getStaticFieldText(astFieldRef: AstFieldRef): String {
		throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	fun getJsMethodName(method: AstMethod): String = getJsMethodName(method.ref)

	fun getJsMethodName(method: AstMethodRef): String = "${method.name}${method.desc}"

	fun getJsClassFqNameLambda(fqName: FqName): String {
		throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	fun getAnnotationProxyName(astType: AstType.REF): String {
		return astType.fqname
	}

	fun getJsClassStaticInit(clazzRef: AstType.REF, joinToString: String): String {
		return "throw 'Not implemented getJsClassStaticInit';"
	}

	fun getFullAnnotationProxyName(type: AstType.REF): String {
		return type.fqname
	}

	fun getJsClassStaticClassInit(ref: AstType.REF): String {
		return ref.fqname
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