package issues

object Issue100Double {
	@JvmStatic fun main(args: Array<String>) {
		println("Issue100Double.main:")
		println("[1]")
		println(0.0)
		println("[2]")
		println(roundPixels400d(0.0))
		println("[3]")
		println(roundPixels400d(10000.0))
		println("[4]")
		println(roundPixels400d(15000.0))
		println("[5]")

		println("[2f]")
		println(roundPixels400f(0f))
		println("[3f]")
		println(roundPixels400f(10000f))
		println("[4f]")
		println(roundPixels400f(15000f))
		println("[5f]")
	}

	@JvmStatic private fun roundPixels400d(pixels: Double): Double = Math.round(pixels * 10000).toDouble() / 10000
	@JvmStatic private fun roundPixels400f(pixels: Float): Float = Math.round(pixels * 10000f).toFloat() / 10000f
}
