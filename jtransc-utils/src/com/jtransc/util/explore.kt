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

package com.jtransc.util

import com.jtransc.ds.Queue
import com.jtransc.error.InvalidOperationException
import java.util.*

fun <T : Any?> recursiveExploration(initialItems: Set<T>, performExploration: (item: T) -> Iterable<T?>): Set<T> {
	return recursiveExploration(initialItems, null, performExploration)
}

fun <T : Any?> recursiveExploration(initialItems: Set<T>, extra: ((item:T) -> Iterable<T?>)?, performExploration: (item: T) -> Iterable<T?>): Set<T> {
	val explored = HashSet<T>(initialItems)
	val toExploreList = Queue<T>(initialItems)

	fun explore(item: T?) {
		if (item != null && item !in explored) {
			explored.add(item)
			toExploreList.queue(item)
		}
	}

	do {
		while (toExploreList.hasMore) {
			val toExplore = toExploreList.dequeue()
			for (item in performExploration(toExplore)) explore(item)
		}
		if (extra != null) {
			explored.toList().forEach {
				extra(it).forEach {
					explore(it)
				}
			}
		}
	} while (toExploreList.hasMore)

	return explored
}

fun <T> List<T>.dependencySorter(allowCycles:Boolean = false, getDependencies: (item:T) -> List<T>):List<T> {
	return this.dependencySorterOld(allowCycles, getDependencies)
}

// @TODO: Optimize this
// This could construct a tree with a root node all items depend on.
// We should remove items that depend on root but also depends on other stuff.
// Later we should iterate the tree from leafs to root. This should be order
// of magnitudes faster with lots of items.
fun <T> List<T>.dependencySorterOld(allowCycles:Boolean = false, getDependencies: (item:T) -> List<T>):List<T> {
	val all = this
	val usages = LinkedHashMap<T, ArrayList<T>>()
	val dependencies = LinkedHashMap<T, ArrayList<T>>()
	for (it in all) usages[it] = arrayListOf()

	for (it in all) {
		val deps = getDependencies(it).toSet() - it
		dependencies[it] = deps.toCollection(arrayListOf<T>())
		for (dep in deps) usages[dep]?.add(it)
	}

	// @TODO: Optimize this
	val out = arrayListOf<T>()
	while (usages.isNotEmpty()) {
		//println("Usages:" + usages.size)
		//val emptyItem = usages.entries.firstOrNull { it.value.isEmpty() } ?: usages.entries.first()
		val emptyItem = if (allowCycles) {
			usages.entries.firstOrNull { it.value.isEmpty() } ?: usages.entries.first()

		} else {
			usages.entries.firstOrNull { it.value.isEmpty() } ?: throw InvalidOperationException("Cycle detected!")
		}
		val item = emptyItem.key
		usages.remove(item)
		for (a in dependencies[item]!!) {
			usages[a]?.remove(item)
		}
		out.add(item)
		//println(item)
	}

	return out.reversed()
}



fun <T> sortDependenciesSimple(entry:T, getDependencies: (item:T) -> List<T>):List<T> {
	var out = arrayListOf<T>()
	fun step(item:T, used:MutableSet<T> = hashSetOf()) {
		if (item in used) return
		used.add(item)
		for (dep in getDependencies(item)) step(dep, used)
		out.add(item)
	}
	step(entry)
	return out
}