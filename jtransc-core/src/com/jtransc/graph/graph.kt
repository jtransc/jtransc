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

import com.jtransc.ds.stripNulls
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface Indexed {
	val id: Int

	companion object {
		inline fun <reified T> extends(size: Int, noinline init: (Int) -> T) = object : ReadWriteProperty<Indexed, T> {
			val items = Array<T>(size, init)

			override fun getValue(thisRef: Indexed, property: KProperty<*>): T {
				return items[thisRef.id]
			}

			override fun setValue(thisRef: Indexed, property: KProperty<*>, value: T) {
				items[thisRef.id] = value
			}
		}
	}
}

fun <T> genericExtends(init: () -> T) = object : ReadWriteProperty<Any?, T> {
	val items = hashMapOf<Any?, T>()

	override fun getValue(thisRef: Any?, property: KProperty<*>): T {
		if (thisRef !in items) items[thisRef] = init()
		return items[thisRef] as T
	}

	override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
		items[thisRef] = value
	}
}

interface Digraph<T> {
	val entry: T
	//val nodes: List<T>
	fun incoming(node: T): List<T>
	fun outgoing(node: T): List<T>
}

fun <T> Digraph<T>.dump() {
	val visited = hashSetOf<T>()
	fun step(node: T) {
		if (node in visited) return
		visited += node
		val children = this.outgoing(node)
		println("$node -> ${ children.joinToString(", ") }")
		children.forEach { step(it) }
	}
	println("--------- : " + this.entry)
	step(this.entry)
}

//val <T> Tree<T>.size: Int get() = this.nodes.size

interface AcyclicTree<T> : Digraph<T> {

}

interface TreeWithSize<T> : Digraph<T> {
	val size: Int
}

interface IndexedTree<T> : Digraph<T> {
	fun index(node: T): Int
}

interface DepthTree<T> : Digraph<T> {
	fun depth(node: T): Int
}

interface SingleParentTree<T> : Digraph<T> {
	fun parent(node: T): T?
}

fun <T> SingleParentTree<T>.containsAncestor(node: T, ancestor: T): Boolean {
	if (node == ancestor) {
		return true
	}
	val parent = this.parent(node)
	if (parent == null) {
		return false
	} else {
		return this.containsAncestor(parent, ancestor)
	}
}

interface MultipleParentTree<T> : Digraph<T> {
	fun parents(node: T): List<T>
}

// Lowest common ancestor
interface LCATree<T> {
	fun lca(an: T, bn: T): T
}

//class Edge<T>(val src:T, val dst:T)

fun <T> digraphCreate(entry: T, vararg edges: Pair<T, T>): Digraph<T> {
	val outgoings = hashMapOf<T, ArrayList<T>>()
	val incomings = hashMapOf<T, ArrayList<T>>()
	for (edge in edges) {
		if (edge.first !in outgoings) outgoings[edge.first] = arrayListOf()
		if (edge.second !in incomings) incomings[edge.second] = arrayListOf()
		outgoings[edge.first]!!.add(edge.second)
		incomings[edge.second]!!.add(edge.first)
	}
	return object : Digraph<T> {
		override val entry: T = entry
		override fun incoming(node: T): List<T> = incomings[node] ?: listOf()
		override fun outgoing(node: T): List<T> = outgoings[node] ?: listOf()
	}
}

class LCATreeSingleParent<T>(val tree: SingleParentTree<T>) : SingleParentTree<T> by tree, LCATree<T> {
	private val T.parent: T? get() = tree.parent(this)
	private var T.depth: Int by genericExtends { -1 }

	init {
		fun step(node: T, depth: Int) {
			node.depth = depth
			for (child in tree.outgoing(node)) {
				step(child, depth + 1)
			}
		}
		step(tree.entry, 0)
	}

	fun depth(node: T): Int = node.depth
	override fun lca(an: T, bn: T): T {
		var a = an
		var b = bn
		while (a.depth > b.depth) a = a.parent!!
		while (b.depth > a.depth) b = b.parent!!
		while (a != b) {
			a = a.parent!!
			b = b.parent!!
		}
		return a
	}
}

val <T> Digraph<T>.size: Int get() {
	return if (this is TreeWithSize) {
		this.size
	} else {
		DepthFirstSearchTree(this).nodes.size
	}
}

// https://en.wikipedia.org/wiki/Depth-first_search
class DepthFirstSearchTree<T>(private val tree: Digraph<T>) : IndexedTree<T>, SingleParentTree<T>, AcyclicTree<T>, TreeWithSize<T>, DepthTree<T> {
	override val entry = tree.entry
	private var T.parent by genericExtends<T?> { null }
	private var T.outgoing by genericExtends { arrayListOf<T>() }
	private var T.retreating by genericExtends { arrayListOf<T>() }
	private var T.cross by genericExtends { arrayListOf<T>() }
	private var T.index by genericExtends { -1 }
	private var T.depth by genericExtends { -1 }
	private val T.visited: Boolean get() = this.index >= 0

	val nodes = this.let {
		val nodes = ArrayList<T>()
		fun step(node: T, parent: T?, depth: Int) {
			//if (node.visited) return
			node.index = nodes.size
			node.parent = parent
			node.depth = depth
			parent?.outgoing?.add(node)
			nodes.add(node)
			tree.outgoing(node).forEach { child ->
				if (!child.visited) {
					step(child, node, depth + 1)
				} else {
					// retreating/forward/cross edge
					val retreating = this.containsAncestor(node, child)
					if (retreating) {
						node.retreating.add(child)
					} else {
						node.cross.add(child)
					}
				}
			}
		}
		step(entry, null, 0)
		nodes.toList()
	}

	override fun index(node: T): Int = node.index
	override fun depth(node: T): Int = node.depth
	override fun parent(node: T): T? = node.parent
	override fun incoming(node: T): List<T> = listOf(parent(node)).stripNulls()
	override fun outgoing(node: T): List<T> = node.outgoing
	fun retreating(node: T): List<T> = node.retreating
	fun cross(node: T): List<T> = node.cross
	override val size: Int get() = nodes.size
}

fun <T> DepthFirstSearchTree<T>.dump2() {
	val visited = hashSetOf<T>()
	fun step(node: T) {
		if (node in visited) return
		visited += node
		val children = this.outgoing(node)
		val retreating = this.retreating(node)
		val cross = this.cross(node)
		println("$node -> ${ children.joinToString(", ") } ; cross = ${ cross.joinToString(", ") } ; retreating = ${ retreating.joinToString(", ") }")
		children.forEach { step(it) }
	}
	println("--------- : " + this.entry)
	step(this.entry)
}
