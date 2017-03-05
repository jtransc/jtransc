package com.jtransc.serialization.xml

import com.jtransc.error.invalidOp
import com.jtransc.text.substr

class StrReader(val str: String, val file: String = "file", var pos: Int = 0) {
	companion object {
		fun literals(vararg lits: String): Literals = Literals.fromList(lits.toList().toTypedArray())
	}

	val length: Int = this.str.length
	val eof: Boolean get() = (this.pos >= this.str.length)
	val hasMore: Boolean get() = (this.pos < this.str.length)

	fun reset() = run { this.pos = 0 }
	fun createRange(range: IntRange): TRange = createRange(range.start, range.endInclusive + 1)
	fun createRange(start: Int = this.pos, end: Int = this.pos): TRange = TRange(start, end, this)

	fun readRange(length: Int): TRange {
		val range = TRange(this.pos, this.pos + length, this)
		this.pos += length
		return range
	}

	inline fun slice(action: () -> Unit): String? {
		val start = this.pos
		action()
		val end = this.pos
		return if (end > start) this.slice(start, end) else null
	}

	fun slice(start: Int, end: Int): String = this.str.substring(start, end)
	fun peek(count: Int): String = substr(this.pos, count)
	fun peek(): Char = if (hasMore) this.str[this.pos] else '\u0000'
	fun peekChar(): Char = if (hasMore) this.str[this.pos] else '\u0000'
	fun read(count: Int): String = this.peek(count).apply { skip(count) }
	fun skipUntil(char: Char) = run { while (hasMore && this.peekChar() != char) this.readChar() }
	fun skipUntilIncluded(char: Char) = run { while (hasMore && this.readChar() != char) Unit }
	//inline fun skipWhile(check: (Char) -> Boolean) = run { while (check(this.peekChar())) this.skip(1) }
	inline fun skipWhile(filter: (Char) -> Boolean) = run { while (hasMore && filter(this.peekChar())) this.readChar() }

	inline fun skipUntil(filter: (Char) -> Boolean) = run { while (hasMore && !filter(this.peekChar())) this.readChar() }
	inline fun matchWhile(check: (Char) -> Boolean): String? = slice { skipWhile(check) }

	fun readUntil(char: Char) = this.slice { skipUntil(char) }
	fun readUntilIncluded(char: Char) = this.slice { skipUntilIncluded(char) }
	inline fun readWhile(filter: (Char) -> Boolean) = this.slice { skipWhile(filter) } ?: ""
	inline fun readUntil(filter: (Char) -> Boolean) = this.slice { skipUntil(filter) } ?: ""
	fun unread(count: Int = 1) = this.apply { this.pos -= count; }
	fun readChar(): Char = if (hasMore) this.str[this.pos++] else '\u0000'
	fun read(): Char = if (hasMore) this.str[this.pos++] else '\u0000'

	fun readExpect(expected: String): String {
		val readed = this.read(expected.length)
		if (readed != expected) throw IllegalArgumentException("Expected '$expected' but found '$readed' at $pos")
		return readed
	}

	fun expect(expected: Char) = readExpect("$expected")
	fun skip(count: Int = 1) = this.apply { this.pos += count; }
	private fun substr(pos: Int, length: Int): String {
		return this.str.substring(Math.min(pos, this.length), Math.min(pos + length, this.length))
	}

	fun matchLit(lit: String): String? {
		if (substr(this.pos, lit.length) != lit) return null
		this.pos += lit.length
		return lit
	}

	fun matchLitRange(lit: String): TRange? = if (substr(this.pos, lit.length) == lit) this.readRange(lit.length) else null

	fun matchLitListRange(lits: Literals): TRange? {
		for (len in lits.lengths) {
			if (lits.contains(substr(this.pos, len))) return this.readRange(len)
		}
		return null
	}

	fun skipSpaces() = this.apply { this.skipWhile { Character.isWhitespace(it) } }

	fun matchIdentifier() = matchWhile { Character.isLetterOrDigit(it) || it == '-' || it == '~' || it == ':' }
	fun matchSingleOrDoubleQuoteString(): String? {
		when (this.peekChar()) {
			'\'', '"' -> {
				return this.slice {
					val quoteType = this.readChar()
					this.readUntil(quoteType)
					this.readChar()
				}
			}
			else -> return null
		}
	}

