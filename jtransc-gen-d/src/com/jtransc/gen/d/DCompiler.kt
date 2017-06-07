package com.jtransc.gen.d

import com.jtransc.error.invalidOp
import com.jtransc.gen.common.BaseCompiler
import java.io.File

object DCompiler {
	fun genCommand(programFile: File, debug: Boolean = false, libs: List<String> = listOf(), extraVars: Map<String, List<String>>): List<String> {
		val provider = listOf(DMD, GDC, LDC).firstOrNull { it.available } ?: invalidOp("Can't find D compiler (dmd, gdc or ldc), please install one of them and put in the path.")
		return provider.genCommand(programFile, BaseCompiler.Config(debug, libs, extraVars = extraVars))
	}

	object DMD : BaseCompiler("dmd") {
		override fun genCommand(programFile: File, config: Config): List<String> {
			if (config.debug) {
				// @TODO: DMD bug -gc allows to print good stacktraces but -gc makes compiler fail on large files
				//return listOf(cmd!!, "-debug", "-gc", "-gx", programFile.absolutePath)
				return listOf(cmd!!, "-debug", "-m64", programFile.absolutePath)
			} else {
				//return listOf(cmd!!, "-release", "-O", "-m64", "-inline", programFile.absolutePath)
				return listOf(cmd!!, "-release", "-O", "-m64", programFile.absolutePath)
			}
		}
	}

	object GDC : BaseCompiler("gdc") {
		override fun genCommand(programFile: File, config: Config): List<String> {
			if (config.debug) {
				return listOf(cmd!!, "-fdebug=1", "-g", "-O0", programFile.absolutePath)
			} else {
				return listOf(cmd!!, "-s", "-O3", programFile.absolutePath)
			}
		}
	}

	object LDC : BaseCompiler("ldc") {
		override fun genCommand(programFile: File, config: Config): List<String> {
			if (config.debug) {
				return listOf(cmd!!, "-O0", programFile.absolutePath)
			} else {
				return listOf(cmd!!, "-s", "-O3", programFile.absolutePath)
			}
		}
	}
}

