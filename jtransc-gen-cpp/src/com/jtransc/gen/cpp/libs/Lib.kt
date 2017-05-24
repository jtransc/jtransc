package com.jtransc.gen.cpp.libs

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

open class Lib(val name: String) {
	fun installIfRequired(resourcesVfs: SyncVfsFile) {
		if (!alreadyInstalled) {
			System.err.println("Required lib $name couldn't be found.")
			System.err.println("Trying to install it now.")
			install(resourcesVfs)
		}
	}

	open val alreadyInstalled = false
	open fun install(resourcesVfs: SyncVfsFile) = Unit
	open val includeFolders: List<File> = listOf()
	open val libFolders: List<File> = listOf()
	open val libs: List<String> = listOf()
	open val extraDefines: List<String> = listOf()

	protected fun unzip(zip: File, targetFile: File) {
		val archiver = ArchiverFactory.createArchiver(ArchiveFormat.ZIP)
		archiver.extract(zip, targetFile)
	}

	protected fun untar(zip: File, targetFile: File) {
		val archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP)
		archiver.extract(zip, targetFile)
	}

	protected fun runCommand(currentDir: File, command: String, args: List<String>) {
		val result = ProcessUtils.run(currentDir, command, args, ExecOptions(passthru = true, sysexec = true))
		if (!result.success) {
			throw RuntimeException("success=${result.success}\nexitCode=${result.exitValue}\noutput='${result.out}'\nerror='${result.err}'")
		}
	}

	protected fun downloadFile(sourceURL: URL, destFile: File) {
		sourceURL.openStream().use({ `in` -> Files.copy(`in`, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING) })
	}
}
