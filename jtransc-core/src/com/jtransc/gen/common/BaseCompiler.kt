package com.jtransc.gen.common

import com.jtransc.io.ProcessUtils
import java.io.File

abstract class BaseCompiler(val cmdName: String) {
	val cmd by lazy { ProcessUtils.which(cmdName) }
	val available by lazy { cmd != null }
	abstract fun genCommand(programFile: File, config: Config): List<String>

	data class Config(
		val debug: Boolean = false,
		val libs: List<String> = listOf(),
		val includeFolders: List<String> = listOf(),
		val libsFolders: List<String> = listOf(),
		val defines: List<String> = listOf(),
		val extraVars: Map<String, List<String>>
	)
}
