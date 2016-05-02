package com.jtransc.async

import java.util.*

class Promise<T : Any>(var parent: Promise<*>?) {
	private var resolved: Boolean = false
	private var resolvedValue: T? = null
	private var resolvedException: Throwable? = null
	private val callbacks = LinkedList<(T) -> Any>()
	private val failcallbacks = LinkedList<(Throwable) -> Any>()


	private constructor(parent: Promise<*>?, value: Throwable) : this(null) {
		resolved = true
		resolvedException = value
	}

	class Deferred<T : Any> {
		val promise: Promise<T> = Promise<T>(null)

		fun resolve(value: T) {
			this.promise.resolve(value)
		}

		fun progress(value: Double) {
			this.promise.progress(value)
		}

		fun reject(value: Throwable) {
			this.promise.reject(value);
		}
	}

	companion object {
		fun <T : Any> invoke(callback: (resolve: (value: T) -> Unit, reject: (error: Throwable) -> Unit) -> Unit): Promise<T> {
			val deferred = Deferred<T>()
			callback({ deferred.resolve(it) }, { deferred.reject(it) })
			return deferred.promise
		}

		fun <T : Any> create(callback: (resolve: (value: T) -> Unit, reject: (error: Throwable) -> Unit) -> Unit): Promise<T> {
			val deferred = Deferred<T>()
			callback({ deferred.resolve(it) }, { deferred.reject(it) })
			return deferred.promise
		}

		fun <T : Any> sequence(vararg promises: () -> Promise<T>): Promise<List<T>> {
			return sequence(promises.toList())
		}

		fun <T : Any> sequence(promises: Iterable<() -> Promise<T>>): Promise<List<T>> {
			val items = promises.toCollection(LinkedList())
			if (items.size == 0) return Promise.resolved(listOf<T>())
			val out = ArrayList<T>(items.size)
			val deferred = Deferred<List<T>>()
			fun step() {
				if (items.isEmpty()) {
					deferred.resolve(out)
				} else {
					val promiseGenerator = items.removeFirst()
					val promise = promiseGenerator()
					promise.then {
						out.add(it)
						step()
					}.fail {
						deferred.reject(it)
					}
				}
			}
			EventLoop.queue { step() }
			return deferred.promise
		}

		fun chain(): Promise<Unit> = resolved(Unit)

		@Deprecated("Use resolved", ReplaceWith("this.resolved(Unit)"))
		fun resolve() = Promise.resolved(Unit)

		fun <T : Any> resolved(value: T): Promise<T> {
			val deferred = Deferred<T>()
			deferred.resolve(value)
			return deferred.promise
		}

		fun <T : Any> rejected(value: Throwable): Promise<T> {
			return Promise(null, value);
		}

		fun <T : Any> all(vararg promises: Promise<T>): Promise<List<T>> {
			return all(promises.toList())
		}

		fun <T : Any> all(promises: Iterable<Promise<T>>): Promise<List<T>> {
			val promiseList = promises.toList()
			var count = 0
			val total = promiseList.size

			val out = arrayListOf<T?>()
			val deferred = Deferred<List<T>>()
			for (n in 0..total - 1) out.add(null)

			fun checkDone() {
				if (count >= total) {
					deferred.resolve(out.map { it!! })
				}
			}

			promiseList.indices.forEach {
				val index = it
				val promise = promiseList[index]
				promise.then {
					out[index] = it
					count++
					checkDone()
				}
			}

			checkDone()

			return deferred.promise
		}

		/*
		fun create<T>(callback: (resolve: (value:T) -> Unit, reject:(exception:Throwable) -> Unit) -> Unit):Promise<T> {
			val deferred = Deferred<T>()
			return deferred.promise
		}
		*/
		fun <T : Any> forever(): Promise<T> {
			return Deferred<T>().promise
		}

		fun <T : Any> any(vararg promises: Promise<T>): Promise<T> {
			val deferred = Promise.Deferred<T>()
			for (promise in promises) {
				promise.then { deferred.resolve(it) }.fail { deferred.reject(it) }
			}
			return deferred.promise
		}
	}

