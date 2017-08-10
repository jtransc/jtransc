package javatest.misc

object TryFinallyCheck {
	var value = 0

	@JvmStatic fun main(args: Array<String>) {
		println("TryFinallyCheck:")
		try {
			demo()
		} catch (e: Throwable) {
			println(e.message)
		}
		//try {
		//	demo()
		//} catch (e: Throwable) {
		//	println(e.message)
		//}
	}

	@JvmStatic fun demo() {
		try {
			throw RuntimeException("Check$value")
		} finally {
			println("Finally")
		}
	}

	//@JvmStatic fun demo() {
	//	tempSet(10) {
	//		throw RuntimeException("Check$value")
	//	}
	//}
//
	//inline fun tempSet(newvalue: Int, callback: () -> Unit) = this.apply {
	//	val old = this.value
	//	try {
	//		this.value = newvalue
	//		callback()
	//	} finally {
	//		this.value = old
	//	}
	//}
}