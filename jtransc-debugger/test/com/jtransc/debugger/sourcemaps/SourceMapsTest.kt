package com.jtransc.debugger.sourcemaps

import org.junit.Assert
import org.junit.Test

class SourceMapsTest {
	@Test fun testDecodeVlq() {
		Assert.assertEquals(listOf(0, 0, 0, 0), Base64Vlq.decode("AAAA"))
		Assert.assertEquals(listOf(7, 0, 0, 8), Base64Vlq.decode("OAAQ"))
		Assert.assertEquals(listOf(2, 0, 1, -7), Base64Vlq.decode("EACP"))
		Assert.assertEquals(listOf(5, 0, 0, 5), Base64Vlq.decode("KAAK"))
		Assert.assertEquals(listOf(2, 0, 0, 2), Base64Vlq.decode("EAAE"))
		Assert.assertEquals(listOf(5, 0, 0, 5), Base64Vlq.decode("KAAK"))
		Assert.assertEquals(listOf(0, 0, 16, 1), Base64Vlq.decode("AAgBC"))
	}

	@Test fun testEncodeVlq() {
		Assert.assertEquals("AAAA", Base64Vlq.encode(listOf(0, 0, 0, 0)))
		Assert.assertEquals("OAAQ", Base64Vlq.encode(listOf(7, 0, 0, 8)))
		Assert.assertEquals("EACP", Base64Vlq.encode(listOf(2, 0, 1, -7)))
		Assert.assertEquals("AAgBC", Base64Vlq.encode(listOf(0, 0, 16, 1)))
		Assert.assertEquals("AAuorrCC", Base64Vlq.encode(listOf(0, 0, 1234567, 1)))
	}

	@Test fun testDecodeRaw() {
		Assert.assertEquals(
			listOf(
				listOf(listOf()),
				listOf(listOf()),
				listOf(listOf()),
				listOf(listOf()),
				listOf(listOf(2, 0, 2, 2), listOf(2, 0, 0, 2), listOf(2, 0, 0, 1), listOf(1, 0, 0, 2)),
				listOf(listOf(2, 1, 8, 12), listOf(10, 0, 1, 1))
			),
			Sourcemaps.decodeRaw(";;;;EAEE,EAAE,EAAC,CAAE;ECQY,UACC")
		)
		//4) [2,0,2,2], [2,0,0,2], [2,0,0,1], [1,0,0,2]
		//5) [2,1,8,12], [10,0,1,1]
	}
}