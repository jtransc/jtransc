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

package com.jtransc.gen.cpp

import com.jtransc.ast.AstMethod

object JniUtils {
	fun mangleLongJavaMethod(method: AstMethod): String {
		return mangleJavaMethod(method, true)
	}

	private fun mangleJavaMethod(method: AstMethod, long: Boolean): String {
		val sb = StringBuilder();
		sb.append("Java_");
		sb.append(mangleString(method.containingClass.name.fqname.replace('.', '/')));
		sb.append("_");
		sb.append(mangleString(method.name));
		if (long) {
			sb.append("__")
			sb.append(mangleString(method.signature.substring(1, method.signature.lastIndexOf(')'))))
		}
		return sb.toString();
	}

	private fun mangleString(string: String): String {
		val sb = StringBuilder(string.length)

		fun mangleChar(c: Char) {
			fun isOnlyAscii(char: Char): Boolean {
				if (c >= '0' && c <= '9' || c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z') return true
				return false
			}
			when {
				isOnlyAscii(c) -> sb.append(c)
				c == '_' -> sb.append("_1")
				c == ';' -> sb.append("_2")
				c == '[' -> sb.append("_3")
				c == '/' -> sb.append("_")
				else -> sb.append(String.format("_0%04x", c.toInt()))
			}
		}
		string.forEach { mangleChar(it) }
		return sb.toString()
	}

	fun mangleShortJavaMethod(method: AstMethod): String {
		return mangleJavaMethod(method, false)
	}
}