package com.jtransc.imaging

import com.jtransc.lang.getResourceBytes
import org.junit.Assert
import org.junit.Test

class ImagePropsDecoderTest {
	fun decodeResourceImageInfo(name: String): ImageInfo? {
		val s = ImagePropsDecoderTest::class.java.getResourceBytes(name) ?: throw RuntimeException("Resource '$name' doesn't exists")
		return ImagePropsDecoder.tryDecodeHeader(s)
	}

	@Test
	fun name() {
		Assert.assertEquals(ImageInfo(width = 32, height = 32, bitsPerPixel = 8), decodeResourceImageInfo("/jtransc-icon.gif")) // GIF not supported yet!
		Assert.assertEquals(ImageInfo(width = 32, height = 32, bitsPerPixel = 32), decodeResourceImageInfo("/jtransc-icon.png"))
		Assert.assertEquals(ImageInfo(width = 32, height = 32, bitsPerPixel = 8), decodeResourceImageInfo("/jtransc-icon-png8.png"))
		Assert.assertEquals(ImageInfo(width = 32, height = 32, bitsPerPixel = 32), decodeResourceImageInfo("/jtransc-icon-icc.png"))
		Assert.assertEquals(ImageInfo(width = 32, height = 32, bitsPerPixel = 32), decodeResourceImageInfo("/jtransc-icon.svg"))
		Assert.assertEquals(ImageInfo(width = 32, height = 32, bitsPerPixel = 24), decodeResourceImageInfo("/jtransc-icon.jpg"))
		Assert.assertEquals(ImageInfo(width = 32, height = 32, bitsPerPixel = 24), decodeResourceImageInfo("/jtransc-icon-progressive.jpg"))
	}
}
