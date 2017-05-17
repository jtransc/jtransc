package com.jtransc.io.async

import com.jtransc.async.JTranscAsyncHandler
import com.jtransc.io.JTranscFileMode
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.suspendCoroutine

fun <T> Continuation<T>.toJTranscAsyncHandler(): JTranscAsyncHandler<T> = JTranscAsyncHandler<T> { value, error ->
	if (error != null) {
		this@toJTranscAsyncHandler.resumeWithException(error)
	} else {
		this@toJTranscAsyncHandler.resume(value)
	}
}

suspend fun JTranscAsyncFile.getLength(): Long = suspendCoroutine { this.getLengthAsync(it.toJTranscAsyncHandler()) }
suspend fun JTranscAsyncFile.mkdir(): Boolean = suspendCoroutine { this.mkdirAsync(it.toJTranscAsyncHandler()) }
suspend fun JTranscAsyncFile.open(mode: JTranscFileMode = JTranscFileMode.READ): JTranscAsyncStream = suspendCoroutine { this.openAsync(mode, it.toJTranscAsyncHandler()) }

suspend fun JTranscAsyncFile.readAll(): ByteArray {
	return openUse {
		readBytes(0L, length().toInt())
	}
}

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

suspend fun JTranscAsyncStream.write(position: Long, out: ByteArray, offset: Int = 0, len: Int = out.size): Int = suspendCoroutine { this.writeAsync(position, out, offset, len, it.toJTranscAsyncHandler()) }
suspend fun JTranscAsyncStream.read(position: Long, out: ByteArray, offset: Int = 0, len: Int = out.size): Int = suspendCoroutine { this.readAsync(position, out, offset, len, it.toJTranscAsyncHandler()) }
suspend fun JTranscAsyncStream.close(): Int = suspendCoroutine { this.closeAsync(it.toJTranscAsyncHandler()) }

//suspend fun JTranscAsyncStream.position(): Long = suspendCoroutine { this.getPositionAsync(it.toJTranscAsyncHandler()) }
suspend fun JTranscAsyncStream.length(): Long = suspendCoroutine { this.getLengthAsync(it.toJTranscAsyncHandler()) }
//suspend fun JTranscAsyncStream.available(): Long = length() - position()

//suspend fun JTranscAsyncStream.setPosition(value: Long): Long = suspendCoroutine { this.setPositionAsync(value, it.toJTranscAsyncHandler()) }
suspend fun JTranscAsyncStream.setLength(value: Long): Long = suspendCoroutine { this.setLengthAsync(value, it.toJTranscAsyncHandler()) }

suspend fun JTranscAsyncStream.readBytes(position: Long, count: Int): ByteArray {
	val out = ByteArray(count)
	val size = read(position, out)
	return out.copyOf(size)
}