package com.jtransc.gen.haxe

import com.jtransc.text.toUcFirst

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

	fun exists(lib: LibraryRef): Boolean {
		return vfs.exec(haxelibCmd, "--always", "path", lib.nameWithVersion).success
	}

	fun install(lib: LibraryRef) {
		if (lib.version.isEmpty()) {
			vfs.passthru(haxelibCmd, "--always", "install", lib.name)
		} else {
			vfs.passthru(haxelibCmd, "--always", "install", lib.name, lib.version)
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

