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

	fun serializeValid(type: AstType?, usePrims:Boolean = true): String {
		return when (type) {
			null -> "null"
			is AstType.VOID -> if (usePrims) "void" else "java.lang.Void"
			is AstType.BOOL -> if (usePrims) "boolean" else "java.lang.Boolean"
			is AstType.BYTE -> if (usePrims) "byte" else "java.lang.Byte"
			is AstType.SHORT -> if (usePrims) "short" else "java.lang.Short"
			is AstType.CHAR -> if (usePrims) "char" else "java.lang.Character"
			is AstType.INT -> if (usePrims) "int" else "java.lang.Integer"
			is AstType.LONG -> if (usePrims) "long" else "java.lang.Long"
			is AstType.FLOAT -> if (usePrims) "float" else "java.lang.Float"
			is AstType.DOUBLE -> if (usePrims) "double" else "java.lang.Double"
			is AstType.ARRAY -> serializeValid(type.element, usePrims) + "[]"
			is AstType.GENERIC -> {
				val base = serializeValid(type.type, usePrims)
				base + type.suffixes.map {
					(it.id ?: "") + if (it.params != null) "<" + it.params!!.map { serializeValid(it, false) }.joinToString(", ") + ">" else ""
				}
			}
			is AstType.METHOD -> {
				val count = type.args.size
				"_root.Functions.F$count<" + type.argsPlusReturn.map { serializeValid(it, false) }.joinToString(", ") + ">"
			}
			is AstType.REF -> this.generateValidFqname(type.name).fqname
		//else -> type.mangle()
			else -> noImpl
		}
	}

	fun child(): JavaIds = JavaIds(this)
}
