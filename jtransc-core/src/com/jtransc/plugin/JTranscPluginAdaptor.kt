package com.jtransc.plugin

import com.jtransc.ast.AstProgram
import com.jtransc.ast.AstType

abstract class JTranscPluginAdaptor : JTranscPlugin {
	override fun onStartBuilding(program: AstProgram) {
	}

	override fun onAfterClassDiscovered(clazz: AstType.REF, program: AstProgram) {
	}

	override fun onAfterAllClassDiscovered(program: AstProgram) {
	}

	override fun processBeforeTreeShaking(programBase: AstProgram) {
	}

	override fun processAfterTreeShaking(program: AstProgram) {
	}
}