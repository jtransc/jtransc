package com.jtransc.gen.cpp.libs

import com.jtransc.gen.cpp.CppCompiler
import com.jtransc.io.ProcessUtils
import com.jtransc.vfs.ExecOptions
import com.jtransc.vfs.SyncVfsFile
import org.rauschig.jarchivelib.ArchiveFormat
import org.rauschig.jarchivelib.ArchiverFactory
import org.rauschig.jarchivelib.CompressionType
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object Libs {
	//val LIBS = listOf(BoostLib, BdwgcLib, JniHeadersLib)
	//val LIBS = listOf(BdwgcLib, JniHeadersLib)
	val LIBS = listOf(BdwgcLib)

	val cppCommonFolder get() = CppCompiler.CPP_COMMON_FOLDER.realfile
	val sdkDir = CppCompiler.CPP_COMMON_FOLDER.realfile
	val includeFolders: List<File> get() = LIBS.flatMap { it.includeFolders }
	val libFolders: List<File> get() = LIBS.flatMap { it.libFolders }

	fun installIfRequired(resourcesVfs: SyncVfsFile) {
		for (lib in LIBS) {
			lib.installIfRequired(resourcesVfs)
		}
	}
}

