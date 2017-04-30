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

package com.jtransc.error

class InvalidOperationException(str: String = "Invalid Operation", cause: Throwable? = null) : Exception(str, cause)
class OutOfBoundsException(index: Int = -1, str: String = "Out Of Bounds") : Exception(str)
class KeyNotFoundException(str: String = "Key Not Found") : Exception(str)
class NotImplementedException(str: String = "Not Implemented") : Exception(str)
class InvalidArgumentException(str: String = "Invalid Argument") : Exception(str)
class MustValidateCodeException(str: String = "Must Validate Code") : Exception(str)
class MustOverrideException(str: String = "Must Override") : Exception(str)
class DeprecatedException(str: String = "Deprecated") : Exception(str)
class UnexpectedException(str: String = "Unexpected") : Exception(str)

val deprecated: Nothing get() = throw MustValidateCodeException()
val mustValidate: Nothing get() = throw NotImplementedException()
val noImpl: Nothing get() = throw NotImplementedException()
val invalidOp: Nothing get() = throw InvalidOperationException()

fun deprecated(msg:String): Nothing { throw DeprecatedException(msg) }
fun mustValidate(msg:String): Nothing { throw MustValidateCodeException(msg) }
fun noImpl(msg:String): Nothing { throw NotImplementedException(msg) }
fun invalidOp(msg:String, cause: Throwable? = null): Nothing {
	throw InvalidOperationException(msg, cause)
}
fun unsupported(msg:String): Nothing { throw UnsupportedOperationException(msg) }
fun invalidArgument(msg:String): Nothing { throw InvalidArgumentException(msg) }
fun unexpected(msg:String): Nothing { throw UnexpectedException(msg) }

// Warns
fun untestedWarn(msg:String): Unit { println("Untested: $msg") }
fun noImplWarn(msg:String): Unit { println("Not implemented: $msg") }

inline fun ignoreErrors(action: () -> Unit) {
	try {
		action()
	} catch (e:Throwable) {
		e.printStackTrace()
	}
}

inline fun <T> nullOnError(action: () -> T): T? {
	try {
		return action()
	} catch (e:Throwable) {
		e.printStackTrace()
		return null
	}
}

inline fun <T, T2> Iterable<T>.firstMapOrNull(callback: (T) -> T2?): T2? {
	for (v in this) {
		val res = callback(v)
		if (res != null) return res
	}
	return null
}