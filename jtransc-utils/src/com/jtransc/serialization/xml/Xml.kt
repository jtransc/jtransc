package com.jtransc.serialization.xml

import org.intellij.lang.annotations.Language
import java.util.*

data class Xml(val type: Type, val name: String, val attributes: Map<String, String>, val allChildren: List<Xml>, val content: String) {
	val nameLC: String = name.toLowerCase().trim()
	val descendants: Iterable<Xml> get() = allChildren.flatMap { it.allChildren + it }
	val allChildrenNoComments: List<Xml> get() = allChildren.filter { it.type != Type.COMMENT }

	companion object {
		fun Tag(tagName: String, attributes: Map<String, Any?>, children: List<Xml>): Xml {
			return Xml(Type.NODE, tagName, attributes.filter { it.value != null }.map { it.key to it.value.toString() }.toMap(), children, "")
		}

		fun Text(text: String): Xml {
			return Xml(Type.TEXT, "_text_", mapOf(), listOf(), text)
		}

		fun Comment(text: String): Xml {
			return Xml(Type.COMMENT, "_comment_", mapOf(), listOf(), text)
		}

		//operator fun invoke(@Language("xml") str: String): Xml = parse(str)

		fun parse(@Language("xml") str: String): Xml {
			try {
				val stream = XmlStream.parse(str).iterator()

				data class Level(val children: List<Xml>, val close: XmlStream.Element.CloseTag?)

				fun level(): Level {
					var children = listOf<Xml>()

					while (stream.hasNext()) {
						val tag = stream.next()
						when (tag) {
							is XmlStream.Element.ProcessingInstructionTag -> Unit
							is XmlStream.Element.CommentTag -> children += Comment(tag.text)
							is XmlStream.Element.Text -> children += Text(tag.text)
							is XmlStream.Element.OpenCloseTag -> children += Tag(tag.name, tag.attributes, listOf())
							is XmlStream.Element.OpenTag -> {
								val out = level()
								if (out.close?.name != tag.name) throw IllegalArgumentException("Expected ${tag.name} but was ${out.close?.name}")
								children += Xml(Type.NODE, tag.name, tag.attributes, out.children, "")
							}
							is XmlStream.Element.CloseTag -> return Level(children, tag)
							else -> throw IllegalArgumentException("Unhandled $tag")
						}
					}

					return Level(children, null)
				}

				val children = level().children
				return children.firstOrNull { it.type == Type.NODE }
					?: children.firstOrNull()
					?: Text("")
			} catch (t: NoSuchElementException) {
				println("ERROR: XML: $str thrown a NoSuchElementException")
				return Text("!!ERRORED!!")
			}
		}
	}

	fun hasAttribute(key: String): Boolean = this.attributes.containsKey(key)
	fun attribute(name: String): String? = this.attributes[name]
	fun getString(name: String): String? = this.attributes[name]?.toString()
	fun getInt(name: String): Int? = this.attributes[name]?.toInt()
	fun getDouble(name: String): Double? = this.attributes[name]?.toDouble()

	val text: String get() = when (type) {
		Type.NODE -> allChildren.map { it.text }.joinToString("")
		Type.TEXT -> content
		Type.COMMENT -> ""
	}

	val outerXml: String get() = when (type) {
		Type.NODE -> {
			val attrs = attributes.toList().map { " ${it.first}=\"${it.second}\"" }.joinToString("")
			if (allChildren.isEmpty()) {
				"<$name$attrs/>"
			} else {
				val children = this.allChildren.map { it.outerXml }.joinToString("")
				"<$name$attrs>$children</$name>"
			}
		}
		Type.TEXT -> content
		Type.COMMENT -> "<!--$content-->"
	}

	val innerXml: String get() = when (type) {
		Type.NODE -> this.allChildren.map { it.outerXml }.joinToString("")
		Type.TEXT -> content
		Type.COMMENT -> "<!--$content-->"
	}

	operator fun get(name: String): Iterable<Xml> = children(name)
	fun children(name: String): Iterable<Xml> = allChildren.filter { it.name == name }
	fun child(name: String): Xml? = children(name).firstOrNull()
	fun childText(name: String): String? = child(name)?.text

	fun double(name: String, defaultValue: Double = 0.0): Double = this.attributes[name]?.toDoubleOrNull() ?: defaultValue
	fun int(name: String, defaultValue: Int = 0): Int = this.attributes[name]?.toInt() ?: defaultValue
	fun str(name: String, defaultValue: String = ""): String = this.attributes[name] ?: defaultValue

	override fun toString(): String = innerXml

	enum class Type { NODE, TEXT, COMMENT }
}
