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

interface ToString {
	override fun toString(): String
}

fun String.toUcFirst():String {
	if (this.isEmpty()) return ""
	return this.substring(0, 1).toUpperCase() + this.substring(1)
}

val HexDigitsLC = "0123456789abcdef"

fun ByteArray.toHexString():String {
	val out = StringBuilder(this.size * 2)
	for (b in this) {
		val bi = b.toInt()
		val l = ((bi ushr 0) and 0xF)
		val h = ((bi ushr 4) and 0xF)
		out.append(HexDigitsLC[h])
		out.append(HexDigitsLC[l])
	}
	return out.toString()
}


fun String.splitLast(char: Char): Pair<String, String> {
	val index = this.lastIndexOf(char)
	if (index < 0) {
		return Pair("", this)
	} else {
		return Pair(this.substring(0, index), this.substring(index + 1))
	}
}
