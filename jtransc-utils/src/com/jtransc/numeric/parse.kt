package com.jtransc.numeric

fun String.toInt(radix: Int) = Integer.parseInt(this, radix)

fun String.toInt(radix: Int, default: Int) = try {
	Integer.parseInt(this, radix)
} catch (t: Throwable) {
	default
}
