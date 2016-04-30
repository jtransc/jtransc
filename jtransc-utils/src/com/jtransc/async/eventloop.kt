package com.jtransc.async

import java.util.*

object EventLoop {
	private val actions = Collections.synchronizedList(LinkedList<() -> Unit>())

	init {
		val thread = Thread {
			executeStep()
		}
		thread.isDaemon = false
		thread.start()
	}

	fun queue(action: () -> Unit) {
		actions += action
	}

	fun executeStep() {
		while (actions.isNotEmpty()) {
			val action = actions.removeAt(0)
			action()
		}
	}
}