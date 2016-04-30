package com.jtransc.debugger.v8

import com.jtransc.async.Promise
import com.jtransc.async.syncWait
import com.jtransc.debugger.JTranscDebugger
import com.jtransc.net.TcpClientAsync
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import java.nio.charset.Charset
import java.util.*

// https://github.com/v8/v8/wiki/Debugging%20Protocol

class V8JTranscDebugger(
	val port: Int,
	val host: String = "127.0.0.1",
	handler: EventHandler
) : JTranscDebugger(handler) {
	val socket = createV8DebugSocket(port, host)

	override var currentPosition: SourcePosition = SourcePosition("unknown", 0)

	init {
		socket.handle {
			//println(it)
		}
		socket.handleBreak { body ->
			currentPosition = body.script.toSourcePosition()
			handler.onBreak()
		}

		socket.cmdRequestScripts().then {
			println(it)
		}
	}

	override fun pause() {
		//socket.cmdPause().syncWait()
	}

	override fun resume() {
		socket.cmdResume().syncWait()
	}

	override fun stepOver() {
		socket.cmdStepNext().syncWait()
	}

	override fun stepInto() {
		socket.cmdStepIn().syncWait()
	}

	override fun stepOut() {
		socket.cmdStepOut().syncWait()
	}

	override fun disconnect() {
		socket.cmdDisconnect()
	}

	private fun ScriptPosition.toSourcePosition(): JTranscDebugger.SourcePosition {
		return JTranscDebugger.SourcePosition(this.name, this.lineOffset)
	}
}


class ScriptPosition {
	@JvmField var id: Int = 0
	@JvmField var name: String = ""
	@JvmField var lineOffset: Int = 0
	@JvmField var columnOffset: Int = 0
	@JvmField var lineCount: Int = 0

	override fun toString(): String {
		return "ScriptPosition(id=$id, name='$name', lineOffset=$lineOffset, columnOffset=$columnOffset, lineCount=$lineCount)"
	}
}

class BreakResponseBody {
	@JvmField var invocationText: String = ""
	@JvmField var sourceLine: Int = 0
	@JvmField var sourceColumn: Int = 0
	@JvmField var sourceLineText: String = ""
	@JvmField var script: ScriptPosition = ScriptPosition()
	@JvmField var breakpoints: List<Int> = listOf()

	override fun toString(): String {
		return "BreakResponseBody(invocationText='$invocationText', sourceLine=$sourceLine, sourceColumn=$sourceColumn, sourceLineText='$sourceLineText', script=$script, breakpoints=$breakpoints)"
	}
}

fun V8DebugSocket.handleBreak(handler: (BreakResponseBody) -> Unit) {
	this.handleEvent { message ->
		if (message.getString("event") == "break") {
			//println(message.encodePrettily())
			handler(Json.decodeValue(message.getJsonObject("body").toString(), BreakResponseBody::class.java))
		}
	}
}

fun V8DebugSocket.cmdRequestScripts(): Promise<JsonObject> {
	return this.sendRequestAndWaitAsync("scripts", mapOf())
}

fun V8DebugSocket.cmdStepIn(count: Int = 1): Promise<JsonObject> {
	return cmdContinue("in", count)
}

fun V8DebugSocket.cmdStepNext(count: Int = 1): Promise<JsonObject> {
	return cmdContinue("next", count)
}

fun V8DebugSocket.cmdStepOut(count: Int = 1): Promise<JsonObject> {
	return cmdContinue("out", count)
}

fun V8DebugSocket.cmdDisconnect() {
	this.sendRequestAndWaitAsync("disconnect", mapOf())
}

fun V8DebugSocket.cmdResume(): Promise<JsonObject> {
	return this.sendRequestAndWaitAsync("continue", mapOf())
}

fun V8DebugSocket.cmdContinue(action: String, count: Int = 1): Promise<JsonObject> {
	return this.sendRequestAndWaitAsync("continue", mapOf("stepaction" to action, "stepcount" to count))
}

// https://github.com/v8/v8/wiki/Debugging-Protocol#request-source
class SourceResponse {
	var source: String = ""
	var fromLine: Int = 0
	var toLine: Int = 0
	var fromPosition: Int = 0
	var toPosition: Int = 0
	var totalLines: Int = 0
	override fun toString() = "SourceResponse(source='$source', fromLine=$fromLine, toLine=$toLine, fromPosition=$fromPosition, toPosition=$toPosition, totalLines=$totalLines)"
}

fun V8DebugSocket.cmdRequestSource(fromLine: Int = -1, toLine: Int = Int.MAX_VALUE): Promise<SourceResponse> {
	return this.sendRequestAndWaitAsync("source", mapOf("fromLine" to fromLine, "toLine" to toLine)).then {
		Json.decodeValue(it.encode(), SourceResponse::class.java)
	}
}

