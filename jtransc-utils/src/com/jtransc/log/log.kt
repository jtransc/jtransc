package com.jtransc.log

import com.jtransc.time.measureTime

object log {
	enum class Level { DEBUG, INFO, WARN, ERROR }

	var logger: (Any?, Level) -> Unit = { content, level -> println(content) }

	fun setTempLogger(logger: (Any?, Level) -> Unit, callback: () -> Unit) {
		val oldLogger = this.logger
		this.logger = logger
		try {
			callback()
		} finally {
			this.logger = oldLogger
		}
	}

	operator fun invoke(v: Any?) {
		logger(v, Level.INFO)
	}

	operator fun invoke(v: Any?, type: Level) {
		logger(v, type)
	}

	fun debug(v: Any?) {
		logger(v, Level.DEBUG)
	}

	fun info(v: Any?) {
		logger(v, Level.INFO)
	}

	fun warn(v: Any?) {
		logger(v, Level.WARN)
	}

	fun error(v: Any?) {
		logger(v, Level.ERROR)
	}

	inline fun <T> logAndTime(text:String, callback: () -> T): T {
		log("$text...")
		val (time, result) = measureTime {
			callback()
		}
		log("Ok($time)")
		return result
	}

	fun printStackTrace(e: Throwable) {
		e.printStackTrace()
	}
}