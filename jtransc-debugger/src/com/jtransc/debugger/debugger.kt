package com.jtransc.debugger

import com.jtransc.async.Promise

open class JTranscDebugger(val handler: EventHandler) {
	open class EventHandler {
		open fun onBreak(): Unit {
		}
	}

	open fun initAsync(): Promise<Unit> {
		return Promise.resolved(Unit)
	}

	data class SourcePosition(val file: String, val line: Int)

	open class Value() {
		open val type: String = "type"
		open val value: String = "value"
	}

	open class Local() {
		open val name: String = ""
		open val value: Value = Value()
	}

	open class Frame() {
		open val position = SourcePosition("unknown", -1)
		open val locals = listOf<Local>()
		open fun evaluate(expr: String): Any? = null
		override fun toString() = "Frame(position=$position)"
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

	open fun backtrace(): List<Frame> {
		return listOf()
	}

	open fun setBreakpoint(script: String, line: Int): Breakpoint {
		return Breakpoint()
	}

	open class Breakpoint {
	}
}
