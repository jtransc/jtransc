package com.jtransc.debugger

import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test

class JTranscDebuggerTest {
	@Test
	fun name() {
		val debugger = JTranscDebugger(object : JTranscDebugger.EventHandler() {

		})
		Assert.assertNotNull(debugger)
	}
}