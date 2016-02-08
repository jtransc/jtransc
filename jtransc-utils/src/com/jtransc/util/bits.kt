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

package com.jtransc.util

object Bits {
	@JvmStatic
	fun mask(count:Int):Int = (1 shl count) - 1

	@JvmStatic
	fun extract(data: Int, offset: Int, length: Int): Int {
		return (data ushr offset) and mask(length)
	}
}

fun Int.extract(offset: Int, length: Int):Int {
	return Bits.extract(this, offset, length)
}

fun Int.hasBitmask(mask: Int):Boolean = (this and mask) == mask
