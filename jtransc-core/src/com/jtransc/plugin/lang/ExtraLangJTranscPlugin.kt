package com.jtransc.plugin.lang

import com.jtransc.ast.AstExpr
import com.jtransc.ast.AstProgram
import com.jtransc.ast.expr
import com.jtransc.ast.fqname
import com.jtransc.plugin.JTranscPlugin
import j.ProgramReflection
import java.lang.reflect.Method

class ExtraLangJTranscPlugin : JTranscPlugin() {
	override fun processAfterTreeShaking(program: AstProgram) {
		if (ProgramReflection::class.java.fqname !in program) return // ProgramReflection not referenced!
		val ProgramReflection = program[ProgramReflection::class.java.name.fqname]
		val ProgramReflection_getMethodByInfo = ProgramReflection.getMethodWithoutOverrides(j.ProgramReflection::getMethodByInfo.name) ?: return

		ProgramReflection_getMethodByInfo.replaceBodyOptBuild { args ->
			val (clazz, methodInfo) = args

			val constructor = program[Method::class.java.fqname].constructors.first()
			RETURN(AstExpr.NEW_WITH_CONSTRUCTOR(constructor.ref, listOf(clazz.expr, methodInfo.expr)))
		}
	}
}