package com.jtransc.gen.haxe

import com.jtransc.text.toUcFirst
import com.jtransc.vfs.CwdVfs

object HaxeLib {
	data class LibraryRef(val name: String, val version: String) {
		val id = name.toUcFirst() // Remove symbols!
		val nameWithVersion = "$name:$version"
		companion object {
			fun fromVersion(it:String): LibraryRef {
				val parts = it.split(':')
				return LibraryRef(parts[0], parts[1])
			}
		}
	}

	val vfs by lazy { CwdVfs() }

	fun exists(lib: LibraryRef):Boolean {
		return vfs.exec("haxelib", "--always", "path", lib.nameWithVersion).success
	}

	fun install(lib: LibraryRef) {
		vfs.passthru("haxelib", "--always", "install", lib.name, lib.version)
	}

	fun installIfNotExists(lib: LibraryRef) {
		if (!exists(lib)) {
			install(lib)
		}
	}
}