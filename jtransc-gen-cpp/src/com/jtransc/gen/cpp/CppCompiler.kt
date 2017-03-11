package com.jtransc.gen.cpp

import com.jtransc.JTranscSystem
import com.jtransc.error.invalidOp
import com.jtransc.gen.common.BaseCompiler
import java.io.File

object CppCompiler {
	class Context(val desiredCompiler: String = "any")
	interface Compiler

	fun genCommand(programFile: File, debug: Boolean = false, libs: List<String> = listOf()): List<String> {
		// -O0 = 23s && 7.2MB
		// -O4 = 103s && 4.3MB

		val compiler = listOf(GPP, CLANG).firstOrNull { it.available } ?: invalidOp("Can't find CPP compiler (gpp or clang), please install one of them and put in the path.")
		return compiler.genCommand(programFile, debug, libs)
	}

	object CLANG : BaseCompiler("clang++") {
		override fun genCommand(programFile: File, debug: Boolean, libs: List<String>): List<String> {
			val cmdAndArgs = arrayListOf<String>()
			cmdAndArgs += "clang++"
			cmdAndArgs += "-std=c++0x"
			if (JTranscSystem.isWindows()) cmdAndArgs += "-fms-compatibility-version=19.00"
			if (debug) cmdAndArgs += "-g"
			cmdAndArgs += if (debug) "-O0" else "-O3"
			cmdAndArgs += "-fexceptions"
			cmdAndArgs += "-Wno-parentheses-equality"
			cmdAndArgs += "-Wimplicitly-unsigned-literal"
			cmdAndArgs += "-frtti"
			cmdAndArgs += programFile.absolutePath
			for (lib in libs) cmdAndArgs += "-l$lib"
			return cmdAndArgs
		}
	}

	object GPP : BaseCompiler("g++") {
		override fun genCommand(programFile: File, debug: Boolean, libs: List<String>): List<String> {
			val cmdAndArgs = arrayListOf<String>()
			cmdAndArgs += "g++"
			cmdAndArgs += "-w"
			cmdAndArgs += "-std=c++0x"
			if (debug) cmdAndArgs += "-g"
			cmdAndArgs += if (debug) "-O0" else "-O3"
			cmdAndArgs += "-fexceptions"
			cmdAndArgs += "-frtti"
			cmdAndArgs += programFile.absolutePath
			for (lib in libs) cmdAndArgs += "-l$lib"
			return cmdAndArgs
		}
	}
}

object MsVcCompiler {
	val COMMON_TOOLS_ENVS = listOf(
		"VS140COMNTOOLS",
		"VS120COMNTOOLS",
		"VS110COMNTOOLS",
		"VS100COMNTOOLS"
	)

	fun detect(context: CppCompiler.Context) {
		val commonTools = COMMON_TOOLS_ENVS.map { System.getenv(it) }.filterNotNull().firstOrNull()

	}
}

object CygwinGccCompiler {
	private val POSSIBLE_GCC = listOf(
		"C:/cygwin64/bin/gcc.exe",
		"C:/cygwin/bin/gcc.exe"
	)

	fun detect(context: CppCompiler.Context) {
		//File()
	}
}