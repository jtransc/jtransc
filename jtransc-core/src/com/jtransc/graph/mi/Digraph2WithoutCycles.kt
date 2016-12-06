package com.jtransc.graph.mi

import java.util.*

class Digraph2WithoutCycles<T> {
	class Node<T>(val item: T) {
		val incoming = hashSetOf<Node<T>>()
		val outgoing = hashSetOf<Node<T>>()
	}

	val nodes = hashMapOf<T, Node<T>>()

	fun node(v: T) = nodes.getOrPut(v) { Node(v) }

	fun link(src: T, dst: T) {
		val nsrc = node(src)
		val ndst = node(dst)
		nsrc.outgoing += ndst
		ndst.incoming += nsrc
	}

	fun topologicalSort(): List<T> {
		val L = arrayListOf<Node<T>>()

		val S = LinkedList(nodes.values.filter { it.incoming.isEmpty() })

		while (S.isNotEmpty()) {
			val n = S.remove()
			nodes.remove(n.item)
			L += n
			for (m in n.outgoing) {
				m.incoming -= n
				if (m.incoming.isEmpty()) {
					S += m
				}
			}
			n.outgoing.clear()
		}

		return L.map { it.item }
	}
}