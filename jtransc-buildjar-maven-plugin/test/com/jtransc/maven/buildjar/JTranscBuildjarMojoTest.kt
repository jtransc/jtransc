package com.jtransc.maven.buildjar

import org.junit.Assert
import org.junit.Test

class JTranscBuildjarMojoTest {
	@Test
	fun name() {
		val mojo = JTranscBuildjarMojo()
		Assert.assertNotNull(mojo)
	}
}