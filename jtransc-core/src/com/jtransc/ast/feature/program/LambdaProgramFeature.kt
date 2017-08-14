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
						val invoke = it
						val methodInInterfaceRef = invoke.methodInInterfaceRef
						val methodToConvertRef = invoke.methodToConvertRef
						val startArgs = invoke.startArgs
						val thisArgs = invoke.thisArgs
						val startAndThisArgs = startArgs + thisArgs
						val startArgTypesAndThis = startAndThisArgs.map { it.type }

						//val className = methodToConvertRef.classRef.fqname + "\$Lambda\$" + lambdaId++
						val className = "L\$${lambdaId++}"
						val lambdaClass = program.createClass(className.fqname, "java.lang.Object".fqname, listOf(methodInInterfaceRef.classRef.name), comment = "INVOKE_DYNAMIC_METHOD(method=$method, methodInInterfaceRef=$methodInInterfaceRef, methodToConvertRef=$methodToConvertRef)")
						val THIS = AstExpr.THIS(lambdaClass.name)

						val storedFields = startArgTypesAndThis.withIndex().map {
							val (index, type) = it
							lambdaClass.createField("f$index", type)
						}
						val thisField = if (invoke.isStatic) null else storedFields.lastOrNull()
						val storedFieldExprs = storedFields.map { AstExpr.FIELD_INSTANCE_ACCESS(it.ref, THIS) }

						// If not static add this reference
						val lambdaClassConstructor = lambdaClass.createConstructor(AstType.METHOD(AstType.VOID, startArgTypesAndThis)) {
							for ((index, field) in storedFields.withIndex()) {
								val arg = AstArgument(index, startArgTypesAndThis[index])
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

							val call = when {
								invoke.isStatic -> methodToCall.invokeStatic(startArgsExpr + argsExpr)
								else -> methodToCall.invokeInstance(THIS[thisField!!].castTo(invoke.methodInInterfaceRef.containingClassType), startArgsExpr + argsExpr)
							}
							if (methodInInterfaceRef.type.retVoid) {
								STM(call)
							} else {
								RETURN(call.castTo(methodInInterfaceRef.type.ret))
							}
						}

						AstExpr.NEW_WITH_CONSTRUCTOR(lambdaClassConstructor.ref, startArgs.exprs + thisArgs.exprs.map { it.castTo(methodInInterfaceRef.containingClassType) })
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