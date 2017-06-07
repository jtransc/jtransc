package com.jtransc.lang

import org.junit.Assert.*
import org.junit.Test

class MapExtKtTest {
	@Test
	fun mergeMapListWith() {
		val map1 = mapOf("a" to listOf(1, 2, 3), "c" to listOf())
		val map2 = mapOf("a" to listOf(4, 5), "b" to listOf(3))

		assertEquals(
			mapOf("a" to listOf(1, 2, 3, 4, 5), "b" to listOf(3), "c" to listOf()),
			map1.mergeMapListWith(map2)
		)
	}
}