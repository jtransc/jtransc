package com.jtransc.onetimescripts

import com.jtransc.text.isPrintable
import com.jtransc.text.uescape
import com.jtransc.text.uquote
import java.nio.charset.Charset

fun handleCharset(charsetName: String, vararg aliases: String) {
	val charset = Charset.forName(charsetName)
	var out = ""
	for (n in 0x00..0xFF) {
		val str = byteArrayOf(n.toByte()).toString(charset)
		val char = str[0]
		val unicode = char.toInt()
		out += char
	}
	println("""charsets.set("${charsetName.toUpperCase()}", new JTranscSingleByteCharset("${charsetName.toUpperCase()}", new String[] { ${aliases.map { "\"${it.toUpperCase()}\"" }.joinToString(", ")} }, ${out.uquote()}));""")
}

fun main(args: Array<String>) {
	handleCharset("cp866")
}
