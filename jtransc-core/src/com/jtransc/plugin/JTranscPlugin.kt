package com.jtransc.plugin

import com.jtransc.ast.AstProgram
import com.jtransc.ast.AstType

abstract class JTranscPlugin {
	open val priority: Int = 0

	open fun onStartBuilding(program: AstProgram): Unit {

	}
	open fun onAfterAllClassDiscovered(program: AstProgram): Unit {

	}
	open fun onAfterClassDiscovered(clazz: AstType.REF, program: AstProgram): Unit {

	}
	open fun processAfterTreeShaking(program: AstProgram): Unit {

	}
	open fun processBeforeTreeShaking(programBase: AstProgram): Unit {

	}
}