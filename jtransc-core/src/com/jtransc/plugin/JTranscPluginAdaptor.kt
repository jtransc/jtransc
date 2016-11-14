package com.jtransc.plugin

import com.jtransc.ast.AstProgram

abstract class JTranscPluginAdaptor : JTranscPlugin {
	override fun processBeforeTreeShaking(programBase: AstProgram) {
	}

	override fun processAfterTreeShaking(program: AstProgram) {
	}
}