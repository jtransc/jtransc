package jtransc.jtransc.js

import com.jtransc.js.*

object MixedJsKotlin {
	@JvmStatic fun main(args: Array<String>) {
		val consoleLog = console.methods["log"]

		consoleLog("MixedJsKotlin.main[1]")
		global.methods["setTimeout"](jsFunctionRaw0 {
			global["console"].methods["log"]("Timeout!")
		}, 10)
		consoleLog("MixedJsKotlin.main[2]")
		consoleLog(jsArray(1, 2, 3))
		val buffer = global["Buffer"].new(16)
		for (n in 0 until buffer["length"].toInt()) {
			buffer[n] = n
		}
		consoleLog(buffer)
		for (stat in jsGetAssetStats()) {
			println(stat)
		}
		//println(Pair<String, Any>("a", 10).javaClass.name)


		consoleLog(global["JSON"].call("stringify", jsObject("a" to 10, "b" to "c", "c" to jsArray(1, 2, 3))))
		consoleLog(jsRequire("path").call("parse", "/hello/world/file.txt")["ext"])
	}
}