	//fun matchEReg(v: Regex): String? {
	//	val result = v.find(this.str.substring(this.pos)) ?: return null
	//	val m = result.groups[0]!!.value
	//	this.pos += m.length
	//	return m
	//}
//
	//fun matchERegRange(v: Regex): TRange? {
	//	val result = v.find(this.str.substring(this.pos)) ?: return null
	//	return this.readRange(result.groups[0]!!.value.length)
	//}

	fun matchStartEnd(start: String, end: String): String? {
		if (substr(this.pos, start.length) != start) return null
		val startIndex = this.pos
		val index = this.str.indexOf(end, this.pos)
		if (index < 0) return null
		//trace(index);
		this.pos = index + end.length
		return this.slice(startIndex, this.pos)
	}

	fun clone(): StrReader = StrReader(str, file, pos)

	fun tryRead(str: String): Boolean {
		if (peek(str.length) == str) {
			skip(str.length)
			return true
		}
		return false
	}

	class Literals(private val lits: Array<String>, private val map: MutableMap<String, Boolean>, val lengths: Array<Int>) {
		companion object {
			fun invoke(vararg lits: String): Literals = fromList(lits.toCollection(arrayListOf<String>()).toTypedArray())
			//fun invoke(lits:Array<String>): Literals = fromList(lits)
			fun fromList(lits: Array<String>): Literals {
				val lengths = lits.map { it.length }.sorted().reversed().distinct().toTypedArray()
				val map = hashMapOf<String, Boolean>()
				for (lit in lits) map[lit] = true
				return Literals(lits, map, lengths)
			}
		}

		fun contains(lit: String) = map.containsKey(lit)

		fun matchAt(str: String, offset: Int): String? {
			for (len in lengths) {
				val id = str.substr(offset, len)
				if (contains(id)) return id
			}
			return null
		}

		override fun toString() = "Literals(${lits.joinToString(" ")})"
	}

	class TRange(val min: Int, val max: Int, val reader: StrReader) {
		companion object {
			fun combine(a: TRange, b: TRange): TRange {
				return TRange(Math.min(a.min, b.min), Math.max(a.max, b.max), a.reader)
			}

			fun combineList(list: List<TRange>): TRange? {
				if (list.isEmpty()) return null
				val first = list[0]
				var min = first.min
				var max = first.max
				for (i in list) {
					min = Math.min(min, i.min)
					max = Math.max(max, i.max)
				}
				return TRange(min, max, first.reader)
			}

			fun createDummy() = TRange(0, 0, StrReader(""))
		}

		fun contains(index: Int): Boolean = index >= this.min && index <= this.max
		override fun toString() = "$min:$max"

		val file: String get() = this.reader.file
		val text: String get () = this.reader.slice(this.min, this.max)

		fun startEmptyRange(): TRange = TRange(this.min, this.min, this.reader)
		fun endEmptyRange(): TRange = TRange(this.max, this.max, this.reader)
		fun displace(offset: Int): TRange = TRange(this.min + offset, this.max + offset, this.reader)
	}
}

fun StrReader.readStringLit(reportErrors: Boolean = true): String {
	val out = StringBuilder()
	val quotec = read()
	when (quotec) {
		'"', '\'' -> Unit
		else -> invalidOp("Invalid string literal")
	}
	var closed = false
	while (hasMore) {
		val c = read()
		if (c == '\\') {
			val cc = read()
			out.append(when (cc) {
				'\\' -> '\\'; '/' -> '/'; '\'' -> '\''; '"' -> '"'
				'b' -> '\b'; 'f' -> '\u000c'; 'n' -> '\n'; 'r' -> '\r'; 't' -> '\t'
				'u' -> read(4).toInt(0x10).toChar()
				else -> invalidOp("Invalid char '$cc'")
			})
		} else if (c == quotec) {
			closed = true
			break
		} else {
			out.append(c)
		}
	}
	if (!closed && reportErrors) {
		throw RuntimeException("String literal not closed! '${this.str}'")
	}
	return out.toString()
}
