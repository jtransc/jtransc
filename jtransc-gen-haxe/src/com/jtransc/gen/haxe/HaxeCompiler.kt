package com.jtransc.gen.haxe

import com.jtransc.JTranscSystem
import com.jtransc.error.invalidOp
import com.jtransc.log.log
import com.jtransc.vfs.*
import java.io.File
import java.net.URL

object HaxeCompiler {
	val HAXE_VERSION = "3.3.0-rc.1"
	val NEKO_VERSION = "2.1.0"

	val HAXE_FILE: String by lazy {
		if (JTranscSystem.isWindows()) "haxe-$HAXE_VERSION-win.zip"
		else if (JTranscSystem.isMac()) "haxe-$HAXE_VERSION-osx.tar.gz"
		else if (JTranscSystem.isLinux() && JTranscSystem.isOs32()) "haxe-$HAXE_VERSION-linux32.tar.gz"
		else if (JTranscSystem.isLinux() && JTranscSystem.isOs64()) "haxe-$HAXE_VERSION-linux64.tar.gz"
		else invalidOp("Not supported operaning system. Just supporting windows, osx and linux.")
	}

	val NEKO_FILE: String by lazy {
		if (JTranscSystem.isWindows()) "neko-$NEKO_VERSION-win.zip"
		else if (JTranscSystem.isMac()) "neko-$NEKO_VERSION-osx64.tar.gz"
		else if (JTranscSystem.isLinux() && JTranscSystem.isOs32()) "neko-$NEKO_VERSION-linux.tar.gz"
		else if (JTranscSystem.isLinux() && JTranscSystem.isOs64()) "neko-$NEKO_VERSION-linux64.tar.gz"
		else invalidOp("Not supported operaning system. Just supporting windows, osx and linux.")
	}

	//https://github.com/jtransc/haxe-releases/releases/download/neko-2.1.0/neko-2.1.0-linux.tar.gz
	//http://haxe.org/website-content/downloads/3.3.0-rc.1/downloads/haxe-3.3.0-rc.1-win.zip
	//http://nekovm.org/media/neko-2.1.0-linux64.tar.gz

	//val haxeCompilerUrl: URL by lazy { URL("https://github.com/jtransc/haxe-releases/releases/download/haxe-$HaxeVersion/$haxeCompilerFile") }
	//val nekoUrl: URL by lazy { URL("https://github.com/jtransc/haxe-releases/releases/download/neko-$NekoVersion/$nekoFile") }

	val HAXE_URL: URL by lazy {
		if (System.getenv("TRAVIS") == "true") {
			// Unsecure but official download to avoid spamming github
			// It would be great to use maven
			// https://github.com/HaxeFoundation/haxe/issues/5331
			URL("http://haxe.org/website-content/downloads/$HAXE_VERSION/downloads/$HAXE_FILE")
		} else {
			URL("https://github.com/jtransc/haxe-releases/blob/master/haxe/$HAXE_VERSION/$HAXE_FILE?raw=true")
		}
	}
	val NEKO_URL: URL by lazy {
		if (System.getenv("TRAVIS") == "true") {
			// Unsecure but official download to avoid spamming github
			// It would be great to use maven
			// https://github.com/HaxeFoundation/haxe/issues/5331
			URL("http://nekovm.org/media/$NEKO_FILE")
		} else {
			URL("https://github.com/jtransc/haxe-releases/blob/master/neko/$NEKO_VERSION/$NEKO_FILE?raw=true")
		}
	}

	val jtranscHaxeFolder: String by lazy { JTranscSystem.getUserHome() + "/.jtransc/haxe" }
	val jtranscNekoFolder: String by lazy { JTranscSystem.getUserHome() + "/.jtransc/neko" }
	val jtranscHaxeNekoFolder: String by lazy { JTranscSystem.getUserHome() + "/.jtransc/haxeneko" }

	val haxeCompilerUrlVfs: SyncVfsFile by lazy { UrlVfs(HAXE_URL) }
	val haxeCompilerLocalFileVfs: SyncVfsFile by lazy { LocalVfsEnsureDirs(File(jtranscHaxeFolder)).access(HAXE_FILE) }
	val haxeLocalFolderVfs: SyncVfsFile by lazy { LocalVfsEnsureDirs(File("$jtranscHaxeFolder/$HAXE_VERSION")) }

	val nekoUrlVfs: SyncVfsFile by lazy { UrlVfs(NEKO_URL) }
	val nekoLocalFileVfs: SyncVfsFile by lazy { LocalVfsEnsureDirs(File(jtranscNekoFolder)).access(NEKO_FILE) }
	val nekoLocalFolderVfs: SyncVfsFile by lazy { LocalVfsEnsureDirs(File("$jtranscNekoFolder/$NEKO_VERSION")) }

	val haxeNekoLocalFolderVfs: SyncVfsFile by lazy { LocalVfsEnsureDirs(File("$jtranscHaxeNekoFolder/${HAXE_VERSION}_$NEKO_VERSION")) }

	fun getHaxeTargetLibraries(subtarget: String): List<String> = when (subtarget) {
		"cpp", "windows", "linux", "mac", "osx" -> listOf("hxcpp")
		"c#", "cs", "csharp" -> listOf("hxcs")
		"java", "jvm" -> listOf("hxjava")
		else -> listOf()
	}

