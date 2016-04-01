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
	}

	inline private fun testDecodeEncodeRaw(item:String) = Assert.assertEquals(item, Sourcemaps.encodeRaw(Sourcemaps.decodeRaw(item)))

	@Test fun testDecodeEncodeRaw() {
		testDecodeEncodeRaw(";;;;EAEE,EAAE,EAAC,CAAE;ECQY,UACC")
	}

	@Test fun testDecode() {
		Assert.assertEquals(
			"MappingFile(rows=[MappingRow(mappings=[]), MappingRow(mappings=[]), MappingRow(mappings=[]), MappingRow(mappings=[]), MappingRow(mappings=[MappingItem(sourceIndex=0, sourceLine=2, sourceColumn=2, targetColumn=2), MappingItem(sourceIndex=0, sourceLine=2, sourceColumn=4, targetColumn=4), MappingItem(sourceIndex=0, sourceLine=2, sourceColumn=5, targetColumn=6), MappingItem(sourceIndex=0, sourceLine=2, sourceColumn=7, targetColumn=7)]), MappingRow(mappings=[MappingItem(sourceIndex=1, sourceLine=10, sourceColumn=19, targetColumn=2), MappingItem(sourceIndex=1, sourceLine=11, sourceColumn=20, targetColumn=12)])])",
			Sourcemaps.decode(";;;;EAEE,EAAE,EAAC,CAAE;ECQY,UACC").toString()
		)
	}
}