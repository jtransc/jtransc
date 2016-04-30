package com.jtransc.async

import com.jtransc.lang.Disposable

class Signal<T : Any> {
	private val callbacks = arrayListOf<(value: T) -> Unit>()

	private class SignalDisposable<T : Any>(val signal: Signal<T>, val callback: (value: T) -> Unit) : Disposable {
		override fun dispose() {
			signal.remove(callback)
		}
	}

	fun removeAll() {
		callbacks.clear()
	}

	private fun remove(callback: (value: T) -> Unit) {
		callbacks.remove(callback)
	}

	fun add(callback: (value: T) -> Unit): Disposable {
		callbacks.add(callback)
		return SignalDisposable(this, callback)
	}

	fun once(callback: (value: T) -> Unit): Disposable {
		var handler: (T) -> Unit = {}
		handler = { value: T ->
			remove(handler)
			callback(value)
		}
		add(handler)
		return SignalDisposable(this, handler)
	}

	fun dispatch(value: T) {
		for (c in callbacks.toList()) c(value)
	}

	operator fun invoke(value: T) = dispatch(value)
	operator fun invoke(callback: (value: T) -> Unit) = add(callback)
}

fun Signal<Unit>.dispatch() {
	return this.dispatch(Unit)
}

fun <T : Any> Signal<T>.pipeTo(that: Signal<T>) {
	this.add { that.dispatch(it) }
}

fun <T : Any> Signal<T>.waitOneAsync(): Promise<T> {
	val deferred = Promise.Deferred<T>()
	this.once { deferred.resolve(it) }
	return deferred.promise
}
