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

package com.jtransc.gen.haxe

import com.jtransc.ast.*
import com.jtransc.ast.feature.SwitchesFeature
import com.jtransc.error.InvalidOperationException
import com.jtransc.gen.*
import com.jtransc.gen.haxe.GenHaxe.getHaxeType
import com.jtransc.io.ProcessResult2
import com.jtransc.io.ProcessUtils
import com.jtransc.lang.getResourceAsString
import com.jtransc.text.Indenter
import com.jtransc.text.escape
import com.jtransc.text.quote
import com.jtransc.text.toUcFirst
import com.jtransc.util.sortDependenciesSimple
import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.SyncVfsFile
import jtransc.annotation.haxe.*
import java.io.File

object HaxeGenDescriptor : GenTargetDescriptor() {
	override val name = "haxe"
	override val longName = "Haxe"
	override val sourceExtension = "hx"
	override val outputExtension = "bin"
	override val extraLibraries = listOf<String>()
	override val extraClasses = listOf<String>()
	override fun getGenerator() = GenHaxe
}

enum class InitMode {
	START,
	START_OLD,
	LAZY
}

//val HaxeFeatures = setOf(GotosFeature, SwitchesFeature)
val HaxeFeatures = setOf(SwitchesFeature)

val HaxeKeywords = setOf(
	"java",
	"package",
	"import",
	"class", "interface", "extends", "implements",
	"internal", "private", "protected", "final",
	"function", "var", "const",
	"if", "else",
	"switch", "case", "default",
	"do", "while", "for", "each", "in",
	"break", "continue",
	"int", "uint", "void",
	"goto"
)

enum class HaxeSubtarget(val switch: String, val singleFile: Boolean, val interpreter: String? = null) {
	JS(switch = "-js", singleFile = true, interpreter = "node"),
	CPP(switch = "-cpp", singleFile = false, interpreter = null),
	SWF(switch = "-swf", singleFile = true, interpreter = null),
	NEKO(switch = "-neko", singleFile = true, interpreter = "neko"),
	PHP(switch = "-php", singleFile = false, interpreter = "php"),
	CS(switch = "-cs", singleFile = false, interpreter = null),
	JAVA(switch = "-java", singleFile = false, interpreter = "java -jar"),
	PYTHON(switch = "-python", singleFile = true, interpreter = "python")
	;

	companion object {
		fun fromString(subtarget: String) = when (subtarget.toLowerCase()) {
			"" -> HaxeSubtarget.JS
			"js", "javascript" -> HaxeSubtarget.JS
			"cpp", "c", "c++" -> HaxeSubtarget.CPP
			"swf", "flash", "as3" -> HaxeSubtarget.SWF
			"neko" -> HaxeSubtarget.NEKO
			"php" -> HaxeSubtarget.PHP
			"cs", "c#" -> HaxeSubtarget.CS
			"java" -> HaxeSubtarget.JAVA
			"python" -> HaxeSubtarget.PYTHON
			else -> throw InvalidOperationException("Unknown subtarget '$subtarget'")
		}
	}
}

object GenHaxe : GenTarget {
	//val copyFiles = HaxeCopyFiles
	//val mappings = HaxeMappings()

	val mappings = ClassMappings()

	val INIT_MODE = InitMode.LAZY

	override val runningAvailable: Boolean = true

	private val AstType.haxeDefault: Any? get() = when (this) {
		is AstType.BOOL -> false
		is AstType.INT, is AstType.SHORT, is AstType.CHAR, is AstType.BYTE -> 0
		is AstType.LONG -> 0L
		is AstType.FLOAT, is AstType.DOUBLE -> 0.0
		is AstType.REF, is AstType.ARRAY, is AstType.NULL -> null
		else -> throw RuntimeException("Not supported haxe type $this")
	}

	private fun getHaxeFilePath(name: FqName): String {
		return getHaxeGeneratedFqName(name).fqname.replace('.', '/') + ".hx"
	}

	private fun getHaxeGeneratedFqPackage(name: FqName): String {
		return name.packageParts.map {
			if (it in HaxeKeywords) "${it}_" else it
		}.joinToString(".")
	}

	private fun getHaxeGeneratedFqName(name: FqName): FqName {
		return FqName(getHaxeGeneratedFqPackage(name), getHaxeGeneratedSimpleClassName(name))
	}

	private fun getHaxeGeneratedSimpleClassName(name: FqName): String {
		return "${name.simpleName.replace('$', '_')}_"
	}

	private fun getHaxeClassFqName(program: AstProgram, name: FqName): String {
		val clazz = program[name]
		if (clazz.isNative) {
			return "${clazz.nativeName}"
		} else {
			return getHaxeGeneratedFqName(name).fqname
		}
	}


	private fun getHaxeFieldName(program: AstProgram, field: AstField): String = getHaxeFieldName(program, field.ref)

	private val cachedFieldNames = hashMapOf<AstFieldRef, String>()

	private fun getHaxeFieldName(program: AstProgram, field: AstFieldRef): String {
		if (field !in cachedFieldNames) {
			val fieldName = field.name.replace('$', '_')
			var name = if (fieldName in HaxeKeywords) "${fieldName}_" else fieldName

			val f = program[field]
			val clazz = f.getContainingClass2(program)
			val clazzAncestors = clazz.getAncestors(program).reversed()
			val names = clazzAncestors.flatMap { it.fields }.filter { it.name == fieldName }.map { getHaxeFieldName(program, it.ref) }.toSet()

			while (name in names) name += "_"
			cachedFieldNames[field] = name
		}
		return cachedFieldNames[field]!!
	}

	class MutableProgramInfo {
		//val initializingCalls = arrayListOf<String>()
	}

	enum class As3Runtime { ADL, FLASH }

