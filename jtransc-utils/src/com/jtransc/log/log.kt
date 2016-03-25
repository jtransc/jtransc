package com.jtransc.log

object log {
	var logger: (Any?) -> Unit = { println(it) }

	operator fun invoke(v: Any?) {
		logger(v)
	}
}