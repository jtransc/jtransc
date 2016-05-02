package com.jtransc.net

import com.jtransc.error.ignoreErrors
import com.jtransc.io.readAvailableChunk
import java.net.ServerSocket
import java.net.Socket
import java.util.*

object SocketUtils {
	fun getFreePort(): Int {
		val ss = ServerSocket(0)
		val localPort = ss.localPort
		ss.close()
		return localPort
	}
}

class TcpClientAsync(val host: String, val port: Int, val handler: Handler) {
	interface Handler {
		fun onOpen(): Unit
		fun onData(data: ByteArray): Unit
		fun onClose(): Unit
	}

	var socket: Socket? = null
	val writeChunks = Collections.synchronizedList(LinkedList<ByteArray>())
	@Volatile var mustClose = false

	init {
		Thread {
			val socket = Socket(host, port)
			this.socket = socket
			val input = socket.inputStream
			val output = socket.outputStream
			handler.onOpen()

			while (!mustClose && !socket.isClosed) {
				val chunk = input.readAvailableChunk()
				if (chunk.size >= 1) {
					handler.onData(chunk)
				}
				while (writeChunks.isNotEmpty()) {
					ignoreErrors { output.write(writeChunks.removeAt(0)) }
				}
				Thread.sleep(1)
			}
			handler.onClose()
			ignoreErrors { socket.close() }
		}.start()
	}

	fun write(data: ByteArray) {
		writeChunks.add(data)
	}

	fun close() {
		mustClose = true
	}
}