	internal fun resolve(value: T) {
		if (resolved) return;
		resolved = true
		resolvedValue = value
		parent = null
		flush();
	}

	internal fun reject(value: Throwable) {
		if (resolved) return;
		resolved = true
		resolvedException = value
		parent = null

		// @TODO: Check why this fails!
		if (failcallbacks.isEmpty() && callbacks.isEmpty()) {
			println("Promise.reject(): Not capturated: $value")
			throw value
		}

		flush();
	}

	internal fun progress(value: Double) {

	}

	private fun flush() {
		if (!resolved || (callbacks.isEmpty() && failcallbacks.isEmpty())) return

		val resolvedValue = this.resolvedValue
		if (resolvedValue != null) {
			while (callbacks.isNotEmpty()) {
				val callback = callbacks.removeAt(0);
				EventLoop.queue({
					callback(resolvedValue)
				})
			}
		} else if (resolvedException != null) {
			while (failcallbacks.isNotEmpty()) {
				val failcallback = failcallbacks.removeAt(0);
				EventLoop.queue({
					failcallback(resolvedException!!)
				})
			}
		}
	}

	fun cancel() {
		parent?.cancel()
		parent = null
		cancelledHandlers.dispatch()
	}

	private var cancelledHandlers = Signal<Unit>()

	fun cancelled(handler: () -> Unit): Promise<T> {
		cancelledHandlers.once { handler() }
		return this;
	}

	fun <T2 : Any> pipe(callback: (value: T) -> Promise<T2>): Promise<T2> {
		try {
			val out = Promise<T2>(this)
			this.failcallbacks.add {
				out.reject(it)
			}
			this.callbacks.add({
				callback(it)
					.then { out.resolve(it) }
					.fail { out.fail { it } }
			})
			return out
		} finally {
			flush()
		}
	}

	fun <T2 : Any> then(callback: (value: T) -> T2): Promise<T2> {
		try {
			val out = Promise<T2>(this)
			this.failcallbacks.add {
				out.reject(it)
			}
			this.callbacks.add {
				try {
					out.resolve(callback(it))
				} catch (t: Throwable) {
					println("then catch:$t")
					t.printStackTrace()
					out.reject(t)
				}
			}
			return out
		} finally {
			flush()
		}
	}

	fun <T2 : Any> fail(failcallback: (throwable: Throwable) -> T2): Promise<T2> {
		try {
			val out = Promise<T2>(this)
			this.failcallbacks.add {
				try {
					out.resolve(failcallback(it))
				} catch (t: Throwable) {
					println("fail catch:$t")
					t.printStackTrace()
					out.reject(t)
				}
			}
			return out
		} finally {
			flush()
		}
	}

	fun timeout(time: Int): Promise<T> {
		return Promise.create<T> { resolve, reject ->
			//EventLoop.setTimeout(time) { reject(TimeoutException()) }

			this.then { resolve(it) }.fail { reject(it) }
		}
	}

	fun always(callback: () -> Unit): Promise<T> {
		then { callback() }.fail { callback() }
		return this
	}
}

val <T : Any> Promise<T>.unit: Promise<Unit> get() = then { Unit }

fun <T : Any> Promise<T>.syncWait(maxMs:Int = Integer.MAX_VALUE): T {
	var completed = false
	var value: T? = null
	var exception: Throwable? = null
	this.then {
		value = it
		completed = true
	}.fail {
		exception = it
		completed = true
	}
	val start = System.currentTimeMillis()
	while (!completed) {
		val current = System.currentTimeMillis()
		val elapsed = current - start
		if (elapsed >= maxMs) throw RuntimeException("Waiting too much!")
		EventLoop.executeStep()
		Thread.sleep(1)
	}
	if (exception != null) throw exception!!
	return value!!
}