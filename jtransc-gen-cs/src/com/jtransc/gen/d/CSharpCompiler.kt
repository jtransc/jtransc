package com.jtransc.gen.d

import com.jtransc.error.invalidOp
import com.jtransc.vfs.LocalVfs
import java.io.File

object CSharpCompiler {
	val dotNetV4Folder by lazy {
		val folder = System.getenv("SystemRoot") + "\\Microsoft.NET\\Framework"
		val local = LocalVfs(File(folder))
		val dotnetv4 = local.listdir().filter { it.name.startsWith("v4") }.firstOrNull() ?: invalidOp("JTransc can't find .net framework v4")
		dotnetv4.file
	}

	fun genCommand(programFile: File, debug: Boolean = false, libs: List<String> = listOf()): List<String> {
		return listOf(dotNetV4Folder["csc"].realpathOS, programFile.absolutePath)
	}
}