	var ensured = false

	fun ensureHaxeCompilerVfs(): SyncVfsFile {
		if (!ensured) {
			ensured = true

			log.info("ensureHaxeCompilerVfs:")

			/////////////////////////////////////////
			// NEKO
			/////////////////////////////////////////
			if (!nekoLocalFileVfs.exists) {
				log.info("Downloading neko: $NEKO_URL...")
				nekoUrlVfs.copyTo(nekoLocalFileVfs)
			}

			if (!nekoLocalFolderVfs["std.ndll"].exists) {
				//for (syncVfsStat in CompressedVfs(nekoLocalFileVfs.realfile).listdirRecursive()) println(syncVfsStat)

				val compvfsBase = CompressedVfs(nekoLocalFileVfs.realfile).firstRecursive { it.file.name == "std.ndll" }.file.parent
				compvfsBase.copyTreeTo(nekoLocalFolderVfs, doLog = false)

				if (!JTranscSystem.isWindows()) {
					nekoLocalFolderVfs["neko"].chmod(FileMode.fromString("-rwxr-xr-x"))
					nekoLocalFolderVfs["nekoc"].chmod(FileMode.fromString("-rwxr-xr-x"))
					nekoLocalFolderVfs["nekoml"].chmod(FileMode.fromString("-rwxr-xr-x"))
				}
			}

			/////////////////////////////////////////
			// HAXE
			/////////////////////////////////////////
			if (!haxeCompilerLocalFileVfs.exists) {
				log.info("Downloading haxe: $HAXE_URL...")
				haxeCompilerUrlVfs.copyTo(haxeCompilerLocalFileVfs)
			}
			if (!haxeLocalFolderVfs["std"].exists) {
				val compvfsBase = CompressedVfs(haxeCompilerLocalFileVfs.realfile).firstRecursive { it.file.name == "std" }.file.parent
				compvfsBase.copyTreeTo(haxeLocalFolderVfs, doLog = false)
				if (!JTranscSystem.isWindows()) {
					haxeLocalFolderVfs["haxe"].chmod(FileMode.fromString("-rwxr-xr-x"))
					haxeLocalFolderVfs["haxelib"].chmod(FileMode.fromString("-rwxr-xr-x"))
				}
			}

			/////////////////////////////////////////
			// NEKO-HAXE mixed so haxelib works fine without strange stuff
			/////////////////////////////////////////
			if (!haxeNekoLocalFolderVfs["haxe"].exists || !haxeNekoLocalFolderVfs["neko"].exists) {
				haxeNekoLocalFolderVfs.ensuredir()
				haxeLocalFolderVfs.copyTreeTo(haxeNekoLocalFolderVfs)
				nekoLocalFolderVfs.copyTreeTo(haxeNekoLocalFolderVfs)

				haxeNekoLocalFolderVfs["neko"].chmod(FileMode.fromString("-rwxr-xr-x"))
				haxeNekoLocalFolderVfs["nekoc"].chmod(FileMode.fromString("-rwxr-xr-x"))
				haxeNekoLocalFolderVfs["nekoml"].chmod(FileMode.fromString("-rwxr-xr-x"))
				haxeNekoLocalFolderVfs["haxe"].chmod(FileMode.fromString("-rwxr-xr-x"))
				haxeNekoLocalFolderVfs["haxelib"].chmod(FileMode.fromString("-rwxr-xr-x"))
			}


			/////////////////////////////////////////
			// HAXELIB
			/////////////////////////////////////////
			if (!haxeNekoLocalFolderVfs["lib"].exists || HaxeLib.getHaxelibFolderFile() == null) {
				HaxeLib.setup(haxeNekoLocalFolderVfs["lib"].realpathOS)
			}

			if (!haxeNekoLocalFolderVfs["lib"].exists) {
				throw RuntimeException("haxelib setup failed!")
			}
		}

		return haxeNekoLocalFolderVfs
	}

	fun getExtraEnvs(): Map<String, String> = mapOf(
		"HAXE_STD_PATH" to haxeNekoLocalFolderVfs["std"].realpathOS,
		"HAXE_HOME" to haxeNekoLocalFolderVfs.realpathOS,
		"HAXEPATH" to haxeNekoLocalFolderVfs.realpathOS,
		"NEKOPATH" to haxeNekoLocalFolderVfs.realpathOS,
		// OSX
		"*DYLD_LIBRARY_PATH" to haxeNekoLocalFolderVfs.realpathOS + File.pathSeparator,
		// Linux
		"*LD_LIBRARY_PATH" to haxeNekoLocalFolderVfs.realpathOS + File.pathSeparator,
		"*PATH" to haxeNekoLocalFolderVfs.realpathOS + File.pathSeparator
	)

	fun ensureHaxeSubtarget(subtarget: String) {
		ensureHaxeCompilerVfs()

		for (lib in getHaxeTargetLibraries(subtarget)) {
			HaxeLib.installIfNotExists(HaxeLib.LibraryRef.fromVersion(lib))
		}
	}
}
