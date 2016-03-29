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

import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.SyncVfs
import com.jtransc.vfs.SyncVfsFile
import java.io.File

object PersistentBinaryCache {
	val tempFolder by lazy { System.getProperty("java.io.tmpdir") }
	private val tempVfs by lazy { LocalVfs(tempFolder) }
	private fun file(key:String):SyncVfsFile {
		val basename = File(key).name
		return tempVfs["$basename.cache"]
	}

	operator fun contains(key: String) = file(key).exists
	operator fun set(key: String, value: ByteArray) = file(key).write(value)
	operator fun get(key: String): ByteArray? = file(key).readOrNull()
}

class PersistentCache<V> {
	val pbc = PersistentBinaryCache

	init {
		println("PersistentCache: ${pbc.tempFolder}")
	}

	operator fun contains(key: String) = key in pbc

	operator fun set(key: String, value: V) {
		pbc[key] = BinarySerializer.serialize(value)
	}

	operator fun get(key: String): V? {
		val bvalue = pbc[key]
		return if (bvalue != null) BinarySerializer.deserialize<V>(bvalue) else null
	}

	fun getCalc(key: String, calculate: (key: String) -> V): V {
		if (key !in this) {
			val obj = calculate(key)
			set(key, obj)
			return obj
		} else {
			return get(key)!!
		}
	}
}
