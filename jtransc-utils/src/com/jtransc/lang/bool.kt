package com.jtransc.lang

inline fun <T> Boolean.map(t: T, f: T) = if (this) t else f

fun Boolean.toBool() = (this)
fun Byte.toBool() = (this.toInt() != 0)
fun Char.toBool() = (this.toInt() != 0)
fun Short.toBool() = (this.toInt() != 0)
fun Int.toBool() = (this.toInt() != 0)
fun Long.toBool() = (this.toInt() != 0)
fun Float.toBool() = (this != 0f)
fun Double.toBool() = (this != 0.0)

fun Boolean.toByte() = this.map(1, 0).toByte()
fun Boolean.toChar() = this.map(1, 0).toChar()
fun Boolean.toShort() = this.map(1, 0).toShort()
fun Boolean.toInt() = this.map(1, 0).toInt()
fun Boolean.toLong() = this.map(1, 0).toLong()
fun Boolean.toFloat() = this.map(1, 0).toFloat()
fun Boolean.toDouble() = this.map(1, 0).toDouble()

