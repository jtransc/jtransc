package com.jtransc.gen.as3

import com.jtransc.ast.*
import com.jtransc.env.OS
import com.jtransc.error.InvalidOperationException
import com.jtransc.gen.GenTarget
import com.jtransc.gen.GenTargetDescriptor
import com.jtransc.gen.GenTargetInfo
import com.jtransc.gen.GenTargetProcessor
import com.jtransc.io.ProcessResult2
import com.jtransc.io.ProcessUtils
import com.jtransc.lang.getResourceAsString
import com.jtransc.text.Indenter
import com.jtransc.text.escape
import com.jtransc.text.toUcFirst
import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.SyncVfsFile
import java.io.File

object As3GenDescriptor : GenTargetDescriptor() {
	override val name = "as3"
	override val longName = "Action Script 3"
	override val sourceExtension = "as"
	override val outputExtension = "swf"
	override val extraLibraries = listOf("com.jtransc:jtransc-rt-as3:0.0.1")
	override val extraClasses = listOf<String>()
	override fun getGenerator() = GenAs3
}

object GenAs3 : GenTarget {
	val mappings = As3Mappings()

	override val runningAvailable: Boolean = true

	private val AstType.as3Default: String get() = when (this) {
		is AstType.BOOL -> "false"
		is AstType.INT, is AstType.SHORT, is AstType.CHAR, is AstType.BYTE -> "0"
		is AstType.LONG -> "all.as3.Long.ZERO"
		is AstType.FLOAT, is AstType.DOUBLE -> "0.0"
		is AstType.REF, is AstType.ARRAY, is AstType.NULL -> "null"
		else -> throw RuntimeException("Not supported as3 type $this")
	}

	private val AstVisibility.as3: String get() = "public"
	/*
	private val AstVisibility.as3: String get() = when (this) {
		AstVisibility.PUBLIC -> "public"
		AstVisibility.PROTECTED -> "protected"
		AstVisibility.PRIVATE -> "private"
	}
	*/

	private fun getAs3FilePath(name: FqName): String {
		return getAs3GeneratedFqName(name).fqname.replace('.', '/') + ".as"
	}

	private fun getAs3GeneratedFqPackage(name: FqName): String {
		return name.packageParts.map {
			if (it in AS3Keywords) "$it\$" else it
		}.joinToString(".")
	}

	private fun getAs3GeneratedFqName(name: FqName): FqName {
		return FqName(getAs3GeneratedFqPackage(name), getAs3GeneratedSimpleClassName(name))
	}

	private fun getAs3GeneratedSimpleClassName(name: FqName): String {
		return "${name.simpleName}\$"
	}

	private fun getAs3ClassFqName(program: AstProgram, name: FqName): String {
		val clazz = program[name]
		if (clazz.isNative) {
			return "${clazz.nativeName}"
		} else {
			return getAs3GeneratedFqName(name).fqname
		}
	}

	private fun getAs3FieldName(program: AstProgram, field: AstField): String = getAs3FieldName(program, field.ref)

	private fun getAs3FieldName(program: AstProgram, field: AstFieldRef): String {
		val fieldName = field.name
		return if (fieldName in AS3Keywords) {
			"$fieldName\$"
		} else {
			fieldName
		}
	}

	fun compileAndRun(program: AstProgram, outputFolder: String) {
		//compileAndRunRedTamarin(program, outputFolder)
		compileAndRunFlash(program, outputFolder)

	}


	val AIRSDK by lazy { AirSdk.AIRSDK }

	fun compileRunAndGetOutput(program: AstProgram, outputFolder: String): List<String> {
		val srcFolder = LocalVfs(outputFolder)
		val info = _write(program, AstFeatures(), srcFolder, As3Runtime.ADL, AS3Features)

		//println("Running: -optimize=true ${info.entryPointFile}")
		if (ProcessUtils.runAndRedirect(srcFolder.realfile, "java", listOf("-Dflexlib=$AIRSDK/frameworks", "-jar", "$AIRSDK/lib/mxmlc-cli.jar", "+configname=air", "-optimize=true", "-compress=false", "--source-path=.", "--output=program.swf", info.entryPointFile)).success) {
			val result = ProcessUtils.runAndReadStderr(srcFolder.realfile, "$AIRSDK/bin/adl.exe", listOf("program.xml")).output
			val result2 = result.replace("\r\r", "").trim()
			return result2.lines()
		}
		return listOf("<error>")
	}

