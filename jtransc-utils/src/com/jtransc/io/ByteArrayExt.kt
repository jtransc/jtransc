package com.jtransc.io

fun ByteArray.contains(other: ByteArray): Boolean = indexOf(other) >= 0

fun ByteArray.indexOf(other: ByteArray): Int {
	val full = this
	for (n in 0 until full.size - other.size) {
		if (other.indices.all { m -> full[n + m] == other[m] }) {
			return n
		}
	}
	return -1
}
