package com.jtransc.meta

import com.jtransc.ast.AstProgram

interface JTranscMetaPlugin {
	fun process(program: AstProgram): Unit
}