package com.jtransc.lang

fun <T, T2> T?.nullMap(notNull:T2, isNull:T2) = if (this != null) notNull else isNull

fun <T, R> T?.nonNullMap(gen: (T) -> R): R? {
	if (this == null) {
		return null
	} else {
		return gen(this)
	}
}