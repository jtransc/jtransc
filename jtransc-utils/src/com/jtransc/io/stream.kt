package com.jtransc.io

import com.jtransc.error.invalidOp
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.*

fun OutputStream.i8(value: Int) {
	this.write(value.toInt())
}

fun OutputStream.i16(value: Int) {
	this.write((value.toInt() ushr 8) and 0xFF)
	this.write((value.toInt() ushr 0) and 0xFF)
}

fun OutputStream.i32(value: Int) {
	this.write((value.toInt() ushr 24) and 0xFF)
	this.write((value.toInt() ushr 16) and 0xFF)
	this.write((value.toInt() ushr 8) and 0xFF)
	this.write((value.toInt() ushr 0) and 0xFF)
}

fun InputStream.i8(): Int {
	return (this.read() and 0xFF).toByte().toInt()
}

fun InputStream.i16(): Int {
	val h = i8()
	val l = i8()
	return ((h shl 8) or (l shl 0)).toShort().toInt()
}

fun InputStream.i32(value: Int): Int {
	val h = i16()
	val l = i16()
	return ((h shl 16) or (l shl 0)).toInt().toInt()
}

fun InputStream.bytes(count: Int): ByteArray {
	val out = ByteArray(count)
	this.read(out)
	return out
}

fun ByteArray.stripTailZeros():ByteArray {
	val index = this.indexOf(0)
	return Arrays.copyOf(this, if (index >= 0) index else this.size)
}

fun InputStream.stringz(count: Int, charset: Charset = Charsets.UTF_8): String {
	return this.bytes(count).stripTailZeros().toString(charset)
}

fun InputStream.stringzTrim(count: Int, charset: Charset = Charsets.UTF_8): String {
	return stringz(count, charset).trimEnd()
}

val EmptyByteArray = ByteArray(0)
fun InputStream.readAvailableChunk(): ByteArray {
	if (this.available() <= 0) return EmptyByteArray
	val out = ByteArray(this.available())
	val readed = this.read(out, 0, out.size)
	return out.copyOf(readed)
}

fun InputStream.readExactBytes(size: Int): ByteArray {
	val out = ByteArray(size)
	var pos = 0
	while (pos < size) {
		val read = this.read(out, pos, size - pos)
		if (read <= 0) invalidOp("Can't read all bytes exactly")
		pos += read
	}
	return out
}
