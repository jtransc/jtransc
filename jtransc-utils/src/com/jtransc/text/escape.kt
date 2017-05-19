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
			in '\u0000'..'\u001f' -> out.append("\\x" + "%02x".format(c.toInt()))
			else -> out.append(c)
		}
	}
	return out.toString()
}

fun String.uescape(): String {
	val out = StringBuilder()
	for (n in 0 until this.length) {
		val c = this[n]
		when (c) {
			0.toChar() -> out.append("\\0")
			'\\' -> out.append("\\\\")
			'"' -> out.append("\\\"")
			'\n' -> out.append("\\n")
			'\r' -> out.append("\\r")
			'\t' -> out.append("\\t")
			//else -> if (c.isPrintable()) {
			//	out.append(c)
			//} else {
			//	out.append("\\u" + "%04x".format(c.toInt()))
			//}
			in 'a' .. 'z', in 'A' .. 'Z', in '0' .. '9', '_', '.', ',', ';', ':', '<', '>', '{', '}', '[', ']', '/', ' ', '=', '!', '%', '&' -> out.append(c)
			else -> out.append("\\u" + "%04x".format(c.toInt()))
		}
	}
	return out.toString()
}

fun String.unescape(): String {
	val out = StringBuilder()
	var n = 0
	while (n < this.length) {
		val c = this[n++]
		when (c) {
			'\\' -> {
				val c2 = this[n++]
				when (c2) {
					'\\' -> out.append('\\')
					'"' -> out.append('\"')
					'n' -> out.append('\n')
					'r' -> out.append('\r')
					't' -> out.append('\t')
					'u' -> {
						val chars = this.substring(n, n + 4)
						n += 4
						out.append(Integer.parseInt(chars, 16).toChar())
					}
					else -> {
						out.append("\\$c2")
					}
				}
			}
			else -> out.append(c)
		}
	}
	return out.toString()
}

fun String?.uquote(): String = if (this != null) "\"${this.uescape()}\"" else "null"
fun String?.quote(): String = if (this != null) "\"${this.escape()}\"" else "null"

fun String.isQuoted(): Boolean = this.startsWith('"') && this.endsWith('"')

fun String.unquote(): String = if (isQuoted()) {
	this.substring(1, this.length - 1).unescape()
} else {
	"$this"
}
