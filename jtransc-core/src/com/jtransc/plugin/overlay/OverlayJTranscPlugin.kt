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

				if (method.isStatic) {
					val overlayMethod = AstMethod(
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
						bodyRef = method.bodyRef,
						parameterAnnotations = method.parameterAnnotations,
						ref = AstMethodRef(overlayClass.name, method.name, method.methodType)
					)
					methodToOverlayMethod[method.ref] = overlayMethod.ref
					overlayClass += overlayMethod
				} else {
					println("WARNING: Not supported instance overlay methods yet!")
				}
			}
		}

		if (methodToOverlayMethod.isNotEmpty()) {
			val methodReplacer = object : AstVisitor() {
				override fun visit(expr: AstExpr.CALL_BASE) {
					super.visit(expr)
					val overlayMethod = methodToOverlayMethod[expr.method]
					if (overlayMethod != null) {
						if (expr is AstExpr.CALL_STATIC) {
							expr.replaceWith(AstExpr.CALL_STATIC(overlayMethod, expr.args.map { it.value }, expr.isSpecial))
						} else {
							println("WARNING: Not supported instance overlay methods yet!")
						}
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

