package com.jtransc.ds

inline fun <T1, T2> MutableMap<T1, T2>.getOrPut2(key: T1, generator: () -> T2): T2 {
	if (key !in this) this[key] = generator()
	return this[key]!!
}

fun <K, V> Map<K, Iterable<V>>.combinedWith(that: Map<K, Iterable<V>>): Map<K, List<V>> {
	return (this.keys + that.keys).distinct()
		.map { it to (this[it] ?: listOf()) + (that[it] ?: listOf()) }
		.toMap()
}
