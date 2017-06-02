package com.jtransc.debugger.v8

import com.jtransc.async.Promise
import com.jtransc.debugger.JTranscDebugger
import com.jtransc.io.ProcessHandler
import com.jtransc.io.ProcessUtils
import com.jtransc.net.SocketUtils
import java.io.File

object NodeJS {
	// node --debug-brk=9222 test.js
	fun debugAsync(js: File, handler: ProcessHandler): Int {
		val port = SocketUtils.getFreePort()
		ProcessUtils.runAsync(js.parentFile, "node", listOf("--debug-brk=$port", js.absolutePath), handler)
		return port
	}

	fun runAsync(js: File, handler: ProcessHandler): Unit {
		ProcessUtils.runAsync(js.parentFile, "node", listOf(js.absolutePath), handler)
	}

	fun debug2Async(js: File, processHandler: ProcessHandler, debuggerHandler: JTranscDebugger.EventHandler): Promise<JTranscDebugger> {
		val deferred = Promise.Deferred<JTranscDebugger>()
		var done = false
		var error = ""
		var port = 0
		port = NodeJS.debugAsync(js, object : ProcessHandler(processHandler) {
			override fun onErrorData(data: String) {
				super.onErrorData(data)
				if (!done) {
					error += data
					if (error.contains("Debugger listening")) {
						done = true
						deferred.resolve(V8JTranscDebugger(port, "127.0.0.1", debuggerHandler))
					}
				}
			}
		})
		return deferred.promise
	}
}