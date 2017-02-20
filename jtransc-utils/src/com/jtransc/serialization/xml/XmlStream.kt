package com.jtransc.serialization.xml

import com.jtransc.text.substr
import java.util.*

object XmlStream {
	fun parse(str: String): Iterable<Element> = parse(StrReader(str))
	fun parse(r: StrReader): Iterable<Element> = Xml2Iterable(r)

	class Xml2Iterator(r2: StrReader) : Iterator<Element> {
		val r = r2.clone()
		var buffer: String = ""
		var hasMore: Boolean = true
		var current: Element? = null

		fun flushBuffer() {
			if (buffer.isNotEmpty()) {
				current = Element.Text(XmlEntities.decode(buffer))
				buffer = ""
			}
		}

		fun prepare() {
			if (current != null) return

			if (r.eof) {
				current = null
				hasMore = false
				return
			}

			mainLoop@ while (!r.eof) {
				when (r.peekChar()) {
					'<' -> {
						flushBuffer()
						if (current != null) return
						r.readExpect("<")

						if (r.matchLit("![CDATA[") != null) {
							val start = r.pos
							while (!r.eof) {
								val end = r.pos
								if (r.matchLit("]]>") != null) {
									current = Element.Text(r.createRange(start until end).text)
									return
								}
								r.readChar()
							}
							break@mainLoop
						} else if (r.matchLit("!--") != null) {
							val start = r.pos
							while (!r.eof) {
								val end = r.pos
								if (r.matchLit("-->") != null) {
									current = Element.CommentTag(r.createRange(start until end).text)
									return
								}
								r.readChar()
							}
							break@mainLoop
						} else {
							r.skipSpaces()
							val processingInstruction = r.matchLit("?") != null
							val close = r.matchLit("/") != null
							r.skipSpaces()
							val name = r.matchIdentifier()!!
							r.skipSpaces()
							val attributes = LinkedHashMap<String, String>()
							while (r.peekChar() != '?' && r.peekChar() != '/' && r.peekChar() != '>') {
								val key = r.matchIdentifier() ?: throw IllegalArgumentException("Malformed document or unsupported xml construct around ~${r.peek(10)}~")
								r.skipSpaces()
								if (r.matchLit("=") != null) {
									r.skipSpaces()
									val argsQuote = r.matchSingleOrDoubleQuoteString()
									attributes[key] = if (argsQuote != null) {
										XmlEntities.decode(argsQuote.substr(1, -1))
									} else {
										val argsNq = r.matchIdentifier()
										XmlEntities.decode(argsNq!!)
									}
								} else {
									attributes[key] = key
								}
								r.skipSpaces()
							}
							val openclose = r.matchLit("/") != null
							val processingInstructionEnd = r.matchLit("?") != null
							r.readExpect(">")
							current = if (processingInstruction) Element.ProcessingInstructionTag(name, attributes)
							else if (openclose) Element.OpenCloseTag(name, attributes)
							else if (close) Element.CloseTag(name)
							else Element.OpenTag(name, attributes)
							return
						}
					}
					else -> {
						buffer += r.readChar()
					}
				}
			}
			hasMore = (buffer.isNotEmpty())
			flushBuffer()

		}

		override fun next(): Element {
			prepare()
			val out = this.current
			this.current = null
			return out!!
		}

		override fun hasNext(): Boolean {
			prepare()
			return hasMore
		}
	}

	class Xml2Iterable(val reader2: StrReader) : Iterable<Element> {
		val reader = reader2.clone()
		override fun iterator(): Iterator<Element> = Xml2Iterator(reader)
	}

	sealed class Element {
		class ProcessingInstructionTag(val name: String, val attributes: Map<String, String>) : Element()
		class OpenCloseTag(val name: String, val attributes: Map<String, String>) : Element()
		class OpenTag(val name: String, val attributes: Map<String, String>) : Element()
		class CommentTag(val text: String) : Element()
		class CloseTag(val name: String) : Element()
		class Text(val text: String) : Element()
	}
}