	fun compileAndRunFlash(program: AstProgram, outputFolder: String) {
		val srcFolder = LocalVfs(outputFolder)
		val info = _write(program, AstFeatures(), srcFolder, As3Runtime.ADL, AS3Features)

		println("Running: -optimize=true ${info.entryPointFile}")
		if (ProcessUtils.runAndRedirect(srcFolder.realfile, "java", listOf("-Dflexlib=$AIRSDK/frameworks", "-jar", "$AIRSDK/lib/mxmlc-cli.jar", "+configname=air", "-optimize=true", "-compress=false", "--source-path=.", "--output=program.swf", info.entryPointFile)).success) {
			val out = ProcessUtils.runAndReadStderr(srcFolder.realfile, "$AIRSDK/bin/adl.exe", listOf("program.xml")).output
			//println(out)
			ProcessUtils.runAndRedirect(srcFolder.realfile, "$AIRSDK/bin/adl.exe", listOf("program.xml"))
			//ProcessUtils.runAndRedirect(srcFolder.realfile, "c:/projects/all/redtamarin/avmshell-debug-debugger.exe", listOf("program.swf", "../../redtamarin/Test.abc"))

			//println("success!")
		} else {
			println("failed!")
		}
	}

	class MutableProgramInfo {
		val initializingImports = arrayListOf<String>()
		val initializingCalls = arrayListOf<String>()
	}

	enum class As3Runtime { ADL, FLASH }

	private fun _write(program: AstProgram, features: AstFeatures, vfs: SyncVfsFile, runtime: As3Runtime, featureSet: Set<AstFeature>): ProgramInfo {
		val mutableInfo = MutableProgramInfo()
		for (clazz in program.classes.filter { !it.isNative }) {
			val result = clazz.gen(program, features, mutableInfo, featureSet)
			for (file in result.files) {
				val (clazzName, content) = file

				vfs[getAs3FilePath(clazzName)] = content.toString()
			}
		}

		for (file in AS3CopyFiles) {
			val resource = "/src/$file"
			//println("Copying from resources '$resource' to output '$file'...")
			vfs[file] = javaClass.getResourceAsString(resource)
		}

		val mainClassFq = program.entrypoint
		val mainClass = getAs3ClassFqName(program, mainClassFq)
		val mainClassWithMain = "$mainClass.main\$\$Ljava_lang_String\$\$V"

		val entryPointClass = FqName(mainClassFq.fqname + "\$EntryPoint")
		val entryPointFilePath = getAs3FilePath(entryPointClass)
		val entryPointFile = getAs3FilePath(entryPointClass)
		val entryPointFqName = getAs3GeneratedFqName(entryPointClass)
		val entryPointSimpleName = getAs3GeneratedSimpleClassName(entryPointClass)
		val entryPointPackage = entryPointFqName.packagePath

		//println(getAs3FilePath(entryPointClass))


		vfs[entryPointFilePath] = Indenter.gen {
			line("package $entryPointPackage") {
				line("import flash.display.Sprite;")
				line("import flash.utils.setTimeout;")
				line("import all.native.As3Natives;")
				line("import all.native.As3Output;")
				line("import all.as3.As3Lib\$;")
				line("import $mainClass;")
				for (init in mutableInfo.initializingImports) line("import $init;")
				line("[SWF(frameRate='24', width='640', height='480', backgroundColor='white')]")
				line("public class $entryPointSimpleName extends Sprite") {
					line("public function $entryPointSimpleName() { setTimeout(main, 0); }")
					line("private function main():void") {
						line("trace('all-as3');")
						line("var sprite:Sprite = new Sprite();")
						line("addChild(sprite);")
						line("addChild(As3Output.outputStream);")
						line("all.as3.As3Lib\$.stage = stage;")
						line("all.as3.As3Lib\$.root = sprite;")
						for (init in mutableInfo.initializingCalls) line("$init")
						line("$mainClassWithMain([]);")
						line("all.native.As3Natives.exitOnAdl();");
					}
				}
			}
		}.toString()

		return ProgramInfo(entryPointFilePath, vfs)
	}

	data class ProgramInfo(val entryPointFile: String, val vfs: SyncVfsFile)


