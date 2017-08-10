package com.jtransc.tools

import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test

class JavaIdsTest {
	@Test
	fun name() {
		val ids = JavaIds.SET
		Assert.assertNotNull(ids)
	}
}