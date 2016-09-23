package com.jtransc.meta

import com.jtransc.ast.*

/**
 * This class aims to create classes to perform reflection on available classes
 */
class MetaReflection : JTranscMetaPlugin {
	override fun process(program: AstProgram) {
		val types = program.types
		val oldClasses = program.classes.toList()
		val ProgramReflectionClass = program.createClass(FqName("jtransc.ProgramReflection"), FqName("java.lang.Object"))
		val ProgramReflectionClass_getAllClasses = ProgramReflectionClass.createMethod(types, "getAllClasses", AstTypeBuild { METHOD(ARRAY(STRING)) }, isStatic = true) {
			val out = AstLocal(0, "out", AstTypeBuild { ARRAY(STRING) })
			stms(
				out assignTo AstExpr.NEW_ARRAY(AstTypeBuild { ARRAY(STRING) }, listOf(oldClasses.size.lit)),
				AstStm.SET_ARRAY_LITERALS(out.local, 0,
					oldClasses.map { it.fqname }.map { it.lit.box }
				),
				AstStm.RETURN(out.local)
			)
		}
	}
}

