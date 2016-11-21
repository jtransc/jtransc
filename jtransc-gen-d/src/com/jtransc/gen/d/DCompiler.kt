package com.jtransc.gen.d

import java.io.File

object DCompiler {
	fun genCommand(programFile: File, debug: Boolean = false, libs: List<String> = listOf()): List<String> {
		return listOf("dmd", programFile.absolutePath)
	}
}