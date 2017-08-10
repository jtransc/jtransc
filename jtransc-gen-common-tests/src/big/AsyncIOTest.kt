package big

import com.jtransc.JTranscSystem
import com.jtransc.JTranscSystemProperties
import com.jtransc.io.async.JTranscAsyncFile
import com.jtransc.io.async.readAll
import com.jtransc.io.async.writeBytes
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext
import kotlin.coroutines.experimental.startCoroutine

class AsyncIOTest {
	companion object {
		private fun runBlocking(callback: suspend () -> Unit) {
			var completed = false
			callback.startCoroutine(object : Continuation<Unit> {
				override val context: CoroutineContext = EmptyCoroutineContext
				override fun resume(value: Unit) {
					completed = true
				}

				override fun resumeWithException(exception: Throwable) {
					exception.printStackTrace()
					completed = true
				}
			})
			if (!JTranscSystem.hasEventLoop()) {
				while (!completed) Thread.sleep(10L)
			}
		}

		@JvmStatic fun main(args: Array<String>) = runBlocking {
			val tmpfile = JTranscSystemProperties.tmpdir() + "/AsyncIOTest.bin"
			JTranscAsyncFile(tmpfile).writeBytes(byteArrayOf(1, 2, 3, 4))
			println(JTranscAsyncFile(tmpfile).readAll().toList())
		}
	}
}
