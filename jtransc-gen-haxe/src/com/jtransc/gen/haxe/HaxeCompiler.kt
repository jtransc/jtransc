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
	val NekoVersion = "2.1.0"

	val haxeCompilerFile: String by lazy {
		//"haxe-$HaxeVersion-linux32.tar.gz"
		if (JTranscSystem.isWindows()) "haxe-$HaxeVersion-win.zip"
		else if (JTranscSystem.isMac()) "haxe-$HaxeVersion-osx.tar.gz"
		else if (JTranscSystem.isLinux() && JTranscSystem.isOs32()) "haxe-$HaxeVersion-linux32.tar.gz"
		else if (JTranscSystem.isLinux() && JTranscSystem.isOs64()) "haxe-$HaxeVersion-linux64.tar.gz"
		else invalidOp("Not supported operaning system. Just supporting windows, osx and linux.")
	}

	val nekoFile: String by lazy {
		//"haxe-$HaxeVersion-linux32.tar.gz"
		if (JTranscSystem.isWindows()) "neko-$NekoVersion-win.zip"
		else if (JTranscSystem.isMac()) "neko-$NekoVersion-osx64.tar.gz"
		else if (JTranscSystem.isLinux() && JTranscSystem.isOs32()) "neko-$NekoVersion-linux.tar.gz"
		else if (JTranscSystem.isLinux() && JTranscSystem.isOs64()) "neko-$NekoVersion-linux64.tar.gz"
		else invalidOp("Not supported operaning system. Just supporting windows, osx and linux.")
	}

	//http://haxe.org/website-content/downloads/3.3.0-rc.1/downloads/haxe-3.3.0-rc.1-win.zip
	//http://nekovm.org/media/neko-2.1.0-linux64.tar.gz
	val haxeCompilerUrl: URL by lazy { URL("http://haxe.org/website-content/downloads/$HaxeVersion/downloads/$haxeCompilerFile") }
	val nekoUrl: URL by lazy { URL("http://nekovm.org/media/$nekoFile") }
	val jtranscHaxeFolder: String by lazy { JTranscSystem.getUserHome() + "/.jtransc/haxe" }
	val jtranscNekoFolder: String by lazy { JTranscSystem.getUserHome() + "/.jtransc/neko" }

	val haxeCompilerUrlVfs: SyncVfsFile by lazy { UrlVfs(haxeCompilerUrl) }
	val haxeCompilerLocalFileVfs: SyncVfsFile by lazy { LocalVfsEnsureDirs(File(jtranscHaxeFolder)).access(haxeCompilerFile) }
	val haxeCompilerLocalFolderVfs: SyncVfsFile by lazy { LocalVfsEnsureDirs(File("$jtranscHaxeFolder/$HaxeVersion")) }

	val nekoUrlVfs: SyncVfsFile by lazy { UrlVfs(nekoUrl) }
	val nekoLocalFileVfs: SyncVfsFile by lazy { LocalVfsEnsureDirs(File(jtranscNekoFolder)).access(nekoFile) }
	val nekoLocalFolderVfs: SyncVfsFile by lazy { LocalVfsEnsureDirs(File("$jtranscNekoFolder/$NekoVersion")) }

	fun getHaxeTargetLibraries(subtarget: String): List<String> = when (subtarget) {
		"cpp", "windows", "linux", "mac", "osx" -> listOf("hxcpp")
		"c#", "cs", "csharp" -> listOf("hxcs")
		"java", "jvm" -> listOf("hxjava")
		else -> listOf()
	}

	var ensured = false

	fun ensureHaxeCompilerVfs(): SyncVfsFile {
		if (!ensured) {
			log.info("ensureHaxeCompilerVfs:")

			/////////////////////////////////////////
			// HAXE
			/////////////////////////////////////////
			if (!haxeCompilerLocalFileVfs.exists) {
				log.info("Downloading haxe: $haxeCompilerUrl...")
				haxeCompilerUrlVfs.copyTo(haxeCompilerLocalFileVfs)
			}
			if (!haxeCompilerLocalFolderVfs["std"].exists) {
				val compvfsBase = CompressedVfs(haxeCompilerLocalFileVfs.realfile).firstRecursive { it.file.name == "std" }.file.parent
				compvfsBase.copyTreeTo(haxeCompilerLocalFolderVfs, doLog = false)
				if (!JTranscSystem.isWindows()) {
					haxeCompilerLocalFolderVfs["haxe"].chmod(FileMode.fromString("-rwxr-xr-x"))
					haxeCompilerLocalFolderVfs["haxelib"].chmod(FileMode.fromString("-rwxr-xr-x"))
				}
			}

			if (!haxeCompilerLocalFolderVfs["lib"].exists) {
				haxeCompilerLocalFolderVfs.passthru(haxeCompilerLocalFolderVfs["haxelib"].realpathOS, "setup", haxeCompilerLocalFolderVfs["lib"].realpathOS)
			}

			/////////////////////////////////////////
			// NEKO
			/////////////////////////////////////////
			if (!nekoLocalFileVfs.exists) {
				log.info("Downloading neko: $nekoUrl...")
				nekoUrlVfs.copyTo(nekoLocalFileVfs)
			}

			if (!nekoLocalFolderVfs["std.ndll"].exists) {
				val compvfsBase = CompressedVfs(nekoLocalFileVfs.realfile).firstRecursive { it.file.name == "std.ndll" }.file.parent
				compvfsBase.copyTreeTo(nekoLocalFolderVfs, doLog = false)

				if (!JTranscSystem.isWindows()) {
					nekoLocalFolderVfs["neko"].chmod(FileMode.fromString("-rwxr-xr-x"))
					nekoLocalFolderVfs["nekoc"].chmod(FileMode.fromString("-rwxr-xr-x"))
					nekoLocalFolderVfs["nekoml"].chmod(FileMode.fromString("-rwxr-xr-x"))
				}
			}

			ensured = true
		}

		return haxeCompilerLocalFolderVfs
	}

	fun getExtraEnvs(): Map<String, String> = mapOf(
		"HAXE_STD_PATH" to haxeCompilerLocalFolderVfs["std"].realpathOS,
		"HAXE_HOME" to haxeCompilerLocalFolderVfs.realpathOS,
		"NEKOPATH" to nekoLocalFolderVfs.realpathOS
	)

	fun ensureHaxeSubtarget(subtarget: String) {
		ensureHaxeCompilerVfs()

		for (lib in getHaxeTargetLibraries(subtarget)) {
			HaxeLib.installIfNotExists(HaxeLib.LibraryRef.fromVersion(lib))
		}
	}
}
