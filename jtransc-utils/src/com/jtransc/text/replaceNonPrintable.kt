package com.jtransc.text

fun String.replaceNonPrintable(): String {
	var out = ""
	for (c in this) out += if (c.isPrintable()) c else '?'
	return out
}

fun String.toCommentString(): String {
	return "/*" + this.replace("*", "").replace("\\n", "\\\\n").replace("\\r", "\\\\r").replace("\\t", "\\\\t").replaceNonPrintable() + "*/"
}