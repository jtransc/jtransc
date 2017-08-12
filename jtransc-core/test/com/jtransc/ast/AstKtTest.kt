package com.jtransc.ast

import org.junit.Assert
import org.junit.Test

class AstKtTest {
	@Test
	fun testIfdef() {
		Assert.assertEquals(true, ifdef("", setOf("WIN32")))
		Assert.assertEquals(true, ifdef("WIN32", setOf("WIN32")))
		Assert.assertEquals(false, ifdef("!WIN32", setOf("WIN32")))
		Assert.assertEquals(true, ifdef("!!WIN32", setOf("WIN32")))
		Assert.assertEquals(false, ifdef("LINUX", setOf("WIN32")))
		Assert.assertEquals(true, ifdef("!LINUX", setOf("WIN32")))
	}
}