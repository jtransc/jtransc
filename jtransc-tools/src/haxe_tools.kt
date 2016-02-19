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

import com.jtransc.ast.*
import com.jtransc.io.createZipFile
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

object HaxeTools {
	@JvmStatic fun main(args: Array<String>) {


		val file = HaxeTools::class.java.getResourceAsStream("sample_lime_neko.xml")
		val fileText = file.readBytes().toString(Charsets.UTF_8)
		//val xml = DocumentBuilderFactory.newInstance()
		val dbf = DocumentBuilderFactory.newInstance()
		val db = dbf.newDocumentBuilder()
		val doc = db.parse(fileText.byteInputStream())
		//println(doc)
		//println(doc.childNodes.item(0))
		val types = HaxeDocXmlParser.parseDocument(doc).filter {
			it.fqname.packagePath.isNotEmpty()
		}

		//for (type in types) {
		//	println("$type {")
		//	for (member in type.members) println("   $member")
		//	println("}")
		//}

		File(File(".").absolutePath + "/test.jar").writeBytes(generateJar(types))

		//lime.AssetLibrary.exists()
		//lime.AssetLibrary.exists()
		//lime.ui.Gamepad.addMappings()

		//doc.dump()
	}

	fun generateClass(type: HaxeType): ByteArray {
		val cw = ClassWriter(0);
		var classAccess = 0
		val isInterface = type.isInterface
		classAccess += Opcodes.ACC_PUBLIC
		if (isInterface) classAccess += Opcodes.ACC_INTERFACE

		cw.visit(
			Opcodes.ASM5,
			classAccess,
			type.fqname.internalFqname,
			"",
			"java.lang.Object",
			arrayOf<String>()
		)

		for (member in type.members) {
			var access = 0
			val typeString = member.type.mangle()
			if (member.isStatic) access = access or Opcodes.ACC_STATIC
			access = access or (if (member.isPublic) Opcodes.ACC_PUBLIC else Opcodes.ACC_PROTECTED)
			val name = member.name
			val isConstructor = (name == "new")


			when (member) {
				is HaxeField -> {
					cw.visitField(
						Opcodes.ACC_PUBLIC,
						name,
						typeString,
						typeString,
						null
					)
				}
				is HaxeMethod -> {
					//if (isConstructor) access = access or Opcodes.ACC_
					if (!isInterface) {
						access += Opcodes.ACC_NATIVE
					} else {
						access += Opcodes.ACC_ABSTRACT
					}
					//cw.visitAnnotation(true)
					cw.visitMethod(
						access,
						if (isConstructor) "<init>" else name,
						typeString,
						typeString,
						arrayOf<String>()
					)
					//cw.
				}
			}
		}
		cw.visitEnd()
		return cw.toByteArray()
	}

	fun generateJar(types: List<HaxeType>): ByteArray {
		return createZipFile(types.associate { Pair(it.fqname.internalFqname + ".class", generateClass(it)) })
	}

	object HaxeDocXmlParser {
		fun parseDocument(doc: Document): List<HaxeType> {
			return doc.elementChildren.first().elementChildren.map {
				parseType(it)
			}
		}

		fun parseType(node: Element): HaxeType {
			val typeName = node.attributes.getNamedItem("path").textContent
			val params = node.attributes.getNamedItem("params").textContent.split(":")
			val isExtern = node.attributes.getNamedItem("extern")?.textContent == "1"
			val isInterface = node.attributes.getNamedItem("interface")?.textContent == "1"
			val members = node.elementChildren.map { parseMember(it) }
			//module="StdTypes" extern="1" interface="1"
			//println(members)
			return when (node.nodeName) {
				"class", "typedef", "abstract","enum"  -> {
					HaxeClass(
						typeName.fqname,
						members,
						isInterface = isInterface,
						isExtern = isExtern
					)
				}
				else -> throw NotImplementedError("type: ${node.nodeName}")
			}
		}

		fun parseArgType(it: Element): AstType {
			return when (it.nodeName) {
				"d" -> AstType.OBJECT // Dynamic!
				"a" -> {
					//HaxeArgTypeBase("a?")
					//noImpl
					AstType.INT
				}
				"unknown" -> {
					//HaxeArgTypeBase("unknown")
					//noImpl
					AstType.INT
				}
				"x", "c", "t", "f", "e" -> {
					val path = it.attribute("path")
					val generics = it.elementChildren.map { parseArgType(it) }
					when (path) {
						"String" -> AstType.STRING
						"Bool" -> AstType.BOOL
						"Int" -> AstType.INT
						"Float" -> AstType.DOUBLE
						"Void" -> AstType.VOID
						"Array" -> AstType.ARRAY(parseArgType(it.elementChildren.first()))
						// @TODO: Type must be nullable so probably we should convert primitive types to class types
						"Null" -> parseArgType(it.elementChildren.first())
						else -> {
							if (generics.isNotEmpty()) {
								//HaxeArgTypeGeneric(HaxeArgTypeBase(path), generics)
								//AstType.INT
							} else {
								//HaxeArgTypeBase(path)
								//AstType.INT
							}

							//noImpl
							//AstType.INT
							AstType.REF(path)
						}
					}

				}
				else -> throw NotImplementedError("argtype: ${it.nodeName}")
			}
		}

		fun parseMember(member: Element): HaxeMember {
			val public = member.attribute("public") == "1"
			val static = member.attribute("static") == "1"
			val set = member.attribute("set")
			return when (set) {
				"method" -> {
					val decl = member.elementChildren.first { it.nodeName == "f" }
					val argNames = decl.attribute("a").split(":")
					val argTypes = decl.elementChildren.map { parseArgType(it) }
					val args = argNames.zip(argTypes.dropLast(1)).withIndex().map {
						AstArgument(it.index, it.value.second, it.value.first)
					}
					val rettype = argTypes.last()
					//println(args + ": RET : " + rettype)
					HaxeMethod(
						name = member.nodeName,
						isPublic = public,
						isStatic = static,
						args = args,
						rettype = rettype
					)
				}
				"null" -> {
					val type2 = parseArgType(member.elementChildren.first())
					HaxeField(name = member.nodeName, type = type2, isPublic = public, isStatic = static)
				}
				"", "dynamic", "accessor" -> {
					HaxeField(name = member.nodeName, type = AstType.OBJECT, isPublic = public, isStatic = static)
				}
				else -> throw NotImplementedError("set: $set")
			}
		}
	}

	interface HaxeType {
		val fqname: FqName
		val members: List<HaxeMember>
		val isInterface: Boolean
		val isExtern: Boolean
	}

	data class HaxeClass(
		override val fqname: FqName,
		override val members: List<HaxeMember>,
	    override val isInterface: Boolean = false,
		override val isExtern: Boolean = false
	) : HaxeType

	interface HaxeMember {
		val name: String
		val type: AstType
		val isStatic: Boolean
		val isPublic: Boolean
	}

	data class HaxeField(override val name: String, override val isPublic: Boolean, override val isStatic: Boolean, override val type: AstType) : HaxeMember
	data class HaxeMethod(override val name: String, override val isPublic: Boolean, val args: List<AstArgument>, val rettype: AstType, override val isStatic: Boolean) : HaxeMember {
		val methodType by lazy { AstType.METHOD_TYPE(args, rettype) }
		override val type by lazy { methodType }
	}

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

//class AstAbstractType : AstType
