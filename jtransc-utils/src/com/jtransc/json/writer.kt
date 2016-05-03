package com.jtransc.json

class JsonWriterException(message: String) : RuntimeException(message)

class JsonWriter(prettify: Boolean = false) {
	val sb = StringBuilder()
	fun write(chr: Char) = this.apply { sb.append(chr) }
	fun write(str: String) = this.apply { sb.append(str) }
	inline fun indent(callback: () -> Unit) {
		try {
			callback()
		} finally {

		}
	}
	fun line() {

	}
	override fun toString() = sb.toString()
}

fun JsonWriter.writeValue(value: Any?): JsonWriter = this.apply {
	when (value) {
		null -> write("null")
		true -> write("true")
		false -> write("false")
		is Map<*, *> -> writeObject(value)
		is Iterable<*> -> writeArray(value)
		is String -> writeString(value)
		is Double -> writeNumber(value)
		is Number -> writeNumber(value.toDouble())
		else -> throw JsonWriterException("Don't know how to serialize $value")
	}
}

fun JsonWriter.writeNumber(value: Double): JsonWriter = this.apply {
	write(value.toString())
}

fun JsonWriter.writeString(str: String): JsonWriter = this.apply {
	write('"')
	for (ch in str) {
		when (ch) {
			'"' -> write("\\\"")
			'\\' -> write("\\\\")
			'/' -> write("\\/")
			'\b' -> write("\\b")
			'\u000c' -> write("\\f")
			'\n' -> write("\\n")
			'\r' -> write("\\r")
			'\t' -> write("\\t")
			in '\u0000'..'\u001f', '\u007f', in '\u00ff'..'\uffff' -> write("\\u%04x".format(ch.toInt()))
			else -> write(ch)
		}
	}
	write('"')
}

fun JsonWriter.writeObject(obj: Map<*, *>): JsonWriter = this.apply {
	write('{')
	indent {
		var first = true
		for (pair in obj) {
			if (first) {
				first = false
			} else {
				write(',')
				line()
			}
			writeString("${pair.key}")
			write(':')
			writeValue(pair.value)
		}
	}
	write('}')
}

fun JsonWriter.writeArray(list: Iterable<*>): JsonWriter = this.apply {
	write('[')
	indent {
		var first = true
		for (item in list) {
			if (first) first = false else {
				write(',')
				line()
			}
			writeValue(item)
		}
	}
	write(']')
}