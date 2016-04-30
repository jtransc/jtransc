package example

import com.jtransc.async.syncWait
import com.jtransc.debugger.v8.*
import com.jtransc.io.ProcessUtils
import java.io.File

class V8Example {
	companion object {
		@JvmStatic fun main(args: Array<String>) {
			val port = NodeJS.debugAsync(File("c:/projects/jtransc/test.js"), object : ProcessUtils.ProcessHandler {
				override fun onOutputData(data: String) {
					System.out.print(data)
				}

				override fun onErrorData(data: String) {
					System.err.print(data)
				}

				override fun onCompleted(exitValue: Int) {
					System.out.println("EXIT:$exitValue")
				}
			})

			val socket = createV8DebugSocket(port, "127.0.0.1")
			socket.cmdRequestScripts() {
				println(it?.encodePrettily())
			}
			println(socket.cmdRequestSource().syncWait())
			println(socket.cmdEvaluate("require").syncWait())
			println(socket.cmdRequestFrames().syncWait())
			socket.handleBreak {
				println(it)
			}
			socket.handleEvent { message ->
				println(message.encodePrettily())
			}

			//vertx.close()
		}
	}
}

