package com.jtransc.gen.haxe

import com.jtransc.gen.haxe.HaxeCompiler
import com.jtransc.text.toUcFirst
import com.jtransc.vfs.LocalVfs

object HaxeLib {
	data class LibraryRef(val name: String, val version: String) {
		val id = name.toUcFirst() // Remove symbols!
		val nameWithVersion = if (version.isNotEmpty()) "$name:$version" else "$name"

		companion object {
			fun fromVersion(it: String): LibraryRef {
				val parts = it.split(':')
				return LibraryRef(parts.getOrElse(0) { "" }, parts.getOrElse(1) { "" })
			}
		}
	}

	val vfs by lazy { HaxeCompiler.ensureHaxeCompilerVfs() }
	val haxelibCmd by lazy { vfs["haxelib"].realpathOS }

	fun haxelib(vararg args: String) = vfs.passthru(haxelibCmd, *args, env = HaxeCompiler.getExtraEnvs())

	fun setup(folder: String) = haxelib("setup", folder)

	fun exists(lib: LibraryRef): Boolean {
		return haxelib("--always", "path", lib.nameWithVersion).success
	}

	fun install(lib: LibraryRef) {
		if (lib.version.isEmpty()) {
			haxelib("--always", "install", lib.name)
		} else {
			haxelib("--always", "install", lib.name, lib.version)
		}
	}

	fun getHaxelibFolderFile(): String? {
		// default folder: /usr/local/lib/haxe/lib
		try {
			return LocalVfs(System.getProperty("user.home") + "/.haxelib").readString().trim()
		} catch (t: Throwable) {
			return null
		}
	}

	//fun downloadHaxeCompiler() {
	//}

	fun installIfNotExists(lib: LibraryRef) {
		if (!exists(lib)) {
			install(lib)
		}
	}
}

