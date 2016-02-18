package com.jtransc.util

import com.jtransc.text.captureStdout

object ClassUtils {
	@JvmStatic fun <T : Any> callMain(clazz: Class<T>, vararg args: String): String {
		println("Executing: " + clazz.name + ".main()")
		val result = captureStdout {
			val m = clazz.getDeclaredMethod("main", Array<String>::class.java)
			m.invoke(null, args as Any)
		}
		println("Result: " + result)
		return result
	}
}