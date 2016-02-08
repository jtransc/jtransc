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

package com.jtransc.input.asm.cfg

import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/*
open class Node {
	val incoming = arrayListOf<Edge>()
	val outgoing = arrayListOf<Edge>()
}

open class Edge(val src: Node, val dst: Node) {
	fun attach() = this.apply {
		src.incoming.add(this)
		dst.outgoing.add(this)
	}
}

fun Node.depthFirstSearch(): List<Node> {
	val visited = hashSetOf<Node>()
	val list = arrayListOf<Node>()
	fun rec(node: Node) {
		if (node in visited) return
		visited.add(node)
		list.add(node)
		node.outgoing.forEach { rec(it.dst) }
	}
	rec(this)
	return list
}

class DominatorTreeBuilder(val entry: Node) {
	val graph = entry.depthFirstSearch()
	val id by
}
*/

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


open class Node internal constructor(val graph: Digraph, override val id: Int) : Indexed {
	val incoming = arrayListOf<Edge>()
	val outgoing = arrayListOf<Edge>()
}

open class Edge(val from: Node, val to: Node) {
	fun attach() = this.apply {
		from.incoming.add(this)
		to.outgoing.add(this)
	}
}

open class Digraph {
	val nodes = arrayListOf<Node>()
	val size: Int get() = nodes.size

	open fun create() = Node(this, nodes.size).apply { nodes.add(this) }
}

/*
fun Node.depthFirstSearch(): List<Node> {
	val visited = BooleanArray(graph.size) { false }
	val list = ArrayList<Node>(graph.size)
	fun rec(node: Node) {
		if (visited[node.id]) return
		visited[node.id] = true
		list.add(node)
	}
	rec(this)
	return list
}
*/

//class DominatorTreeBuilder(val entry: Node) {
//	/*
//	class DTNode(val id: Int, val node: Node)
//	val dfsGraph = entry.depthFirstSearch().withIndex().map {
//		DTNode(it.index, it.value)
//	}
//	*/
//
//	val dfsGraph = entry.depthFirstSearch()
//	var Node.dfsId: Int by Indexed.extends(entry.graph.size) { -1 }.apply {
//		for (it in dfsGraph.withIndex()) it.value.dfsId = it.index
//	}
//	//var Node.semidominator by com.jtransc.input.asm.Indexed.extends<com.jtransc.input.asm.Digraph.Node?>(entry.graph.size) { null }
//}