	//override fun getProcessor(program: AstProgram, outputFile: String, settings: AstBuildSettings, subtarget:String): GenTargetProcessor {
	override fun getProcessor(tinfo: GenTargetInfo): GenTargetProcessor {
		val outputFile2 = File(tinfo.outputFile).absolutePath
		//val tempdir = System.getProperty("java.io.tmpdir")
		val tempdir = tinfo.targetDirectory
		var pinfo: ProgramInfo? = null

		File("$tempdir/all-as3").mkdirs()
		val srcFolder = LocalVfs("$tempdir/all-as3").ensuredir()

		println("Temporal as3 files: $tempdir/all-as3")

		return object : GenTargetProcessor {
			override fun buildSource() {
				pinfo = _write(tinfo.program, AstFeatures(), srcFolder, As3Runtime.ADL, AS3Features)
			}

			override fun compile(): Boolean {
				if (pinfo == null) throw InvalidOperationException("Must call .buildSource first")
				File(outputFile2).delete()
				File(outputFile2 + ".xml").delete()
				println("as3.build source path: " + srcFolder.realpathOS)

				val xmlFile = "$outputFile2.xml"

				val baseName = File(outputFile2).name

				File(xmlFile).writeText("""<?xml version="1.0" encoding="utf-8" ?>
				<application xmlns="http://ns.adobe.com/air/application/18.0">
				    <id>program</id>
				    <version>0.0</version>
				    <versionNumber>0</versionNumber>
				    <filename>program</filename>
				    <initialWindow>
				        <content>$baseName</content>
				        <visible>false</visible>
				    </initialWindow>
				</application>
			"""
				)

				val jarArgs = listOf("-Dflexlib=$AIRSDK/frameworks", "-jar", "$AIRSDK/lib/mxmlc-cli.jar")
				val buildArgs = listOf(
					"+configname=air",
					"--source-path=.",
					"--output=$outputFile2",
						pinfo!!.entryPointFile
				)
				val releaseArgs = if (tinfo.settings.release) listOf("-optimize=true", "-compress=true") else listOf("-optimize=false", "-compress=false")

				//println("Running: -optimize=true ${info.entryPointFile}")
				return ProcessUtils.runAndRedirect(srcFolder.realfile, "java", jarArgs + releaseArgs + buildArgs).success
			}

			override fun run(redirect: Boolean): ProcessResult2 {
				val outputFile2 = File(tinfo.outputFile).absolutePath
				if (!File(outputFile2).exists()) {
					return ProcessResult2("file $outputFile2 doesn't exist", -1)
				}
				println("run: $outputFile2")
				val parentDir = File(outputFile2).parentFile
				val xmlFile = "$outputFile2.xml"

				return ProcessUtils.run(
					parentDir,
					if (OS.isWindows) "$AIRSDK/bin/adl.exe" else "$AIRSDK/bin/adl",
					listOf(xmlFile),
					redirect = redirect
				)
			}

		}
	}

	val customImports = hashSetOf<String>()
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

