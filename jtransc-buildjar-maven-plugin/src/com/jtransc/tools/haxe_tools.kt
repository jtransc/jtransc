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
import com.jtransc.error.noImpl
import com.jtransc.gen.haxe.HaxeCompiler
import com.jtransc.gen.haxe.HaxeLib
import com.jtransc.io.createZipFile
import com.jtransc.org.objectweb.asm.ClassWriter
import com.jtransc.org.objectweb.asm.Opcodes
import com.jtransc.text.Indenter
import com.jtransc.vfs.LocalVfs
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

@Suppress("unused")
object HaxeTools {
	@JvmStatic fun main(args: Array<String>) {
		val vfs = LocalVfs(File("."))


		val libraryInfo = LibraryInfo(
			libraries = listOf("lime:5.2.1"),
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

		//val libraryInfo = LibraryInfo(
		//	libraries = listOf(),
		//	includePackages = listOf(),
		//	includePackagesRec = listOf(
		//	),
		//	target = "as3"
		//)

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

		val librariesInfo = libraries.map { HaxeLib.LibraryRef.fromVersion(it) }

		val outXml = vfs[xmlFile.name]
		println(outXml.realpathOS)
		for (info in librariesInfo) {
			HaxeLib.installIfNotExists(info)
		}

		val haxeargs = listOf(
			"-cp", xmlFile.parentFile.absolutePath,
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

		HaxeCompiler.ensureHaxeCompilerVfs()
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
		return getTypesFromHaxelib(libraryInfo).associate { generateJavaSource(it) } + mapOf(
			"_root/Functions.java" to Indenter.genString {
				line("package _root;")
				line("public class Functions") {
					for (n in 0 until 16) {
						val genericArgs = (0 until n).map { "T$it" }
						val genericTypes = genericArgs + listOf("TR")
						val genericTypesStr = genericTypes.joinToString(", ")
						val genericArgsStr = genericArgs.withIndex().map { "${it.value} " + ('a' + it.index) }.joinToString(", ")
						line("public interface F$n<$genericTypesStr> { TR handle($genericArgsStr); }")
					}
				}
			}
		) + (libraryInfo.libraries.map {
			val lib = HaxeLib.LibraryRef.fromVersion(it)
			val name = lib.id
			val className = "${name}Library"
			Pair("$className.java", Indenter.genString {
				//line("package _root;")
				line("""@jtransc.annotation.haxe.HaxeAddLibraries({ "${lib.nameWithVersion}" })""")
				line("public class $className") {
					line("""static public void use() { }""")
					line("""static public java.lang.String getName() { return "${lib.name}"; }""")
					line("""static public java.lang.String getVersion() { return "${lib.version}"; }""")
					line("""static public java.lang.String getNameWithVersion() { return "${lib.name}:${lib.version}"; }""")
				}
			})
		}).toMap()
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

			if (type.doc.isNotBlank()) {
				line("/** ${type.doc} */")
			}
			line(classDecl) {
				//line("public $classType ${validFqname.simpleName}") {
				for (member in type.members) {
					if (member.name == "toString") continue

					val cids = ids.child()
					for (generic in member.generics) {
						cids.transforms[FqName(member.name + "." + generic)] = FqName(generic)
					}
					val modifiers = sortedSetOf<String>()
					val name = member.name
					val isConstructor = (member.name == "new")
					val validName = cids.generateValidMemberName(
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

					if (!member.doc.isNullOrBlank()) {
						line("/** ${member.doc} */")
					}

					when (member) {
						is HaxeField -> {
							if (!isInterface) {
								val typeString = cids.serializeValid(member.type)
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
							val returnTypeString = if (isConstructor) "" else cids.serializeValid(member.type.ret)
							if (isConstructor) {
								if (member.args.isNotEmpty()) line("public $validName() { super(); }")
							}
							val modifiersStr2 = if (isConstructor) "public" else if (isInterface) "" else modifiersStr
							val endStr = if (isConstructor) "{ super(); }" else ";"

							for (args in member.args.possibleSignatures()) {
								if (isConstructor && args.isEmpty()) continue
								val argsString = args.map { cids.serializeValid(it.type) + " " + cids.generateValidId(it.name) }.joinToString(", ")
								if (!isConstructor) {
									line("@jtransc.annotation.JTranscMethod(\"$name\")")
								}
								line("$modifiersStr2 $memberGenericString $returnTypeString $validName($argsString)$endStr")
							}
						}
						is HaxeEnumItem -> {
							if (member.args.isEmpty()) {
								line("static public ${validClassName.fqname} $validName;")
							} else {
								data class EnumEntry(val name: String, val type: AstType) {
									val validName = cids.generateValidId(this.name)
									val validType = cids.serializeValid(this.type)
								}

								line("static public class $validName extends ${validClassName.fqname}") {
									val args = member.args.map { EnumEntry(it.name, it.type) }

									for (arg in args) {
										line("@jtransc.annotation.JTranscField(\"${arg.name}\")")
										line("public final ${arg.validType} ${arg.validName};")
									}

									val argsStr = args.map { "${it.validType} ${it.validName}" }.joinToString(", ")
									line("public $validName($argsStr)") {
										for (arg in args) line("this.${arg.validName} = ${arg.validName};")
									}
								}
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
		val options = arrayListOf(this)
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

	@Suppress("UNUSED_VARIABLE")
	fun generateClass(type: HaxeType): ByteArray {
		val cw = ClassWriter(0)
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
		val SPECIAL_NAMES = setOf("extends", "implements", "meta", "this", "to", "from", "impl", "haxe_doc")

		fun parseDocument(doc: Document): List<HaxeType> {
			return doc.elementChildren.first().elementChildren.map {
				parseType(it)
			}
		}

		fun parseType(node: Element): HaxeType {
			val typeType = node.nodeName
			val typeName = node.attributes.getNamedItem("path").textContent
			val generics = node.attributes.getNamedItem("params").textContent.split(":")
			val isExtern = node.attributes.getNamedItem("extern")?.textContent == "1"
			val isInterface = node.attributes.getNamedItem("interface")?.textContent == "1"
			val isEnum = node.nodeName == "enum"
			val implements = arrayListOf<FqName>()
			val extends = arrayListOf<FqName>()
			val doc = node.elementChildren.firstOrNull { it.tagName == "haxe_doc" }?.textContent ?: ""
			var linkType: AstType? = null
			var members = listOf<HaxeMember>()

			@Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UNUSED_VALUE")
			fun parseType2(node: Element) {
				val specials = node.elementChildren.filter { it.nodeName in SPECIAL_NAMES }
				members = node.elementChildren.filter { it.nodeName !in SPECIAL_NAMES }.map { parseMember(it, typeType) }
				var abstractThis: AstType = AstType.OBJECT
				val abstractTo = arrayListOf<AstType>()
				val abstractFrom = arrayListOf<AstType>()

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
						"this" -> abstractThis = parseHaxeType(special.elementChildren.first())
						"to" -> abstractTo.add(parseHaxeType(special.elementChildren.first()))
						"from" -> abstractFrom.add(parseHaxeType(special.elementChildren.first()))
						"impl" -> {

						}
					}
				}

				//module="StdTypes" extern="1" interface="1"
				//println(members)
				when (node.nodeName) {
					"class", "typedef", "abstract", "enum" -> {

					}
					else -> throw NotImplementedError("type: ${node.nodeName}")
				}
			}

			fun parseTypeTypedef(node: Element) {
				linkType = parseHaxeType(node.elementChildren.first())
			}

			fun parseTypeAbstract(node: Element) {
				val impl = node.elementChildren.firstOrNull { it.tagName == "impl" }?.elementChildren?.first()
				if (impl != null) {
					parseType2(impl)
				}
			}

			when (node.nodeName) {
				"typedef" -> parseTypeTypedef(node)
				"abstract" -> parseTypeAbstract(node)
				else -> parseType2(node)
			}

			return HaxeType(
				fqname = typeName.fqname,
				doc = doc,
				generics = generics,
				members = members,
				isEnum = isEnum,
				isInterface = isInterface,
				isExtern = isExtern,
				implements = implements,
				extends = extends,
				linkType = linkType
			)
		}

		fun HaxeArgument(index: Int, nameWithExtra: String, type: AstType): AstArgument {
			val name = nameWithExtra.trim('?')
			val optional = nameWithExtra.startsWith('?')
			return AstArgument(index, type, name, optional)
		}

		fun HaxeArguments(names: List<String>, types: List<AstType>): List<AstArgument> {
			return names.zip(types).withIndex().map {
				val index = it.index
				val nameWithExtra = it.value.first
				val type = it.value.second
				HaxeArgument(index, nameWithExtra, type)
			}
		}

		fun parseMember(member: Element, typeType: String): HaxeMember {
			return when (typeType) {
				"enum" -> parseEnumMember(member)
				else -> parseNormalMember(member)
			}
		}

		fun parseEnumMember(member: Element): HaxeEnumItem {
			val name = member.tagName
			val names = member.attribute("a").split(":")
			val types = member.elementChildren.filter { it.tagName !in SPECIAL_NAMES }.map { parseHaxeType(it) }

			return HaxeEnumItem(
				name = name,
				doc = "",
				generics = listOf(),
				isPublic = true,
				isStatic = true,
				type = AstType.OBJECT,
				args = HaxeArguments(names, types)
			)
		}

		fun parseNormalMember(member: Element): HaxeMember {
			val name = member.nodeName
			val public = member.attribute("public") == "1"
			val static = member.attribute("static") == "1"
			val set = member.attribute("set")
			val get = member.attribute("get")
			val generics = member.attribute("params").split(":")
			val type2 = parseHaxeType(member.elementChildren.firstOrNull { it.tagName !in SPECIAL_NAMES })
			var doc = ""

			for (child in member.elementChildren) {
				when (child.tagName) {
					"haxe_doc" -> doc = child.textContent
				}
			}

			val isMethod = (get == "inline" && type2 is AstType.METHOD) || (set == "method")

			return if (isMethod) {
				//println(args + ": RET : " + rettype)
				HaxeMethod(
					name = name,
					doc = doc,
					generics = generics,
					isPublic = public,
					isStatic = static,
					methodType = type2 as AstType.METHOD
				)

			} else {
				HaxeField(
					name = member.nodeName,
					doc = doc,
					generics = generics,
					type = type2,
					isPublic = public,
					isStatic = static
				)
			}
		}
	}

	data class HaxeType(
		val fqname: FqName,
		val doc: String,
		val generics: List<String>,
		val members: List<HaxeMember>,
		val isInterface: Boolean,
		val isEnum: Boolean,
		val isExtern: Boolean,
		val implements: List<FqName>,
		val extends: List<FqName>,
		val linkType: AstType?
	)

	interface HaxeMember {
		val name: String
		val doc: String
		val generics: List<String>
		val type: AstType
		val isStatic: Boolean
		val isPublic: Boolean
	}

	data class HaxeField(
		override val name: String,
		override val doc: String,
		override val generics: List<String>,
		override val isPublic: Boolean,
		override val isStatic: Boolean,
		override val type: AstType
	) : HaxeMember

	data class HaxeMethod(
		override val name: String,
		override val doc: String,
		override val generics: List<String>,
		override val isPublic: Boolean,
		val methodType: AstType.METHOD,
		override val isStatic: Boolean
	) : HaxeMember {
		override val type by lazy { methodType }
		val args = methodType.args
		val rettype = methodType.ret
	}

	data class HaxeEnumItem(
		override val name: String,
		override val doc: String,
		override val generics: List<String>,
		override val isPublic: Boolean,
		override val isStatic: Boolean,
		override val type: AstType,
		val args: List<AstArgument>
	) : HaxeMember

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

	fun parseHaxeType(it: Element?): AstType {
		if (it == null) return AstType.OBJECT
		return when (it.nodeName) {
			null -> AstType.OBJECT
			"d" -> AstType.OBJECT // Dynamic!
			"a" -> {
				AstType.OBJECT
			}
			"icast" -> parseHaxeType(it.elementChildren.first())
			"unknown" -> {
				//HaxeArgTypeBase("unknown")
				//noImpl
				AstType.INT
			}
		// Function
			"f" -> {
				val names = it.attribute("a").split(':')
				val types = it.elementChildren.map { parseHaxeType(it) }
				val args = types.dropLast(1)
				val rettype = types.last()
				AstType.METHOD(HaxeDocXmlParser.HaxeArguments(names, args), rettype)
			}
			"x", "c", "t", "e" -> {
				val path = it.attribute("path")
				val generics = it.elementChildren.map { parseHaxeType(it) }
				when (path) {
					"Void" -> AstType.VOID
					"Bool" -> AstType.BOOL
					"Int" -> AstType.INT
					"UInt" -> AstType.INT
					"Float" -> AstType.DOUBLE
					"Array" -> AstType.ARRAY(parseHaxeType(it.elementChildren.first()))
					"String" -> AstType.STRING
					"flash.Vector" -> AstType.ARRAY(parseHaxeType(it.elementChildren.first()))
					"haxe.io.Int16Array" -> AstType.ARRAY(AstType.SHORT)
					"haxe.io.UInt16Array" -> AstType.ARRAY(AstType.CHAR)
					"haxe.io.Int32Array" -> AstType.ARRAY(AstType.INT)
					"haxe.io.Float32Array" -> AstType.ARRAY(AstType.FLOAT)
				// @TODO: Type must be nullable so probably we should convert primitive types to class types
					"Null" -> parseHaxeType(it.elementChildren.first())
					else -> {
						val base = if (path.isNullOrBlank()) AstType.OBJECT else AstType.REF(path)
						if (generics.isEmpty()) base else AstType.GENERIC(base, generics)
					}
				}

			}
			else -> {
				noImpl("argtype: ${it.nodeName}")
			}
		}
	}
}

//class AstAbstractType : AstType
