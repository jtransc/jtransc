package com.jtransc.ast.feature.program

import com.jtransc.ast.*
import com.jtransc.plugin.reflection.createClass
import com.jtransc.plugin.reflection.createConstructor
import com.jtransc.plugin.reflection.createField
import com.jtransc.plugin.reflection.createMethod

class LambdaProgramFeature : AstProgramFeature() {
	override fun onMissing(program: AstProgram, settings: AstBuildSettings, types: AstTypes) {
		var lambdaId = 0

		fun processMethodWithDynamic(method: AstMethod) {
			method.transformInplace {
				when (it) {
					is AstExpr.INVOKE_DYNAMIC_METHOD -> {
						val methodInInterfaceRef = it.methodInInterfaceRef
						val methodToConvertRef = it.methodToConvertRef
						val startArgs = it.startArgs
						val startArgsTypes = it.startArgs.map { it.type }
						//val className = methodToConvertRef.classRef.fqname + "\$Lambda\$" + lambdaId++
						val className = "L\$${lambdaId++}"
						val lambdaClass = program.createClass(className.fqname, "java.lang.Object".fqname, listOf(methodInInterfaceRef.classRef.name))
						val THIS = AstExpr.THIS(lambdaClass.name)

						val storedFields = startArgsTypes.withIndex().map {
							val (index, type) = it
							lambdaClass.createField("f$index", type)
						}
						val storedFieldExprs = storedFields.map { AstExpr.FIELD_INSTANCE_ACCESS(it.ref, THIS) }

						// If not static add this reference
						val lambdaClassConstructor = lambdaClass.createConstructor(AstType.METHOD(AstType.VOID, startArgsTypes)) {
							for ((index, field) in storedFields.withIndex()) {
								val arg = AstArgument(index, startArgsTypes[index])
								STM(AstStm.SET_FIELD_INSTANCE(field.ref, THIS, arg.expr))
							}
						}

						lambdaClass.createMethod(methodInInterfaceRef.name, methodInInterfaceRef.type) {
							val methodToCall = methodToConvertRef.ref

							val argsFrom = methodInInterfaceRef.type.args
							val argsStartTo = methodToConvertRef.type.argTypes.take(startArgs.size)
							val argsTo = methodToConvertRef.type.argTypes.drop(startArgs.size)

							val startArgsExpr = storedFieldExprs.zip(argsStartTo).map { it.first.castTo(it.second) }
							val argsExpr = argsFrom.zip(argsTo).map { it.first.expr.castTo(it.second) }

							val call = methodToCall(startArgsExpr + argsExpr)
							if (methodInInterfaceRef.type.retVoid) {
								STM(call)
							} else {
								RETURN(call.castTo(methodInInterfaceRef.type.ret))
							}
						}

						AstExpr.NEW_WITH_CONSTRUCTOR(lambdaClassConstructor.ref, startArgs.exprs)
					}
					else -> it
				}
			}
		}

		for (clazz in program.classes.toList()) {
			for (method in clazz.methods.toList().filter { it.body?.flags?.hasDynamicInvoke ?: false }) {
				processMethodWithDynamic(method)
			}
		}
	}
}