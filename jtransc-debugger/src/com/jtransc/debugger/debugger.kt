package com.jtransc.debugger

open class JTranscDebugger(val handler: EventHandler) {
	open class EventHandler {
		open fun onBreak(): Unit {
		}
	}

	data class SourcePosition(val file: String, val line: Int)

	open class Frame() {
		open val position = SourcePosition("unknown", -1)
	}

	open val currentPosition = SourcePosition("unknown", -1)

	open fun resume() {
	}
	open fun pause() {
	}

	open fun stepOver() {
	}
	open fun stepInto() {
	}
	open fun stepOut() {
	}
	open fun disconnect() {

	}
}
