package com.jtransc.tools

import com.jtransc.ast.AstType
import com.jtransc.ast.FqName
import com.jtransc.ast.mangle
import com.jtransc.error.noImpl


class JavaIds(val parent: JavaIds? = null) {
	companion object {
		val SET = setOf(
			"package", "import",
			"enum", "class", "interface", "extends", "implements", "throws",
			"void", "boolean", "byte", "char", "short", "int", "long", "float", "double",
			"public", "private", "protected", "static", "native", "abstract", "synchronized", "transient", "final", "const", "strictfp", "volatile",
			"for", "do", "while", "continue", "break",
			"instanceof",
			"if", "else", "switch", "case", "default",
			"assert", "throw", "try", "catch", "finally",
			"return",
			"super", "this"
		)
	}

	val transforms = hashMapOf<FqName, FqName>()

	fun generateValidId(id: String):String {
		return if (id in SET) "${id}_" else id
	}

	fun generateValidMemberName(name: String, isStatic: Boolean):String {
		val out = generateValidId(name)
		// @TODO: quick and dirty fix to avoid static collision in lime.
		// @TODO: This should be automatically detected with the types tree but requires some effort
		return if (isStatic && name == "initialize") "s_$out" else "$out"
	}

	fun getTransform(name:FqName):FqName? {
		return transforms[name] ?: parent?.getTransform(name)
	}

	fun generateValidFqname(name: FqName): FqName {
		val transformed = getTransform(name)
		if (transformed != null) {
			return transformed
		} else if (name.packagePath.isEmpty()) {
			return generateValidFqname(FqName(listOf("_root", name.simpleName)))
		} else {
			return FqName(name.parts.map { generateValidId(it) })
		}
	}

	fun serializeValid(type: AstType?): String {
		return when (type) {
			null -> "null"
			is AstType.VOID -> "void"
			is AstType.BOOL -> "boolean"
			is AstType.BYTE -> "byte"
			is AstType.SHORT -> "short"
			is AstType.CHAR -> "char"
			is AstType.INT -> "int"
			is AstType.LONG -> "long"
			is AstType.FLOAT -> "float"
			is AstType.DOUBLE -> "double"
			is AstType.ARRAY -> serializeValid(type.element) + "[]"
			is AstType.REF -> this.generateValidFqname(type.name).fqname
		//else -> type.mangle()
			else -> noImpl
		}
	}

	fun child(): JavaIds = JavaIds(this)
}
