package com.jtransc.gen.cs

import com.jtransc.env.OS
import com.jtransc.error.invalidOp
import com.jtransc.io.ProcessUtilsBase
import com.jtransc.sys.Arch
import com.jtransc.vfs.RootLocalVfs
import com.jtransc.vfs.SyncVfsFile
import java.io.File

class CSharpCompiler(
	val rootVfs: SyncVfsFile = RootLocalVfs()
) {
	val processUtils = ProcessUtilsBase(rootVfs)

	val dotNetV4Folder: SyncVfsFile by lazy {
		//println(System.getProperty("os.arch"))

		val ProgramFilesX86 = rootVfs.getenv("ProgramFiles(x86)") ?: rootVfs.getenv("ProgramFiles")
		val folders = arrayListOf<String>()
		folders += "$ProgramFilesX86\\Microsoft Visual Studio\\2017\\Community\\MSBuild\\15.0\\Bin\\Roslyn"
		//C:\Program Files (x86)\Microsoft Visual Studio\2017\Community\MSBuild\15.0\Bin\Roslyn

		val cscFolder = processUtils.findCommandInPathsOrNull(folders, "csc")

		if (cscFolder != null) {
			rootVfs[cscFolder]
		} else {
			val folder = when (Arch.CURRENT) {
				Arch.X86 -> rootVfs.getenv("SystemRoot") + "\\Microsoft.NET\\Framework"
				Arch.X64 -> rootVfs.getenv("SystemRoot") + "\\Microsoft.NET\\Framework64"
				else -> rootVfs.getenv("SystemRoot") + "\\Microsoft.NET\\Framework"
			// C:\Program Files (x86)\Microsoft Visual Studio\2017\Community\MSBuild\15.0\Bin\Roslyn
			}
			val local = rootVfs[folder]

			if (!local.exists) invalidOp("JTransc can't find .net framework v4")

			val dotnetv4 = local.listdir().filter { it.name.startsWith("v4") }.firstOrNull() ?: invalidOp("JTransc can't find .net framework v4")
			dotnetv4.file
		}
	}

	data class Compiler(val path: String, val isMono: Boolean)

	fun getCompiler(extraParams: Map<String?, String?>): Compiler {
		val forceMono = "CSHARP_USE_MONO" in extraParams
		val csharpCommand = extraParams["CSHARP_CMD"]
		val csharpCommandWin = extraParams["CSHARP_CMD_WIN"] ?: csharpCommand
		val csharpCommandUnix = extraParams["CSHARP_CMD_UNIX"] ?: csharpCommand

		return when {
			forceMono || !OS.isWindows -> Compiler(csharpCommandWin ?: MCS, isMono = true)
			else -> Compiler(csharpCommandUnix ?: CSC, isMono = false)
		}
	}

	fun genCommand(programFile: File, debug: Boolean = false, libs: List<String> = listOf(), extraParams: Map<String?, String?>): List<String> {
		val compiler = getCompiler(extraParams)
		when {
			compiler.isMono -> return listOf(compiler.path, "-debug", "-unsafe+", "-checked-", "-optimize+", "-define:UNSAFE", "-nowarn:169", programFile.absolutePath)
			else -> return listOf(compiler.path, "/debug:full", "/unsafe+", "/checked-", "/optimize+", "/define:UNSAFE", "/nowarn:169", programFile.absolutePath)
		}
	}

	val MCS by lazy { "mcs" }
	val CSC by lazy { processUtils.locateCommand("csc") ?: dotNetV4Folder["csc"].path }
}
