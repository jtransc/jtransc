package com.jtransc.vfs

import com.jtransc.text.indexOfOrNull
import com.jtransc.text.lastIndexOfOrNull

class PathInfo(val fullpath: String) {
	val fullpathNormalized: String = fullpath.replace('\\', '/')
	val folder: String by lazy {
		fullpath.substring(0, fullpathNormalized.lastIndexOfOrNull('/') ?: 0)
	}
	val folderWithSlash: String by lazy {
		fullpath.substring(0, fullpathNormalized.lastIndexOfOrNull('/')?.plus(1) ?: 0)
	}
	val basename: String by lazy { fullpathNormalized.substringAfterLast('/') }
	val pathWithoutExtension: String by lazy {
		val startIndex = fullpathNormalized.lastIndexOfOrNull('/')?.plus(1) ?: 0
		fullpath.substring(0, fullpathNormalized.indexOfOrNull('.', startIndex) ?: fullpathNormalized.length)
	}

	fun pathWithExtension(ext: String): String = if (ext.isEmpty()) pathWithoutExtension else "$pathWithoutExtension.$ext"

	val fullnameWithoutExtension: String by lazy { fullpath.substringBeforeLast('.', fullpath) }
	val basenameWithoutExtension: String by lazy { basename.substringBeforeLast('.', basename) }

	val fullnameWithoutCompoundExtension: String by lazy { folderWithSlash + basenameWithoutCompoundExtension }
	val basenameWithoutCompoundExtension: String by lazy { basename.substringBefore('.', basename) }

	fun basenameWithExtension(ext: String): String = if (ext.isEmpty()) pathWithoutExtension else "$pathWithoutExtension.$ext"

	val extension: String by lazy { basename.substringAfterLast('.', "") }
	val extensionLC: String by lazy { extension.toLowerCase() }

	val compoundExtension: String by lazy { basename.substringAfter('.', "") }
	val compoundExtensionLC: String by lazy { compoundExtension.toLowerCase() }

	//val mimeTypeByExtension get() = MimeType.getByExtension(extensionLC)

	fun getComponents(): List<String> = fullpathNormalized.split('/')

	fun getFullComponents(): List<String> {
		val out = arrayListOf<String>()
		for (n in fullpathNormalized.indices) {
			when (fullpathNormalized[n]) {
				'/', '\\' -> {
					out += fullpathNormalized.substring(0, n)
				}
			}
		}
		out += fullpathNormalized
		return out
	}
}
