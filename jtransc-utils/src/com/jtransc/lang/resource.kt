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

package com.jtransc.lang

import com.jtransc.vfs.UTF8
import java.nio.charset.Charset

fun <T> Class<T>.getResourceBytes(path:String):ByteArray? {
	return try {
		this.getResourceAsStream(path).readBytes()
	} catch (t:Throwable) {
		null
	}
}
fun <T> Class<T>.getResourceAsString(path:String, charset: Charset = UTF8):String? {
	return try {
		this.getResourceAsStream(path).readBytes().toString(charset)
	} catch (t:Throwable) {
		null
	}
}
