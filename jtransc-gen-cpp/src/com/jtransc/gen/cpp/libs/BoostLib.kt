package com.jtransc.gen.cpp.libs

import com.jtransc.JTranscSystem
import com.jtransc.error.ignoreErrors
import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.SyncVfsFile
import com.jtransc.vfs.ensureExists
import com.jtransc.vfs.get
import java.io.File
import java.net.URL
import java.util.*

open class BoostLib : Lib("boost") {
	override val alreadyInstalled: Boolean get() = Libs.cppCommonFolder["boost/compiled-libs/"].exists()
	//override val alreadyInstalled: Boolean get() = false

	override val libFolders: List<File> = listOf(Libs.cppCommonFolder["bdwgc"][".libs"], Libs.cppCommonFolder["boost"]["compiled-libs"])
	override val includeFolders: List<File> = listOf(Libs.cppCommonFolder["bdwgc"]["include"], Libs.cppCommonFolder["boost"]["boost_1_64_0"])
	override val libs: List<String> = listOf("boost_thread", "boost_system")
	override val extraDefines: List<String> = listOf("USE_BOOST=1")

	val boostDir by lazy { Libs.sdkDir["boost"] }

	override fun install(resourcesVfs: SyncVfsFile) {
		boostDir.ensureExists()

		//boost
		val boostDestZip = File(boostDir, "boost.zip")
		downloadFile(URL("https://dl.bintray.com/boostorg/release/1.64.0/source/boost_1_64_0.tar.gz"), boostDestZip)
		untar(boostDestZip, boostDir)
		val boostDestDir = boostDir.listFiles { dir, name -> name.startsWith("boost") && !name.endsWith(".zip") && !name.endsWith(".gz") } // find the unzipped folder
		if (boostDestDir == null) {
			System.err.println("Unzipped directory for boost files couldn't be found. Probably unzipping failed")
			System.exit(-1)
		} else if (boostDestDir.size != 1) {
			System.err.println("Unzipped directory for boost files could be found but expected only one file, but was: ${boostDestDir.size}")
			System.err.println(Arrays.toString(boostDestDir))
			System.exit(-1)
		}

		val boostDestDir0 = boostDestDir[0]

		val prefix = listOf("--prefix=${Libs.sdkDir}/boost/compiled-libs/")
		val withLibs = listOf("--with-thread", "--with-system", "--with-chrono")

		if (JTranscSystem.isWindows()) {
			runCommand(boostDestDir0, "bootstrap.bat", listOf())
			runCommand(boostDestDir0, boostDestDir0["b2.exe"].absolutePath, listOf("install") + prefix + withLibs)
			boostDir["compiled-libs"].mkdirs()
			copyWindowsLibs(boostDestDir0)
		} else {
			boostDestDir0["bootstrap.sh"].setExecutable(true)
			runCommand(boostDestDir0, "./bootstrap.sh", listOf("--prefix=${Libs.sdkDir}/boost/compiled-libs/", "--with-libraries=thread,system,chrono"))

			boostDestDir0["b2"].setExecutable(true)
			runCommand(boostDestDir0, "./b2", listOf("install") + prefix + withLibs)
		}
	}

	private fun copyWindowsLibs(boostDestDir0: File) {
		ignoreErrors { boostDir["compiled-libs"].mkdirs() }
		try {
			boostDestDir0["stage/lib"].copyRecursively(boostDir["compiled-libs"])
		} catch (e: Throwable) {
		}
		val compiledLibs = LocalVfs(boostDir["compiled-libs"])
		val files = LocalVfs(boostDestDir0["bin.v2"]).listdirRecursive().filter { it.isFile }
		for (file in files.filter { it.name.contains(".lib") || it.name.contains(".a") || it.name.contains(".la") }) {

			file.file.copyTo(compiledLibs[file.name])
		}
	}
}