	internal fun _write(program: AstProgram, features: AstFeatures, vfs: SyncVfsFile, runtime: As3Runtime, featureSet: Set<AstFeature>, limeEntryPoint: Boolean): ProgramInfo {
		val mutableInfo = MutableProgramInfo()
		for (clazz in program.classes.filter { !it.isNative }) {
			if (clazz.implCode != null) {
				vfs[getHaxeFilePath(clazz.name)] = clazz.implCode!!
			} else {
				val result = clazz.gen(program, features, mutableInfo, featureSet)
				for (file in result.files) {
					val (clazzName, content) = file
					vfs[getHaxeFilePath(clazzName)] = content.toString()
				}
			}
		}

		val copyFiles = program.classes.flatMap {
			it.resolveAnnotation(HaxeAddFiles::value)?.toList() ?: listOf()
		}

		for (file in copyFiles) {
			vfs[file] = program.resourcesVfs[file]
		}

		val mainClassFq = program.entrypoint
		val mainClass = getHaxeClassFqName(program, mainClassFq)
		val mainMethod = "main__Ljava_lang_String__V"

		val entryPointClass = FqName(mainClassFq.fqname + "_EntryPoint")
		val entryPointFilePath = getHaxeFilePath(entryPointClass)
		val entryPointFile = getHaxeFilePath(entryPointClass)
		val entryPointFqName = getHaxeGeneratedFqName(entryPointClass)
		val entryPointSimpleName = getHaxeGeneratedSimpleClassName(entryPointClass)
		val entryPointPackage = entryPointFqName.packagePath

		fun calcClasses(program: AstProgram, mainClass: AstClass): List<AstClass> {
			return sortDependenciesSimple(mainClass) {
				it.classDependencies.map { program[it] }
			}
		}

		fun inits() = Indenter.gen {
			line("haxe.CallStack.callStack();")
			when (INIT_MODE) {
				InitMode.START_OLD -> line("$mainClass.__hx_static__init__();")
				InitMode.START -> {
					for (clazz in calcClasses(program, program[mainClassFq])) {
						line(getHaxeClassFqNameInt(program, clazz.name) + ".__hx_static__init__();")
					}
				}
				else -> {

				}
			}
		}

		val customMain = program.classes
			.map { it.resolveAnnotation(HaxeCustomMain::value) }
			.filterNotNull()
			.firstOrNull()

		val plainMain = Indenter.genString {
			line("package \$entryPointPackage;")
			line("class \$entryPointSimpleName") {
				line("static public function main()") {
					line("\$inits")
					line("\$mainClass.\$mainMethod(HaxeNatives.strArray(HaxeNatives.args()));")
				}
			}
		}

		val realMain = customMain ?: plainMain

		vfs[entryPointFilePath] = Indenter.replaceString(realMain, mapOf(
			"entryPointPackage" to entryPointPackage,
			"entryPointSimpleName" to entryPointSimpleName,
			"mainClass" to mainClass,
			"mainMethod" to mainMethod,
			"inits" to inits().toString()
		))

		vfs["HaxeReflectionInfo.hx"] = Indenter.genString {
			fun AstType.REF.getAnnotationProxyName(program:AstProgram):String {
				return "AnnotationProxy_${getHaxeGeneratedFqName(this.name).fqname.replace('.', '_')}"
			}
			val annotationProxyTypes = Indenter.genString {
				val annotationTypes = program.allAnnotations.map { it.type }.distinct()
				for (at in annotationTypes) {
					val clazz = program[at.name]
					//at.name
					line("// annotation type: $at")
					line("class ${clazz.astType.getAnnotationProxyName(program)} extends jtransc.internal_.JTranscAnnotationBase_ implements ${getHaxeClassFqName(program, clazz.name)}") {
						line("private var _data:Array<Dynamic>;")
						line("public function new(_data:Dynamic = null) { super(); this._data = _data; }")
						line("override public function getClass__Ljava_lang_Class_():java_.lang.Class_ { return HaxeNatives.resolveClass(${clazz.fqname.quote()}); }")
						for ((index, m) in clazz.methods.withIndex()) {
							line("public function ${m.getHaxeMethodName(program)}():${m.methodType.ret.getTypeTag(program)} { return this._data[$index]; }")
						}
					}
				}
			}
			fun annotation(a:AstAnnotation):String {
				fun escapeValue(it:Any?):String {
					return when (it) {
						null -> "null"
						is AstAnnotation -> annotation(it)
						is Pair<*, *> -> escapeValue(it.second)
						is AstFieldRef -> getHaxeClassFqName(program, it.containingTypeRef.name) + "." + getHaxeFieldName(program, it)
						is String -> "HaxeNatives.str(${it.quote()})"
						is Int -> "HaxeNatives.int($it)"
						is Long -> "HaxeNatives.long($it)"
						is Float -> "HaxeNatives.float($it)"
						is Double -> "HaxeNatives.double($it)"
						is List<*> -> "[" + it.map { escapeValue(it) }.joinToString(", ") + "]"
						else -> throw InvalidOperationException("Can't handle value ${it.javaClass.name} : $it")
					}
				}
				//val itStr = a.elements.map { it.key.quote() + ": " + escapeValue(it.value) }.joinToString(", ")
				val annotation = program[a.type.classRef]
				val itStr = annotation.methods.map {
					if (it.name in a.elements) {
						escapeValue(a.elements[it.name]!!)
					} else {
						escapeValue(it.defaultTag)
					}
				}.joinToString(", ")
				return "new ${a.type.getAnnotationProxyName(program)}([$itStr])"
			}
			fun annotations(annotations:List<AstAnnotation>):String {
				return "[" + annotations.map { annotation(it) }.joinToString(", ") + "]"
			}
			line("class HaxeReflectionInfo") {
				val classes = program.classes.sortedBy { it.fqname }
				val classToId = classes.withIndex().map { Pair(it.value, it.index) }.toMap()

				line("static public function __initClass(c:java_.lang.Class_):Bool") {
					line("var cn = c.name._str;")
					line("if (cn.substr(0, 1) == '[') return true;")
					line("if (cn == 'V' || cn == 'B' || cn == 'C' || cn == 'S' || cn == 'I' || cn == 'L' || cn == 'J') return true;")
					line("switch (cn.length)") {
						for (clazzGroup in program.classes.groupBy { it.fqname.length }.toList().sortedBy { it.first }) {
							val length = clazzGroup.first
							val classesWithLength = clazzGroup.second
							line("case $length:")
							indent {
								for (clazz in classesWithLength.sortedBy { it.fqname }) {
									val index = classToId[clazz]
									line("if (cn == \"${clazz.fqname}\") return c$index(c);")
								}
							}
						}
					}
					line("return false;")
				}

				line("static public function internalClassNameToName(internalClassName:String):String") {
					line("var cn = internalClassName;")
					line("switch (cn.length)") {
						for (clazzGroup in program.classes.groupBy { it.fqname.length }.toList().sortedBy { it.first }) {
							val length = clazzGroup.first
							val classesWithLength = clazzGroup.second
							line("case $length:")
							indent {
								for (clazz in classesWithLength.sortedBy { it.fqname }) {
									line("if (cn == \"${getHaxeGeneratedFqName(clazz.name)}\") return \"${clazz.fqname}\";")
								}
							}
						}
					}
					line("return null;")
				}

				for (clazz in classes) {
					val index = classToId[clazz]
					line("static private function c$index(c:java_.lang.Class_):Bool") {
						line("info(c, \"${getHaxeGeneratedFqName(clazz.name)}\", " + (clazz.extending?.fqname?.quote() ?: "null") + ", [" + clazz.implementing.map { "\"${it.fqname}\"" }.joinToString(", ") + "], ${clazz.modifiers}, " + annotations(clazz.annotations) + ");")
						for ((slot, field) in clazz.fields.withIndex()) {
							val internalName = getHaxeFieldName(program, field)
							line("field(c, ${internalName.quote()}, $slot, \"${field.name}\", \"${field.descriptor}\", ${field.modifiers}, ${field.genericSignature.quote()}, ${annotations(field.annotations)});");
						}
						for ((slot, method) in clazz.methods.withIndex()) {
							val internalName = method.getHaxeMethodName(program)
							if (method.name == "<init>") {
								line("constructor(c, ${internalName.quote()}, $slot, ${method.modifiers}, ${method.signature.quote()}, ${method.genericSignature.quote()}, ${annotations(method.annotations)});");
							} else if (method.name == "<clinit>") {
							} else {
								line("method(c, ${internalName.quote()}, $slot, \"${method.name}\", ${method.modifiers}, ${method.desc.quote()}, ${method.genericSignature.quote()}, ${annotations(method.annotations)});");
							}
						}
						line("return true;")
					}
				}
				line("static public function getJavaClass(str:String)") {
					line("return java_.lang.Class_.forName_Ljava_lang_String__Ljava_lang_Class_(HaxeNatives.str(str));")
				}
				line("static private function info(c:java_.lang.Class_, internalName:String, parent:String, interfaces:Array<String>, modifiers:Int, annotations:Array<Dynamic>)") {
					line("c._hxClass = Type.resolveClass(internalName);");
					line("c._internalName = internalName;")
					line("c._parent = parent;")
					line("c._interfaces = interfaces;")
					line("c._modifiers = modifiers;")
					line("c._fields = [];")
					line("c._methods = [];")
					line("c._constructors = [];")
					line("c._annotations = annotations;")
				}
				line("static private function field(c:java_.lang.Class_, internalName:String, slot:Int, name:String, type:String, modifiers:Int, genericDescriptor:String, annotations:Array<Dynamic>)") {
					line("var out = new java_.lang.reflect.Field_();")
					line("out.clazz = c;")
					line("out.name = HaxeNatives.str(name);")
					line("out._internalName = name;")
					//line("out.type = getJavaClass(type);")
					line("out.modifiers = modifiers;")
					line("out.signature = HaxeNatives.str(type);")
					line("out.genericSignature = HaxeNatives.str(genericDescriptor);")
					line("out.slot = slot;")
					line("out._annotations = annotations;")
					line("c._fields.push(out);")
				}
				line("static private function method(c:java_.lang.Class_, internalName:String, slot:Int, name:String, modifiers:Int, signature:String, genericDescriptor:String, annotations:Array<Dynamic>)") {
					line("var out = new java_.lang.reflect.Method_();")
					line("out._internalName = internalName;")
					line("out.clazz = c;")
					line("out.name = HaxeNatives.str(name);")
					line("out.signature = HaxeNatives.str(signature);")
					line("out.genericSignature = HaxeNatives.str(genericDescriptor);")
					line("out.slot = slot;")
					line("out.modifiers = modifiers;")
					line("out._annotations = annotations;")
					line("c._methods.push(out);")
				}
				line("static private function constructor(c:java_.lang.Class_, internalName:String, slot:Int, modifiers:Int, signature:String, genericDescriptor:String, annotations:Array<Dynamic>)") {
					line("var out = new java_.lang.reflect.Constructor_();")
					line("out._internalName = internalName;")
					line("out.clazz = c;")
					line("out.slot = slot;")
					line("out.modifiers = modifiers;")
					line("out.signature = HaxeNatives.str(signature);")
					line("out.genericSignature = HaxeNatives.str(genericDescriptor);")
					line("out._annotations = annotations;")
					line("c._constructors.push(out);")
				}
			}
			line(annotationProxyTypes)
		}

		return ProgramInfo(entryPointClass, entryPointFilePath, vfs)
	}

