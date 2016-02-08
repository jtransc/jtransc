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

package com.jtransc.graph

import java.util.*

interface Graph {
	fun size(): Int

	fun incomingEdges(node: Int): IntArray

	fun outgoingEdges(node: Int): IntArray

	fun incomingEdgesCount(node: Int): Int

	fun outgoingEdgesCount(node: Int): Int
}

class DominatorTreeBuilder(private val graph: Graph) {
	private val semidominators: IntArray = IntArray(graph.size())
	var vertices: IntArray = IntArray(graph.size())
	private val parents: IntArray = IntArray(graph.size())
	private val ancestors: IntArray = IntArray(graph.size())
	private val labels: IntArray = IntArray(graph.size())
	var dominators: IntArray = IntArray(graph.size())
	private val bucket: Array<IntegerArray?> = arrayOfNulls(graph.size())
	private val path: IntArray = IntArray(graph.size())
	private var effectiveSize: Int = 0

	fun build() {
		for (i in labels.indices) {
			labels[i] = i
		}
		Arrays.fill(ancestors, -1)
		dfs()
		for (i in effectiveSize - 1 downTo 0) {
			val w = vertices[i]
			if (parents[w] < 0) {
				continue
			}
			for (v in graph.incomingEdges(w)) {
				val u = eval(v)
				if (semidominators[u] >= 0) {
					semidominators[w] = Math.min(semidominators[w], semidominators[u])
				}
			}
			addToBucket(vertices[semidominators[w]], w)
			link(parents[w], w)
			for (v in getBucket(w)) {
				val u = eval(v)
				dominators[v] = if (semidominators[u] < semidominators[v]) u else parents[w]
			}
			bucket[w] = null
		}
		for (i in 0..graph.size() - 1) {
			val w = vertices[i]
			if (w < 0 || parents[w] < 0) {
				continue
			}
			if (dominators[w] != vertices[semidominators[w]]) {
				dominators[w] = dominators[dominators[w]]
			}
		}
	}

	private fun addToBucket(v: Int, w: Int) {
		var ws: IntegerArray? = bucket[v]
		if (ws == null) {
			ws = IntegerArray(1)
			bucket[v] = ws
		}
		ws.add(w)
	}

	private fun getBucket(v: Int): IntArray {
		val ws = bucket[v]
		return if (ws != null) ws.all else IntArray(0)
	}

	private fun link(v: Int, w: Int) {
		ancestors[w] = v
	}

	private fun eval(v: Int): Int {
		var v = v
		var ancestor = ancestors[v]
		if (ancestor == -1) {
			return v
		}
		var i = 0
		while (ancestor >= 0) {
			path[i++] = v
			v = ancestor
			ancestor = ancestors[v]
		}
		ancestor = v
		while (--i >= 0) {
			v = path[i]
			if (semidominators[labels[v]] > semidominators[labels[ancestor]]) {
				labels[v] = labels[ancestor]
			}
			ancestors[v] = ancestor
			ancestor = v
		}
		return labels[v]
	}

	private fun dfs() {
		Arrays.fill(semidominators, -1)
		Arrays.fill(vertices, -1)
		val stack = IntegerStack(graph.size())
		for (i in graph.size() - 1 downTo 0) {
			if (graph.incomingEdgesCount(i) === 0) {
				stack.push(i)
				parents[i] = -1
			}
		}
		var i = 0
		while (!stack.isEmpty) {
			val v = stack.pop()
			if (semidominators[v] >= 0) {
				continue
			}
			// We don't need vertex index after its dominator has computed.
			semidominators[v] = i
			vertices[i++] = v
			for (w in graph.outgoingEdges(v)) {
				if (semidominators[w] < 0) {
					parents[w] = v
					stack.push(w)
				}
			}
		}
		effectiveSize = i
	}
}

class IntegerArray(capacity: Int) {
	private var data: IntArray = IntArray(capacity)
	private var sz: Int = 0

	fun clear() {
		sz = 0
	}

	fun optimize() {
		if (sz > data.size) {
			data = Arrays.copyOf(data, sz)
		}
	}

	val all: IntArray
		get() = if (sz > 0) Arrays.copyOf(data, sz) else emptyData

	operator fun get(index: Int): Int {
		return data[index]
	}

	fun getRange(start: Int, end: Int): IntArray {
		return Arrays.copyOfRange(data, start, end)
	}

	operator fun set(index: Int, value: Int) {
		if (index >= sz) {
			throw IndexOutOfBoundsException("Index $index is greater than the list size $sz")
		}
		data[index] = value
	}

	private fun ensureCapacity() {
		if (sz <= data.size) {
			return
		}
		var newCap = data.size
		while (sz > newCap) {
			newCap = newCap * 3 / 2 + 1
		}
		data = Arrays.copyOf(data, newCap)
	}

	fun size(): Int {
		return sz
	}

	fun addAll(items: IntArray) {
		val target = sz
		sz += items.size
		ensureCapacity()
		System.arraycopy(items, 0, data, target, items.size)
	}

	fun add(item: Int) {
		++sz
		ensureCapacity()
		data[sz - 1] = item
	}

	@JvmOverloads fun remove(index: Int, count: Int = 1) {
		System.arraycopy(data, index + count, data, index, sz - index - count)
		sz -= count
	}

	operator fun contains(item: Int): Boolean {
		for (i in 0..sz - 1) {
			if (data[i] == item) {
				return true
			}
		}
		return false
	}

	companion object {
		private val emptyData = IntArray(0)

		fun of(vararg values: Int): IntegerArray {
			val array = IntegerArray(values.size)
			array.data = Arrays.copyOf(values, values.size)
			array.sz = values.size
			return array
		}
	}
}

class IntegerStack(capacity: Int) {
	private var buffer = IntArray(capacity)
	private var head: Int = 0

	fun push(value: Int) {
		if (head == buffer.size) {
			buffer = Arrays.copyOf(buffer, Math.max(buffer.size * 2, 1))
		}
		buffer[head++] = value
	}

	fun pop(): Int {
		return buffer[--head]
	}

	fun peek(): Int {
		return buffer[head - 1]
	}

	val isEmpty: Boolean
		get() = head == 0
}
