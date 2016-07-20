package com.jtransc.gen.common

import com.jtransc.ast.*
import com.jtransc.text.isLetterDigitOrUnderscore

abstract class CommonNames(val program: AstResolver) {
	abstract fun buildTemplateClass(clazz: FqName): String
	abstract fun buildTemplateClass(clazz: AstClass): String
	abstract fun buildField(field: AstField, static: Boolean): String
	abstract fun buildMethod(method: AstMethod, static: Boolean): String
	abstract fun buildStaticInit(clazz: AstClass): String
	abstract fun buildConstructor(method: AstMethod): String
	abstract fun escapeConstant(value: Any?): String
	abstract fun escapeConstant(value: Any?, type: AstType): String

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

	open fun getNativeName(local: LocalRef): String = normalizeName(local.name)
}