	data class ProgramInfo(val entryPointClass: FqName, val entryPointFile: String, val vfs: SyncVfsFile) {
		fun getEntryPointFq(program: AstProgram) = getHaxeClassFqName(program, entryPointClass)
	}

	override fun getProcessor(tinfo: GenTargetInfo): GenTargetProcessor {
		val actualSubtarget = HaxeSubtarget.fromString(tinfo.subtarget)

		val outputFile2 = File(File(tinfo.outputFile).absolutePath)
		//val tempdir = System.getProperty("java.io.tmpdir")
		val tempdir = tinfo.targetDirectory
		var info: ProgramInfo? = null
		val program = tinfo.program

		File("$tempdir/jtransc-haxe/src").mkdirs()
		val srcFolder = LocalVfs(File("$tempdir/jtransc-haxe/src")).ensuredir()

		println("Temporal haxe files: $tempdir/jtransc-haxe")

		return object : GenTargetProcessor {
			override fun buildSource() {
				info = _write(tinfo.program, AstFeatures(), srcFolder, As3Runtime.ADL, HaxeFeatures, limeEntryPoint = false)
			}

			override fun compile(): Boolean {
				if (info == null) throw InvalidOperationException("Must call .buildSource first")
				outputFile2.delete()
				println("haxe.build source path: " + srcFolder.realpathOS)

				val buildArgs = arrayListOf(
					"-cp", ".",
					"-main", info!!.entryPointFile
				)
				val releaseArgs = if (tinfo.settings.release) listOf() else listOf("-debug")
				val subtargetArgs = listOf(actualSubtarget.switch, outputFile2.absolutePath)

				for (lib in program.classes.map { it.resolveAnnotation(HaxeAddLibraries::value) }.filterNotNull().flatMap { it.toList() }) {
					buildArgs += listOf("-lib", lib)
				}

				//println("Running: -optimize=true ${info.entryPointFile}")
				return ProcessUtils.runAndRedirect(
					srcFolder.realfile,
					"haxe",
					releaseArgs + subtargetArgs + buildArgs
				).success
			}

			override fun run(redirect: Boolean): ProcessResult2 {
				if (!outputFile2.exists()) {
					return ProcessResult2("file $outputFile2 doesn't exist", -1)
				}
				println("run: ${outputFile2.absolutePath}")
				val parentDir = outputFile2.parentFile

				val runner = actualSubtarget.interpreter ?: "echo"

				return ProcessUtils.run(parentDir, runner, listOf(outputFile2.absolutePath), redirect = redirect)
			}
		}
	}

