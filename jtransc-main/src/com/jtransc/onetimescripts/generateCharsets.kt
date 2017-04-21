package com.jtransc.onetimescripts

import com.jtransc.text.isPrintable
import com.jtransc.text.uescape
import com.jtransc.text.uquote
import java.nio.charset.Charset

fun handleCharset(charsetName: String) {
	val charset = Charset.forName(charsetName)
	val aliases = charset.aliases();
	var out = ""
	for (n in 0x00..0xFF) {
		val str = byteArrayOf(n.toByte()).toString(charset)
		val char = str[0]
		val unicode = char.toInt()
		out += char
	}
	println("""charsets.set("${charset.name().toUpperCase()}", new JTranscSingleByteCharset("${charset.name().toUpperCase()}", new String[] { ${aliases.map { "\"${it.toUpperCase()}\"" }.joinToString(", ")} }, ${out.uquote()}));""")
}

fun main(args: Array<String>) {
	handleCharset("cp866")
	handleCharset("ISO-8859-1")
	handleCharset("US-ASCII")
	println(Charset.forName("UTF-16LE").name() + " : " + Charset.forName("UTF-16LE").aliases())
	println(Charset.forName("UTF-16BE").name() + " : " + Charset.forName("UTF-16BE").aliases())
}
