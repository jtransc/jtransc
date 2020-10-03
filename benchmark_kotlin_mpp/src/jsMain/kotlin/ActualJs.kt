import kotlin.js.Date

actual val kotlinTarget: String = "js"

actual fun currentTimeMillis(): Double {
	return Date.now()
}