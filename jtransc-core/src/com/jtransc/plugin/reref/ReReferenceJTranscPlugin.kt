package com.jtransc.plugin.reref

import com.jtransc.ast.*
import com.jtransc.gen.TargetName
import com.jtransc.plugin.JTranscPlugin

class ReReferenceJTranscPlugin : JTranscPlugin() {
	val refs = hashMapOf<AstType.REF, AstType.REF>()

	override fun onAfterAllClassDiscovered(program: AstProgram) {
		val target = program.injector.get<TargetName>()
		for (clazz in program.classes) {
			val reref = clazz.getRerefFor(target) ?: continue
			program.addReference(reref.ref, clazz.ref)
			refs[clazz.name.ref] = reref.ref
		}

		if (refs.all { program.contains(it.value.name) }) {
			for (clazz in program.classes) {
				if (clazz.ref in refs) continue

				for (method in clazz.methods) {
					val body = method.body ?: continue
					var updated = false
					body.visit(object : AstVisitor() {
						override fun visit(expr: AstExpr.NEW_WITH_CONSTRUCTOR) {
							super.visit(expr)
							val ref = refs[expr.constructor.containingClassType]
							if (ref != null) {
								expr.box.replaceWith(AstExpr.NEW_WITH_CONSTRUCTOR(expr.constructor.withClass(ref), expr.args.unbox))
								updated = true
							}
						}

						override fun visit(expr: AstExpr.CALL_BASE) {
							super.visit(expr)
							val ref = refs[expr.method.containingClassType]
							if (ref != null) {
								expr.box.replaceWith(expr.withReplacedMethod(expr.method.withClass(ref)))
								updated = true
							}
						}


					})
					if (updated) method.replaceBody(body.stm)
				}
			}
		}
	}
}