package com.jtransc.debugger

open class JTranscDebugger {
	data class SourcePosition(val file: String, val line: Int)

	open class Frame() {
		open val position = SourcePosition("unknown", -1)
	}

	open val currentPosition = SourcePosition("unknown", -1)

	open fun _continue() {
	}
	open fun stepInto() {
	}
	open fun stepOut() {
	}
}
