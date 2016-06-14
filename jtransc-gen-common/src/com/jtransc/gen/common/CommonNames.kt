package com.jtransc.gen.common

import com.jtransc.ast.*

interface CommonNames {
	val program: AstResolver

	fun buildTemplateClass(clazz: FqName): String

	fun buildTemplateClass(clazz: AstClass): String

	fun buildField(field: AstField, static: Boolean): String

	fun buildMethod(method: AstMethod, static: Boolean): String

	fun buildStaticInit(clazz: AstClass): String

	fun buildConstructor(method: AstMethod): String
}