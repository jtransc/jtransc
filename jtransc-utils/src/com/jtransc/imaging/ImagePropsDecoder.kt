package com.jtransc.imaging

import com.jtransc.io.indexOf
import com.jtransc.io.readUpToBytes
import com.jtransc.util.open
import com.jtransc.util.toIntOrNull2
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

data class ImageInfo(
	var width: Int = 0,
	var height: Int = 0,
	var bitsPerPixel: Int = 0
)

interface ImagePropsDecoder {
	fun tryDecodeHeader(s: InputStream): ImageInfo?

	companion object {
		val decoders = listOf(
			PNGImagePropsDecoder,
			JPEGImagePropsDecoder,
			SVGImagePropsDecoder,
			GIFImagePropsDecoder
		)

		fun tryDecodeHeader(file: File): ImageInfo? {
			return tryDecodeHeader(file.readBytes())
		}

		fun tryDecodeHeader(data: ByteArray): ImageInfo? {
			for (decoder in decoders) {
				try {
					val res = decoder.tryDecodeHeader(ByteArrayInputStream(data)) ?: continue
					return res
				} catch (e: Throwable) {
					e.printStackTrace()
				}
			}
			return null
		}
	}
}

object GIFImagePropsDecoder : ImagePropsDecoder {
	override fun tryDecodeHeader(s: InputStream): ImageInfo? {
		val header = s.readUpToBytes(0x10).open()
		val magic = header.readStringz(6, Charsets.UTF_8)
		if (!magic.startsWith("GIF")) return null
		val width = header.readU16_LE()
		val height = header.readU16_LE()
		return ImageInfo(width, height, bitsPerPixel = 8)
	}

}

object SVGImagePropsDecoder : ImagePropsDecoder {
	val svgStartParser = Regex("<svg(.*?)>", RegexOption.IGNORE_CASE)
	val widthRegex = Regex("width\\s*=\\s*(['\"]?)([0-9]+)\\1", RegexOption.IGNORE_CASE)
	val heightRegex = Regex("height\\s*=\\s*(['\"]?)([0-9]+)\\1", RegexOption.IGNORE_CASE)
	override fun tryDecodeHeader(s: InputStream): ImageInfo? {
		val info = s.readUpToBytes(0x100)
		val str = info.toString(Charsets.UTF_8)
		val base = svgStartParser.find(str) ?: return null
		val svgLine = base.groupValues[0]
		val width = widthRegex.find(svgLine)?.groupValues?.getOrNull(2)?.toIntOrNull2() ?: return null
		val height = heightRegex.find(svgLine)?.groupValues?.getOrNull(2)?.toIntOrNull2() ?: return null

		return ImageInfo(width = width, height = height, bitsPerPixel = 32)
	}
}

object PNGImagePropsDecoder : ImagePropsDecoder {
	private val IHDRBytes = "IHDR".toByteArray(Charsets.UTF_8)

	override fun tryDecodeHeader(s: InputStream): ImageInfo? {
		val info = s.readUpToBytes(0x100)
		val index = info.indexOf(IHDRBytes)
		if (index < 0) return null
		val hs = info.open()
		hs.position = index.toLong() + 4
		val width = hs.readS32_BE()
		val height = hs.readS32_BE()
		val bits = hs.readS8_BE()
		val colorSpace = hs.readS8_BE().toInt()
		val compressionMethod = hs.readS8_BE()
		val filterMethod = hs.readS8_BE()
		val interlaceMethod = hs.readS8_BE()
		return ImageInfo(
			width = width,
			height = height,
			bitsPerPixel = when (colorSpace) {
				0 -> 8
				2 -> 24
				3 -> 8
				4 -> 16
				6 -> 32
				else -> -1
			}
		)
	}
}

object JPEGImagePropsDecoder : ImagePropsDecoder {
	override fun tryDecodeHeader(ss: InputStream): ImageInfo? {
		try {
			val s = ss.readBytes(ss.available()).open()
			val magic = s.readU16_BE()
			if (magic != 0xFFD8) return null
			while (!s.eof()) {
				val mark = s.readU16_BE()
				val size = s.readU16_BE()
				when (mark) {
					0xFFE0 -> {// M_APP0
						s.readBytes(5) // JFIF
						s.readU16_BE() // major version
						val density = s.readU8_BE()
						val width = s.readU16_BE()
						val height = s.readU16_BE()
						return ImageInfo(width, height, bitsPerPixel = 24)
					}
					0xFFC0, 0xFFC2 -> { // M_SOF0, M_SOF2
						val prec = s.readU8_BE()
						val width = s.readU16_BE()
						val height = s.readU16_BE()
						return ImageInfo(width, height, bitsPerPixel = 24)
					}
					else -> {
						s.skip((size - 2).toLong())
					}
				}
			}
			return null
		} catch (e: Throwable) {
			e.printStackTrace()
			return null
		}
	}
}