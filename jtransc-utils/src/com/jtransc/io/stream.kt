package com.jtransc.io

import java.io.InputStream
import java.io.OutputStream

fun OutputStream.i8(value:Int) {
	this.write(value.toInt())
}

fun OutputStream.i16(value:Int) {
	this.write((value.toInt() ushr 8) and 0xFF)
	this.write((value.toInt() ushr 0) and 0xFF)
}

fun OutputStream.i32(value:Int) {
	this.write((value.toInt() ushr 24) and 0xFF)
	this.write((value.toInt() ushr 16) and 0xFF)
	this.write((value.toInt() ushr 8) and 0xFF)
	this.write((value.toInt() ushr 0) and 0xFF)
}

fun InputStream.i8():Int {
	return (this.read() and 0xFF).toByte().toInt()
}

fun InputStream.i16():Int {
	val h = i8()
	val l = i8()
	return ((h shl 8) or (l shl 0)).toShort().toInt()
}

fun InputStream.i32(value:Int):Int {
	val h = i16()
	val l = i16()
	return ((h shl 16) or (l shl 0)).toInt().toInt()
}