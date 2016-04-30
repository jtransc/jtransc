package example

import com.jtransc.debugger.v8.*
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.core.net.NetSocket
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.locks.Lock

class V8Example {
	companion object {
		@JvmStatic fun main(args: Array<String>) {
			val vertx = Vertx.vertx(VertxOptions());

			//Launcher().dispatch(arrayOf("run", "example.V8Example"))
			//val vertx = VertxImpl()
			val socket = vertx.createV8DebugSocket(5858, "127.0.0.1")
			socket.cmdRequestScripts() {
				println(it.encodePrettily())
			}
			socket.cmdRequestSource() {
				println(it)
			}
			socket.cmdEvaluate("1+2") {
				println(it)
			}
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