	var _usedDependencies = hashSetOf<AstType.REF>()
	private fun addTypeReference(type: AstType?) {
		when (type) {
			null -> {
			}
			is AstType.METHOD_TYPE -> {
				for (arg in type.argTypes) addTypeReference(arg)
				addTypeReference(type.ret)
			}
			is AstType.REF -> _usedDependencies.add(type)
			is AstType.ARRAY -> addTypeReference(type.elementType)
			else -> {

			}
		}
	}

	data class ClassResult(val files: List<Pair<FqName, Indenter>>)

	val AstVisibility.haxe: String get() = "public"

	fun AstClass.gen(program: AstProgram, features: AstFeatures, programInfo: MutableProgramInfo, featureSet: Set<AstFeature>): ClassResult {
		val clazz = this
		val isRootObject = clazz.name.fqname == "java.lang.Object"
		val isInterface = clazz.isInterface
		val isAbstract = (clazz.classType == AstClassType.ABSTRACT)
		val isNormalClass = (clazz.classType == AstClassType.CLASS)
		val classType = if (isInterface) "interface" else "class"
		val simpleClassName = getHaxeGeneratedSimpleClassName(clazz.name)
		fun getInterfaceList(keyword: String): String {
			return (
				(if (clazz.implementing.isNotEmpty()) " $keyword " else "") + clazz.implementing.map { getHaxeClassFqName(program, it) }.joinToString(" $keyword ")
				)
		}
		//val implementingString = getInterfaceList("implements")
		val isInterfaceWithStaticMembers = isInterface && clazz.fields.any { it.isStatic }
		//val isInterfaceWithStaticFields = clazz.name.withSimpleName(clazz.name.simpleName + "\$StaticMembers")
		_usedDependencies.clear()
		if (!clazz.extending?.fqname.isNullOrEmpty()) {
			addTypeReference(AstType.REF(clazz.extending!!))
		}
		for (impl in clazz.implementing) {
			addTypeReference(AstType.REF(impl))
		}
		//val interfaceClassName = clazz.name.append("_Fields");

		var output = arrayListOf<Pair<FqName, Indenter>>()

		fun writeField(indenter: Indenter, field: AstField, isInterface: Boolean) {
			val static = if (field.isStatic) "static " else ""
			val visibility = if (isInterface) " " else field.visibility.haxe
			val fieldType = field.type
			addTypeReference(fieldType)
			val defaultValue: Any? = if (field.hasConstantValue) field.constantValue else fieldType.haxeDefault
			val fieldName = getHaxeFieldName(program, field)
			if (mappings.isFieldAvailable(field.ref) && !field.hasAnnotation<HaxeRemoveField>()) {
				indenter.line("$static$visibility var $fieldName:${fieldType.getTypeTag(program)} = cast ${escapeConstant(defaultValue)};")
			}
		}

		fun writeMethod(indenter: Indenter, method: AstMethod, isInterface: Boolean) {
			val static = if (method.isStatic) "static " else ""
			val visibility = if (isInterface) " " else method.visibility.haxe
			addTypeReference(method.methodType)
			val margs = method.methodType.args.map { it.name + ":" + it.type.getHaxeType(program, TypeKind.TYPETAG) }
			var override = if (method.isOverriding) "override " else ""
			val inline = if (method.isInline) "inline " else ""
			val decl = try {
				"$static $visibility $inline $override function ${method.ref.getHaxeMethodName(program)}(${margs.joinToString(", ")}):${method.methodType.ret.getHaxeType(program, TypeKind.TYPETAG)}".trim()
			} catch (e: RuntimeException) {
				println("@TODO abstract interface not referenced: ${method.containingClass.fqname} :: ${method.name} : $e")
				return
				//""
			}

			if (isInterface) {
				if (!method.isImplementing) {
					indenter.line("$decl;")
				}
			} else {
				val body = mappings.getBody(method.ref) ?: method.resolveAnnotation(HaxeMethodBody::value)

				if (method.body != null && body == null) {
					indenter.line(decl) {
						when (INIT_MODE) {
							InitMode.START_OLD -> indenter.line("__hx_static__init__();")
						}
						indenter.line(features.apply(method.body!!, featureSet).gen(program, method, clazz))
					}
				} else {
					val body2 = body ?: "throw \"Native or abstract: ${clazz.name}.${method.name} :: ${method.desc} :: ${method.isExtraAdded}\";"
					indenter.line("$decl { $body2 }")
				}
			}
		}

		fun addClassInit(clazz: AstClass) = Indenter.gen {
			when (INIT_MODE) {
				InitMode.START_OLD, InitMode.LAZY -> line("static public var __hx_static__init__initialized_ = false;");
				else -> Unit
			}
			line("static public function __hx_static__init__()") {
				when (INIT_MODE) {
					InitMode.START_OLD, InitMode.LAZY -> {
						line("if (__hx_static__init__initialized_) return;")
						line("__hx_static__init__initialized_ = true;")
					}
					else -> Unit
				}
				when (INIT_MODE) {
					InitMode.START_OLD -> {
						for (clazz in clazz.classDependencies) {
							line(getHaxeClassFqNameInt(program, clazz.name) + ".__hx_static__init__();")
						}
					}
				}

				if (clazz.hasStaticInit) {
					val methodName = clazz.staticInitMethod!!.getHaxeMethodName(program)
					line("$methodName();")
				}
			}
		}

		output.add(clazz.name to Indenter.gen {
			line("package ${getHaxeGeneratedFqPackage(clazz.name)};")

			if (isAbstract) line("// ABSTRACT")
			var declaration = "$classType $simpleClassName"
			if (isInterface) {
				if (clazz.implementing.isNotEmpty()) declaration += getInterfaceList("extends")
			} else {
				if (clazz.extending != null && clazz.name.fqname != "java.lang.Object") declaration += " extends ${getHaxeClassFqName(program, clazz.extending!!)}"
				if (clazz.implementing.isNotEmpty()) declaration += getInterfaceList("implements")
			}

			line(declaration) {
				if (!isInterface) {
					line("public function new()") {
						line(if (isRootObject) "" else "super();")
						if (INIT_MODE == InitMode.LAZY) {
							line("__hx_static__init__();")
						}
					}
				}
				val nativeImports = mappings.getClassMapping(clazz.ref)?.nativeImports ?: listOf<String>()
				val mappingNativeMembers = (mappings.getClassMapping(clazz.ref)?.nativeMembers ?: listOf<String>())
				val haxeNativeMembers = clazz.resolveAnnotation(HaxeAddMembers::value)?.toList() ?: listOf()
				val nativeMembers = mappingNativeMembers + haxeNativeMembers

				for (member in nativeMembers) line(member)

				if (!isInterface) {
					for (field in clazz.fields) {
						writeField(this, field, isInterface)
					}
				}

				for (method in clazz.methods) {
					if (isInterface && method.isStatic) continue
					writeMethod(this, method, isInterface)
				}

				if (!isInterface) {
					//println(clazz.fqname + " -> " + program.getAllInterfaces(clazz))
					val isFunctionType = program.isImplementing(clazz, "all.core.AllFunction")

					if (isFunctionType) {
						val executeFirst = clazz.methodsByName["execute"]!!.first()
						line("public const _execute:Function = ${executeFirst.ref.getHaxeMethodName(program)};")
					}
				}

				/*
				if (isNormalClass) {
					val override = if (isRootObject) " " else "override "
					line("$override public function toString():String { return HaxeNatives.toNativeString(this.toString__Ljava_lang_String_()); }")
				}
				*/
				if (isRootObject) {
					line("public function toString():String { return HaxeNatives.toNativeString(this.toString__Ljava_lang_String_()); }")
				}

				if (!isInterface) {
					line(addClassInit(clazz))
				}
			}

			//if (isInterfaceWithStaticMembers) {
			if (isInterface) {
				line("class ${simpleClassName}_IFields") {
					line("public function new() {}")
					for (field in clazz.fields) writeField(this, field, isInterface = false)

					for (method in clazz.methods.filter { it.isStatic }) writeMethod(this, method, isInterface = false)

					line(addClassInit(clazz))
				}
			}
		})

		return ClassResult(output)
	}

