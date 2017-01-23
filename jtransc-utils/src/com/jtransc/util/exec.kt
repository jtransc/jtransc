package com.jtransc.util

import com.jtransc.text.captureStdout

object ClassUtils {
	@JvmStatic fun <T : Any> callMain(clazz: Class<T>, vararg args: String): String {
		com.jtransc.log.log("Executing: " + clazz.name + ".main()")
		val result = captureStdout {
			val m = clazz.getDeclaredMethod("main", Array<String>::class.java) ?: throw RuntimeException("Class $clazz doesn't have a static method 'main'")
			m.invoke(null, args as Any)
		}
		com.jtransc.log.log("Result: " + result)
		return result
	}
}