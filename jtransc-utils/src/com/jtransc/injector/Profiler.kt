package com.jtransc.injector

import com.jtransc.time.measureTime

@Singleton
class Profiler {
	inline fun <T> measure(name: String, callback: () -> T): T {
		com.jtransc.log.log("$name...")
		val (time, result) = measureTime { callback() }
		com.jtransc.log.log("Ok ($time)")
		return result
	}
}