	fun escapeConstant(value: Any?): String = when (value) {
		null -> "null"
		is Boolean -> if (value) "true" else "false"
		is String -> "HaxeNatives.str(\"" + value.escape() + "\")"
		is Short -> "$value"
		is Char -> "$value"
		is Int -> "$value"
		is Byte -> "$value"
		is Long -> "haxe.Int64.make(${((value ushr 32) and 0xFFFFFFFF).toInt()}, ${((value ushr 0) and 0xFFFFFFFF).toInt()})"
		is Float -> escapeConstant(value.toDouble())
		is Double -> if (value.isInfinite()) {
			if (value < 0) {
				"Math.NEGATIVE_INFINITY"
			} else {
				"Math.POSITIVE_INFINITY"
			}
		} else if (value.isNaN()) {
			"Math.NaN"
		} else {
			"$value"
		}
		else -> throw NotImplementedError("Literal of type $value")
	}

	enum class TypeKind { TYPETAG, NEW, CAST }

	fun AstType.getTypeTag(program: AstProgram): FqName {
		return this.getHaxeType(program, TypeKind.TYPETAG)
	}

	fun AstType.getHaxeType(program: AstProgram, typeKind: TypeKind): FqName {
		val type = this

		return FqName(when (type) {
			is AstType.NULL -> "Dynamic"
			is AstType.VOID -> "Void"
			is AstType.BOOL -> "Bool"
			is AstType.INT, is AstType.SHORT, is AstType.CHAR, is AstType.BYTE -> "Int"
			is AstType.FLOAT, is AstType.DOUBLE -> "Float"
			is AstType.LONG -> "haxe.Int64"
			is AstType.REF -> {
				val typeName = type.name
				if (mappings.hasClassReplacement(typeName)) {
					val replacement = mappings.getClassReplacement(typeName)!!
					when (typeKind) {
						TypeKind.TYPETAG -> replacement.typeTag
						TypeKind.NEW -> replacement.importNew
						TypeKind.CAST -> replacement.importNew
					}
				} else {
					if (mappings.isAdaptorSet(typeName.fqname)) {
						typeName.fqname
					} else {
						program[typeName].nativeName ?: getHaxeClassFqName(program, typeName)
					}
				}
			}
			is AstType.ARRAY -> when (type.element) {
				is AstType.BOOL -> "HaxeByteArray"
				is AstType.BYTE -> "HaxeByteArray"
				is AstType.CHAR -> "HaxeShortArray"
				is AstType.SHORT -> "HaxeShortArray"
				is AstType.INT -> "HaxeIntArray"
				is AstType.LONG -> "HaxeLongArray"
				is AstType.FLOAT -> "HaxeFloatArray"
				is AstType.DOUBLE -> "HaxeDoubleArray"
				else -> "HaxeArray"
			}
			else -> throw RuntimeException("Not supported haxe type $this")
		})
	}

