/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
