package com.jtransc.gen.cpp.libs

import com.jtransc.JTranscSystem
import com.jtransc.vfs.SyncVfsFile
import com.jtransc.vfs.ensureExists
import com.jtransc.vfs.get
import java.io.File

open class JniHeadersLib : Lib("jni-headers") {
	val JNI_PLATFORM by lazy {
		when {
			JTranscSystem.isWindows() -> "win32"
			JTranscSystem.isMac() -> "mac"
			JTranscSystem.isLinux() -> "linux"
			else -> "linux"
		}
	}

	override val alreadyInstalled: Boolean get() = Libs.cppCommonFolder["jni-headers/jni.h"].exists()

	override val includeFolders: List<File> = listOf(Libs.cppCommonFolder["jni-headers"], Libs.cppCommonFolder["jni-headers"][JNI_PLATFORM])

	//fun copyJniHeaders(program: AstProgram) {
	override fun install(resourcesVfs: SyncVfsFile) {
		val jniDir = File(Libs.sdkDir, "jni-headers")
		if (!jniDir.exists() && !jniDir.mkdir()) throw RuntimeException("Failed")
		val os = arrayOf("mac", "linux", "win32")
		File(jniDir, "jni.h").apply {
			createNewFile()
			writeBytes(resourcesVfs["cpp/jni-headers/jni.h"].readBytes())
		}

		File(jniDir, "jawt.h").apply {
			createNewFile()
			writeBytes(resourcesVfs["cpp/jni-headers/jawt.h"].readBytes())
		}
		File(jniDir, "classfile_constants.h").apply {
			createNewFile()
			writeBytes(resourcesVfs["cpp/jni-headers/classfile_constants.h"].readBytes())
		}
		File(jniDir, "jdwpTransport.h").apply {
			createNewFile()
			writeBytes(resourcesVfs["cpp/jni-headers/jdwpTransport.h"].readBytes())
		}
		os.forEach {
			val osDir = File(jniDir, it)
			osDir.ensureExists()
			File(osDir, "jni_md.h").apply {
				createNewFile()
				writeBytes(resourcesVfs["cpp/jni-headers/${it}/jni_md.h"].readBytes())
			}
			if (it != "mac") File(osDir, "jawt_md.h").apply {
				createNewFile()
				writeBytes(resourcesVfs["cpp/jni-headers/${it}/jawt_md.h"].readBytes())
			}
		}
	}


}