	val AstLocal.haxeName: String get() = this.name.replace('$', '_')

	fun AstBody.gen(program: AstProgram, method: AstMethod, clazz: AstClass): Indenter {
		val body = this
		return Indenter.gen {
			for (local in body.locals) {
				addTypeReference(local.type)
				line("var ${local.haxeName}: ${local.type.getHaxeType(program, TypeKind.TYPETAG)} = ${local.type.haxeDefault};")
			}
			if (body.traps.isNotEmpty()) {
				line("var __exception__:Dynamic = null;")
			}
			for (field in method.dependencies.getFields2(program).filter { it.isStatic }) {
				val clazz = field.getContainingClass2(program)
				if (clazz.isInterface) {

				} else {
				}
			}

			val mutableBody = MutableBody(method)
			val bodyContent = body.stm.gen(program, clazz, mutableBody)

			if (INIT_MODE == InitMode.LAZY) {
				for (clazzRef in mutableBody.classes) {
					line(getHaxeClassStaticInit(program, clazzRef))
				}
			}
			line(bodyContent)
		}
	}

	fun getStaticFieldText(program: AstProgram, field: AstFieldRef): String {
		val prefix = getHaxeClassFqNameInt(program, field.classRef.name)
		return "$prefix.${getHaxeFieldName(program, field)}"
	}

	private fun getHaxeClassFqNameInt(program: AstProgram, name: FqName): String {
		val clazz = program[name]
		val simpleName = getHaxeGeneratedSimpleClassName(name)
		val suffix = if (clazz.isInterface) ".${simpleName}_IFields" else ""
		return getHaxeClassFqName(program, clazz.name) + "$suffix"
	}

	private fun getHaxeClassStaticInit(program: AstProgram, classRef: AstClassRef): String {
		return "${getHaxeClassFqNameInt(program, classRef.name)}.__hx_static__init__();"
	}

	class MutableBody(
		val method: AstMethod
	) {
		val classes = linkedSetOf<AstClassRef>()
		fun initClassRef(classRef: AstClassRef) {
			classes.add(classRef)
		}
	}

