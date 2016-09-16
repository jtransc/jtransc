package com.jtransc.gen.cpp

import java.io.File

object CppCompiler {
	class Context(val desiredCompiler: String = "any")
	interface Compiler
}

object GccCompiler {

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