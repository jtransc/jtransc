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

import java.io.*

object BinarySerializer {
	fun <T> serialize(value: T): ByteArray {
		val bos = ByteArrayOutputStream()
		val oos = ObjectOutputStream(bos)
		oos.writeUnshared(value)
		return bos.toByteArray()
	}

	@Suppress("unchecked_cast")
	fun <T> deserialize(data:ByteArray):T {
		val ois = ObjectInputStream(ByteArrayInputStream(data, 0, data.size))
		return ois.readUnshared() as T
	}
}