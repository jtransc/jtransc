package com.jtransc.gen.haxe

import com.jtransc.JTranscSystem
import com.jtransc.error.invalidOp
import com.jtransc.log.log
import com.jtransc.numeric.toInt
import com.jtransc.vfs.*
import java.io.File
import java.net.URL

object HaxeCompiler {
	val HaxeVersion = "3.3.0-rc.1"

	val haxeCompilerFile: String by lazy {
		//"haxe-$HaxeVersion-linux32.tar.gz"
		if (JTranscSystem.isWindows()) "haxe-$HaxeVersion-win.zip"
		else if (JTranscSystem.isMac()) "haxe-$HaxeVersion-osx.tar.gz"
		else if (JTranscSystem.isLinux() && JTranscSystem.isOs32()) "haxe-$HaxeVersion-linux32.tar.gz"
		else if (JTranscSystem.isLinux() && JTranscSystem.isOs64()) "haxe-$HaxeVersion-linux64.tar.gz"
		else invalidOp("Not supported operaning system. Just supporting windows, osx and linux.")
	}

	//http://haxe.org/website-content/downloads/3.3.0-rc.1/downloads/haxe-3.3.0-rc.1-win.zip
	val haxeCompilerUrl: URL by lazy { URL("http://haxe.org/website-content/downloads/$HaxeVersion/downloads/$haxeCompilerFile") }
	val jtranscFolder: String by lazy { JTranscSystem.getUserHome() + "/.jtransc" }

	val haxeCompilerUrlVfs: SyncVfsFile by lazy { UrlVfs(haxeCompilerUrl) }
	val haxeCompilerLocalFileVfs: SyncVfsFile by lazy { LocalVfsEnsureDirs(File(jtranscFolder)).access(haxeCompilerFile) }

	val haxeCompilerLocalFolderVfs: SyncVfsFile by lazy { LocalVfsEnsureDirs(File("$jtranscFolder/$HaxeVersion")) }

	fun getHaxeTargetLibraries(subtarget: String): List<String> = when (subtarget) {
		"cpp", "windows", "linux", "mac", "osx" -> listOf("hxcpp")
		"c#", "cs", "csharp" -> listOf("hxcs")
		"java", "jvm" -> listOf("hxjava")
		else -> listOf()
	}

	fun ensureHaxeCompilerVfs(): SyncVfsFile {
		log.info("ensureHaxeCompilerVfs:")
		if (!haxeCompilerLocalFileVfs.exists) {
			log.info("Downloading haxe: $haxeCompilerUrl...")
			haxeCompilerUrlVfs.copyTo(haxeCompilerLocalFileVfs)
		}
		if (!haxeCompilerLocalFolderVfs["std"].exists) {
			val compvfs = CompressedVfs(haxeCompilerLocalFileVfs.realfile)
			val compvfsBase = compvfs.firstRecursive { it.file.name == "std" }.file.parent
			compvfsBase.copyTreeTo(haxeCompilerLocalFolderVfs)
		}

		if (!haxeCompilerLocalFolderVfs["lib"].exists) {
			haxeCompilerLocalFolderVfs.passthru("haxelib", "setup", haxeCompilerLocalFolderVfs["lib"].realpathOS)
			if (!JTranscSystem.isWindows()) {
				haxeCompilerLocalFolderVfs["haxe"].chmod("0777".toInt(8))
				haxeCompilerLocalFolderVfs["haxelib"].chmod("0777".toInt(8))
			}
		}

		return haxeCompilerLocalFolderVfs
	}

	fun ensureHaxeSubtarget(subtarget: String) {
		ensureHaxeCompilerVfs()

		for (lib in getHaxeTargetLibraries(subtarget)) {
			HaxeLib.installIfNotExists(HaxeLib.LibraryRef.fromVersion(lib))
		}
	}
}