	fun AstClass.gen(program: AstProgram, features: AstFeatures, programInfo: MutableProgramInfo, featureSet: Set<AstFeature>): ClassResult {
		val clazz = this
		val isRootObject = clazz.name.fqname == "java.lang.Object"
		val isInterface = clazz.isInterface
		val isAbstract = (clazz.classType == AstClassType.ABSTRACT)
		val isNormalClass = (clazz.classType == AstClassType.CLASS)
		val classType = if (isInterface) "interface" else "class"
		val implementingString = clazz.implementing.map { getAs3ClassFqName(program, it) }.joinToString(", ")
		val isInterfaceWithStaticMembers = isInterface && clazz.fields.any { it.isStatic }
		//val isInterfaceWithStaticFields = clazz.name.withSimpleName(clazz.name.simpleName + "\$StaticMembers")
		_usedDependencies.clear()
		customImports.clear()
		customImports.add("all.native.As3Natives")
		customImports.add("all.as3.Long")
		if (!clazz.extending?.fqname.isNullOrEmpty()) {
			addTypeReference(AstType.REF(clazz.extending!!))
		}
		for (impl in clazz.implementing) {
			addTypeReference(AstType.REF(impl))
		}
		val interfaceClassName = clazz.name.append("\$Fields");

		var output = arrayListOf<Pair<FqName, Indenter>>()

		fun writeField(indenter: Indenter, field: AstField) {
			val static = if (field.isStatic) "static " else ""
			val visibility = if (isInterface) " " else field.visibility.as3
			val fieldType = field.type
			addTypeReference(fieldType)
			val defaultValue: Any? = if (field.hasConstantValue) {
				field.constantValue
			} else {
				fieldType.as3Default
			}
			indenter.line("$static$visibility var ${getAs3FieldName(program, field)}:${fieldType.getAs3Type(program, TypeKind.TYPETAG)} = ${escapeConstant(defaultValue)};")
		}

		fun writeMethod(indenter: Indenter, method: AstMethod) {
			val static = if (method.isStatic) "static " else ""
			val visibility = if (isInterface) " " else method.visibility.as3
			addTypeReference(method.methodType)
			val margs = method.methodType.args.map { it.name + ":" + it.type.getAs3Type(program, TypeKind.TYPETAG) }
			var override = if (method.isOverriding) "override " else ""
			val decl = "$static $visibility $override function ${method.ref.getAs3MethodName(program)}(${margs.joinToString(", ")}):${method.methodType.ret.getAs3Type(program, TypeKind.TYPETAG)}".trim()

			if (isInterface) {
				if (!method.isImplementing) {
					indenter.line("$decl;")
				}
			} else {
				val body = mappings.getBody(method.ref)
				if (method.body != null && body == null) {
					indenter.line(decl) {
						indenter.line(features.apply(method.body!!, featureSet).gen(program, clazz))
					}
				} else {
					val body2 = body ?: "throw new Error(\"Native or abstract: ${clazz.name}.${method.name} :: ${method.desc} :: ${method.isExtraAdded}\");"
					indenter.line("$decl { $body2 }")
				}
			}
		}

		if (isInterfaceWithStaticMembers) {
			//println("instance with static members!")
			output.add(interfaceClassName to Indenter.gen {
				line("package ${getAs3GeneratedFqPackage(interfaceClassName)}") {
					line("public class ${interfaceClassName.fqname}") {
						for (field in clazz.fields) writeField(this, field)

						for (method in clazz.methods.filter { it.isStatic }) writeMethod(this, method)

						if ("<clinit>" in clazz.methodsByName) {
							programInfo.initializingImports.add(getAs3ClassFqName(program, interfaceClassName))
							programInfo.initializingCalls.add(getAs3ClassFqName(program, interfaceClassName) + ".\$clinit\$\$\$V();")
						}
					}
				}
			})
		}

		output.add(clazz.name to Indenter.gen {
			line("package ${getAs3GeneratedFqPackage(clazz.name)}") {
				linedeferred {
					for (import in _usedDependencies) {
						val importType = import.getAs3Type(program, TypeKind.IMPORT).fqname
						if (importType != "*") line("import $importType;")
					}
					for (import in customImports) line("import $import;")
				}

				if (isAbstract) line("// ABSTRACT")
				var declaration = "public $classType ${getAs3GeneratedSimpleClassName(clazz.name)}"
				if (isInterface) {
					if (clazz.implementing.isNotEmpty()) declaration += " extends $implementingString"
				} else {
					if (clazz.extending != null && clazz.name.fqname != "java.lang.Object") declaration += " extends ${getAs3ClassFqName(program, clazz.extending!!)}"
					if (clazz.implementing.isNotEmpty()) declaration += " implements $implementingString"
				}

				line(declaration) {
					val nativeImports = mappings.getClassMapping(clazz.ref)?.nativeImports
					val nativeMembers = mappings.getClassMapping(clazz.ref)?.nativeMembers
					if (nativeImports != null) {
						customImports.addAll(nativeImports)
					}
					if (nativeMembers != null) {
						for (member in nativeMembers) line(member)
					}

					if (!isInterface) {
						for (field in clazz.fields) writeField(this, field)
					}

					for (method in clazz.methods) {
						if (isInterface && method.isStatic) continue
						writeMethod(this, method)
					}

					if (!isInterface) {
						//println(clazz.fqname + " -> " + program.getAllInterfaces(clazz))
						val isFunctionType = program.isImplementing(clazz, "all.core.AllFunction")

						if (isFunctionType) {
							val executeFirst = clazz.methodsByName["execute"]!!.first()
							line("public const \$execute:Function = ${executeFirst.ref.getAs3MethodName(program)};")
						}
					}

					if (isNormalClass) {
						val override = if (isRootObject) " " else "override "
						line("$override public function toString():String { return this.toString\$\$Ljava_lang_String\$(); }")
					}

					if (!isInterface && ("<clinit>" in clazz.methodsByName)) {
						programInfo.initializingImports.add(getAs3ClassFqName(program, clazz.name))
						programInfo.initializingCalls.add(getAs3ClassFqName(program, clazz.name) + ".\$clinit\$\$\$V();")
					}
				}
			}
		})

		return ClassResult(output)
	}

