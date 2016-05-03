package com.jtransc.json

class JsonReaderException(message: String) : RuntimeException(message)

// @TODO: skip spaces!

class JsonReader(val str: String) {
	var offset = 0
	val length: Int get() = str.length
	val eof: Boolean get() = offset >= length
	fun peekch() = str[offset]
	fun readch() = str[offset++]
	fun peek(count:Int): String  = str.substring(offset, Math.min(length, offset + count))
	fun read(count:Int): String {
		val out = peek(count)
		offset += count
		return out
	}
	fun expect(expected: String): String {
		val c = read(expected.length)
		if (c != expected) expectedError(expected, c)
		return c
	}
	fun trych(expected: Char): Char? {
		return if (peekch() == expected) readch() else null
	}
	fun expectch(expected: Char): Char {
		val c = peekch()
		if (c != expected) {
			expectedError("$expected", "$c")
		}
		return readch()
	}
	fun expectch(expected: Set<Char>): Char {
		val c = peekch()
		if (c !in expected) expectedError("$expected", "$c")
		return readch()
	}
	fun expectedError(expected: String, found: String): Nothing {
		//println("String: $str")
		throw JsonReaderException("Expected '$expected' but found '$found' at $offset")
	}
	fun expectedError(expected: String): Nothing {
		//println("String: $str")
		throw JsonReaderException("Expected '$expected' at $offset")
	}
}

private val COMMA_OR_CLOSE_CURLY_BRACES = setOf(',', '}')
private val COMMA_OR_CLOSE_BRACKETS = setOf(',', ']')

fun JsonReader.skipSpaces() {
	loop@while (!eof) {
		when (peekch()) {
			' ', '\r', '\n', '\t' -> readch()
			else -> break@loop
		}
	}
}

fun JsonReader.readValue(): Any? {
	skipSpaces()
	val v = peekch()
	val result = when (v) {
		'-', '.', in '0' .. '9' -> readNumber()
		'"' -> readString()
		'{' -> readObject()
		'[' -> readArray()
		't' -> readTrue()
		'f' -> readFalse()
		'n' -> readNull()
		else -> expectedError("Json value", "$v")
	}
	skipSpaces()
	return result
}

fun JsonReader.readTrue(): Boolean = true.apply { skipSpaces(); expect("true"); skipSpaces() }
fun JsonReader.readFalse(): Boolean = false.apply { skipSpaces(); expect("false"); skipSpaces() }
fun JsonReader.readNull(): Any? = null.apply { skipSpaces(); expect("null"); skipSpaces() }

fun JsonReader.readNumber(): Double {
	skipSpaces()
	var out = ""
	loop@while (!eof) {
		val ch = peekch()
		when (ch) {
			'+', '-', 'e', 'E', '.', in '0' .. '9' -> out += readch()
			else -> break@loop
		}
	}
	skipSpaces()
	return java.lang.Double.parseDouble(out)
}

fun JsonReader.readString(): String {
	var out = ""
	skipSpaces()
	expectch('"')
	loop@while (!eof) {
		val c = peekch()
		when (c) {
			'\\' -> {
				readch()
				val k = readch()
				when (k) {
					'"' -> out += '"'
					'\\' -> out += '\\'
					'/' -> out += '/'
					'b' -> out += '\b'
					'f' -> out += '\u000c'
					'n' -> out += '\n'
					'r' -> out += '\r'
					't' -> out += '\t'
					'u' -> out += Integer.parseInt(read(4), 16)
					else -> expectedError("Expected escape sequence")
				}
			}
			'"' -> break@loop
			else -> out += readch()
		}
	}
	expectch('"')
	skipSpaces()
	return out
}

fun JsonReader.readObject(): Map<String, Any?> {
	val out = hashMapOf<String, Any?>()
	skipSpaces()
	expectch('{')
	skipSpaces()
	do {
		skipSpaces()
		if (trych('}') != null) break
		skipSpaces()
		val key = readString()
		skipSpaces()
		expectch(':')
		skipSpaces()
		val value = readValue()
		skipSpaces()
		out[key] = value
	} while (expectch(COMMA_OR_CLOSE_CURLY_BRACES) == ',')
	skipSpaces()
	return out
}

fun JsonReader.readArray(): List<Any?> {
	val out = arrayListOf<Any?>()
	skipSpaces()
	expectch('[')
	skipSpaces()
	do {
		skipSpaces()
		if (trych(']') != null) break
		skipSpaces()
		val value = readValue()
		skipSpaces()
		out += value
	} while (expectch(COMMA_OR_CLOSE_BRACKETS) == ',')
	skipSpaces()
	return out
}