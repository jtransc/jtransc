package com.jtransc.gen.common

import com.jtransc.io.ProcessUtils
import java.io.File

abstract class BaseCompiler(val cmdName: String) {
	val cmd by lazy { ProcessUtils.which(cmdName) }
	val available by lazy { cmd != null }
	abstract fun genCommand(programFile: File, debug: Boolean = false, libs: List<String> = listOf()): List<String>
}
