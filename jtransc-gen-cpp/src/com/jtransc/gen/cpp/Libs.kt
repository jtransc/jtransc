package com.jtransc.gen.cpp

import com.jtransc.JTranscSystem
import com.jtransc.ast.AstProgram
import com.jtransc.io.ProcessUtils
import com.jtransc.vfs.ExecOptions
import com.jtransc.vfs.get
import org.rauschig.jarchivelib.ArchiveFormat
import org.rauschig.jarchivelib.ArchiverFactory
import org.rauschig.jarchivelib.CompressionType
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*

private fun File.ensureExists() {
	if (!this.exists() && !this.mkdir()) throw RuntimeException("Failed to create folder: ${this.absolutePath}")
}

object Libs {
	val sdkDir = CppCompiler.CPP_COMMON_FOLDER.realfile
	private fun unzip(zip: File, targetFile: File) {
		val archiver = ArchiverFactory.createArchiver(ArchiveFormat.ZIP)
		archiver.extract(zip, targetFile)
	}

	private fun untar(zip: File, targetFile: File) {
		val archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP)
		archiver.extract(zip, targetFile)
	}


	private fun runCommand(currentDir: File, command: String, args: List<String>) {
		val result = ProcessUtils.run(currentDir, command, args, ExecOptions(passthru = true))
		if (!result.success) {
			throw RuntimeException("success=${result.success}\nexitCode=${result.exitValue}\noutput='${result.out}'\nerror='${result.err}'")
		}
	}

	private fun downloadFile(sourceURL: URL, destFile: File) {
		sourceURL.openStream().use({ `in` -> Files.copy(`in`, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING) })
	}

	val cppCommonFolder get() = CppCompiler.CPP_COMMON_FOLDER.realfile

	fun areRequiredLibsInstalled(): Boolean = listOf(
		cppCommonFolder["boost/compiled-libs/"],
		cppCommonFolder["bdwgc/.libs/"],
		cppCommonFolder["jni-headers/jni.h"]
	).all { it.exists() }

	val JNI_PLATFORM by lazy {
		when {
			JTranscSystem.isWindows() -> "win32"
			JTranscSystem.isMac() -> "mac"
			JTranscSystem.isLinux() -> "linux"
			else -> "linux"
		}
	}

	val includeFolders by lazy {
		listOf(
			cppCommonFolder["jni-headers"],
			cppCommonFolder["jni-headers"][JNI_PLATFORM],
			cppCommonFolder["bdwgc"]["include"],
			cppCommonFolder["boost"]["boost_1_64_0"]

		)
	}

	val libFolders by lazy {
		listOf(
			cppCommonFolder["bdwgc"][".libs"],
			cppCommonFolder["boost"]["compiled-libs"]

		)
	}

	fun installRequiredLibs(program: AstProgram) {
		installBdwgc()
		installBoost()
		copyJniHeaders(program)
	}

	fun installBdwgc() {
		if (cppCommonFolder["bdwgc/.libs/"].exists()) return // Already compiled

		// Boehm conservative garbage collector and libatomics
		val bdwgcDir = File(sdkDir, "bdwgc")
		//gc
		val bdwgcDestZip = File(sdkDir, "gc.zip")
		downloadFile(URL("https://github.com/ivmai/bdwgc/archive/gc7_6_0.zip"), bdwgcDestZip)
		unzip(bdwgcDestZip, sdkDir)
		val bdwgcDestDir = sdkDir.listFiles { dir, name -> name.startsWith("bdwgc") && dir.isDirectory } // find the unzipped folder
		if (bdwgcDestDir == null) {
			System.err.println("Unzipped directory for gc files couldn't be found. Probably unzipping failed")
			System.exit(-1)
		} else if (bdwgcDestDir.size != 1) {
			System.err.println("Unzipped directory for gc files could be found but expected only one file, but was: ${bdwgcDestDir.size}")
			System.err.println(Arrays.toString(bdwgcDestDir))
			System.exit(-1)
		}

		bdwgcDestDir[0].renameTo(bdwgcDir)


		//libatomics
		val libaDestZip = File(bdwgcDir, "libatomic_ops.zip")
		downloadFile(URL("https://github.com/intrigus/libatomic_ops/archive/master.zip"), libaDestZip)
		unzip(libaDestZip, bdwgcDir)
		val libaDestDir = bdwgcDir.listFiles { dir, name -> name.startsWith("libatomic_ops") && !name.endsWith(".zip") } // find the unzipped folder
		if (libaDestDir == null) {
			System.err.println("Unzipped directory for gc files couldn't be found. Probably unzipping failed")
			System.exit(-1)
		} else if (libaDestDir.size != 1) {
			System.err.println("Unzipped directory for gc files could be found but expected only one file, but was: ${libaDestDir.size}")
			System.err.println(Arrays.toString(libaDestDir))
			System.exit(-1)
		}

		libaDestDir[0].renameTo(File(bdwgcDir, "libatomic_ops"))

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
			runCommand(bdwgcDir, "./configure", listOf("--enable-cplusplus"))

			runCommand(bdwgcDir, "make", listOf("-j"))
		}
	}

	fun installBoost() {
		if (cppCommonFolder["boost/compiled-libs/"].exists()) return // Already compiled

		val boostDir = File(sdkDir, "boost")
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
			runCommand(boostDestDir0, "cmd", mutableListOf("/c", "bootstrap.bat", "--prefix=${sdkDir}/boost/compiled-libs/", "--with-libraries=thread,system,chrono"))
			runCommand(boostDestDir0, boostDestDir0["b2.exe"].absolutePath, mutableListOf("install"))
			boostDir["compiled-libs"].mkdirs()
			boostDestDir0["stage/lib"].copyRecursively(boostDir["compiled-libs"])
		} else {
			boostDestDir0["bootstrap.sh"].setExecutable(true)
			runCommand(boostDestDir0, "./bootstrap.sh", mutableListOf("--prefix=${sdkDir}/boost/compiled-libs/", "--with-libraries=thread,system,chrono"))

			boostDestDir0["b2"].setExecutable(true)
			runCommand(boostDestDir0, "./b2", mutableListOf("install"))
		}
	}

	fun copyJniHeaders(program: AstProgram) {
		val jniDir = File(sdkDir, "jni-headers")
		if (!jniDir.exists() && !jniDir.mkdir()) throw RuntimeException("Failed")
		val os = arrayOf("mac", "linux", "win32")
		File(jniDir, "jni.h").apply {
			createNewFile()
			writeBytes(program.resourcesVfs["cpp/jni-headers/jni.h"].readBytes())
		}

		File(jniDir, "jawt.h").apply {
			createNewFile()
			writeBytes(program.resourcesVfs["cpp/jni-headers/jawt.h"].readBytes())
		}
		File(jniDir, "classfile_constants.h").apply {
			createNewFile()
			writeBytes(program.resourcesVfs["cpp/jni-headers/classfile_constants.h"].readBytes())
		}
		File(jniDir, "jdwpTransport.h").apply {
			createNewFile()
			writeBytes(program.resourcesVfs["cpp/jni-headers/jdwpTransport.h"].readBytes())
		}
		os.forEach {
			val osDir = File(jniDir, it)
			osDir.ensureExists()
			File(osDir, "jni_md.h").apply {
				createNewFile()
				writeBytes(program.resourcesVfs["cpp/jni-headers/${it}/jni_md.h"].readBytes())
			}
			if (it != "mac") File(osDir, "jawt_md.h").apply {
				createNewFile()
				writeBytes(program.resourcesVfs["cpp/jni-headers/${it}/jawt_md.h"].readBytes())
			}
		}
	}
}