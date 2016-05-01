package com.jtransc.intellij.plugin

import com.intellij.openapi.compiler.CompileScope

class JTranscCompiler : com.intellij.openapi.compiler.Compiler {
	override fun validateConfiguration(scope: CompileScope?): Boolean {
		//scope.
		//throw UnsupportedOperationException()
		return true
	}

	override fun getDescription(): String {
		//throw UnsupportedOperationException()
		return "JTRansc Compiler"
	}

}