package com.jtransc.async

import com.jtransc.io.async.JTranscAsyncFile
import com.jtransc.io.async.stat
import org.junit.Assert
import org.junit.Test
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

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

		override fun resumeWith(value: Result<Unit>) {
			if (value.isSuccess) {
				result = value.getOrNull()
			} else {
				result = value.exceptionOrNull()
			}
		}
	})

	while (result == null) {
		Thread.sleep(1L)
	}
	if (result is Throwable) throw result as Throwable
	return Unit
}