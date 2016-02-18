package com.jtransc.io

import java.io.IOException

fun <T> Class<T>.readBytes(): ByteArray {
	val filePath = this.name.replace('.', '/') + ".class"
	val stream = this.classLoader.getResourceAsStream(filePath) ?: throw IOException("Can't find class $filePath")
	return stream.readBytes()
}