	fun AstStm.gen(program: AstProgram, clazz: AstClass, mutableBody: MutableBody): Indenter {
		val stm = this
		return Indenter.gen {
			when (stm) {
				is AstStm.NOP -> Unit
				is AstStm.IF -> {
					line("if (${stm.cond.gen(program, clazz, stm, mutableBody)})") { line(stm.strue.gen(program, clazz, mutableBody)) }
					if (stm.sfalse != null) {
						line("else") { line(stm.sfalse!!.gen(program, clazz, mutableBody)) }
					}
				}
				is AstStm.RETURN -> {
					if (stm.retval != null) {
						line("return cast ${stm.retval!!.gen(program, clazz, stm, mutableBody)};")
					} else {
						line("return;")
					}
				}
				is AstStm.SET -> {
					val localType = stm.local.type
					val exprType = stm.expr.type
					val adaptor = if (localType != exprType) mappings.getClassAdaptor(exprType, localType) else null
					if (adaptor != null) {
						addTypeReference(AstType.REF(adaptor.adaptor))
						line("${stm.local.haxeName} = new ${adaptor.adaptor}(${stm.expr.gen(program, clazz, stm, mutableBody)});")
					} else {
						val expr = stm.expr.gen(program, clazz, stm, mutableBody)
						//line("${stm.local.haxeName} = cast $expr;")
						//if (clazz.fqname == "java.lang.Float" && mutableBody.method.name == "intValue") {
						//	println("processing Float.intValue")
						//}
						if (stm.local.type == AstType.INT) {
							line("${stm.local.haxeName} = cast(cast ($expr) | cast(0));") // @TODO: Shouldn't do this! Investigate!
						} else {
							line("${stm.local.haxeName} = cast ($expr);")
						}
					}
				}
				is AstStm.SET_NEW_WITH_CONSTRUCTOR -> {
					val newClazz = program[stm.target.name]
					//val mapping = mappings.getClassMapping(newClazz)
					addTypeReference(stm.target)
					val commaArgs = stm.args.map { it.gen(program, clazz, stm, mutableBody) }.joinToString(", ")
					val className = stm.target.getHaxeType(program, TypeKind.NEW)

					if (newClazz.nativeName != null) {
						line("${stm.local.haxeName} = new $className($commaArgs);")
					} else {
						val methodInline = mappings.getFunctionInline(stm.method)
						if (methodInline != null) {
							line("${stm.local.haxeName} = ${methodInline.replacement.replace("@args", commaArgs)};")
						} else {
							line("${stm.local.haxeName} = new $className();")
							line("${stm.local.haxeName}.${stm.method.getHaxeMethodName(program)}($commaArgs);")
						}
					}
				}
				is AstStm.SET_ARRAY -> line("${stm.local.haxeName}.set(${stm.index.gen(program, clazz, stm, mutableBody)}, ${stm.expr.gen(program, clazz, stm, mutableBody)});")
				is AstStm.SET_FIELD_STATIC -> {
					addTypeReference(stm.clazz)
					mutableBody.initClassRef(stm.field.classRef)
					line("${getStaticFieldText(program, stm.field)} = cast ${stm.expr.gen(program, clazz, stm, mutableBody)};")
				}
				is AstStm.SET_FIELD_INSTANCE -> line("${stm.left.gen(program, clazz, stm, mutableBody)}.${getHaxeFieldName(program, stm.field)} = cast ${stm.expr.gen(program, clazz, stm, mutableBody)};")
				is AstStm.STM_EXPR -> line("${stm.expr.gen(program, clazz, stm, mutableBody)};")
				is AstStm.STMS -> for (s in stm.stms) line(s.gen(program, clazz, mutableBody))
				is AstStm.STM_LABEL -> line("${stm.label.name}:;")
				is AstStm.BREAK -> line("break;")
				is AstStm.BREAK -> line("break;")
				is AstStm.CONTINUE -> line("continue;")
				is AstStm.WHILE -> {
					line("while (${stm.cond.gen(program, clazz, stm, mutableBody)})") {
						line(stm.iter.gen(program, clazz, mutableBody))
					}
				}
				is AstStm.SWITCH -> {
					line("switch (${stm.subject.gen(program, clazz, stm, mutableBody)})") {
						for (case in stm.cases) {
							val value = case.first
							val caseStm = case.second
							line("case $value:")
							indent {
								line(caseStm.gen(program, clazz, mutableBody))
							}
						}
						line("default:")
						indent {
							line(stm.default.gen(program, clazz, mutableBody))
						}
					}
				}
				is AstStm.TRY_CATCH -> {
					line("try") {
						line(stm.trystm.gen(program, clazz, mutableBody))
					}
					line("catch (__i__exception__: Dynamic)") {
						line("__exception__ = __i__exception__;")
						line(stm.catch.gen(program, clazz, mutableBody))
					}
				}
				is AstStm.THROW -> line("throw ${stm.value.gen(program, clazz, stm, mutableBody)};")
				is AstStm.RETHROW -> {
					line("""
						//#if js
						//if (untyped __js__('typeof haxe_CallStack !== "undefined"')) {
						//	untyped __js__('throw haxe_CallStack.lastException');
						//} else {
						//	throw __i__exception__;
						//}
						//#else
						throw __i__exception__;
						//#end
					""")
				}
				is AstStm.MONITOR_ENTER -> line("// MONITOR_ENTER")
				is AstStm.MONITOR_EXIT -> line("// MONITOR_EXIT")
				else -> throw RuntimeException("Unhandled statement $stm")
			}
		}
	}

	fun AstProgram.locateMethod(ref: AstMethodRef): AstMethod? {
		val methodClass = this[ref.containingClass]
		return methodClass.getMethod(ref.name, ref.desc)
	}

