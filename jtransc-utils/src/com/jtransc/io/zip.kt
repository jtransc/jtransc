package com.jtransc.io

import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

fun createZipFile(files: Map<String, ByteArray>): ByteArray {
	val baos = ByteArrayOutputStream()
	val out = ZipOutputStream(baos);

	for ((name, content) in files) {
		val entry = ZipEntry(name)
		out.putNextEntry(entry);
		out.write(content)
		out.closeEntry()
	}

	out.close()

	return baos.toByteArray()
}