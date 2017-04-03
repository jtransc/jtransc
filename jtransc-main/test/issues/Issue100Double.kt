package issues

object Issue100Double {
	@JvmStatic fun main(args: Array<String>) {
		println(roundPixels400(0.0))
		println(roundPixels400(10000.0))
		println(roundPixels400(15000.0))
	}

	@JvmStatic private fun roundPixels400(pixels: Double): Double = Math.round(pixels * 10000).toDouble() / 10000
}
