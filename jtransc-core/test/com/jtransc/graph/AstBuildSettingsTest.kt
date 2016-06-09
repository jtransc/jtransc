package com.jtransc.graph

import com.jtransc.ast.AstBuildSettings
import org.junit.Assert
import org.junit.Test

class AstBuildSettingsTest {
	@Test
	fun testDefaultSettings() {
		Assert.assertEquals(3, AstBuildSettings().rtAndRtCore.size)
	}
}