package com.jtransc.debugger.v8

import com.jtransc.debugger.JTranscDebugger
import com.jtransc.io.ProcessUtils
import com.jtransc.net.SocketUtils
import java.io.File

object NodeJS {
	// node --debug-brk=9222 test.js
	fun debugAsync(js: File, handler: ProcessUtils.ProcessHandler): Int {
		val port = SocketUtils.getFreePort()
		ProcessUtils.runAsync(js.parentFile, "node", listOf("--debug-brk=$port", js.absolutePath), handler)
		return port
	}

	fun runAsync(js: File, handler: ProcessUtils.ProcessHandler): Unit {
		ProcessUtils.runAsync(js.parentFile, "node", listOf(js.absolutePath), handler)
	}

	fun debug2Async(js: File, processHandler: ProcessUtils.ProcessHandler, debuggerHandler: JTranscDebugger.EventHandler): JTranscDebugger {
		val port = NodeJS.debugAsync(js, processHandler)
		return V8JTranscDebugger(port, "127.0.0.1", debuggerHandler)
	}
}