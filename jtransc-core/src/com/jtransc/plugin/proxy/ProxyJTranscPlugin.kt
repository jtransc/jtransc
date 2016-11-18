package com.jtransc.plugin.proxy

import com.jtransc.ast.*
import com.jtransc.error.invalidOp
import com.jtransc.plugin.JTranscPlugin
import com.jtransc.plugin.reflection.createClass
import com.jtransc.plugin.reflection.createConstructor
import com.jtransc.plugin.reflection.createField
import com.jtransc.plugin.reflection.createMethod
import j.ProgramReflection
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class ProxyJTranscPlugin : JTranscPlugin() {
	override val priority: Int = 1
	/*
	override fun onAfterAllClassDiscovered(program: AstProgram) {
		program.addReference()
	}
	*/

	override fun processAfterTreeShaking(program: AstProgram) {
		if (Proxy::class.java.fqname !in program) return // Proxy not referenced!
		if (ProgramReflection::class.java.fqname !in program) return // ProgramReflection not referenced!

		val cl = program[Proxy::class.java.fqname]
		val Proxy_newProxyInstance = cl.getMethodWithoutOverrides("_newProxyInstance") ?: return // Proxy.newProxyInstance not referenced
		val InvocationHandler_class = program.getOrNull(java.lang.reflect.InvocationHandler::class.java.name.fqname) ?: return // InvocationHandler not referenced
		val InvocationHandler_invoke = InvocationHandler_class.getMethodWithoutOverrides(InvocationHandler::invoke.name) ?: return

		val ProgramReflection = program[ProgramReflection::class.java.name.fqname]
		val ProgramReflection_getDirectMethod = ProgramReflection.getMethodWithoutOverrides(j.ProgramReflection::getDirectMethod.name) ?: return
		val ProgramReflection_getMethodByInfo = ProgramReflection.getMethodWithoutOverrides(j.ProgramReflection::getMethodByInfo.name) ?: return


		val interfaces = program.classes.toList().filter { it.isInterface && it.visible }
		val proxies = hashMapOf<AstClass, AstClass>()

		for (interfase in interfaces) {
			proxies[interfase] = program.createClass("${interfase.fqname}\$Proxy".fqname, interfaces = listOf(interfase.name)) {
				extraVisible = false

				val icField = createField("ic", InvocationHandler_class.ref)

				createConstructor(AstType.METHOD(AstType.VOID, listOf(InvocationHandler_class.ref))) { args ->
					val (icArg) = args
					STM(AstStm.SET_FIELD_INSTANCE(icField.ref, THIS, icArg.expr))
				}

				for (methodRef in interfase.allMethodsToImplement) {
					val method = interfase.getMethodInAncestorsAndInterfaces(methodRef) ?: invalidOp("Can't find method $methodRef")
					val methodType = methodRef.type
					createMethod(methodRef.name, methodType) {
						//STM(icField)
						val call = THIS[icField][InvocationHandler_invoke.ref](
							THIS,
							ProgramReflection_getDirectMethod(interfase.classId.lit, method.id.lit),
							CREATE_ARRAY(ARRAY(OBJECT), methodType.args.map { it.expr.castTo(OBJECT) })
						)
						if (methodType.retVoid) {
							STM(call)
							RETURN()
						} else {
							RETURN(call.castTo(methodType.ret))
						}
					}
				}
			}
		}

		Proxy_newProxyInstance.replaceBodyOptBuild { args ->
			val (ifcId, ih) = args

			SWITCH(ifcId.expr) {
				for (ifc in interfaces) {
					CASE(ifc.classId) {
						val proxy = proxies[ifc]
						val constructor = proxy!!.constructors.first()
						RETURN(AstExpr.NEW_WITH_CONSTRUCTOR(constructor.ref, listOf(ih.expr)))
					}
				}
			}

			RETURN(NULL)
		}
	}
}