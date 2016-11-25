package com.jtransc.gen.d

import com.jtransc.error.invalidOp
import com.jtransc.io.ProcessUtils
import java.io.File

object DCompiler {
	fun genCommand(programFile: File, debug: Boolean = false, libs: List<String> = listOf()): List<String> {
		val provider = listOf(DMD, GDC, LDC).firstOrNull { it.available } ?: invalidOp("Can't find D compiler (dmd, gdc or ldc), please install one of them and put in the path.")
		return provider.genCommand(programFile, debug, libs)
	}

	abstract class Provider(val cmdName: String) {
		val cmd by lazy { ProcessUtils.which(cmdName) }
		val available by lazy { cmd != null }
		abstract fun genCommand(programFile: File, debug: Boolean = false, libs: List<String> = listOf()): List<String>
	}

	object DMD : Provider("dmd") {
		override fun genCommand(programFile: File, debug: Boolean, libs: List<String>): List<String> {
			if (debug) {
				// @TODO: DMD bug -gc allows to print good stacktraces but -gc makes compiler fail on large files
				//return listOf(cmd!!, "-debug", "-gc", "-gx", programFile.absolutePath)
				return listOf(cmd!!, "-debug", programFile.absolutePath)
			} else {
				return listOf(cmd!!, "-release", "-O", "-o-", programFile.absolutePath)
			}
		}
	}

	object GDC : Provider("gdc") {
		override fun genCommand(programFile: File, debug: Boolean, libs: List<String>): List<String> {
			if (debug) {
				return listOf(cmd!!, "-O0", programFile.absolutePath)
			} else {
				return listOf(cmd!!, "-s", "-O3", programFile.absolutePath)
			}
		}
	}

	object LDC : Provider("ldc") {
		override fun genCommand(programFile: File, debug: Boolean, libs: List<String>): List<String> {
			if (debug) {
				return listOf(cmd!!, "-O0", programFile.absolutePath)
			} else {
				return listOf(cmd!!, "-s", "-O3", programFile.absolutePath)
			}
		}
	}
}