	fun escapeConstant(value: Any?): String = if (value is String)
		value.escape()
	else if (value is Long)
		"all.as3.Long.value(${(value ushr 0) and 0xFFFFFFFF}, ${(value ushr 32) and 0xFFFFFFFF})"
	else
		"$value"

	enum class TypeKind { IMPORT, TYPETAG, NEW, CAST }

	fun AstType.getAs3Type(program: AstProgram, typeKind: TypeKind): FqName {
		val type = this

		return FqName(when (type) {
			is AstType.NULL -> "*"
			is AstType.VOID -> "void"
			is AstType.BOOL -> "Boolean"
			is AstType.INT, is AstType.SHORT, is AstType.CHAR, is AstType.BYTE -> "int"
			is AstType.LONG -> "all.as3.Long"
			is AstType.REF -> {
				val typeName = type.name
				if (mappings.hasClassReplacement(typeName)) {
					val replacement = mappings.getClassReplacement(typeName)!!
					when (typeKind) {
						TypeKind.TYPETAG -> replacement.typeTag
						TypeKind.IMPORT -> replacement.importNew
						TypeKind.NEW -> replacement.importNew
						TypeKind.CAST -> replacement.importNew
					}
				} else {
					if (mappings.isAdaptorSet(typeName.fqname)) {
						typeName.fqname
					} else {
						val clazz = program[typeName]
						if (clazz.nativeName != null) {
							clazz.nativeName!!
						} else {
							getAs3ClassFqName(program, typeName)
						}
					}
				}
			}
			is AstType.FLOAT, is AstType.DOUBLE -> "Number"
			is AstType.ARRAY -> {
				when (type.element) {
					is AstType.REF, is AstType.ARRAY -> "Array"
					else -> "Vector.<${type.element.getAs3Type(program, TypeKind.TYPETAG)}>"
				}
			}
			else -> throw RuntimeException("Not supported as3 type $this")
		})
	}

	fun AstBody.gen(program: AstProgram, clazz: AstClass): Indenter {
		val body = this
		return Indenter.gen {
			for (local in body.locals) {
				addTypeReference(local.type)
				//line("var ${local.name}: ${local.type.getAs3Type(program, TypeKind.TYPETAG)} = ${local.type.as3Default};")
				line("var ${local.name}: ${local.type.getAs3Type(program, TypeKind.TYPETAG)};")
			}
			line(body.stm.gen(program, clazz))
		}
	}

	fun getStaticFieldText(program: AstProgram, field: AstFieldRef): String {
		val fq = getAs3ClassFqName(program, field.containingTypeRef.name)
		return "$fq.${getAs3FieldName(program, field)}"
	}

