package com.jtransc.plugin

import com.jtransc.ast.AstProgram
import com.jtransc.ast.AstType

interface JTranscPlugin {
	fun onStartBuilding(program: AstProgram): Unit
	fun onAfterAllClassDiscovered(program: AstProgram): Unit
	fun onAfterClassDiscovered(clazz: AstType.REF, program: AstProgram): Unit
	fun processAfterTreeShaking(program: AstProgram): Unit
	fun processBeforeTreeShaking(programBase: AstProgram): Unit
}