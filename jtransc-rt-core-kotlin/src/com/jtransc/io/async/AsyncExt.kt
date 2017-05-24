package com.jtransc.io.async

import com.jtransc.async.JTranscAsyncHandler
import com.jtransc.io.JTranscFileMode
import com.jtransc.io.JTranscFileStat
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.suspendCoroutine

private fun <T> Continuation<T>.jt(): JTranscAsyncHandler<T> = JTranscAsyncHandler<T> { value, error ->
	if (error != null) this@jt.resumeWithException(error) else this@jt.resume(value)
}

suspend fun JTranscAsyncFile.getLength(): Long = suspendCoroutine { this.getLengthAsync(it.jt()) }
suspend fun JTranscAsyncFile.mkdir(): Boolean = suspendCoroutine { this.mkdirAsync(it.jt()) }
suspend fun JTranscAsyncFile.stat(): JTranscFileStat = suspendCoroutine { this.statAsync(it.jt()) }
suspend fun JTranscAsyncFile.open(mode: JTranscFileMode = JTranscFileMode.READ): JTranscAsyncStream = suspendCoroutine { this.openAsync(mode, it.jt()) }
suspend fun JTranscAsyncFile.readAll(): ByteArray = openUse { readBytes(0L, getLength().toInt()) }

suspend fun JTranscAsyncFile.writeBytes(data: ByteArray): Unit {
	openUse(JTranscFileMode.WRITE) {
		setLength(0L)
		write(0L, data)
	}
}

suspend fun <T> JTranscAsyncFile.openUse(mode: JTranscFileMode = JTranscFileMode.READ, callback: suspend JTranscAsyncStream.() -> T): T {
	val file = open(mode)
	try {
		return callback(file)
	} finally {
		file.close()
	}
}

suspend fun JTranscAsyncStream.write(position: Long, out: ByteArray, offset: Int = 0, len: Int = out.size): Int = suspendCoroutine { this.writeAsync(position, out, offset, len, it.jt()) }
suspend fun JTranscAsyncStream.read(position: Long, out: ByteArray, offset: Int = 0, len: Int = out.size): Int = suspendCoroutine { this.readAsync(position, out, offset, len, it.jt()) }
suspend fun JTranscAsyncStream.close(): Int = suspendCoroutine { this.closeAsync(it.jt()) }

//suspend fun JTranscAsyncStream.position(): Long = suspendCoroutine { this.getPositionAsync(it.toJTranscAsyncHandler()) }
suspend fun JTranscAsyncStream.getLength(): Long = suspendCoroutine { this.getLengthAsync(it.jt()) }
//suspend fun JTranscAsyncStream.available(): Long = length() - position()

//suspend fun JTranscAsyncStream.setPosition(value: Long): Long = suspendCoroutine { this.setPositionAsync(value, it.toJTranscAsyncHandler()) }
suspend fun JTranscAsyncStream.setLength(value: Long): Long = suspendCoroutine { this.setLengthAsync(value, it.jt()) }

suspend fun JTranscAsyncStream.readBytes(position: Long, count: Int): ByteArray {
	val out = ByteArray(count)
	val size = read(position, out)
	return out.copyOf(size)
}

val jtranscAsyncFileSystem get() = JTranscAsyncFileSystem.getInstance()

suspend fun JTranscAsyncFileSystem.mkdir(path: String): Boolean = suspendCoroutine { this.mkdir(path, it.jt()) }
suspend fun JTranscAsyncFileSystem.delete(path: String): Boolean = suspendCoroutine { this.delete(path, it.jt()) }
suspend fun JTranscAsyncFileSystem.rename(src: String, dst: String): Boolean = suspendCoroutine { this.rename(src, dst, it.jt()) }
suspend fun JTranscAsyncFileSystem.list(path: String): List<String> = suspendCoroutine<Array<String>> { this.list(path, it.jt()) }.toList()

val jtranscAsyncResources get() = JTranscAsyncResources.getInstance()

suspend fun JTranscAsyncResources.open(path: String, classLoader: ClassLoader = ClassLoader.getSystemClassLoader()): JTranscAsyncStream = suspendCoroutine { this.openAsync(classLoader, path, it.jt()) }
suspend fun JTranscAsyncResources.stat(path: String, classLoader: ClassLoader = ClassLoader.getSystemClassLoader()): JTranscFileStat = suspendCoroutine { this.statAsync(classLoader, path, it.jt()) }