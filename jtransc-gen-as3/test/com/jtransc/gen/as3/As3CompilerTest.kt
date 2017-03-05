package com.jtransc.gen.as3

import org.junit.Assert
import org.junit.Test

class As3CompilerTest {
	val AIR18_DESCRIPTOR = """<?xml version="1.0"?>
		<air-sdk-description>
		<name>AIR 18.0.0</name>
		<version>18.0.0</version>
		<build>180</build>
		</air-sdk-description>
	"""

	@Test
	fun versionString() {
		Assert.assertEquals("18.0.0", As3Compiler.getSdkVersionFromString(AIR18_DESCRIPTOR))
	}

	@Test
	fun versionInt() {
		Assert.assertEquals(18, As3Compiler.getSdkIntVersionFromString(AIR18_DESCRIPTOR))
	}

	//@Test
	//fun name2() {
	//	println(As3Compiler.AIRSDK_VERSION)
	//}
}