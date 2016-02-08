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

import com.jtransc.error.InvalidOperationException
import com.jtransc.error.noImpl
import com.jtransc.input.asm.Stm
import org.junit.Assert.assertEquals
import org.junit.Test

class GraphTest {
	@Test
	fun testName() {
		val G = digraphCreate(
			"a",
			"a" to "b1",
			"a" to "b2",
			"b1" to "c",
			"b2" to "c"
			//"c" to "a"
		)

		val dfst = DepthFirstSearchTree(G)

		val tree = LCATreeSingleParent(dfst)
		G.dump()
		dfst.dump()
		tree.dump()
		assertEquals("a", tree.lca("b1", "b2"))
		//G.dump()
		//dfst.dump()
	}

	@Test
	fun testName2() {
		val G = DepthFirstSearchTree(digraphCreate("a", "a" to "a"))
		assertEquals(listOf("a"), G.retreating("a"))
	}

	@Test
	fun testName3() {
		val G = DepthFirstSearchTree(digraphCreate(
			"a",
			"a" to "b1",
			"a" to "b2",
			"b1" to "b2"
		))
		G.dump2()
		assertEquals(listOf<String>(), G.retreating("b1"))
		assertEquals(listOf("b2"), G.cross("a"))
	}

	@Test
	fun testName4() {
		val G = DepthFirstSearchTree(digraphCreate("a",
			"a" to "b",
			"a" to "c",
			"b" to "c"
		))
	}
}

fun <T> Digraph<T>.lca(a:T, b:T):T {
	noImpl
}

fun DepthFirstSearchTree<Stm>.render(): Stm {
	// Make it reducible: all retreating edges are back edges
	fun step(node: Stm): Stm {
		val childs = this.outgoing(node)
		when (childs.size) {
			0 -> node
			1 -> {
				// @TODO: Check ancestors
				Stm.STMS(listOf(node, step(childs.first())))
			}
			2 -> {
				val a = childs[0]
				val b = childs[1]
				if (lca(a, b) == node) {
					// if ... else
				} else {
					// if
				}
			}
		}
		noImpl
	}
	return step(this.entry)
}