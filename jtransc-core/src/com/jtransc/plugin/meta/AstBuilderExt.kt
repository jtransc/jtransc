package com.jtransc.plugin.meta

import com.jtransc.ast.*

fun AstProgram.createClass(name: FqName, parent: FqName, interfaces: List<FqName> = listOf()): AstClass {
	val program = this
	val clazz = AstClass("source", program, name, AstModifiers.withFlags(AstModifiers.ACC_PUBLIC), parent, interfaces)
	program.add(clazz)
	return clazz
}

fun AstClass.createMethod(types: AstTypes, name: String, desc: AstType.METHOD, isStatic: Boolean = false, body: AstBuilder.() -> AstStm = { AstStm.RETURN_VOID() }): AstMethod {
	val clazz = this
	val method = AstMethod(
		containingClass = clazz,
		id = clazz.lastMethodId++,
		name = name,
		methodType = desc,
		annotations = listOf(),
		parameterAnnotations = listOf(),
		modifiers = AstModifiers(AstModifiers.ACC_PUBLIC or if (isStatic) AstModifiers.ACC_STATIC else 0),
		generateBody = { AstBody(types, AstBuilder(types).body(), desc) },
		defaultTag = null,
		signature = desc.mangle(),
		genericSignature = null,
		types = types
	)
	clazz.add(method)
	return method
}

/*
class AstBuilder(val program: AstProgram) {
	fun createClass
}
*/