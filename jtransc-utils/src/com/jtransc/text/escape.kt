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

package com.jtransc.text

fun String.escape(): String {
	val out = StringBuilder()
	for (n in 0 until this.length) {
		val c = this[n]
		when (c) {
			'\\' -> out.append("\\\\")
			'"' -> out.append("\\\"")
			'\n' -> out.append("\\n")
			'\r' -> out.append("\\r")
			'\t' -> out.append("\\t")
			in '\u0000' .. '\u001f' -> out.append("\\x" + "%02x".format(c.toInt()))
			else -> out.append(c)
		}
	}
	return out.toString()
}

fun String?.quote(): String = if (this != null) "\"${this.escape()}\"" else "null"
