package com.jtransc.debugger.v8

import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.core.net.NetSocket
import java.nio.charset.Charset

// https://github.com/v8/v8/wiki/Debugging%20Protocol

class ScriptPosition {
	@JvmField var id:Int = 0
	@JvmField var name:String = ""
	@JvmField var lineOffset:Int = 0
	@JvmField var columnOffset:Int = 0
	@JvmField var lineCount:Int = 0

	override fun toString(): String{
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

	override fun toString(): String{
		return "BreakResponseBody(invocationText='$invocationText', sourceLine=$sourceLine, sourceColumn=$sourceColumn, sourceLineText='$sourceLineText', script=$script, breakpoints=$breakpoints)"
	}
}
fun V8DebugSocket.handleBreak(handler: (BreakResponseBody) -> Unit) {
	this.handleEvent { message ->
		if (message.getString("event") == "break") {
			handler(Json.decodeValue(message.getJsonObject("body").toString(), BreakResponseBody::class.java))
		}
	}
}

fun V8DebugSocket.cmdRequestScripts(handler: (JsonObject) -> Unit) {
	this.writeAndWait(JsonObject(mapOf("seq" to this.seq++, "command" to "scripts", "type" to "request"))) { message ->
		handler(message)
	}
}

// https://github.com/v8/v8/wiki/Debugging-Protocol#request-source
data class SourceResponse(val source:String, val fromLine:Int, val toLine:Int, val fromPosition:Int, val toPosition: Int, val totalLines:Int)
fun V8DebugSocket.cmdRequestSource(fromLine:Int = -1, toLine:Int = Int.MAX_VALUE, handler: (SourceResponse) -> Unit) {
	this.writeAndWait(JsonObject(mapOf("seq" to this.seq++, "command" to "source", "type" to "request", "arguments" to mapOf(
		"fromLine" to fromLine,
		"toLine" to toLine
	)))) { message ->
		val body = message.getJsonObject("body")
		//println("REPLY: ${message.encodePrettily()}")
		handler(SourceResponse(
			source = body.getString("source", ""),
			fromLine = body.getInteger("fromLine", 0),
			toLine = body.getInteger("toLine", 0),
			fromPosition = body.getInteger("fromPosition", 0),
			toPosition = body.getInteger("toPosition", 0),
			totalLines = body.getInteger("totalLines", 0)
		))
	}
}

fun V8DebugSocket.cmdEvaluate(expression:String, handler: (Any?) -> Unit) {
	//{"seq":117,"type":"request","command":"evaluate","arguments":{"expression":"1+2"}}
	this.writeAndWait(JsonObject(mapOf("seq" to this.seq++, "type" to "request", "command" to "evaluate", "arguments" to mapOf(
		"expression" to expression
	)))) { message ->
		val body = message.getJsonObject("body")

		//println("REPLY: ${message.encodePrettily()}")
		handler(body.getValue("value"))
	}
}

fun Vertx.createV8DebugSocket(port: Int = 5858, host: String = "127.0.0.1") = V8DebugSocket(this, port, host)

class V8DebugSocket(val vertx: Vertx, val port: Int = 5858, val host: String = "127.0.0.1") {
	private var socket: NetSocket? = null
	private val bufferedMessages = arrayListOf<Buffer>()

	private val handlers = arrayListOf<(message: JsonObject) -> Unit>()
	private var state = State.HEADER
	@Volatile private var buffer = byteArrayOf()
	var contentLength = 0
	private val UTF8 = Charsets.UTF_8
	var seq = 0

	enum class State { HEADER, HEADER_SPACE, BODY }

	init {
		val client = vertx.createNetClient()
		client.connect(5858, "127.0.0.1") {
			socket = it.result()
			while (bufferedMessages.isNotEmpty()) {
				val m = bufferedMessages.removeAt(0)
				write(m)
			}

			socket!!.handler {
				//val data = it.toString("UTF-8")
				buffer += it.bytes
				processBuffer()
			}
		}
	}

	private fun readBuffer(len:Int):ByteArray {
		val out = buffer.sliceArray(0 until len)
		buffer = buffer.sliceArray(len until buffer.size)
		return out
	}

	private fun tryReadLine():String? {
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
				State.BODY-> {
					if (buffer.size < contentLength) return
					val data = readBuffer(contentLength).toString(UTF8)
					if (data.length > 0) {
						//println("DATA($data)")
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

	fun write(data: Buffer) {
		if (socket != null) {
			//println("SEND:$data")
			socket!!.write(data)
		} else {
			bufferedMessages += data
		}
	}

	fun write(message: JsonObject) {
		val body = message.encode().toByteArray(Charset.forName("UTF-8"))
		val head = "Content-Length: ${body.size}\r\n\r\n".toByteArray(Charset.forName("UTF-8"))
		write(Buffer.buffer(head + body))
	}

	fun writeAndWait(message: JsonObject, handler: (JsonObject) -> Unit) {
		val seq = message.getInteger("seq")
		var myhandler: ((obj: JsonObject) -> Unit)? = null
		myhandler = { message: JsonObject ->
			if (message.getInteger("request_seq") == seq) {
				handlers -= myhandler!!
				handler(message)
			}
		}
		write(message)
		handlers += myhandler
	}
}