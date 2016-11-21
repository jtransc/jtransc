package com.jtransc.gen.common

class StringPool {
	enum class Type { GLOBAL, PER_CLASS }

	private var lastId = 0
	private val stringIds = hashMapOf<String, Int>()
	private var valid = false
	private var cachedEntries = listOf<CommonGenerator.StringInPool>()
	fun alloc(str: String): Int {
		return stringIds.getOrPut(str) {
			valid = false
			lastId++
		}
	}

	fun getAllSorted(): List<CommonGenerator.StringInPool> {
		if (!valid) {
			cachedEntries = stringIds.entries.map { CommonGenerator.StringInPool(it.value, it.key) }.sortedBy { it.id }.toList()
			valid = true
		}
		return cachedEntries
	}
}