	fun AstStm.gen(program: AstProgram, clazz: AstClass): Indenter {
		val stm = this
		return Indenter.gen {
			when (stm) {
				is AstStm.NOP -> {
				}
				is AstStm.IF_GOTO -> line("if (${stm.cond.gen(program, clazz, stm)}) goto ${stm.label.name};")
				is AstStm.IF -> {
					line("if (${stm.cond.gen(program, clazz, stm)})") { line(stm.strue.gen(program, clazz)) }
					if (stm.sfalse != null) {
						line("else") { line(stm.sfalse!!.gen(program, clazz)) }
					}
				}
				is AstStm.RETURN -> line("return ${stm.retval?.gen(program, clazz, stm) ?: ""};")
				is AstStm.SET -> {
					val localType = stm.local.type
					val exprType = stm.expr.type
					val adaptor = if (localType != exprType) mappings.getClassAdaptor(exprType, localType) else null
					if (adaptor != null) {
						addTypeReference(AstType.REF(adaptor.adaptor))
						line("${stm.local.name} = new ${adaptor.adaptor}(${stm.expr.gen(program, clazz, stm)});")
					} else {
						line("${stm.local.name} = ${stm.expr.gen(program, clazz, stm)};")
					}
				}
				is AstStm.SET_NEW_WITH_CONSTRUCTOR -> {
					val newClazz = program[stm.target.name]
					//val mapping = mappings.getClassMapping(newClazz)
					addTypeReference(stm.target)
					val commaArgs = stm.args.map { it.gen(program, clazz, stm) }.joinToString(", ")
					val className = stm.target.getAs3Type(program, TypeKind.NEW)

					if (newClazz.nativeName != null) {
						line("${stm.local.name} = new $className($commaArgs);")
					} else {
						val methodInline = mappings.getFunctionInline(stm.method)
						if (methodInline != null) {
							customImports.addAll(methodInline.imports)
							line("${stm.local.name} = ${methodInline.replacement.replace("@args", commaArgs)};")
						} else {
							line("${stm.local.name} = new $className();")
							line("${stm.local.name}.${stm.method.getAs3MethodName(program)}($commaArgs);")
						}
					}
				}
				is AstStm.SET_ARRAY -> line("${stm.local.name}[${stm.index.gen(program, clazz, stm)}] = ${stm.expr.gen(program, clazz, stm)};")
				is AstStm.SET_FIELD_STATIC -> {
					addTypeReference(stm.clazz)
					line("${getStaticFieldText(program, stm.field)} = ${stm.expr.gen(program, clazz, stm)};")
				}
				is AstStm.SET_FIELD_INSTANCE -> line("${stm.left.gen(program, clazz, stm)}.${getAs3FieldName(program, stm.field)} = ${stm.expr.gen(program, clazz, stm)};")
				is AstStm.STM_EXPR -> line("${stm.expr.gen(program, clazz, stm)};")
				is AstStm.STMS -> for (s in stm.stms) line(s.gen(program, clazz))
				is AstStm.STM_LABEL -> line("${stm.label.name}:;")
				is AstStm.GOTO -> line("goto ${stm.label.name};")
				is AstStm.BREAK -> line("break;")
				is AstStm.CONTINUE -> line("continue;")
				is AstStm.WHILE -> {
					line("while (${stm.cond.gen(program, clazz, stm)})") {
						line(stm.iter.gen(program, clazz))
					}
				}
				is AstStm.SWITCH_GOTO -> {
					line("switch (${stm.subject.gen(program, clazz, stm)})") {
						for (case in stm.cases) {
							val value = case.first
							val caseLabel = case.second
							line("case $value: goto ${caseLabel.name}")
						}
						line("default: goto ${stm.default.name}")
					}
				}
				is AstStm.SWITCH -> {
					line("switch (${stm.subject.gen(program, clazz, stm)})") {
						for (case in stm.cases) {
							val value = case.first
							val caseStm = case.second
							line("case $value:")
							indent {
								line(caseStm.gen(program, clazz))
							}
							line("break;")
						}
						line("default:")
						indent {
							line(stm.default.gen(program, clazz))
						}
						line("break;")
					}
				}
				is AstStm.TRY_CATCH -> {
					line("try") {
						line(stm.trystm.gen(program, clazz))
					}
					line("catch (e: *)") {
						line(stm.catch.gen(program, clazz))
					}
					/*
					for ((type, cstm) in stm.catches) {
						line("catch (e: ${type.getAs3Type(program, TypeKind.TYPETAG)})") {
							line(cstm.gen(program, clazz))
						}
					}
					*/
				}
				is AstStm.THROW -> line("throw ${stm.value.gen(program, clazz, stm)};")
				is AstStm.MONITOR_ENTER -> {
					line("// MONITOR_ENTER")
				}
				is AstStm.MONITOR_EXIT -> {
					line("// MONITOR_EXIT")
				}
				else -> throw RuntimeException("Unhandled statement $stm")
			}
		}
	}

	fun AstProgram.locateMethod(ref: AstMethodRef): AstMethod? {
		val methodClass = this[ref.containingClass]
		return methodClass.getMethod(ref.name, ref.desc)
	}

