package com.jtransc.lang

fun Any?.toBetterString(): String {
	if (this == null) return "null"
	val clazz = this.javaClass
	if (clazz.isArray) return "" + ReflectedArray(this).toList()
	return "$this"
}