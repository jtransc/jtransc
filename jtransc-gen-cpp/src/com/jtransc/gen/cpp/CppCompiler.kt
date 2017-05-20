package com.jtransc.gen.cpp

import com.jtransc.JTranscSystem
import com.jtransc.JTranscVersion
import com.jtransc.error.invalidOp
import com.jtransc.gen.common.BaseCompiler
import com.jtransc.io.ProcessUtils
import com.jtransc.vfs.ExecOptions
import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.get
import java.io.File

object CppCompiler {
	class Context(val desiredCompiler: String = "any")
	interface Compiler

	val CMAKE by lazy { ProcessUtils.which("cmake") }

	val CPP_COMMON_FOLDER by lazy {
		val jtranscVersion = JTranscVersion.getVersion().replace('.', '_');
		val folder = File(System.getProperty("user.home") + "/.jtransc/cpp/" + jtranscVersion)
		folder.mkdirs()
		LocalVfs(folder)
	}

	fun genCommand(programFile: File, debug: Boolean = false, libs: List<String> = listOf()): List<String> {
		// -O0 = 23s && 7.2MB
		// -O4 = 103s && 4.3MB

		if (JTranscSystem.isWindows()) {
			val cmake = CMAKE ?: invalidOp("Can't find cmake in the path.")
			val cmakeCache = programFile.parentFile["CMakeCache.txt"]
			if (!cmakeCache.exists()) {
				LocalVfs(cmakeCache.parentFile).exec(listOf(cmake, "."), ExecOptions(sysexec = true, fixencoding = false, passthru = true))
			}
			val config = if (debug) "Debug" else "Release"
			//return listOf(cmake, "--build", ".", "--target", "ALL_BUILD", "-DCMAKE_GENERATOR_PLATFORM=x64", "--config", config)
			return listOf(cmake, "--build", ".", "--target", "ALL_BUILD", "--config", config)
		} else {
			val compiler = listOf(GPP, CLANG).firstOrNull { it.available } ?: invalidOp("Can't find CPP compiler (g++ or clang), please install one of them and put in the path.")
			return compiler.genCommand(programFile, debug, libs)
		}
	}

	fun addCommonCmdArgs(cmdAndArgs: MutableList<String>) {
		val commonFolder = CPP_COMMON_FOLDER.realpathOS

		when {
			JTranscSystem.isWindows() -> cmdAndArgs.add("-I${commonFolder}/jni-headers/win32/")
			JTranscSystem.isLinux() -> cmdAndArgs.add("-I${commonFolder}/jni-headers/linux")
			JTranscSystem.isMac() -> cmdAndArgs.add("-I${commonFolder}/jni-headers/mac")
			else -> {
				System.err.println("Unkown OS detected: Aborting.")
				System.exit(-1)
			}
		}
		cmdAndArgs.add("-I$commonFolder/jni-headers/")
		cmdAndArgs.add("-I$commonFolder/boost/compiled-libs/include/")
		cmdAndArgs.add("-I$commonFolder/bdwgc/include/")
		cmdAndArgs.add("$commonFolder/bdwgc/.libs/libgccpp.a")
		cmdAndArgs.add("$commonFolder/bdwgc/.libs/libgc.a")
		cmdAndArgs.add("$commonFolder/boost/compiled-libs/lib/libboost_thread.a")
		cmdAndArgs.add("$commonFolder/boost/compiled-libs/lib/libboost_system.a")
	}

	object CLANG : BaseCompiler("clang++") {
		override fun genCommand(programFile: File, debug: Boolean, libs: List<String>): List<String> {
			val cmdAndArgs = arrayListOf<String>()
			cmdAndArgs += "clang++"
			cmdAndArgs += "-std=c++11"
			if (JTranscSystem.isWindows()) cmdAndArgs += "-fms-compatibility-version=19.00"
			if (debug) cmdAndArgs += "-g"
			cmdAndArgs += if (debug) "-O0" else "-O3"
			cmdAndArgs += "-fexceptions"
			cmdAndArgs += "-Wno-parentheses-equality"
			cmdAndArgs += "-Wimplicitly-unsigned-literal"
			if (!JTranscSystem.isMac()) cmdAndArgs += "-pthread"
			cmdAndArgs += "-frtti"
			cmdAndArgs += programFile.absolutePath
			addCommonCmdArgs(cmdAndArgs)
			if (!JTranscSystem.isMac()) cmdAndArgs += "-lrt"
			for (lib in libs) cmdAndArgs += "-l$lib"
			return cmdAndArgs
		}
	}

	object GPP : BaseCompiler("g++") {
		override fun genCommand(programFile: File, debug: Boolean, libs: List<String>): List<String> {
			val cmdAndArgs = arrayListOf<String>()
			cmdAndArgs += "g++"
			cmdAndArgs += "-w"
			cmdAndArgs += "-std=c++11"
			if (debug) cmdAndArgs += "-g"
			cmdAndArgs += if (debug) "-O0" else "-O3"
			cmdAndArgs += "-fexceptions"
			cmdAndArgs += "-frtti"
			if (!JTranscSystem.isMac()) cmdAndArgs += "-pthread"
			cmdAndArgs += programFile.absolutePath
			addCommonCmdArgs(cmdAndArgs)
			if (!JTranscSystem.isMac()) cmdAndArgs += "-lrt"
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