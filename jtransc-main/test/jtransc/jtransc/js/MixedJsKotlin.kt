package jtransc.jtransc.js

import com.jtransc.js.*

object MixedJsKotlin {
	@JvmStatic fun main(args: Array<String>) {
		console.methods["log"]("MixedJsKotlin.main[1]")
		global.methods["setTimeout"](jsFunctionRaw0 {
			global["console"].methods["log"]("Timeout!")
		}, 10)
		console.methods["log"]("MixedJsKotlin.main[2]")
		console.methods["log"](jsArray(1, 2, 3))
		val buffer = global["Buffer"].new(16)
		for (n in 0 until buffer["length"].toInt()) {
			buffer[n] = n
		}
		console.methods["log"](buffer)
	}
}