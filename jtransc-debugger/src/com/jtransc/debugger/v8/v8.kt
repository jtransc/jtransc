package com.jtransc.debugger.v8

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.jtransc.async.EventLoop
import com.jtransc.async.Promise
import com.jtransc.async.syncWait
import com.jtransc.debugger.JTranscDebugger
import com.jtransc.net.TcpClientAsync
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
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
	val scriptsByHandle = hashMapOf<Long, V8ScriptResponse>()

	init {
		socket.handle {
			//println(it)
		}
		socket.handleBreak { body ->
			currentPosition = body.script.toSourcePosition()
			handler.onBreak()
		}

		socket.cmdRequestScripts().then { scripts ->
			for (script in scripts) {
				scriptsByHandle[script.handle] = script
			}
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

	override fun backtrace(): List<Frame> {
		//println("aaaaaaaaaaaaaa")
		val frames = socket.cmdRequestFrames().syncWait()
		//println("FRAMES: $frames")
		return frames.frames.map { frame ->
			val script2 = scriptsByHandle[frame.script.ref]
			//println("frame: $frame")
			object : Frame() {
				//override val position: SourcePosition get() = SourcePosition(frame.file, frame.line)
				override val position: SourcePosition get() = SourcePosition(script2?.name ?: "unknown", frame.line)

				override fun evaluate(expr: String): Any? = socket.cmdEvaluate(frame.index, expr).syncWait()
			}
		}
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

@JsonIgnoreProperties class V8ScriptResponse {
	@JvmField var handle = 14L
	@JvmField var id = 0
	@JvmField var type = ""
	@JvmField var name = ""
	@JvmField var lineCount = 0
	@JvmField var lineOffset = 0
	@JvmField var columnOffset = 0
	@JvmField var sourceStart = ""
	@JvmField var sourceLength = 0
	@JvmField var scriptType = 0
	@JvmField var compilationType = 0
	@JvmField var context = V8Ref()
	@JvmField var text = ""

	override fun toString(): String {
		return "V8ScriptResponse(handle=$handle, id=$id, type='$type', name='$name', lineCount=$lineCount, lineOffset=$lineOffset, columnOffset=$columnOffset, sourceStart='$sourceStart', sourceLength=$sourceLength, scriptType=$scriptType, compilationType=$compilationType, context=$context, text='$text')"
	}
}

fun V8DebugSocket.cmdRequestScripts(): Promise<List<V8ScriptResponse>> {
	return this.sendRequestAndWaitAsync("scripts", mapOf()).then {
		//println("SCRIPTS:" + it.encodePrettily())
		//it
		it.getJsonArray("array").map { Json.decodeValue(it.toString(), V8ScriptResponse::class.java) }
		//listOf<V8ScriptResponse>()
		//DATA({"seq":2,"type":"response","command":"scripts","success":true,"body":[{"handle":14,"type":"script","name":"node.js","id":37,"lineOffset":0,"columnOffset":0,"lineCount":446,"sourceStart":"// Hello, and welcome to hacking node.js!\n//\n// This file is invoked by node::Lo","sourceLength":13764,"scriptType":2,"compilationType":0,"context":{"ref":13},"text":"node.js (lines: 446)"},{"handle":16,"type":"script","name":"events.js","id":38,"lineOffset":0,"columnOffset":0,"lineCount":478,"sourceStart":"(function (exports, require, module, __filename, __dirname) { 'use strict';\n\nvar","sourceLength":13015,"scriptType":2,"compilationType":0,"context":{"ref":15},"text":"events.js (lines: 478)"},{"handle":18,"type":"script","name":"util.js","id":39,"lineOffset":0,"columnOffset":0,"lineCount":927,"sourceStart":"(function (exports, require, module, __filename, __dirname) { 'use strict';\n\ncon","sourceLength":25960,"scriptType":2,"compilationType":0,"context":{"ref":17},"text":"util.js (lines: 927)"},{"handle":20,"type":"script","name":"buffer.js","id":40,"lineOffset":0,"columnOffset":0,"lineCount":1295,"sourceStart":"(function (exports, require, module, __filename, __dirname) { /* eslint-disable ","sourceLength":32678,"scriptType":2,"compilationType":0,"context":{"ref":19},"text":"buffer.js (lines: 1295)"},{"handle":22,"type":"script","name":"internal/util.js","id":41,"lineOffset":0,"columnOffset":0,"lineCount":94,"sourceStart":"(function (exports, require, module, __filename, __dirname) { 'use strict';\n\ncon","sourceLength":2710,"scriptType":2,"compilationType":0,"context":{"ref":21},"text":"internal/util.js (lines: 94)"},{"handle":24,"type":"script","name":"timers.js","id":42,"lineOffset":0,"columnOffset":0,"lineCount":636,"sourceStart":"(function (exports, require, module, __filename, __dirname) { 'use strict';\n\ncon","sourceLength":17519,"scriptType":2,"compilationType":0,"context":{"ref":23},"text":"timers.js (lines: 636)"},{"handle":26,"type":"script","name":"internal/linkedlist.js","id":43,"lineOffset":0,"columnOffset":0,"lineCount":59,"sourceStart":"(function (exports, require, module, __filename, __dirname) { 'use strict';\n\nfun","sourceLength":1098,"scriptType":2,"compilationType":0,"context":{"ref":25},"text":"internal/linkedlist.js (lines: 59)"},{"handle":28,"type":"script","name":"assert.js","id":44,"lineOffset":0,"columnOffset":0,"lineCount":364,"sourceStart":"(function (exports, require, module, __filename, __dirname) { // http://wiki.com","sourceLength":12444,"scriptType":2,"compilationType":0,"context":{"ref":27},"text":"assert.js (lines: 364)"},{"handle":30,"type":"script","name":"internal/process.js","id":45,"lineOffset":0,"columnOffset":0,"lineCount":188,"sourceStart":"(function (exports, require, module, __filename, __dirname) { 'use strict';\n\nvar","sourceLength":4628,"scriptType":2,"compilationType":0,"context":{"ref":29},"text":"internal/process.js (lines: 188)"},{"handle":32,"type":"script","name":"internal/process/warning.js","id":46,"lineOffset":0,"columnOffset":0,"lineCount":51,"sourceStart":"(function (exports, require, module, __filename, __dirname) { 'use strict';\n\ncon","sourceLength":1773,"scriptType":2,"compilationType":0,"context":{"ref":31},"text":"internal/process/warning.js (lines: 51)"},{"handle":34,"type":"script","name":"internal/process/next_tick.js","id":47,"lineOffset":0,"columnOffset":0,"lineCount":159,"sourceStart":"(function (exports, require, module, __filename, __dirname) { 'use strict';\n\nexp","sourceLength":4392,"scriptType":2,"compilationType":0,"context":{"ref":33},"text":"internal/process/next_tick.js (lines: 159)"},{"handle":36,"type":"script","name":"internal/process/promises.js","id":48,"lineOffset":0,"columnOffset":0,"lineCount":63,"sourceStart":"(function (exports, require, module, __filename, __dirname) { 'use strict';\n\ncon","sourceLength":1942,"scriptType":2,"compilationType":0,"context":{"ref":35},"text":"internal/process/promises.js (lines: 63)"},{"handle":38,"type":"script","name":"internal/process/stdio.js","id":49,"lineOffset":0,"columnOffset":0,"lineCount":163,"sourceStart":"(function (exports, require, module, __filename, __dirname) { 'use strict';\n\nexp","sourceLength":4260,"scriptType":2,"compilationType":0,"context":{"ref":37},"text":"internal/process/stdio.js (lines: 163)"},{"handle":40,"type":"script","name":"path.js","id":50,"lineOffset":0,"columnOffset":0,"lineCount":1597,"sourceStart":"(function (exports, require, module, __filename, __dirname) { 'use strict';\n\ncon","sourceLength":46529,"scriptType":2,"compilationType":0,"context":{"ref":39},"text":"path.js (lines: 1597)"},{"handle":42,"type":"script","name":"module.js","id":51,"lineOffset":0,"columnOffset":0,"lineCount":641,"sourceStart":"(function (exports, require, module, __filename, __dirname) { 'use strict';\n\ncon","sourceLength":18241,"scriptType":2,"compilationType":0,"context":{"ref":41},"text":"module.js (lines: 641)"},{"handle":44,"type":"script","name":"internal/module.js","id":52,"lineOffset":0,"columnOffset":0,"lineCount":98,"sourceStart":"(function (exports, require, module, __filename, __dirname) { 'use strict';\n\nexp","sourceLength":2625,"scriptType":2,"compilationType":0,"context":{"ref":43},"text":"internal/module.js (lines: 98)"},{"handle":46,"type":"script","name":"vm.js","id":53,"lineOffset":0,"columnOffset":0,"lineCount":59,"sourceStart":"(function (exports, require, module, __filename, __dirname) { 'use strict';\n\ncon","sourceLength":1697,"scriptType":2,"compilationType":0,"context":{"ref":45},"text":"vm.js (lines: 59)"},{"handle":48,"type":"script","name":"fs.js","id":54,"lineOffset":0,"columnOffset":0,"lineCount":2035,"sourceStart":"(function (exports, require, module, __filename, __dirname) { // Maintainers, ke","sourceLength":51790,"scriptType":2,"compilationType":0,"context":{"ref":47},"text":"fs.js (lines: 2035)"},{"handle":50,"type":"script","name":"stream.js","id":56,"lineOffset":0,"columnOffset":0,"lineCount":109,"sourceStart":"(function (exports, require, module, __filename, __dirname) { 'use strict';\n\nmod","sourceLength":2506,"scriptType":2,"compilationType":0,"context":{"ref":49},"text":"stream.js (lines: 109)"},{"handle":52,"type":"script","name":"_stream_readable.js","id":57,"lineOffset":0,"columnOffset":0,"lineCount":930,"sourceStart":"(function (exports, require, module, __filename, __dirname) { 'use strict';\n\nmod","sourceLength":25764,"scriptType":2,"compilationType":0,"context":{"ref":51},"text":"_stream_readable.js (lines: 930)"},{"handle":54,"type":"script","name":"_stream_writable.js","id":58,"lineOffset":0,"columnOffset":0,"lineCount":531,"sourceStart":"(function (exports, require, module, __filename, __dirname) { // A bit simpler t","sourceLength":14353,"scriptType":2,"compilationType":0,"context":{"ref":53},"text":"_stream_writable.js (lines: 531)"},{"handle":56,"type":"script","name":"_stream_duplex.js","id":59,"lineOffset":0,"columnOffset":0,"lineCount":59,"sourceStart":"(function (exports, require, module, __filename, __dirname) { // a duplex stream","sourceLength":1501,"scriptType":2,"compilationType":0,"context":{"ref":55},"text":"_stream_duplex.js (lines: 59)"},{"handle":58,"type":"script","name":"_stream_transform.js","id":60,"lineOffset":0,"columnOffset":0,"lineCount":194,"sourceStart":"(function (exports, require, module, __filename, __dirname) { // a transform str","sourceLength":6414,"scriptType":2,"compilationType":0,"context":{"ref":57},"text":"_stream_transform.js (lines: 194)"},{"handle":60,"type":"script","name":"_stream_passthrough.js","id":61,"lineOffset":0,"columnOffset":0,"lineCount":24,"sourceStart":"(function (exports, require, module, __filename, __dirname) { // a passthrough s","sourceLength":592,"scriptType":2,"compilationType":0,"context":{"ref":59},"text":"_stream_passthrough.js (lines: 24)"},{"handle":12,"type":"script","name":"/Users/soywiz/Projects/jtransc/jtransc/jtransc-debugger/target/test-classes/test.js","id":62,"lineOffset":0,"columnOffset":0,"lineCount":2,"sourceStart":"(function (exports, require, module, __filename, __dirname) { console.log('test'","sourceLength":86,"scriptType":2,"compilationType":0,"context":{"ref":11},"text":"/Users/soywiz/Projects/jtransc/jtransc/jtransc-debugger/target/test-classes/test.js (lines: 2)"}],"refs":[{"handle":13,"type":"context","text":"#<ContextMirror>"},{"handle":15,"type":"context","text":"#<ContextMirror>"},{"handle":17,"type":"context","text":"#<ContextMirror>"},{"handle":19,"type":"context","text":"#<ContextMirror>"},{"handle":21,"type":"context","text":"#<ContextMirror>"},{"handle":23,"type":"context","text":"#<ContextMirror>"},{"handle":25,"type":"context","text":"#<ContextMirror>"},{"handle":27,"type":"context","text":"#<ContextMirror>"},{"handle":29,"type":"context","text":"#<ContextMirror>"},{"handle":31,"type":"context","text":"#<ContextMirror>"},{"handle":33,"type":"context","text":"#<ContextMirror>"},{"handle":35,"type":"context","text":"#<ContextMirror>"},{"handle":37,"type":"context","text":"#<ContextMirror>"},{"handle":39,"type":"context","text":"#<ContextMirror>"},{"handle":41,"type":"context","text":"#<ContextMirror>"},{"handle":43,"type":"context","text":"#<ContextMirror>"},{"handle":45,"type":"context","text":"#<ContextMirror>"},{"handle":47,"type":"context","text":"#<ContextMirror>"},{"handle":49,"type":"context","text":"#<ContextMirror>"},{"handle":51,"type":"context","text":"#<ContextMirror>"},{"handle":53,"type":"context","text":"#<ContextMirror>"},{"handle":55,"type":"context","text":"#<ContextMirror>"},{"handle":57,"type":"context","text":"#<ContextMirror>"},{"handle":59,"type":"context","text":"#<ContextMirror>"},{"handle":11,"type":"context","text":"#<ContextMirror>"}],"running":false})
	}
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
	this.sendRequestAndWaitAsync("disconnect")
}

fun V8DebugSocket.cmdResume(): Promise<JsonObject> {
	return this.sendRequestAndWaitAsync("continue")
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

class V8Ref {
	@JvmField var ref: Long = 0

	override fun toString() = "V8Ref(ref=$ref)"

}

@JsonIgnoreProperties
class V8Arguments {
	@JvmField var name: String = ""
	@JvmField var value: V8Ref = V8Ref()
}

@JsonIgnoreProperties
class V8Local {
	@JvmField var name: String = ""
	@JvmField var value: V8Ref = V8Ref()
}

@JsonIgnoreProperties
class V8Scope {
	@JvmField var type: Int = 0
	@JvmField var index: Int = 0
}

@JsonIgnoreProperties
class V8FrameResponse {
	@JvmField var type: String = ""
	@JvmField var index: Int = 0
	@JvmField var receiver: V8Ref = V8Ref()
	@JvmField var func: V8Ref = V8Ref()
	@JvmField var script: V8Ref = V8Ref()
	@JvmField var constructCall: Boolean = false
	@JvmField var atReturn: Boolean = false
	@JvmField var debuggerFrame: Boolean = false
	@JvmField var arguments: List<V8Arguments> = arrayListOf()
	@JvmField var locals: List<V8Local> = arrayListOf()
	@JvmField var scopes: List<V8Scope> = arrayListOf()
	@JvmField var position: Int = 0
	@JvmField var line: Int = 0
	@JvmField var column: Int = 0
	@JvmField var sourceLineText: String = ""
	@JvmField var text: String = ""
	override fun toString() = "V8FrameResponse(type='$type', index=$index, receiver=$receiver, func=$func, script=$script, constructCall=$constructCall, atReturn=$atReturn, debuggerFrame=$debuggerFrame, arguments=$arguments, locals=$locals, scopes=$scopes, position=$position, line=$line, column=$column, sourceLineText='$sourceLineText', text='$text')"
}

class V8BacktraceResponse {
	@JvmField var fromFrame: Int = 0
	@JvmField var toFrame: Int = 0
	@JvmField var totalFrames: Int = 0
	@JvmField var frames: List<V8FrameResponse> = arrayListOf()
	override fun toString() = "V8BacktraceResponse(fromFrame=$fromFrame, toFrame=$toFrame, totalFrames=$totalFrames, frames=$frames)"
}

fun V8DebugSocket.cmdRequestFrames(fromFrame: Int = 0, toFrame: Int = 10): Promise<V8BacktraceResponse> {
	return this.sendRequestAndWaitAsync("backtrace", mapOf("fromFrame" to fromFrame, "toFrame" to toFrame)).then {
		//println("BACKTRACE: ${it.encodePrettily()}")

		Json.decodeValue(it.toString(), V8BacktraceResponse::class.java)
	}
}

//{"seq":117,"type":"request","command":"evaluate","arguments":{"expression":"1+2"}}
fun V8DebugSocket.cmdEvaluate(expression: String): Promise<Any> {
	return this.sendRequestAndWaitAsync("evaluate", mapOf("expression" to expression)).then { it.getValue("value") ?: Unit }
}

fun V8DebugSocket.cmdEvaluate(frame: Int, expression: String): Promise<Any> {
	return this.sendRequestAndWaitAsync("evaluate", mapOf("frame" to frame, "expression" to expression)).then { it.getValue("value") ?: Unit }
}

fun V8DebugSocket.sendRequestAndWaitAsync(command: String, arguments: Map<String, Any?>? = null): Promise<JsonObject> {
	val obj = LinkedHashMap<String, Any?>()
	obj["seq"] = seq++
	obj["type"] = "request"
	obj["command"] = command
	if (arguments != null) obj["arguments"] = arguments
	return this.writeAndWaitAsync(JsonObject(obj)).then { message ->
		if (message.getBoolean("success")) {
			val result = message.getValue("body")
			when (result) {
				is JsonObject -> result
				is JsonArray -> JsonObject(mapOf("array" to result))
				else -> JsonObject(mapOf())
			}
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
	var seq = 1

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
			println("Disconnected!")
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
						if (DEBUG) println("RECV: $data")
						val message = JsonObject(data)
						for (handler in handlers.toList()) {
							EventLoop.queue {
								handler(message)
							}
						}
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
		//println("SEND: ${data.toString(Charsets.UTF_8)}")
	}

	fun write(message: JsonObject) {
		val bodyString = message.encode()
		val body = bodyString.toByteArray(Charset.forName("UTF-8"))
		val head = "Content-Length: ${body.size}\r\n\r\n".toByteArray(Charset.forName("UTF-8"))
		if (DEBUG) println("SEND: $bodyString")
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

const val DEBUG = true