	fun AstExpr.gen(program: AstProgram, clazz: AstClass, stm: AstStm): String = when (this) {
		is AstExpr.THIS -> "this"
		is AstExpr.LITERAL -> {
			val value = this.value
			when (value) {
				null -> "null"
				is Boolean -> if (value) "true" else "false"
				is Byte -> "$value"
				is Short -> "$value"
				is Char -> "$value"
				is Int -> "$value"
				is Long -> "all.as3.Long.fromInt($value)"
				is Float -> "$value"
				is Double -> "$value"
				is String -> "\"" + value.escape() + "\""
				else -> throw NotImplementedError("Literal of type $value")
			}
		}
		is AstExpr.PARAM -> "${this.argument.name}"
		is AstExpr.LOCAL -> "${this.local.name}"
		is AstExpr.UNOP -> {
			val resultType = type
			val rightType = right.type
			val r = right.gen(program, clazz, stm)
			when (rightType) {
				is AstType.LONG -> "all.as3.Long.${op.str}($r)"
				else -> "${op.symbol}$r"
			}
		}
		is AstExpr.BINOP -> {
			val resultType = type
			val leftType = left.type
			val rightType = right.type
			var l = left.gen(program, clazz, stm)
			var r = right.gen(program, clazz, stm)

			// @TODO: do this better!
			if (((resultType == AstType.BOOL) || (leftType == AstType.BOOL) || (rightType == AstType.BOOL)) && op.symbol == "^") {
				"Boolean($l) != Boolean($r)"
			} else if (((resultType == AstType.BOOL) || (leftType == AstType.BOOL) || (rightType == AstType.BOOL)) && op.symbol == "&") {
				"Boolean($l) && Boolean($r)"
			} else if (((resultType == AstType.BOOL) || (leftType == AstType.BOOL) || (rightType == AstType.BOOL)) && op.symbol == "|") {
				"Boolean($l) || Boolean($r)"
			} else {
				when (leftType) {
					is AstType.LONG -> {
						if (leftType == AstType.INT) l = "all.as3.Long.fromInt($l)"
						if (rightType == AstType.INT) r = "all.as3.Long.fromInt($r)"
						"all.as3.Long.${op.str}($l, $r)"
					}
				// @TODO: Short, should truncate (<< 16 >> 16) or & 0x0000FFFF
					else -> {
						when (op.symbol) {
							"cmp", "cmpl", "cmpg" -> "all.native.As3Natives.${op.symbol}($l, $r)"
							else -> "$l ${op.symbol} $r"
						}
					}
				}
			}

		}
		is AstExpr.CALL_BASE -> {
			val refMethod = program.locateMethod(this.method) ?: throw InvalidOperationException("Can't find method: ${method} while generating ${clazz.name}")

			if (this is AstExpr.CALL_STATIC) {
				addTypeReference(this.clazz)
			}

			val replacement = mappings.getFunctionInline(this.method)
			val commaArgs = args.map { it.gen(program, clazz, stm) }.joinToString(", ")

			// Calling a method on an array!!
			if (this is AstExpr.CALL_INSTANCE && this.obj.type is AstType.ARRAY) {
				val args = "${obj.gen(program, clazz, stm)}, $commaArgs".trim(',', ' ')
				"all.native.As3Natives.array${this.method.name.toUcFirst()}($args)"
			} else {
				val base = when (this) {
					is AstExpr.CALL_STATIC -> "${this.clazz.getAs3Type(program, TypeKind.NEW)}"
					is AstExpr.CALL_SUPER -> "super"
					is AstExpr.CALL_INSTANCE -> "${obj.gen(program, clazz, stm)}"
					else -> throw InvalidOperationException("Unexpected")
				}

				if (replacement != null) {
					customImports.addAll(replacement.imports)
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
					"$base.${method.getAs3MethodName(program)}($commaArgs) /* $method */"
				}
			}
		}
		is AstExpr.INSTANCE_FIELD_ACCESS -> {
			"${this.expr.gen(program, clazz, stm)}.${getAs3FieldName(program, field)}"
		}
		is AstExpr.STATIC_FIELD_ACCESS -> {
			addTypeReference(clazzName)
			"${getStaticFieldText(program, field)}"
		}
		is AstExpr.ARRAY_LENGTH -> {
			"${array.gen(program, clazz, stm)}.length"
		}
		is AstExpr.ARRAY_ACCESS -> {
			"${array.gen(program, clazz, stm)}[${index.gen(program, clazz, stm)}]"
		}
		is AstExpr.CAST -> {
			val e = expr.gen(program, clazz, stm)
			addTypeReference(from)
			addTypeReference(to)
			if (from == to) {
				"$e"
			} else {
				when (from) {
					is AstType.INT, is AstType.CHAR, is AstType.SHORT, is AstType.BYTE -> {
						when (to) {
							is AstType.LONG -> "all.as3.Long.fromInt($e)"
							is AstType.INT -> "($e)"
							is AstType.BOOL -> "(($e) != 0)"
							is AstType.CHAR -> "(($e) & 0xFFFF)"
							is AstType.SHORT -> "((($e) << 16) >> 16)"
							is AstType.BYTE -> "((($e) << 24) >> 24)"
							is AstType.FLOAT, is AstType.DOUBLE -> "Number($e)"
							else -> throw NotImplementedError("Unhandled conversion $from -> $to")
						}
					}
					is AstType.DOUBLE, is AstType.FLOAT -> {
						when (to) {
							is AstType.LONG -> "all.as3.Long.fromNumber($e)"
							is AstType.INT -> "($e)"
							is AstType.BOOL -> "(($e) != 0)"
							is AstType.CHAR -> "(($e) & 0xFFFF)"
							is AstType.SHORT -> "((($e) << 16) >> 16)"
							is AstType.BYTE -> "((($e) << 24) >> 24)"
							is AstType.FLOAT, is AstType.DOUBLE -> "Number($e)"
							else -> throw NotImplementedError("Unhandled conversion $from -> $to")
						}
					}
					is AstType.LONG -> {
						when (to) {
							is AstType.LONG -> "$e"
							is AstType.INT -> "($e).toInt()"
							is AstType.BOOL -> "(($e).toInt() != 0)"
							is AstType.CHAR -> "(($e).toInt() & 0xFFFF)"
							is AstType.SHORT -> "((($e).toInt() << 16) >> 16)"
							is AstType.BYTE -> "((($e).toInt() << 24) >> 24)"
							is AstType.FLOAT, is AstType.DOUBLE -> "($e).toNumber()"
							else -> throw NotImplementedError("Unhandled conversion $from -> $to")
						}
					}
					is AstType.REF, is AstType.ARRAY -> {
						when (to) {
							AstType.OBJECT -> "$e"
							is AstType.REF, is AstType.ARRAY -> {
								when (to) {
									AstType.REF("all.core.AllFunction") -> "(all.native.As3Natives.getFunction($e))"
									else -> "(($e) as ${to.getAs3Type(program, TypeKind.CAST)})"
								}
							}
							else -> throw NotImplementedError("Unhandled conversion $from -> $to")
						}
					}
					is AstType.NULL -> "$e"
					else -> throw NotImplementedError("Unhandled conversion $from -> $to")
				}
			}
		}
		is AstExpr.NEW -> {
			addTypeReference(target)
			val className = target.getAs3Type(program, TypeKind.NEW)
			"new $className()"
		}
		is AstExpr.INSTANCE_OF -> {
			addTypeReference(checkType)
			"${expr.gen(program, clazz, stm)} is ${checkType.getAs3Type(program, TypeKind.CAST)}"
		}
		is AstExpr.NEW_ARRAY -> {
			addTypeReference(type.element)
			when (counts.size) {
				1 -> {
					"new ${type.getAs3Type(program, TypeKind.NEW)}(${counts[0].gen(program, clazz, stm)})"
				}
				else -> throw NotImplementedError("Not implemented multidimensional arrays")
			}
		}
		is AstExpr.CLASS_CONSTANT -> {
			val classType = this.classType
			when (classType) {
			//is AstType.REF -> "all.native.As3Natives.getJavaClass(${getAs3ClassFqName(program, classType.name)})"
				is AstType.REF -> "all.native.As3Natives.getJavaClass('${classType.name.fqname}')"
				is AstType.ARRAY -> "all.native.As3Natives.getJavaClassArray(${getAs3ClassFqName(program, (classType.element as AstType.REF).name)})"
				else -> throw NotImplementedError("Not implemented multidimensional arrays")
			}
		}
		is AstExpr.CAUGHT_EXCEPTION -> {
			"/*CAUGHT_EXCEPTION*/null"
		}
		else -> throw NotImplementedError("Unhandled expression $this")
	}

	//val FqName.as3Fqname: String get() = this.fqname
	fun AstMethodRef.getAs3MethodName(program: AstProgram): String {
		val method = this
		val realmethod = program[method]
		if (realmethod.nativeMethod != null) {
			return realmethod.nativeMethod!!
		} else {
			return "${name}${this.desc}".map {
				if (it.isLetterOrDigit()) "$it" else if (it == '.' || it == '/') "_" else "\$"
			}.joinToString("")
		}
	}
}