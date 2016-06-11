package com.jtransc.ds

class Allocator<T> {
	private var lastIndex = 0;
	private val _array = arrayListOf<T>()
	private val map = hashMapOf<T, Int>()
	val array: List<T> = _array

	fun allocateOnce(value: T): Int {
		if (value !in map) {
			map[value] = lastIndex++
			_array += value
		}
		return map[value]!!
	}
}