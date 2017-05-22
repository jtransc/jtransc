package com.jtransc.gen.cpp.libs

import com.jtransc.gen.cpp.CppCompiler
import com.jtransc.io.ProcessUtils
import com.jtransc.service.JTranscService
import com.jtransc.vfs.ExecOptions
import com.jtransc.vfs.SyncVfsFile
import org.rauschig.jarchivelib.ArchiveFormat
import org.rauschig.jarchivelib.ArchiverFactory
import org.rauschig.jarchivelib.CompressionType
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*

object Libs {
	//val LIBS = ServiceLoader.load(Lib::class.java).toList()
	//val LIBS = listOf(BoostLib(), BdwgcLib(), JniHeadersLib())
	//val LIBS = listOf(BdwgcLib(), JniHeadersLib())
	val LIBS = listOf(BdwgcLib())

	val cppCommonFolder get() = CppCompiler.CPP_COMMON_FOLDER.realfile
	val sdkDir = CppCompiler.CPP_COMMON_FOLDER.realfile
	val includeFolders: List<File> get() = LIBS.flatMap { it.includeFolders }
	val libFolders: List<File> get() = LIBS.flatMap { it.libFolders }
	val libs: List<String> get() = LIBS.flatMap { it.libs }
	val extraDefines: List<String> get() = LIBS.flatMap { it.extraDefines }

	fun installIfRequired(resourcesVfs: SyncVfsFile) {
		for (lib in LIBS) {
			lib.installIfRequired(resourcesVfs)
		}
	}
}

