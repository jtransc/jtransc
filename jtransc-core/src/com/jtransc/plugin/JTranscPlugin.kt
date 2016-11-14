package com.jtransc.plugin

import com.jtransc.ast.AstProgram

interface JTranscPlugin {
	fun processAfterTreeShaking(program: AstProgram): Unit
	fun processBeforeTreeShaking(programBase: AstProgram): Unit
}