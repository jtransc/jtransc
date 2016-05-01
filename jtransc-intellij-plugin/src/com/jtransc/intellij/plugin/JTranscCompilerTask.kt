package com.jtransc.intellij.plugin

import com.intellij.openapi.compiler.CompileContext
import com.intellij.openapi.compiler.CompileTask

class JTranscCompilerTask : CompileTask {
	override fun execute(context: CompileContext?): Boolean {
		return true
	}
}