package com.jtransc.log

import com.jtransc.time.measureTime

object log {
	var logger: (Any?) -> Unit = { println(it) }

	operator fun invoke(v: Any?) {
		logger(v)
	}

	inline fun <T> logAndTime(text:String, callback: () -> T): T {
		log("$text...")
		val (time, result) = measureTime {
			callback()
		}
		log("Ok($time)")
		return result
	}
}