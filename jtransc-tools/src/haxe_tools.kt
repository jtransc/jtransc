/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import javax.xml.parsers.DocumentBuilderFactory

object HaxeTools {
	@JvmStatic public fun main(args: Array<String>) {
		val file = HaxeTools::class.java.getResourceAsStream("sample_lime_neko.xml")
		//val xml = DocumentBuilderFactory.newInstance()
		val dbf = DocumentBuilderFactory.newInstance()
		val db = dbf.newDocumentBuilder()
		val doc = db.parse(file)
		val types = HaxeDocXmlParser.parseDocument(doc)
		for (type in types) {
			println("$type {")
			for (member in type.members) {
				println("   $member")
			}
			println("}")
		}
		//doc.dump()
	}

	object HaxeDocXmlParser {
		fun parseDocument(doc: Document): List<HaxeType> {
			return doc.firstChild.elementChildren.map {
				parseType(it)
			}
		}

		fun parseType(node: Element): HaxeType {
			val typeName = node.attributes.getNamedItem("path").textContent
			val params = node.attributes.getNamedItem("params").textContent.split(":")
			val members = node.elementChildren.map { parseMember(it) }
			println(members)
			return when (node.nodeName) {
				"class" -> {
					HaxeClass(typeName, members)
				}
				"typedef" ->
					HaxeClass(typeName, members)
				"abstract" ->
					HaxeClass(typeName, members)
				"enum" ->
					HaxeClass(typeName, members)
				else -> throw NotImplementedError("type: ${node.nodeName}")
			}
		}

		fun parseArgType(it: Element): HaxeArgType {
			return when (it.nodeName) {
				"d" -> HaxeArgTypeBase("dynamic")
				"a" -> HaxeArgTypeBase("a?")
				"unknown" -> HaxeArgTypeBase("unknown")
				"x", "c", "t", "f", "e" -> {
					// abstract, class, typedef, anonymous?, enum
					val path = it.attribute("path")
					val generics = it.elementChildren.map { parseArgType(it) }
					if (generics.isNotEmpty()) {
						HaxeArgTypeGeneric(HaxeArgTypeBase(path), generics)
					} else {
						HaxeArgTypeBase(path)
					}
				}
				else -> throw NotImplementedError("argtype: ${it.nodeName}")
			}
		}

		fun parseMember(member: Element): HaxeMember {
			val static = member.attribute("static") == "1"
			val set = member.attribute("set")
			return when (set) {
				"method" -> {
					val decl = member.elementChildren.first { it.nodeName == "f" }
					val argNames = decl.attribute("a").split(":")
					val argTypes = decl.elementChildren.map { parseArgType(it) }
					val args = argNames.zip(argTypes.dropLast(1))
					val rettype = argTypes.last()
					//println(args + ": RET : " + rettype)
					HaxeMethod(
						name = member.nodeName, isStatic = static,
						args = args,
						rettype = rettype
					)
				}
				"null" -> {
					val type2 = parseArgType(member.elementChildren.first())
					HaxeField(name = member.nodeName, type = type2, isStatic = static)
				}
				"", "dynamic", "accessor" -> HaxeField(name = member.nodeName, type = HaxeArgTypeBase("unknown"), isStatic = static)
				else -> throw NotImplementedError("set: $set")
			}
		}
	}

	interface HaxeType {
		val fqname: String
		val members: List<HaxeMember>
	}
	data class HaxeClass(override val fqname: String, override val members: List<HaxeMember>) : HaxeType

	interface HaxeMember
	data class HaxeField(val name: String, val isStatic: Boolean, val type: HaxeArgType) : HaxeMember
	data class HaxeMethod(val name: String, val args: List<Pair<String, HaxeArgType>>, val rettype: HaxeArgType, val isStatic: Boolean) : HaxeMember

	interface HaxeArgType
	data class HaxeArgTypeBase(val text:String) : HaxeArgType
	data class HaxeArgTypeGeneric(val base: HaxeArgType, val params: List<HaxeArgType>) : HaxeArgType

	fun Node.dump() {
		println(this)
		for (child in this.childNodes.list()) {
			child.dump()
		}
	}

	fun Node.attribute(name: String): String = this.attributes.getNamedItem(name)?.textContent ?: ""

	val Node.children: List<Node> get() = this.childNodes.list()
	val Node.elementChildren: List<Element> get() = this.children.filterIsInstance<Element>()

	fun NodeList.list() = (0 until this.length).map { this.item(it) }
}