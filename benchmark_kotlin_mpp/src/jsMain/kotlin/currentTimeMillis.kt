import kotlin.js.Date

actual fun currentTimeMillis(): Double {
	return Date.now()
}