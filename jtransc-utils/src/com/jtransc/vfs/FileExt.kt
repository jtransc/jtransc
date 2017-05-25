package com.jtransc.vfs

import java.io.File

operator fun File.get(name: String) = File(this, name)

fun File.ensureExists() {
	if (!this.exists() && !this.mkdir()) throw RuntimeException("Failed to create folder: ${this.absolutePath}")
}

fun ClassLoader.getResourceBytes(name: String): ByteArray {
	return this.getResourceAsStream(name).use { stream ->
		stream.readBytes(stream.available())
	}
}