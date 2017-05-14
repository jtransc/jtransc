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

package com.jtransc.vfs

import com.jtransc.error.noImpl
import com.jtransc.io.ProcessUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.nio.charset.Charset
import java.util.*

//fun String.toBuffer(encoding: Charset = UTF8): ByteBuffer = encoding.toBuffer(this)
//fun ByteBuffer.toString(encoding: Charset): String = encoding.toString(this)

//fun Charset.toBuffer(_data: String): ByteBuffer = this.encode(_data)
//fun Charset.toString(data: ByteBuffer): String = this.toString(data.getBytes())

fun Charset.toBytes(data: String) = data.toByteArray(this)
fun Charset.toString(data: ByteArray) = data.toString(this)
//fun Charset.toString(data: ByteBuffer):String = data.getBytes().toString(this)

//fun ByteBuffer.length(): Int = this.limit()
//
//fun ByteBuffer.getBytes(): ByteArray {
//	val out = ByteArray(this.length())
//	for (n in 0 until out.size) out[n] = this.get(n)
//	//this.get(out)
//	return out
//}
//
//fun ByteArray.toBuffer(): ByteBuffer = ByteBuffer.wrap(this)

//object ByteBufferUtils {
//	fun copy(from: ByteBuffer, fromOffset: Int, to: ByteBuffer, toOffset: Int, size: Int) {
//		for (n in 0..size - 1) to.put(toOffset + n, from[fromOffset + n])
//	}
//
//	fun combine(buffers: Iterable<ByteBuffer>): ByteBuffer {
//		val totalLength = buffers.sumBy { it.length() }
//		val out = ByteBuffer.allocateDirect(totalLength)
//		var pos = 0
//		for (buffer in buffers) {
//			copy(buffer, 0, out, pos, buffer.length())
//			pos += buffer.length()
//		}
//		return out
//	}
//}

inline fun <T> chdirTemp(path: String, callback: () -> T): T {
	val old = RawIo.cwd()
	RawIo.chdir(path)
	try {
		return callback();
	} finally {
		RawIo.chdir(old)
	}
}

data class ProcessResult(val output: ByteArray, val error: ByteArray, val exitCode: Int) {
	val outputString: String get() = output.toString(UTF8).trim()
	val errorString: String get() = error.toString(UTF8).trim()
	val success: Boolean get() = exitCode == 0
}

/*
data class Encoding(val name: String) {
    public val charset: Charset = java.nio.charset.Charset.forName(name)
    fun toBytes(data: String) = data.toByteArray(charset)
    fun toString(data: ByteArray) = data.toString(charset)
    fun toString(data: ByteBuffer) = data.getBytes().toString(charset)
}
*/

val UTF8 = Charsets.UTF_8

class StopExecException() : Exception()

fun File.readFastBytes(): ByteArray = FileInputStream(this@readFastBytes).use { s ->
	//println("READ: $this")
	val out = ByteArray(this@readFastBytes.length().toInt())
	var offset = 0
	while (true) {
		val read = s.read(out, offset, out.size - offset)
		if (read <= 0) break
		offset += read
	}
	out
}

object RawIo {
	private var userDir = System.getProperty("user.dir")
	//private var userDir = File(".").getCanonicalPath()

	fun fileRead(path: String): ByteArray = File(path).readFastBytes()
	fun fileWrite(path: String, data: ByteArray): Unit = File(path).writeBytes(data)

	fun listdir(path: String): Array<File> {
		val file = File(path)
		if (!file.exists()) {
			throw FileNotFoundException("Can't find $path")
		}
		return file.listFiles()
	}

	fun fileExists(path: String): Boolean {
		return File(path).exists()
	}

	fun rmdir(path: String): Unit {
		File(path).delete()
	}

	fun fileRemove(path: String): Unit {
		File(path).delete()
	}

	fun fileStat(path: String): File = File(path)

	fun setMtime(path: String, time: Date) {
		File(path).setLastModified(time.time)
	}

	fun cwd(): String = userDir
	fun script(): String = userDir
	fun chdir(path: String) {
		userDir = File(path).canonicalPath
	}

	fun execOrPassthruSync(path: String, cmd: String, args: List<String>, options: ExecOptions): ProcessResult {
		val result = ProcessUtils.run(File(path), cmd, args, options = options)
		return ProcessResult(result.out.toByteArray(), result.err.toByteArray(), result.exitValue)
	}

	fun mkdir(path: String) {
		File(path).mkdir()
	}

	//private fun getNioPath(path: String): java.nio.file.Path = Paths.get(URI("file://$path"))

	fun chmod(path: String, mode: FileMode): Boolean {
		noImpl("Not implemented chmod")
		/*
		try {
			Files.setPosixFilePermissions(getNioPath(path), mode.toPosix())
			return true
		} catch (t: Throwable) {
			return false
		}
		*/
	}

	fun symlink(link: String, target: String) {
		noImpl("Not implemented symlink")
		//Files.createSymbolicLink(getNioPath(link), getNioPath(target))
		//execOrPassthruSync(".", "ln", listOf("-s", target, link))
	}
}


//class BufferReader(val buffer: ByteBuffer) {
//	//val rb = buffer.order(ByteOrder.LITTLE_ENDIAN)
//	val rb = buffer.order(ByteOrder.BIG_ENDIAN)
//
//	var offset = 0
//	val length: Int get() = buffer.length()
//
//	private fun move(count: Int): Int {
//		val out = offset
//		offset += count
//		return out
//	}
//
//	fun dump() {
//		for (n in 0..31) {
//			println(Integer.toHexString(buffer.get(n).toInt() and 0xFF))
//		}
//	}
//
//	fun f32(): Double = rb.getFloat(move(4)).toDouble()
//	fun i32(): Int = rb.getInt(move(4))
//	fun i16(): Int = rb.getShort(move(2)).toInt()
//	fun i8(): Int = rb.get(move(1)).toInt()
//}

//fun BufferReader.buffer(len: Int): ByteBuffer {
//	val out = ByteBuffer.allocateDirect(len)
//	for (n in 0..len - 1) out.put(n, this.i8().toByte())
//	return out
//}

//fun BufferReader.strRaw(len: Int): String = buffer(len).toString(UTF8)
//fun BufferReader.u32(): Int = i32() // 31 bit
//fun BufferReader.u16(): Int = i16() and 0xFFFF
//fun BufferReader.u8(): Int = i8() and 0xFF
//
//fun BufferReader.fs8() = u8().toDouble() / 255.0
//fun BufferReader.rgba8(): Int = i32()
//fun BufferReader.quality(): Int = u8()
//fun BufferReader.str(): String = strRaw(u16())
//fun BufferReader.bool(): Boolean = u8() != 0
//fun BufferReader.boolInt(): Int = if (u8() != 0) 1 else 0

//fun ByteBuffer.reader(): BufferReader = BufferReader(this)