package com.jtransc.lang

class ReflectedArray(val array: Any) {
	fun getType(): Class<*> = array.javaClass.componentType
	operator fun get(index:Int): Any? = java.lang.reflect.Array.get(array, index)
	operator fun set(index:Int, value: Any?): Unit {
		java.lang.reflect.Array.set(array, index, value)
	}
	val size: Int get() = java.lang.reflect.Array.getLength(array)
}