fun V8DebugSocket.cmdRequestFrames(fromFrame: Int = 0, toFrame: Int = 10): Promise<JsonObject> {
	return this.sendRequestAndWaitAsync("backtrace", mapOf("fromFrame" to fromFrame, "toFrame" to toFrame)).then {
		println(it)
		it
	}
}

//{"seq":117,"type":"request","command":"evaluate","arguments":{"expression":"1+2"}}
fun V8DebugSocket.cmdEvaluate(expression: String): Promise<Any> {
	return this.sendRequestAndWaitAsync("evaluate", mapOf("expression" to expression)).then { it.getValue("value") ?: Unit }
}

fun V8DebugSocket.sendRequestAndWaitAsync(command: String, arguments: Map<String, Any?>): Promise<JsonObject> {
	return this.writeAndWaitAsync(JsonObject(mapOf("seq" to seq++, "type" to "request", "command" to command, "arguments" to arguments))).then { message ->
		if (message.getBoolean("success")) {
			message.getJsonObject("body") ?: JsonObject(mapOf())
		} else {
			throw RuntimeException(message.getString("message"))
		}
	}
}

fun createV8DebugSocket(port: Int = 5858, host: String = "127.0.0.1") = V8DebugSocket(port, host)

class V8DebugSocket(val port: Int = 5858, val host: String = "127.0.0.1") {
	private val handlers = Collections.synchronizedList(arrayListOf<(message: JsonObject) -> Unit>())
	private var state = State.HEADER
	@Volatile private var buffer = byteArrayOf()
	var contentLength = 0
	private val UTF8 = Charsets.UTF_8
	var seq = 0

	enum class State { HEADER, HEADER_SPACE, BODY }

	private var client = TcpClientAsync(host, port, object : TcpClientAsync.Handler {
		override fun onOpen() {
			println("Connected!")
		}

		override fun onData(data: ByteArray) {
			buffer += data
			processBuffer()
		}

		override fun onClose() {
		}
	}).apply {
		println("Connecting...")
	}

	private fun readBuffer(len: Int): ByteArray {
		val out = buffer.sliceArray(0 until len)
		buffer = buffer.sliceArray(len until buffer.size)
		return out
	}

	private fun tryReadLine(): String? {
		val index = buffer.indexOf('\n'.toByte())
		if (index >= 0) {
			return readBuffer(index + 1).toString(UTF8)
		} else {
			return null
		}
	}

	private fun processBuffer() {
		main@while (true) {
			when (state) {
				State.HEADER -> {
					while (true) {
						val line = (tryReadLine() ?: return).trim()
						//println("HEADER: '$line'")
						val result = Regex("content-length:\\s*(\\d+)", RegexOption.IGNORE_CASE).find(line)
						if (result != null) {
							contentLength = result.groupValues[1].toInt()
							state = State.HEADER_SPACE
							continue@main
						}
					}
				}
				State.HEADER_SPACE -> {
					val line = tryReadLine() ?: return
					//println("EMPTY line($line)")
					state = State.BODY
					continue@main
				}
				State.BODY -> {
					if (buffer.size < contentLength) return
					val data = readBuffer(contentLength).toString(UTF8)
					if (data.length > 0) {
						println("DATA($data)")
						val message = JsonObject(data)
						for (handler in handlers.toList()) handler(message)
					}
					state = State.HEADER
					continue@main
				}
			}
		}
	}

	fun handle(handler: (message: JsonObject) -> Unit) {
		handlers += handler
	}

	fun handleEvent(handler: (message: JsonObject) -> Unit) {
		handlers += { message ->
			if (message.getString("type") == "event") {
				handler(message)
			}
		}
	}

	fun write(data: ByteArray) {
		client.write(data)
	}

	fun write(message: JsonObject) {
		val body = message.encode().toByteArray(Charset.forName("UTF-8"))
		val head = "Content-Length: ${body.size}\r\n\r\n".toByteArray(Charset.forName("UTF-8"))
		write(head + body)
	}

	fun writeAndWaitAsync(message: JsonObject): Promise<JsonObject> {
		val deferred = Promise.Deferred<JsonObject>()

		val seq = message.getInteger("seq")
		var myhandler: ((obj: JsonObject) -> Unit)? = null
		myhandler = { message: JsonObject ->
			if (message.getInteger("request_seq") == seq) {
				handlers -= myhandler!!
				deferred.resolve(message)
			}
		}
		write(message)
		handlers += myhandler
		return deferred.promise
	}
}