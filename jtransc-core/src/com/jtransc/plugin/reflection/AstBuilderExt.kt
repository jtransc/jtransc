package com.jtransc.plugin.reflection

import com.jtransc.ast.*

fun AstProgram.createClass(name: FqName, parent: FqName? = "java.lang.Object".fqname, interfaces: List<FqName> = listOf(), gen: AstClass.() -> Unit = { }): AstClass {
	val program = this
	val clazz = AstClass("source", program, name, AstModifiers.withFlags(AstModifiers.ACC_PUBLIC), parent, interfaces)
	clazz.gen()
	program.add(clazz)
	return clazz
}

fun AstClass.hasMethod(name: String, desc: AstType.METHOD): Boolean {
	return this.getMethod(name, desc.desc) != null
}

fun AstClass.createMethod(name: String, desc: AstType.METHOD, isStatic: Boolean = false, body: AstBuilder2.(args: List<AstArgument>) -> Unit = { RETURN() }): AstMethod {
	val clazz = this
	val types: AstTypes = this.program.types
	val method = AstMethod(
		containingClass = clazz,
		name = name,
		methodType = desc,
		annotations = listOf(),
		parameterAnnotations = listOf(),
		modifiers = AstModifiers(AstModifiers.ACC_PUBLIC or if (isStatic) AstModifiers.ACC_STATIC else 0),
		generateBody = { AstBody(types, AstBuilder2(types, AstBuilderBodyCtx()).apply { body(desc.args) }.genstm(), desc, AstMethodRef(clazz.name, name, desc)) },
		defaultTag = null,
		signature = desc.mangle(),
		genericSignature = null
	)
	clazz.add(method)
	return method
}

fun AstClass.createConstructor(desc: AstType.METHOD, body: AstBuilder2.(args: List<AstArgument>) -> Unit = { RETURN() }): AstMethod {
	return createMethod("<init>", desc, isStatic = false, body = body);
}

fun AstClass.createField(name: String, type: AstType, isStatic: Boolean = false, constantValue: Any? = null): AstField {
	val clazz = this
	val types: AstTypes = this.program.types
	val field = AstField(
		containingClass = clazz,
		name = name,
		type = type,
		modifiers = AstModifiers(AstModifiers.ACC_PUBLIC or if (isStatic) AstModifiers.ACC_STATIC else 0),
		desc = type.mangle(),
		annotations = listOf(),
		genericSignature = null,
		constantValue = constantValue,
		types = types
	)
	clazz.add(field)
	return field
}

fun AstProgram.createDataClass(name: FqName, fieldsInfo: List<Pair<String, AstType>>, parent: FqName = "java.lang.Object".fqname, interfaces: List<FqName> = listOf(), gen: AstClass.() -> Unit = { }): AstClass {
	val clazz = createClass(name, parent, interfaces) {
		val fields = fieldsInfo.map { createField(it.first, it.second) }
		val args = fieldsInfo.withIndex().map { AstArgument(it.index, it.value.second) }

		createConstructor(AstType.METHOD(args, AstType.VOID)) {
			for ((arg, field) in args.zip(fields)) {
				STM(AstStm.SET_FIELD_INSTANCE(field.ref, THIS, arg.expr))
			}
		}
	}
	clazz.gen()
	return clazz
}

/*
class AstBuilder(val program: AstProgram) {
	fun createClass
}
*/