package com.jtransc.lang

import java.util.*

fun <K, V> HashMap<K, V>.putIfAbsentJre7(key: K, value: V): V? = if (!containsKey(key)) {
	put(key, value)
} else {
	get(key)
}