package com.jtransc.plugin.overlay

import com.jtransc.ast.*
import com.jtransc.plugin.JTranscPlugin

class OverlayJTranscPlugin : JTranscPlugin() {
	override val priority: Int = Int.MAX_VALUE - 999

	override fun processAfterTreeShaking(program: AstProgram) {
		val nativeClassesWithOverlays = program.classes
			.filter { it.nativeNameForTarget(targetName) != null }
			.filter { it.methodsWithoutConstructors.any { it.hasBody } }

		val overlayMethodsByClass = nativeClassesWithOverlays.map { it to it.methodsWithoutConstructors.filter { it.hasBody } }.toMap()
		val methodToOverlayMethod = hashMapOf<AstMethodRef, AstMethodRef>()

		for ((clazz, overlayMethods) in overlayMethodsByClass) {
			//println("$clazz:"); for (m in overlayMethods) println("- $m")
			val overlayClass = AstClass(
				source = clazz.source,
				program = program,
				name = (clazz.fqname + "\$JTOverlay").fqname,
				modifiers = AstModifiers(AstModifiers.ACC_PUBLIC or AstModifiers.ACC_FINAL),
				extending = "java.lang.Object".fqname,
				implementing = listOf(),
				annotations = listOf()
			)
			program.add(overlayClass)
			for (method in overlayMethods) {
				// We shouldn't transform call-site bodies
				if (method.annotationsList.getCallSiteBodyForTarget(targetName) != null) continue

				val overlayMethod = if (method.isStatic) {
					AstMethod(
						containingClass = overlayClass,
						id = program.lastMethodId++,
						name = method.name,
						methodType = method.methodType,
						annotations = method.annotations,
						signature = method.signature,
						genericSignature = method.genericSignature,
						defaultTag = method.defaultTag,
						modifiers = method.modifiers,
						generateBody = method.generateBody,
						//bodyRef = method.bodyRef,
						parameterAnnotations = method.parameterAnnotations
					)
				} else {
					//println("WARNING: Not supported instance overlay methods yet!")
					val thisArg = AstArgument(0, overlayClass.ref, "_jt_ov_this", false)
					val newParams = method.methodType.args.map { AstArgument(it.index + 1, it.type, it.name, it.optional) }
					val ovArgTypes = listOf(thisArg) + newParams
					val newMethodType = AstType.METHOD(ovArgTypes, method.returnTypeWithThis, method.methodType.paramTypes)

					AstMethod(
						containingClass = overlayClass,
						id = program.lastMethodId++,
						name = method.name,
						methodType = newMethodType,
						annotations = method.annotations,
						signature = method.signature,
						genericSignature = method.genericSignature,
						defaultTag = method.defaultTag,
						modifiers = method.modifiers.with(AstModifiers.ACC_STATIC),
						generateBody = {
							val body = method.generateBody()
							body?.visit(object : AstVisitor() {
								override fun visit(expr: AstExpr.THIS) = expr.replaceWith(AstExpr.PARAM(thisArg))
								override fun visit(expr: AstExpr.PARAM) = expr.replaceWith(AstExpr.PARAM(newParams[expr.argument.index]))
							})
							body
						},
						//bodyRef = method.bodyRef,
						parameterAnnotations = method.parameterAnnotations
					)
				}

				methodToOverlayMethod[method.ref] = overlayMethod.ref
				overlayClass += overlayMethod

			}
		}

		if (methodToOverlayMethod.isNotEmpty()) {
			val methodReplacer = object : AstVisitor() {
				override fun visit(expr: AstExpr.CALL_BASE) {
					super.visit(expr)
					val overlayMethod = methodToOverlayMethod[expr.method] ?: return
					if (expr is AstExpr.CALL_STATIC) {
						expr.replaceWith(AstExpr.CALL_STATIC(overlayMethod, expr.args.map { it.value }, expr.isSpecial))
					} else if (expr is AstExpr.CALL_INSTANCE) {
						expr.replaceWith(AstExpr.CALL_STATIC(overlayMethod, listOf(expr.obj.value) + expr.args.map { it.value }, expr.isSpecial))
					} else {
						println("Unsupported overlay call (no static, no instance) to $expr")
					}
				}
			}

			//println(methodToOverlayMethod)

			for (clazz in program.classes) {
				for (method in clazz.methods) {
					val body = method.body ?: continue
					methodReplacer.visit(body)
				}
			}
			//println(nativeClassesWithOverlays)
		}
	}
}

