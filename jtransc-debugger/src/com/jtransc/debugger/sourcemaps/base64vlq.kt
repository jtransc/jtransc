package com.jtransc.debugger.sourcemaps

import com.jtransc.error.invalidOp
import com.jtransc.error.noImpl
import com.jtransc.lang.map
import com.jtransc.lang.nullMap
import com.jtransc.text.StrReader

// sourcemaps

// http://murzwin.com/base64vlq.html
object Base64Vlq {
	private fun decodeB64Char(c: Char): Int = when (c) {
		in 'A'..'Z' -> (c - 'A') + 0;
		in 'a'..'z' -> (c - 'a') + 26
		in '0'..'9' -> (c - '0') + 52
		'+' -> 62
		'/' -> 63
		else -> invalidOp("Invalid: $c")
	}

	private fun encodeB64Char(v: Int): Char = when (v) {
		in 0..25 -> ('A' + v - 0).toChar();
		in 26..51 -> ('a' + v - 26).toChar();
		in 52..61 -> ('0' + v - 52).toChar();
		62 -> '+'
		63 -> '/'
		else -> invalidOp("Invalid: $v")
	}

	fun decode(str: String): List<Int> {
		val out = arrayListOf<Int>()
		val r = StrReader(str)
		while (!r.eof) {
			var v = 0
			var offset = 0
			do {
				var c = decodeB64Char(r.readch())
				v = v or (c shl offset)
				offset += 5
			} while (((c ushr 5) and 1) != 0)
			val negative = (v and 1) != 0
			val absValue = v ushr 1
			out.add(if (negative) -absValue else absValue)
		}
		return out
	}

	private fun encodeOne(item:Int):String {
		val negative = (item < 0)
		val absValue = Math.abs(item)
		val valueToEncode = (absValue shl 1) or negative.map(1, 0)
		var v = valueToEncode
		var out = ""
		do {
			val chunk = v and 0x1F
			v = v ushr 5
			out += encodeB64Char(chunk or (v != 0).map(0x20, 0))
		} while (v != 0)
		return out
	}

	fun encode(items: List<Int>): String {
		return items.map { encodeOne(it) }.joinToString("")
	}
}

