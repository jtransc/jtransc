package example

import com.jtransc.debugger.JTranscDebugger
import com.jtransc.debugger.v8.NodeJS
import com.jtransc.debugger.v8.V8JTranscDebugger
import com.jtransc.io.ProcessUtils
import java.io.File

class V8Example {
	companion object {
		@JvmStatic fun main(args: Array<String>) {
			class Test {
				val process = this

				val testjsPath = V8Example::class.java.getResource("/test.js")
				val testjsFile = File(testjsPath.path).absoluteFile

				init {
					println("testjsPath: $testjsPath")
				}

				@Volatile var debugger: V8JTranscDebugger? = null
				val debuggerPromise = NodeJS.debug2Async(testjsFile, object : ProcessUtils.ProcessHandler() {
					override fun onStarted() {
						println("Started!")
					}

					override fun onOutputData(data: String) {
						System.out.print(data)
					}

					override fun onErrorData(data: String) {
						System.err.print(data)
					}

					override fun onCompleted(exitValue: Int) {
						println("EXIT:$exitValue!")
					}
				}, object : JTranscDebugger.EventHandler() {
					override fun onBreak() {
						println(process.debugger?.currentPosition)
						println("break!")
						//debugger!!.socket.cmdRequestScripts()
						for (frame in debugger!!.backtrace()) {
							println("FRAME: $frame")
						}
					}
				}).then {
					println("set debugger!")
					debugger = it as V8JTranscDebugger
					startedDebugger(it as V8JTranscDebugger)
				}

				fun startedDebugger(debugger: JTranscDebugger) {
					//for (script in debugger.socket.cmdRequestScripts().syncWait()) {
					//	println("SCRIPT: $script")
					//}
					debugger.setBreakpoint("""C:\projects\jtransc\jtransc\jtransc-debugger\target\test-classes\test.js""", 5)
					debugger.resume()
				}
			}

			Test()

			/*
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
			*/

			//vertx.close()
		}
	}
}

