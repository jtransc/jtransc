package example

import all.as3.As3Lib
import all.media.*
import all.media.as3.As3MediaEngine
import flash.display.Bitmap
import flash.display.BitmapData
import flash.display.PixelSnapping
import kotlin.properties.Delegates

object Test {
	private fun rgba(r: Int, g: Int, b: Int, a: Int): Int {
		return ((b and 255) shl 0) or ((g and 255) shl 8) or ((r and 255) shl 16) or ((a and 255) shl 24)
	}

	private fun rgbaf(r: Double, g: Double, b: Double, a: Double): Int {
		return rgba((r * 255).toInt(), (g * 255).toInt(), (b * 255).toInt(), (a * 255).toInt())
	}

	private val RED = rgbaf(1.0, 0.0, 0.0, 1.0)
	private val GREEN = rgbaf(0.0, 1.0, 0.0, 1.0)
	private val BLUE = rgbaf(0.0, 0.0, 1.0, 1.0)
	private val COLORS = intArrayOf(RED, GREEN, BLUE)

	private fun putBlock(x: Int, y: Int, color: Int) {
		val bitmapData = BitmapData(64, 64, false, color)
		val bitmap = Bitmap(bitmapData, PixelSnapping.AUTO, true)
		bitmap.x = x.toDouble()
		bitmap.y = y.toDouble()
		As3Lib.stage.addChild(bitmap)
	}

	@JvmStatic fun main(args: Array<String>) {
		println(listOf(1, 2, 1, 2, 3, 3).sum())
		println(listOf(1, 2, 1, 2, 3, 3).distinct().sum())
		println(Integer.parseInt("1000", 16))
		val engine = As3MediaEngine()
		engine.start(object : AllMediaHandler() {
			//private var texture: AllRenderTexture by Delegates.notNull()
			private var texture: AllRenderTexture? = null

			override fun init(context: AllRenderContext) {
				val clazz = Test::class.java
				println("init! ${Test::class.java.name}" )
				this.texture = context.createTexture(object: AllTextureSource {
					override fun getWidth(): Int = 1
					override fun getHeight(): Int = 1
					override fun init() {
					}

					override fun getData(): ByteArray {
						return byteArrayOf(-1, -1, 0x00, -1)
					}
				})
				this.texture!!.dispose()
			}

			override fun render(context: AllRenderContext) {
				val vertices = context.createVertexBuffer(3, 3)
				val indices = context.createIndexBuffer(3);
				vertices.dispose()
				indices.dispose()
				/*
				indices.upload(intArrayOf(0, 1, 2), 0, 3)
				*/
				//println("render!")
				//context.createTexture();
			}
		})
		/*
		for (x in 0..5) {
			for (y in 0..5) {
				putBlock(x * 64, y * 64, COLORS[(x + y) % COLORS.size()])
			}
		}
		*/
		//throw Exception("Too bad")
	}
}
