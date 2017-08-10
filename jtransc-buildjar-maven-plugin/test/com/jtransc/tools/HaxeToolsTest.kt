package com.jtransc.tools

import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test

class HaxeToolsTest {
	@Test
	fun name() {
		val haxeTools = HaxeTools
		Assert.assertNotNull(haxeTools)
	}
}