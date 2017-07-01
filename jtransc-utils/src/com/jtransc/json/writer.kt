package com.jtransc.json

class JsonWriterException(message: String) : RuntimeException(message)

class JsonWriter(private val prettify: Boolean = false) {
	private val sb = StringBuilder()
	var indentation = 0
	var lineStart = true
	private fun _write() {
		if (lineStart && prettify) {
			lineStart = false
			for (n in 0 until indentation) sb.append('\t')
		}
	}

	fun write(chr: Char) = this.apply { _write(); sb.append(chr) }
	fun write(str: String) = this.apply { _write(); sb.append(str) }
	inline fun indent(callback: () -> Unit) {
		try {
			indentation++
			callback()
		} finally {
			indentation--
		}
	}

	fun space() = this.apply { if (prettify) write(' ') }
	fun line() = this.apply { if (prettify) write('\n'); lineStart = true }
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
		else -> writeObject(com.jtransc.lang.Dynamic.toMap(value))
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
	if (obj.isEmpty()) {
		write("{}")
	} else {
		write('{')
		var first = true
		indent {
			line()
			for (pair in obj) {
				if (first) {
					first = false
				} else {
					write(',')
					line()
				}
				writeString("${pair.key}")
				space()
				write(':')
				space()
				writeValue(pair.value)
			}
		}
		if (first != true) line()
		write('}')
	}
}

fun JsonWriter.writeArray(list: Iterable<*>): JsonWriter = this.apply {
	val list2 = list.toList()
	if (list2.isEmpty()) {
		write("[]")
	} else {
		write('[')
		line()
		var first = true
		indent {
			for (item in list2) {
				if (first) first = false else {
					write(',')
					line()
				}
				writeValue(item)
			}
		}
		if (first != true) line()
		write(']')
	}
}