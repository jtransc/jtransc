package big

import com.jtransc.JTranscSystem
import com.jtransc.JTranscSystemProperties
import com.jtransc.io.async.JTranscAsyncFile
import com.jtransc.io.async.readAll
import com.jtransc.io.async.writeBytes
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

class AsyncIOTest {
	companion object {
		private fun runBlocking(callback: suspend () -> Unit) {
			var completed = false
			callback.startCoroutine(object : Continuation<Unit> {
				override val context: CoroutineContext = EmptyCoroutineContext

				override fun resumeWith(result: Result<Unit>) {
					completed = true
					if (result.isFailure) {
						result.exceptionOrNull()?.printStackTrace()
					}
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