	fun AstExpr.gen(program: AstProgram, clazz: AstClass, stm: AstStm, mutableBody: MutableBody): String = when (this) {
		is AstExpr.THIS -> "this"
		is AstExpr.LITERAL -> escapeConstant(this.value)
		is AstExpr.PARAM -> "${this.argument.name}"
		is AstExpr.LOCAL -> "${this.local.haxeName}"
		is AstExpr.UNOP -> "${op.symbol}(" + right.gen(program, clazz, stm, mutableBody) + ")"
		is AstExpr.BINOP -> {
			val resultType = type
			val leftType = left.type
			val rightType = right.type
			var l = left.gen(program, clazz, stm, mutableBody)
			var r = right.gen(program, clazz, stm, mutableBody)
			val opSymbol = op.symbol

			val boolMap = mapOf(
				"^" to "!=",
				"&" to "&&",
				"|" to "||",
				"==" to "==",
				"!=" to "!="
			)

			// @TODO: do this better!
			if (((resultType == AstType.BOOL) || (leftType == AstType.BOOL) || (rightType == AstType.BOOL)) && op.symbol in boolMap) {
				"cast($l) ${boolMap[opSymbol]} cast($r)"
			} else if (resultType == AstType.INT && op.symbol == "/") {
				"Std.int($l / $r)"
			} else {
				when (opSymbol) {
					"lcmp", "cmp", "cmpl", "cmpg" -> "HaxeNatives.$opSymbol($l, $r)"
					else -> "$l $opSymbol $r"
				}
			}

		}
		is AstExpr.CALL_BASE -> {
			val refMethod = program.locateMethod(this.method) ?: throw InvalidOperationException("Can't find method: ${method} while generating ${clazz.name}")

			if (this is AstExpr.CALL_STATIC) {
				addTypeReference(this.clazz)
				mutableBody.initClassRef(this.clazz.classRef)
			}

			val replacement = mappings.getFunctionInline(this.method)
			val commaArgs = args.map { "cast " + it.gen(program, clazz, stm, mutableBody) }.joinToString(", ")

			// Calling a method on an array!!
			if (this is AstExpr.CALL_INSTANCE && this.obj.type is AstType.ARRAY) {
				val args = "${obj.gen(program, clazz, stm, mutableBody)}, $commaArgs".trim(',', ' ')
				"HaxeNatives.array${this.method.name.toUcFirst()}($args)"
			} else {
				val base = when (this) {
					is AstExpr.CALL_STATIC -> "${this.clazz.getHaxeType(program, TypeKind.NEW)}"
					is AstExpr.CALL_SUPER -> "super"
					is AstExpr.CALL_INSTANCE -> "${obj.gen(program, clazz, stm, mutableBody)}"
					else -> throw InvalidOperationException("Unexpected")
				}

				if (replacement != null) {
					replacement.replacement.replace("@obj", base).replace("@args", commaArgs)
				} else if (refMethod.getterField != null) {
					if (refMethod.getterField!!.contains('$')) {
						refMethod.getterField!!.replace("\$", base);
					} else {
						"$base.${refMethod.getterField}"
					}

				} else if (refMethod.setterField != null) {
					"$base.${refMethod.setterField} = $commaArgs"
				} else {
					"$base.${method.getHaxeMethodName(program)}($commaArgs)"
				}
			}
		}
		is AstExpr.INSTANCE_FIELD_ACCESS -> {
			"${this.expr.gen(program, clazz, stm, mutableBody)}.${getHaxeFieldName(program, field)}"
		}
		is AstExpr.STATIC_FIELD_ACCESS -> {
			addTypeReference(clazzName)
			mutableBody.initClassRef(field.classRef)
			"${getStaticFieldText(program, field)}"
		}
		is AstExpr.ARRAY_LENGTH -> "${array.gen(program, clazz, stm, mutableBody)}.length"
		is AstExpr.ARRAY_ACCESS -> "${array.gen(program, clazz, stm, mutableBody)}.get(${index.gen(program, clazz, stm, mutableBody)})"
		is AstExpr.CAST -> {
			val e = expr.gen(program, clazz, stm, mutableBody)
			addTypeReference(from)
			addTypeReference(to)
			if (from == to) {
				"$e"
			} else {
				when (from) {
					// @TODO: Should we treat bool as ints?
					is AstType.BOOL -> {
						when (to) {
							is AstType.LONG -> "HaxeNatives.intToLong($e ? 1 : 0)"
							is AstType.INT -> "($e ? 1 : 0)"
							is AstType.BOOL -> "($e)"
							is AstType.CHAR -> "(($e) ? 1 : 0)"
							is AstType.SHORT -> "(($e) ? 1 : 0)"
							is AstType.BYTE -> "(($e) ? 1 : 0)"
							is AstType.FLOAT, is AstType.DOUBLE -> "(($e) ? 1.0 : 0.0)"
							else -> throw NotImplementedError("Unhandled conversion $from -> $to")
						}
					}
					is AstType.INT, is AstType.CHAR, is AstType.SHORT, is AstType.BYTE -> {
						when (to) {
							is AstType.LONG -> "HaxeNatives.intToLong($e)"
							is AstType.INT -> "($e)"
							is AstType.BOOL -> "(($e) != 0)"
							is AstType.CHAR -> "(($e) & 0xFFFF)"
							is AstType.SHORT -> "((($e) << 16) >> 16)"
							is AstType.BYTE -> "((($e) << 24) >> 24)"
							is AstType.FLOAT, is AstType.DOUBLE -> "($e)"
							else -> throw NotImplementedError("Unhandled conversion $from -> $to")
						}
					}
					is AstType.DOUBLE, is AstType.FLOAT -> {
						when (to) {
							is AstType.LONG -> "HaxeNatives.floatToLong($e)"
							is AstType.INT -> "Std.int($e)"
							is AstType.BOOL -> "(($e) != 0)"
							is AstType.CHAR -> "(($e) & 0xFFFF)"
							is AstType.SHORT -> "((($e) << 16) >> 16)"
							is AstType.BYTE -> "((($e) << 24) >> 24)"
							is AstType.FLOAT, is AstType.DOUBLE -> "($e)"
							else -> throw NotImplementedError("Unhandled conversion $from -> $to")
						}
					}
					is AstType.LONG -> {
						when (to) {
							is AstType.LONG -> "$e"
							is AstType.INT -> "($e).low"
							is AstType.BOOL -> "(($e).low != 0)"
							is AstType.CHAR -> "(($e).low & 0xFFFF)"
							is AstType.SHORT -> "((($e).low << 16) >> 16)"
							is AstType.BYTE -> "((($e).low << 24) >> 24)"
							is AstType.FLOAT, is AstType.DOUBLE -> "HaxeNatives.longToFloat($e)"
							else -> throw NotImplementedError("Unhandled conversion $from -> $to")
						}
					}
					is AstType.REF, is AstType.ARRAY -> {
						when (to) {
							AstType.REF("all.core.AllFunction") -> "(HaxeNatives.getFunction($e))"
							else -> "HaxeNatives.cast2($e, ${to.getHaxeType(program, TypeKind.CAST)})"
						}
					}
					is AstType.NULL -> "$e"
					else -> throw NotImplementedError("Unhandled conversion $from -> $to")
				}
			}
		}
		is AstExpr.NEW -> {
			addTypeReference(target)
			val className = target.getHaxeType(program, TypeKind.NEW)
			"new $className()"
		}
		is AstExpr.INSTANCE_OF -> {
			addTypeReference(checkType)
			"Std.is(${expr.gen(program, clazz, stm, mutableBody)}, ${checkType.getHaxeType(program, TypeKind.CAST)})"
		}
		is AstExpr.NEW_ARRAY -> {
			addTypeReference(type.element)
			when (counts.size) {
				1 -> "new ${type.getHaxeType(program, TypeKind.NEW)}(${counts[0].gen(program, clazz, stm, mutableBody)})"
				else -> throw NotImplementedError("Not implemented multidimensional arrays")
			}
		}
		is AstExpr.CLASS_CONSTANT -> "HaxeNatives.resolveClass(${classType.mangle().quote()})"
		is AstExpr.CAUGHT_EXCEPTION -> "cast __exception__"
		else -> throw NotImplementedError("Unhandled expression $this")
	}

	//val FqName.as3Fqname: String get() = this.fqname
	fun AstMethod.getHaxeMethodName(program: AstProgram): String = this.ref.getHaxeMethodName(program)

	fun AstMethodRef.getHaxeMethodName(program: AstProgram): String {
		val method = this
		val realmethod = program[method]
		if (realmethod.nativeMethod != null) {
			return realmethod.nativeMethod!!
		} else {
			return "${name}${this.desc}".map {
				if (it.isLetterOrDigit()) "$it" else if (it == '.' || it == '/') "_" else "_"
			}.joinToString("")
		}
	}
}