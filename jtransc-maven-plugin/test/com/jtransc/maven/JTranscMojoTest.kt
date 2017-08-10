package com.jtransc.maven

import org.junit.Assert.*
import org.junit.Test

class JTranscMojoTest {
	@Test
	fun name() {
		val mojo = JTranscMojo()
		assertNotNull(mojo)
	}
}