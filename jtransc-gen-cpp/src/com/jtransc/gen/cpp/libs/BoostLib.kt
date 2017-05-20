package com.jtransc.gen.cpp.libs

import com.jtransc.JTranscSystem
import com.jtransc.vfs.SyncVfsFile
import com.jtransc.vfs.ensureExists
import com.jtransc.vfs.get
import java.io.File
import java.net.URL
import java.util.*

object BoostLib : Lib("boost") {
	override val alreadyInstalled: Boolean get() = Libs.cppCommonFolder["boost/compiled-libs/"].exists()

	override val libFolders: List<File> = listOf(Libs.cppCommonFolder["bdwgc"][".libs"], Libs.cppCommonFolder["boost"]["compiled-libs"])
	override val includeFolders: List<File> = listOf(Libs.cppCommonFolder["bdwgc"]["include"], Libs.cppCommonFolder["boost"]["boost_1_64_0"])

	override fun install(resourcesVfs: SyncVfsFile) {
		if (Libs.cppCommonFolder["boost/compiled-libs/"].exists()) return // Already compiled

		val boostDir = File(Libs.sdkDir, "boost")
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

		if (JTranscSystem.isWindows()) {
			runCommand(boostDestDir0, "cmd", mutableListOf("/c", "bootstrap.bat", "--prefix=${Libs.sdkDir}/boost/compiled-libs/", "--with-libraries=thread,system,chrono"))
			runCommand(boostDestDir0, boostDestDir0["b2.exe"].absolutePath, mutableListOf("install"))
			boostDir["compiled-libs"].mkdirs()
			boostDestDir0["stage/lib"].copyRecursively(boostDir["compiled-libs"])
		} else {
			boostDestDir0["bootstrap.sh"].setExecutable(true)
			runCommand(boostDestDir0, "./bootstrap.sh", mutableListOf("--prefix=${Libs.sdkDir}/boost/compiled-libs/", "--with-libraries=thread,system,chrono"))

			boostDestDir0["b2"].setExecutable(true)
			runCommand(boostDestDir0, "./b2", mutableListOf("install"))
		}
	}
}
