package com.jtransc.gen.cpp.libs

import com.jtransc.JTranscSystem
import com.jtransc.vfs.*
import java.io.File
import java.net.URL
import java.util.*

open class BdwgcLib : Lib("bdwgc") {
	val baseFolder by lazy { Libs.cppCommonFolder["bdwgc"] }
	val libsFolder by lazy { baseFolder[".libs"] }
	override val alreadyInstalled: Boolean get() = libsFolder.exists()
	override val libFolders: List<File> = listOf(libsFolder)
	override val includeFolders: List<File> = listOf(baseFolder["include"])
	override val libs: List<String> = listOf("gc")
	override val extraDefines: List<String> = listOf("GC_NOT_DLL=1", "USE_BOEHM_GC=1")

	override fun install(resourcesVfs: SyncVfsFile) {
		// Boehm conservative garbage collector and libatomics
		val bdwgcDir = baseFolder.absoluteFile
		//gc
		//val bdwgcDestZip = File(Libs.sdkDir, "gc.zip")
		//downloadFile(URL("https://github.com/ivmai/bdwgc/archive/gc7_6_0.zip"), bdwgcDestZip)
		//unzip(bdwgcDestZip, Libs.sdkDir)

		val gzzip = ZipVfs(BdwgcLib::class.java.classLoader.getResourceBytes("com/jtransc/gen/cpp/libs/bdwgc.zip"))
		gzzip.copyTreeTo(LocalVfs(Libs.cppCommonFolder))

		val bdwgcDestDir = Libs.sdkDir.listFiles { dir, name -> name.startsWith("bdwgc") && dir.isDirectory } // find the unzipped folder
		if (bdwgcDestDir == null) {
			System.err.println("Unzipped directory for gc files couldn't be found. Probably unzipping failed")
			System.exit(-1)
		} else if (bdwgcDestDir.size != 1) {
			System.err.println("Unzipped directory for gc files could be found but expected only one file, but was: ${bdwgcDestDir.size}")
			System.err.println(Arrays.toString(bdwgcDestDir))
			System.exit(-1)
		}

		bdwgcDestDir[0].renameTo(bdwgcDir)

		if (JTranscSystem.isWindows()) {
			runCommand(bdwgcDir, "cmake", listOf(bdwgcDir.absolutePath))
			runCommand(bdwgcDir, "cmake", listOf("--build", bdwgcDir.absolutePath, "--config", "Release"))
			bdwgcDir[".libs"].mkdirs()
			bdwgcDir["Release"].copyRecursively(bdwgcDir[".libs"])
			bdwgcDir["Release"]["gc-lib.lib"].copyTo(bdwgcDir[".libs"]["gc.lib"])
		} else {
			bdwgcDir["autogen.sh"].setExecutable(true)
			runCommand(bdwgcDir, "./autogen.sh", listOf())

			bdwgcDir["configure"].setExecutable(true)
			//runCommand(bdwgcDir, "./configure", listOf("--enable-cplusplus", "--enable-static", "--disable-shared"))
			runCommand(bdwgcDir, "./configure", listOf("--enable-cplusplus", "--enable-static"))

			runCommand(bdwgcDir, "make", listOf("-j"))
		}
	}
}