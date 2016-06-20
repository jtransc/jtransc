package com.jtransc.gen.common

import com.jtransc.ast.*

abstract class CommonNames(val program: AstResolver) {
	abstract fun buildTemplateClass(clazz: FqName): String

	abstract fun buildTemplateClass(clazz: AstClass): String

	abstract fun buildField(field: AstField, static: Boolean): String

	abstract fun buildMethod(method: AstMethod, static: Boolean): String

	abstract fun buildStaticInit(clazz: AstClass): String

	abstract fun buildConstructor(method: AstMethod): String
}