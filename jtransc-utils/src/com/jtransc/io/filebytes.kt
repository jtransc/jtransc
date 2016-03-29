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

package com.jtransc.io

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.Charset

object FileBytes {
	fun makeDirectories(file: File) = file.mkdirs()

	fun copy(from: File, to: File) = write(to, read(from))
	fun read(file: File): ByteArray = read(FileInputStream(file))

	fun read(file: File, charset: Charset): String = String(read(file), charset)

	fun write(file: File, data: ByteArray): Unit {
		val fout = FileOutputStream(file)
		fout.write(data)
		fout.close()
	}

	fun write(file: File, charset: Charset, string: String): Unit {
		val bb = charset.encode(string)
		val data = ByteArray(bb.remaining())
		bb.get(data)
		write(file, data)
	}

	fun read(`is`: InputStream): ByteArray {
		val data = ByteArray(`is`.available().toInt())
		`is`.read(data)
		`is`.close()
		return data
	}
}
