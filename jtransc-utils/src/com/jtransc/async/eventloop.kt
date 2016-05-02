package com.jtransc.async

import java.util.*

object EventLoop {
	private val actions = Collections.synchronizedList(LinkedList<() -> Unit>())

	init {
		val thread = Thread {
			while (!Thread.currentThread().isInterrupted) {
				executeStep()
				Thread.sleep(1)
			}
		}
		thread.isDaemon = false
		thread.start()
	}

	fun queue(action: () -> Unit) {
		synchronized(actions) {
			actions += action
		}
	}

	private fun tryDequeue(): (() -> Unit)? {
		return synchronized(actions) {
			if (actions.isNotEmpty()) actions.removeAt(0) else null
		}
	}

	fun executeStep() {
		while (true) {
			val action = tryDequeue()
			if (action != null) action() else break
		}
	}
}