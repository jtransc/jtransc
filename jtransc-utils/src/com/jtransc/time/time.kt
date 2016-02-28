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

package com.jtransc.time

data class TimeResult<T>(val time: Long, val result: T)

inline fun <T> measureProcess(name:String, callback: () -> T): T {
	print("$name...")
	val (time, result) = measureTime { callback() }
	println("Ok ($time)")
	return result
}

inline fun <T> measureTime(callback: () -> T): TimeResult<T> {
	val start = System.nanoTime()
	val result = callback()
	val end = System.nanoTime()
	return TimeResult(time = (end - start) / 1000000L, result = result)
}
