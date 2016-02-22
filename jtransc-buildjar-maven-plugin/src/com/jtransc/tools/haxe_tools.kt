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

package com.jtransc.tools

import com.jtransc.ast.*
import com.jtransc.error.invalidOp
import com.jtransc.io.createZipFile
import com.jtransc.text.Indenter
import com.jtransc.vfs.LocalVfs
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
		val vfs = LocalVfs(File("."))

		val libraryInfo = LibraryInfo(
			libraries = listOf("lime:2.9.0"),
			includePackages = listOf("lime"),
			includePackagesRec = listOf(
				"lime.ui", "lime.app",
				"lime.audio",
				"lime.math",
				"lime.project",
				"lime.system",
				"lime.text",
				"lime.utils",
				"lime.vm",
				"lime.tools"
			),
			target = "cpp"
		)

		//val outJar = vfs["out.jar"]
		//outJar.write(generateJarFromHaxeLib(libraryInfo))
		//println(outJar.realpathOS)
		val outJar = vfs["out_java"]
		for ((name, content) in generateJavaSourcesFromHaxeLib(libraryInfo)) {
			outJar[name] = content
		}
	}

	data class LibraryInfo(
		val libraries: List<String>,
		val includePackages: List<String>,
		val includePackagesRec: List<String>,
		val target: String
	)

	fun generateXmlFromHaxelib(libraryInfo: LibraryInfo): String {
		val libraries = libraryInfo.libraries
		val includePackages = libraryInfo.includePackages
		val includePackagesRec = libraryInfo.includePackagesRec
		val target = libraryInfo.target

		val xmlFile = createTempFile("jtransc_haxe_tools", ".xml")
		val vfs = LocalVfs(xmlFile.parentFile)

		data class LibraryVersion(val name: String, val version: String) {
			val nameWithVersion = "$name:$version"
		}

		val librariesInfo = libraries.map {
			val parts = it.split(':')
			LibraryVersion(parts[0], parts[1])
		}

		val outXml = vfs[xmlFile.name]
		println(outXml.realpathOS)
		for (info in librariesInfo) {
			if (!vfs.exec("haxelib", "path", info.nameWithVersion).success) {
				vfs.passthru("haxelib", "install", info.name, info.version)
			}
		}

		val haxeargs = listOf(
			"-cp", ".",
			"-xml", outXml.realpathOS,
			"--no-output",
			"-$target", "dummy"
		) + librariesInfo.flatMap { listOf("-lib", it.nameWithVersion) } + listOf(
			"-swf-version", "20",
			"--macro", "allowPackage('flash')",
			"--macro", "allowPackage('js')"
		) + includePackages.flatMap { listOf("--macro", "include('$it', false)") } +
			includePackagesRec.flatMap { listOf("--macro", "include('$it', true)") }

		println("haxe " + haxeargs)

		vfs.passthru("haxe", haxeargs)

		//val file = HaxeTools::class.java.getResourceAsStream("sample_lime_neko.xml")
		try {
			return outXml.readString(Charsets.UTF_8)
		} finally {
			//xmlFile.delete()
		}
	}

	fun getTypesFromHaxelib(libraryInfo: LibraryInfo): List<HaxeType> {
		val fileText = generateXmlFromHaxelib(libraryInfo)
		//val xml = DocumentBuilderFactory.newInstance()
		val dbf = DocumentBuilderFactory.newInstance()
		val db = dbf.newDocumentBuilder()
		val doc = db.parse(fileText.byteInputStream())
		//println(doc)
		//println(doc.childNodes.item(0))
		return HaxeDocXmlParser.parseDocument(doc).filter {
			when (it.fqname.fqname) {
				"haxe.Unserializer" -> false
				else -> true
			}
			//it.fqname.packagePath.isNotEmpty()
		}
	}

	fun generateClassesFromHaxeLib(libraryInfo: LibraryInfo): Map<String, ByteArray> {
		val types = getTypesFromHaxelib(libraryInfo)
		//for (type in types) {
		//	println("$type {")
		//	for (member in type.members) println("   $member")
		//	println("}")
		//}
		return types
			.associate { Pair(it.fqname.internalFqname + ".class", generateClass(it)) }
	}

	fun generateJarFromHaxeLib(libraryInfo: LibraryInfo): ByteArray {
		return createZipFile(generateClassesFromHaxeLib(libraryInfo))
	}

	fun generateJavaSourcesFromHaxeLib(libraryInfo: LibraryInfo): Map<String, String> {
		return getTypesFromHaxelib(libraryInfo).associate { generateJavaSource(it) }
	}

	fun generateJavaSource(type: HaxeType): Pair<String, String> {
		val ids = JavaIds()
		val originalClassName = type.fqname
		val validClassName = ids.generateValidFqname(type.fqname)
		val isInterface = type.isInterface
		val isEnum = type.isEnum

		for (generic in type.generics) {
			val fullGeneric = FqName(type.fqname.fqname + "." + generic)
			ids.transforms[fullGeneric] = FqName(generic)
		}

		return Pair("${validClassName.internalFqname}.java", Indenter.genString {

			line("package ${validClassName.packagePath};")

			val classType = if (isInterface) {
				"interface"
			} else {
				"class"
			}

			line("@jtransc.annotation.JTranscNativeClass(\"${originalClassName.fqname}\")")

			val implementsListString = if (type.implements.isNotEmpty()) {
				"implements " + type.implements.map { ids.generateValidFqname(it) }.joinToString(", ")
			} else {
				""
			}

			val extendsListString = if (type.extends.isNotEmpty()) {
				"extends " + type.extends.map { ids.generateValidFqname(it) }.joinToString(", ")
			} else {
				""
			}

			val genericListString = if (type.generics.isNotEmpty()) {
				"<" + type.generics.joinToString(", ") + ">"
			} else {
				""
			}

			// @TODO: This should be innecesary
			val genericListString2 = if (genericListString == "<>") "" else genericListString

			val classDecl = "public $classType ${validClassName.simpleName}$genericListString2 $extendsListString $implementsListString"

			line(classDecl) {
				//line("public $classType ${validFqname.simpleName}") {
				for (member in type.members) {
					if (member.name == "toString") continue

					val ids = ids.child()
					for (generic in member.generics) {
						ids.transforms[FqName(member.name + "." + generic)] = FqName(generic)
					}
					val modifiers = sortedSetOf<String>()
					val name = member.name
					val isConstructor = (member.name == "new")
					val validName = ids.generateValidMemberName(
						if (isConstructor) validClassName.simpleName else member.name,
						member.isStatic
					)
					if (member is HaxeMethod) {
						modifiers.add("native")
					}
					if (member.isStatic) modifiers.add("static")
					if (member.isPublic) modifiers.add("public") else modifiers.add("protected")
					//if (member.name == "toString") {
					//	modifiers.remove("protected")
					//	modifiers.remove("public")
					//	modifiers.add("public")
					//}

					val modifiersStr = modifiers.joinToString(" ")
					val memberGenericString2 = if (member.generics.isNotEmpty()) {
						"<" + member.generics.joinToString(", ") + ">"
					} else {
						""
					}
					val memberGenericString = if (memberGenericString2 == "<>") "" else memberGenericString2

					when (member) {
						is HaxeField -> {
							if (!isInterface) {
								val typeString = ids.serializeValid(member.type)
								line("@jtransc.annotation.JTranscField(\"$name\")")
								if (isEnum) {
									// @:fakeEnum String
									line("static public ${validClassName.fqname} $validName;")
								} else {
									line("$modifiersStr $typeString $validName;")
								}
							}
						}
						is HaxeMethod -> {
							val returnTypeString = if (isConstructor) "" else ids.serializeValid(member.type.ret)
							if (isConstructor) {
								if (member.args.isNotEmpty()) line("public $validName() { super(); }")
							}
							val modifiersStr = if (isConstructor) "public" else if (isInterface) "" else modifiersStr
							val endStr = if (isConstructor) "{ super(); }" else ";"

							for (args in member.args.possibleSignatures()) {
								if (isConstructor && args.isEmpty()) continue
								val argsString = args.map { ids.serializeValid(it.type) + " " + ids.generateValidId(it.name) }.joinToString(", ")
								if (!isConstructor) {
									line("@jtransc.annotation.JTranscMethod(\"$name\")")
								}
								line("$modifiersStr $memberGenericString $returnTypeString $validName($argsString)$endStr")
							}
						}
						else -> invalidOp("Unknown member type")
					}
					line("")
				}
			}
		})
	}

	fun List<AstArgument>.possibleSignatures(): List<List<AstArgument>> {
		var options = arrayListOf(this)
		var currentOption = this
		while (currentOption.isNotEmpty()) {
			if (currentOption.lastOrNull()?.optional == true) {
				currentOption = currentOption.dropLast(1)
				options.add(currentOption)
			} else {
				break
			}
		}
		return options
	}

	fun generateClass(type: HaxeType): ByteArray {
		val cw = ClassWriter(0);
		var classAccess = 0
		val isInterface = type.isInterface
		classAccess += Opcodes.ACC_PUBLIC
		if (isInterface) classAccess += Opcodes.ACC_INTERFACE

		val typeExtends = if (type.extends.isEmpty()) {
			listOf(FqName("java.lang.Object"))
		} else {
			type.extends
		}

		cw.visit(
			Opcodes.ASM5,
			classAccess,
			type.fqname.internalFqname,
			"",
			type.extends.first().internalFqname,
			type.implements.map { it.internalFqname }.toTypedArray()
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

	object HaxeDocXmlParser {
		fun parseDocument(doc: Document): List<HaxeType> {
			return doc.elementChildren.first().elementChildren.map {
				parseType(it)
			}
		}

		fun parseType(node: Element): HaxeType {
			val typeName = node.attributes.getNamedItem("path").textContent
			val generics = node.attributes.getNamedItem("params").textContent.split(":")
			val isExtern = node.attributes.getNamedItem("extern")?.textContent == "1"
			val isInterface = node.attributes.getNamedItem("interface")?.textContent == "1"
			val specialNames = setOf("extends", "implements", "meta")
			val specials = node.elementChildren.filter { it.nodeName in specialNames }
			val members = node.elementChildren
				.filter { it.nodeName !in specialNames }
				.map { parseMember(it) }
			val implements = arrayListOf<FqName>()
			val extends = arrayListOf<FqName>()

			for (special in specials) {
				when (special.tagName) {
					"implements" -> {
						implements.add(FqName(special.getAttribute("path")))
					}
					"extends" -> {
						extends.add(FqName(special.getAttribute("path")))
					}
					"meta" -> {

					}
				}
			}

			//module="StdTypes" extern="1" interface="1"
			//println(members)
			return when (node.nodeName) {
				"class", "typedef", "abstract", "enum" -> {
					HaxeType(
						fqname = typeName.fqname,
						generics = generics,
						members = members,
						isEnum = node.nodeName == "enum",
						isInterface = isInterface,
						isExtern = isExtern,
						implements = implements,
						extends = extends
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
						"UInt" -> AstType.INT
						"Float" -> AstType.DOUBLE
						"Void" -> AstType.VOID
						"Array" -> AstType.ARRAY(parseArgType(it.elementChildren.first()))
						"haxe.io.Int16Array" -> AstType.ARRAY(AstType.SHORT)
						"haxe.io.UInt16Array" -> AstType.ARRAY(AstType.CHAR)
						"haxe.io.Int32Array" -> AstType.ARRAY(AstType.INT)
						"haxe.io.Float32Array" -> AstType.ARRAY(AstType.FLOAT)
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
							if (path.isNullOrBlank()) AstType.OBJECT else AstType.REF(path)
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
			val generics = member.attribute("params").split(":")
			return when (set) {
				"method" -> {
					val decl = member.elementChildren.first { it.nodeName == "f" }
					val argNames = decl.attribute("a").split(":")
					val argTypes = decl.elementChildren.map { parseArgType(it) }
					val args = argNames.zip(argTypes.dropLast(1)).withIndex().map {
						val nameWithExtras = it.value.first
						val name = nameWithExtras.trim('?')
						val optional = (nameWithExtras.startsWith('?'))
						AstArgument(it.index, it.value.second, name, optional = optional)
					}
					val rettype = argTypes.last()
					//println(args + ": RET : " + rettype)
					HaxeMethod(
						name = member.nodeName,
						generics = generics,
						isPublic = public,
						isStatic = static,
						args = args,
						rettype = rettype
					)
				}
				"null" -> {
					val type2 = parseArgType(member.elementChildren.first())
					HaxeField(name = member.nodeName, generics = generics, type = type2, isPublic = public, isStatic = static)
				}
				"", "dynamic", "accessor" -> {
					HaxeField(name = member.nodeName, generics = generics, type = AstType.OBJECT, isPublic = public, isStatic = static)
				}
				else -> throw NotImplementedError("set: $set")
			}
		}
	}

	data class HaxeType(
		val fqname: FqName,
		val generics: List<String>,
		val members: List<HaxeMember>,
		val isInterface: Boolean,
		val isEnum: Boolean,
		val isExtern: Boolean,
		val implements: List<FqName>,
		val extends: List<FqName>
	)

	interface HaxeMember {
		val name: String
		val generics: List<String>
		val type: AstType
		val isStatic: Boolean
		val isPublic: Boolean
	}

	data class HaxeField(override val name: String, override val generics: List<String>, override val isPublic: Boolean, override val isStatic: Boolean, override val type: AstType) : HaxeMember
	data class HaxeMethod(override val name: String, override val generics: List<String>, override val isPublic: Boolean, val args: List<AstArgument>, val rettype: AstType, override val isStatic: Boolean) : HaxeMember {
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
