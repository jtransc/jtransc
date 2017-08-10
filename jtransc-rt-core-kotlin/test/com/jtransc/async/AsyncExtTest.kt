package com.jtransc.async

import com.jtransc.io.async.JTranscAsyncFile
import com.jtransc.io.async.stat
import org.junit.Assert
import org.junit.Test
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext
import kotlin.coroutines.experimental.startCoroutine

class AsyncExtTest {
	@Test
	fun name() = syncTest {
		val stat = JTranscAsyncFile("/").stat()
		Assert.assertEquals(true, stat.exists)
	}
}

fun syncTest(block: suspend () -> Unit): Unit {
	var result: Any? = null

	block.startCoroutine(object : Continuation<Unit> {
		override val context: CoroutineContext = EmptyCoroutineContext

		override fun resume(value: Unit) = run {
			result = value
		}

		override fun resumeWithException(exception: Throwable) = run {
			result = exception
		}
	})

	while (result == null) {
		Thread.sleep(1L)
	}
	if (result is Throwable) throw result as Throwable
	return Unit
}