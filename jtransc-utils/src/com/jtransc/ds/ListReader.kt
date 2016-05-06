package com.jtransc.ds

class ListReader<T>(val list: List<T>) {
	var position = 0
	val size: Int get() = list.size
	val eof: Boolean get() = position >= list.size
	val hasMore: Boolean get() = position < list.size
	fun peek(): T = list[position]
	fun skip(count:Int = 1) = this.apply { this.position += count }
	fun read(): T = peek().apply { skip(1) }
}