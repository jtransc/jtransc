package com.jtransc.lang

import java.util.*

fun <K, V> HashMap<K, V>.putIfAbsentJre7(key: K, value: V): V? = if (!containsKey(key)) put(key, value) else get(key)

fun <K, V> Map<K, List<V>>.mergeMapListWith(other: Map<K, List<V>>): Map<K, List<V>> {
	val first = this
	val second = other
	return (this.keys + other.keys).distinct().map {
		it to ((first[it] ?: listOf()) + (second[it] ?: listOf()))
	}.toMap()
}