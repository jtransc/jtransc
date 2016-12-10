/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jtransc.ds

import java.util.*

inline fun <reified T> Iterable<Any?>.cast() = this.filterIsInstance<T>()

fun <T> queueOf(vararg items: T): java.util.Queue<T> = LinkedList<T>(items.toList())

fun <A, B> List<Pair<A, B>>.uniqueMap(): HashMap<A, B>? {
	val h = hashMapOf<A, B>()
	val okay = this.all { x ->
		val y = h.put(x.first, x.second)
		(y == null) || (y == x.second)
	}
	return if (okay) h else null
}

val <T> List<T>.head: T get() = this.first()
val <T> List<T>.tail: List<T> get() = this.drop(1)

fun <K, V> Map<K, V>.flip(): Map<V, K> = this.map { it.value to it.key }.toMap()

val <T1, T2> Pair<List<T1>, List<T2>>.zipped: List<Pair<T1, T2>> get() {
	return this.first.zip(this.second)
}

class Queue<T>() : Iterable<T> {
	constructor(items: Iterable<T>) : this() {
		queueAll(items)
	}

	private val data = LinkedList<T>()

	val hasMore: Boolean get() = data.isNotEmpty()
	val length: Int get() = data.size

	fun queue(value: T): T {
		data.addFirst(value)
		return value
	}

	fun queueAll(items: Iterable<T>): Queue<T> {
		for (item in items) queue(item)
		return this
	}

	fun dequeue(): T {
		return data.removeLast()
	}

	override fun iterator(): Iterator<T> = data.iterator()
}

class Stack<T>(
	private var data: ArrayList<T> = arrayListOf<T>()
): Iterable<T> {

	val hasMore: Boolean get() = data.isNotEmpty()

	val length: Int get() = data.size

	fun push(value: T): T {
		data.add(value)
		return value
	}

	fun pop(): T {
		return data.removeAt(data.size - 1)
	}

	fun clone(): Stack<T> = Stack<T>(ArrayList(data))

	fun toList(): List<T> = data.toList()

	override fun iterator(): Iterator<T> = data.iterator()

	fun isEmpty() = !hasMore
}

// @TODO: Make clearer!
fun <T> List<T>.flatMapInChunks(chunkSize: Int, handler: (items: List<T>) -> List<T>): List<T> {
	var out = this
	var n = 0
	while (n + chunkSize <= out.size) {
		//println("IN: $out")
		val extracted = out.slice(n until n + chunkSize)
		val toInsert = handler(extracted)
		out = out.slice(0 until n) + toInsert + out.slice(n + chunkSize until out.size)
		//println("OUT: $out")
		n++
	}
	return out
}

fun <T, T2> List<T>.flatMapInChunks2(chunkSize: Int, handler: (items: List<T>) -> List<T2>): List<T2> {
	val out = arrayListOf<T2>()
	for (n in 0 until this.size / chunkSize) {
		//println("IN: $out")
		out.addAll(handler(this.slice(n * chunkSize until n * chunkSize + chunkSize)))
	}
	return out
}

fun <T> List<T>?.createPairs(): List<Pair<T, T>> {
	return this?.flatMapInChunks2(2) { listOf(Pair(it[0], it[1])) } ?: listOf()
}

fun <K, V> Map<K, V>.toHashMap(): HashMap<K, V> {
	val out = hashMapOf<K, V>()
	for (pair in this) {
		out.put(pair.key, pair.value)
	}
	return out
}

fun <T> Iterable<T?>.stripNulls(): List<T> {
	return this.filter { it != null }.map { it!! }
}

fun List<Any?>.toTypedArray2(clazz: Class<*>): Any {
	val items = this
	val typedArray = java.lang.reflect.Array.newInstance(clazz, items.size)
	for (n in 0 until items.size) {
		java.lang.reflect.Array.set(typedArray, n, items[n])
	}
	return typedArray
}

fun List<Any?>?.toTypedArray2(): Any? {
	if (this != null && this.isEmpty()) {
		//println("empty array!")
	}
	return this?.toTypedArray2(this.getOrNull(0)?.javaClass ?: Any::class.java)
}

data class DiffResult<T>(val both: List<T>, val justFirst: List<T>, val justSecond: List<T>)

fun <T> Iterable<T>.diff(that: Iterable<T>): DiffResult<T> {
	val first = this
	val second = that
	val firstSet = first.toSet()
	val secondSet = second.toSet()

	return DiffResult(
		both = first.filter { it in secondSet },
		justFirst = first.filter { it !in secondSet },
		justSecond = second.filter { it !in firstSet }
	)
}

infix fun Int.clearFlags(that:Int) = (this and that.inv())
infix fun Int.hasFlag(that:Int) = (this and that) != 0
infix fun Int.hasAnyFlags(that:Int) = (this and that) != 0
infix fun Int.hasAllFlags(that:Int) = (this and that) == that

fun <T> List<List<T?>?>?.concatNotNull(): List<T> {
	val out = arrayListOf<T>()
	if (this != null) for (l in this) {
		if (l != null) {
			for (i in l) {
				if (i != null) out += i
			}
		}
	}
	return out
}

fun <T> Concat(vararg list: List<T>?): List<T> {
	var out = listOf<T>()
	for (l in list) if (l != null) out += l
	return out
}

fun <T> List<T>.split(separator: T): List<List<T>> {
	val out = arrayListOf<List<T>>()
	val part = arrayListOf<T>()
	fun flush() {
		out += part.toList()
		part.clear()
	}
	for (item in this) {
		if (item == separator) {
			flush()
		} else {
			part += item
		}
	}
	flush()
	return out
}
