package com.jtransc.gen.common

class PerClassNameAllocator {
	val usedNames = hashSetOf<String>()
	val allocatedNames = hashMapOf<Any, String>()

	fun allocate(key: Any, requestedName: () -> String): String {
		if (key !in allocatedNames) {
			var finalName = requestedName()
			while (finalName in usedNames) finalName += "_"
			usedNames += finalName
			allocatedNames[key] = finalName
		}
		return allocatedNames[key]!!
	}
}
