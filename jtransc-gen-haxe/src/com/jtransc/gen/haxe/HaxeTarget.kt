package com.jtransc.gen.haxe

import com.jtransc.ConfigOutputFile
import com.jtransc.ConfigSubtarget
import com.jtransc.ConfigTargetDirectory
import com.jtransc.JTranscVersion
import com.jtransc.annotation.JTranscKeep
import com.jtransc.annotation.haxe.*
import com.jtransc.ast.*
import com.jtransc.ast.feature.method.GotosFeature
import com.jtransc.ast.feature.method.SwitchFeature
import com.jtransc.ds.concatNotNull
import com.jtransc.ds.getOrPut2
import com.jtransc.ds.split
import com.jtransc.error.invalidOp
import com.jtransc.error.unexpected
import com.jtransc.gen.GenTargetDescriptor
import com.jtransc.gen.common.*
import com.jtransc.injector.Injector
import com.jtransc.injector.Singleton
import com.jtransc.io.ProcessResult2
import com.jtransc.io.ProcessUtils
import com.jtransc.lang.toBetterString
import com.jtransc.log.log
import com.jtransc.sourcemaps.Sourcemaps
import com.jtransc.template.Minitemplate
import com.jtransc.text.Indenter
import com.jtransc.text.quote
import com.jtransc.time.measureProcess
import com.jtransc.util.sortDependenciesSimple
import com.jtransc.vfs.*
import java.io.File
import java.util.*

//data class ConfigHaxeVfs(val vfs: SyncVfsFile)
data class ConfigHaxeAddSubtarget(val subtarget: HaxeAddSubtarget)

class HaxeTarget() : GenTargetDescriptor() {
	override val priority = 1000
	override val name = "haxe"
	override val longName = "Haxe"
	override val sourceExtension = "hx"
	override val outputExtension = "bin"
	override val extraLibraries = listOf<String>()
	override val extraClasses = listOf<String>()
	override val runningAvailable: Boolean = true

	override fun getGenerator(injector: Injector): CommonGenerator {
		val program = injector.get<AstProgram>()
		val configTargetDirectory = injector.get<ConfigTargetDirectory>()
		val configSubtarget = injector.get<ConfigSubtarget>()
		val settings = injector.get<AstBuildSettings>()
		val targetDirectory = configTargetDirectory.targetDirectory
		val outputFileBaseName = injector.get<ConfigOutputFile>().outputFileBaseName
		val targetFolder = LocalVfsEnsureDirs(File("$targetDirectory/jtransc-haxe"))

		val actualSubtargetName = configSubtarget.subtarget
		val availableHaxeSubtargets: List<HaxeAddSubtarget> = program.allAnnotations
			.map { it.toObject<HaxeAddSubtargetList>() ?: it.toObject<HaxeAddSubtarget>() }
			.flatMap {
				if (it == null) listOf() else when (it) {
					is HaxeAddSubtargetList -> it.value.toList()
					is HaxeAddSubtarget -> listOf(it)
					else -> listOf()
				}
			}
			.filterNotNull()

		injector.mapInstance(ConfigHaxeAddSubtarget(availableHaxeSubtargets.last { it.name == actualSubtargetName || actualSubtargetName in it.alias }))

		injector.mapInstance(ConfigSrcFolder(HaxeGenTools.getSrcFolder(targetDirectory)))
		injector.mapInstance(CommonGenFolders(settings.assets.map { LocalVfs(it) }))

		injector.mapInstance(ConfigTargetFolder(targetFolder))
		injector.mapInstance(ConfigOutputFile2(targetFolder[outputFileBaseName].realfile))

		return injector.get<HaxeGenerator>()
	}

	override fun getTargetByExtension(ext: String): String? = when (ext) {
		"js" -> "haxe:js"
		"php" -> "haxe:php"
		"exe" -> "haxe:cpp"
		"swf" -> "haxe:swf"
		else -> null
	}
}

@Singleton
class HaxeGenerator(injector: Injector) : FilePerClassCommonGenerator(injector) {
	val haxeConfigMergedAssetsFolder: HaxeConfigMergedAssetsFolder? = injector.getOrNull()
	val configHaxeAddSubtarget: ConfigHaxeAddSubtarget? = injector.getOrNull()

	override val outputFile2 = File(super.outputFile2.parentFile, "program.${configHaxeAddSubtarget?.subtarget?.extension ?: "out"}")

	companion object {
		//const val ENABLE_HXCPP_GOTO_HACK = true
		const val ENABLE_HXCPP_GOTO_HACK = false
	}

	val subtarget = injector.get<ConfigSubtarget>().subtarget
	override val methodFeatures = if (ENABLE_HXCPP_GOTO_HACK && (subtarget in setOf("cpp", "windows", "linux", "mac", "android"))) {
		super.methodFeatures + setOf(SwitchFeature::class.java, GotosFeature::class.java)
	} else {
		super.methodFeatures + setOf(SwitchFeature::class.java)
	}
	override val keywords = super.keywords + setOf(
		//////////////////////
		"haxe", "Dynamic", "Void", "java", "package", "import",
		"class", "interface", "extends", "implements",
		"internal", "private", "protected", "final",
		"function", "var", "const",
		"if", "else", "switch", "case", "default",
		"do", "while", "for", "each", "in",
		"try", "catch", "finally",
		"break", "continue",
		"int", "uint", "void", "goto",
		//////////////////////
		"hx",
		"z", // used for package
		"N", // used for Haxe Natives
		"SI", // STATIC INIT
		"SII", // STATIC INIT INITIALIZED
		"HAXE_CLASS_INIT", // Information about the class
		"HAXE_CLASS_NAME", // Information about the class
		"unix",
		"OVERFLOW", // iphone sdk
		"UNDERFLOW", // iphone sdk
		//////////////////////
		"toString", "hashCode"
	)

	val actualSubtarget = configHaxeAddSubtarget?.subtarget
	val targetDirectory = configTargetDirectory.targetDirectory
	//override val tempdir = configTargetDirectory.targetDirectory
	val mergedAssetsFolder = haxeConfigMergedAssetsFolder?.mergedAssetsFolder
	val mergedAssetsVfs by lazy { LocalVfs(mergedAssetsFolder!!) }
	//override val outputFile2 = configOutputFile2.file

	override fun buildSource() {
		_write()
		setInfoAfterBuildingSource()
	}

	fun haxeCopyEmbeddedResourcesToFolder(assetsFolder: File?) {
		val files = program.allAnnotationsList.getAllTyped<HaxeAddAssets>().flatMap { it.value.toList() }
		val resourcesVfs = program.resourcesVfs
		log("GenTargetInfo.haxeCopyResourcesToAssetsFolder: $assetsFolder")
		if (assetsFolder != null) {
			assetsFolder.mkdirs()
			val outputVfs = com.jtransc.vfs.LocalVfs(assetsFolder)
			for (file in files) {
				log("GenTargetInfo.haxeCopyResourcesToAssetsFolder.copy: $file")
				outputVfs[file] = resourcesVfs[file]
			}
		}
	}

	//val BUILD_COMMAND = listOf("haxelib", "run", "lime", "@@SWITCHES", "build", "@@SUBTARGET")

	override fun compileAndRun(redirect: Boolean): ProcessResult2 {
		return _compileRun(run = true, redirect = redirect)
	}

	override fun compile(): ProcessResult2 {
		return _compileRun(run = false, redirect = false)
	}

	fun _compileRun(run: Boolean, redirect: Boolean): ProcessResult2 {
		outputFile2.delete()
		log("haxe.build (" + JTranscVersion.getVersion() + ") source path: " + srcFolder.realpathOS)

		program.haxeInstallRequiredLibs(settings)

		log("Copying assets... ")
		haxeCopyEmbeddedResourcesToFolder(mergedAssetsFolder)
		for (asset in settings.assets) LocalVfs(asset).copyTreeTo(mergedAssetsVfs, doLog = true)

		log("Compiling... ")

		val buildVfs = srcFolder.parent.jail()

		val copyFilesBeforeBuildTemplate = program.classes.flatMap { it.annotationsList.getTyped<HaxeAddFilesBeforeBuildTemplate>()?.value?.toList() ?: listOf() }
		for (file in copyFilesBeforeBuildTemplate) buildVfs[file] = gen(program.resourcesVfs[file].readString())

		val buildAndRunAsASingleCommand = run && program.allAnnotationsList.contains<HaxeCustomBuildAndRunCommandLine>()

		val lines = if (buildAndRunAsASingleCommand) {
			(program.allAnnotationsList.getTyped<HaxeCustomBuildAndRunCommandLine>()?.value?.toList() ?: listOf("{{ defaultBuildCommand() }}")).map { it.trim() }
		} else {
			(program.allAnnotationsList.getTyped<HaxeCustomBuildCommandLine>()?.value?.toList() ?: listOf("{{ defaultBuildCommand() }}")).map { it.trim() }
		}

		val lines2 = lines.map {
			if (it.startsWith("@")) {
				program.resourcesVfs[it.substring(1).trim()].readString()
			} else {
				it
			}
		}

		val cmdAll = gen(lines2.joinToString("\n")).split("\n").map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }
		val cmdList = cmdAll.split("----").filter { it.isNotEmpty() }

		log("Commands to execute (buildAndRunAsASingleCommand=$buildAndRunAsASingleCommand):")
		for (cmd in cmdList) {
			log("- ${cmd.joinToString(" ")}")
		}
		for (cmd in cmdList) {
			val commandRaw = cmd.first()
			val cmdArgs = cmd.drop(1)

			val command = when (commandRaw) {
				"haxe" -> cmpvfs["haxe"].realpathOS
				"haxelib" -> cmpvfs["haxelib"].realpathOS
				else -> commandRaw
			}

			val processResult = log.logAndTime("Executing: $command ${cmdArgs.joinToString(" ")}") {
				ProcessUtils.runAndRedirect(buildVfs.realfile, command, cmdArgs, env = HaxeCompiler.getExtraEnvs())
			}

			if (!processResult.success) return ProcessResult2(processResult.exitValue)
		}
		return if (run && !buildAndRunAsASingleCommand) this.run(redirect) else ProcessResult2(0)
	}

	override fun run(redirect: Boolean): ProcessResult2 {
		if (!outputFile2.exists()) {
			return ProcessResult2(-1, "file $outputFile2 doesn't exist")
		}
		val fileSize = outputFile2.length()
		log("run: ${outputFile2.absolutePath} ($fileSize bytes)")
		val parentDir = outputFile2.parentFile

		val runner = actualSubtarget!!.interpreter
		val arguments = listOf(outputFile2.absolutePath + actualSubtarget.interpreterSuffix)

		log.info("Running: $runner ${arguments.joinToString(" ")}")
		return measureProcess("Running") {
			ProcessUtils.run(parentDir, runner, arguments, options = ExecOptions(passthru = redirect))
		}
	}

	override val defaultGenStmSwitchHasBreaks = false

	internal fun _write() {
		val vfs = srcFolder
		for (clazz in program.classes.filter { !it.isNative }) {
			if (clazz.implCode != null) {
				vfs[clazz.name.haxeFilePath] = clazz.implCode!!
			} else {
				//try {
				writeClass(clazz, vfs)
				//} catch (e: InvalidOperationException) {
				//	invalidOp("${e.message} while generating $context", e)
				//}
			}
		}

		val copyFilesRaw = program.classes.flatMap { it.annotationsList.getTyped<HaxeAddFilesRaw>()?.value?.toList() ?: listOf() }
		val copyFilesTemplate = program.classes.flatMap {
			val template = it.annotationsList.getTyped<HaxeAddFilesTemplate>()
			if (template != null) {
				val baseFile = File(template.base)
				template.value.toList().map { it to File(it).relativeTo(baseFile).path }
			} else {
				listOf()
			}
		}

		for (file in copyFilesRaw) vfs[file] = program.resourcesVfs[file]
		for ((src, dst) in copyFilesTemplate) {
			vfs[dst] = gen(program.resourcesVfs[src].readString(), context, "copyFilesTemplate")
		}

		val mainClassFq = program.entrypoint
		val mainClass = mainClassFq.haxeClassFqName
		val mainMethod = program[mainClassFq].getMethod("main", types.build { METHOD(VOID, ARRAY(STRING)) }.desc)!!.targetName
		entryPointClass = FqName(mainClassFq.fqname + "_EntryPoint")
		entryPointFilePath = entryPointClass.haxeFilePath
		val entryPointFqName = entryPointClass.haxeGeneratedFqName
		val entryPointSimpleName = entryPointClass.targetGeneratedSimpleClassName
		val entryPointPackage = entryPointFqName.packagePath

		fun calcClasses(program: AstProgram, mainClass: AstClass): List<AstClass> {
			return sortDependenciesSimple(mainClass) {
				it.classDependencies.map { program.get3(it) }
			}
		}

		fun inits() = Indenter.gen {
			line("HaxePolyfills.install();");
			line("haxe.CallStack.callStack();")
			line(getClassStaticInit(program[mainClassFq].ref, "program main"))
		}

		val customMain = program.allAnnotationsList.getTyped<HaxeCustomMain>()?.value

		val plainMain = Indenter.genString {
			line("package {{ entryPointPackage }};")
			line("class {{ entryPointSimpleName }}") {
				line("@:unreflective static public function main()") {
					line("{{ inits }}")
					line("{{ mainClass }}.{{ mainMethod }}(N.strArray(N.args()));")
				}
			}
		}

		log("Using ... " + if (customMain != null) "customMain" else "plainMain")

		setExtraData(mapOf(
			"entryPointPackage" to entryPointPackage,
			"entryPointSimpleName" to entryPointSimpleName,
			"mainClass" to mainClass,
			"mainClass2" to mainClassFq.fqname,
			"mainMethod" to mainMethod,
			"inits" to inits().toString()
		))
		vfs[entryPointFilePath] = gen(customMain ?: plainMain)
	}

	override fun N_AGET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String): String {
		val get = when (elementType) {
			AstType.BOOL -> "getBool"
			else -> "get"
		}
		return "$array.$get($index)"
	}

	override fun N_ASET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String, value: String): String {
		val set = when (elementType) {
			AstType.BOOL -> "setBool"
			else -> "set"
		}
		return "$array.$set($index, $value);"
	}

	override fun genStmTryCatch(stm: AstStm.TRY_CATCH) = indent {
		line("try") {
			line(stm.trystm.genStm())
		}
		line("catch (J__i__exception__: Dynamic)") {
			line("J__exception__ = J__i__exception__;")
			line(stm.catch.genStm())
		}
	}

	override fun genStmRethrow(stm: AstStm.RETHROW) = indent { line("""throw J__i__exception__;""") }

	override fun genBodyLocal(local: AstLocal): Indenter = indent { line("var ${local.nativeName}: ${local.type.targetTypeTag} = ${local.type.haxeDefaultString};") }
	override fun genBodyTrapsPrefix(): Indenter = indent { line("var J__exception__:Dynamic = null;") }
	override fun genBodyStaticInitPrefix(clazzRef: AstType.REF, reasons: ArrayList<String>) = indent {
		line(getClassStaticInit(clazzRef, reasons.joinToString(", ")))
	}

	private fun getStringId(id: Int) = "__str$id"
	private fun getStringId(clazz: FqName, str: String) = getStringId(allocString(clazz, str))

	override fun genExprThis(e: AstExpr.THIS): String = "this"
	override fun genLiteralString(v: String): String = getStringId(context.clazz.name, v)

	override fun genExprArrayLength(e: AstExpr.ARRAY_LENGTH): String {
		val type = e.array.type
		return if (type is AstType.ARRAY) {
			"(${e.array.genNotNull()}).length"
		} else {
			"cast(${e.array.genNotNull()}, $BaseArrayType).length"
		}
	}

	override fun convertToFromTarget(type: AstType, text: String, toTarget: Boolean): String {
		if (type is AstType.ARRAY) {
			return (if (toTarget) "N.unbox($text)" else "cast(N.box($text), ${getNativeType(type, TypeKind.CAST)})")
		}

		if (type is AstType.REF) {
			val conversion = program[type.name].annotationsList.getTyped<HaxeNativeConversion>()
			if (conversion != null) {
				return (if (toTarget) conversion.toHaxe else conversion.toJava).replace("@self", text)
			}
		}
		return text
	}

	//"Std.is(${e.expr.genExpr()}, ${e.checkType.targetTypeCast})"
	//override fun N_is(a: String, b: String) = "N.is($a, $b)"
	override fun N_is(a: String, b: String) = "Std.is($a, $b)"

	override fun N_z2i(str: String) = "N.z2i($str)"
	override fun N_i(str: String) = "(($str)|0)"
	override fun N_i2z(str: String) = "(($str)!=0)"
	override fun N_i2b(str: String) = "N.i2b($str)"
	override fun N_i2c(str: String) = "(($str)&0xFFFF)"
	override fun N_i2s(str: String) = "N.i2s($str)"
	override fun N_f2i(str: String) = "Std.int($str)"
	override fun N_i2i(str: String) = N_i(str)
	override fun N_i2j(str: String) = "N.intToLong($str)"
	override fun N_i2f(str: String) = "Math.fround(($str))"
	override fun N_i2d(str: String) = "($str)"
	override fun N_f2f(str: String) = "Math.fround($str)"
	override fun N_f2d(str: String) = "($str)"
	override fun N_d2f(str: String) = "Math.fround(($str))"
	override fun N_d2d(str: String) = "($str)"
	override fun N_d2i(str: String) = "Std.int($str)"
	override fun N_l2i(str: String) = "(($str).low)"
	override fun N_l2l(str: String) = "($str)"
	override fun N_l2f(str: String) = "N.longToFloat($str)"
	override fun N_l2d(str: String) = "N.longToFloat($str)"
	override fun N_getFunction(str: String) = "N.getFunction($str)"
	override fun N_c(str: String, from: AstType, to: AstType) = "N.c($str, ${to.targetTypeCast})"
	override fun N_idiv(l: String, r: String): String = "N.idiv($l, $r)"
	override fun N_imul(l: String, r: String): String = "N.imul($l, $r)"
	override fun N_ishl(l: String, r: String): String = "N.ishl($l, $r)"
	override fun N_ishr(l: String, r: String): String = "N.ishr($l, $r)"
	override fun N_iushr(l: String, r: String): String = "N.iushr($l, $r)"

	override fun N_obj_eq(l: String, r: String): String = "N.eq($l, $r)"
	override fun N_obj_ne(l: String, r: String): String = "N.ne($l, $r)"

	// @TODO: Use this.annotationsList.getTypedList
	private fun AstMethod.getHaxeNativeBodyList(): List<HaxeMethodBody> {
		val bodyList = this.annotationsList.getTyped<HaxeMethodBodyList>()
		val bodyEntry = this.annotationsList.getTyped<HaxeMethodBody>()
		val bodies = listOf(bodyList?.value?.toList(), listOf(bodyEntry)).concatNotNull()
		return bodies
	}

	private fun AstMethod.hasHaxeNativeBody(): Boolean = this.annotationsList.contains<HaxeMethodBodyList>() || this.annotationsList.contains<HaxeMethodBody>()

	private fun AstMethod.getHaxeNativeBody(defaultContent: Indenter): Indenter {
		val method = this

		val bodies = this.getHaxeNativeBodyList()

		return if (bodies.isNotEmpty()) {
			val pre = method.annotationsList.getTyped<HaxeMethodBodyPre>()?.value ?: ""
			val post = method.annotationsList.getTyped<HaxeMethodBodyPost>()?.value ?: ""

			val bodiesmap = bodies.map { it.target to it.value }.toMap()
			val defaultbody: Indenter = if ("" in bodiesmap) Indenter.gen { line(bodiesmap[""]!!) } else defaultContent
			val extrabodies = bodiesmap.filterKeys { it != "" }
			Indenter.gen {
				line(pre)
				if (extrabodies.size == 0) {
					line(defaultbody)
				} else {
					var first = true
					for ((target, extrabody) in extrabodies) {
						line((if (first) "#if" else "#elseif") + " ($target) $extrabody")
						first = false
					}
					line("#else")
					line(defaultbody)
					line("#end")
				}
				line(post)
			}
		} else {
			defaultContent
		}
	}

	fun writeClass(clazz: AstClass, vfs: SyncVfsFile) {
		setCurrentClass(clazz)

		val isRootObject = clazz.name.fqname == "java.lang.Object"
		val isInterface = clazz.isInterface
		val isAbstract = clazz.isAbstract
		val classType = if (isInterface) "interface" else "class"
		val simpleClassName = clazz.name.targetGeneratedSimpleClassName
		fun getInterfaceList(keyword: String) = (if (clazz.implementing.isNotEmpty()) " $keyword " else "") + clazz.implementing.map { it.haxeClassFqName }.joinToString(" $keyword ")
		refs._usedDependencies.clear()

		if (!clazz.extending?.fqname.isNullOrEmpty()) refs.add(AstType.REF(clazz.extending!!))
		for (impl in clazz.implementing) refs.add(AstType.REF(impl))

		fun writeField(field: AstField, isInterface: Boolean): Indenter = Indenter.gen {
			val static = if (field.isStatic) "static " else ""
			val visibility = if (isInterface) " " else field.visibility.haxe
			val fieldType = field.type
			refs.add(fieldType)
			val defaultValue: Any? = if (field.hasConstantValue) field.constantValue else fieldType.haxeDefault
			val fieldName = field.haxeName
			if (!field.annotationsList.contains<HaxeRemoveField>()) {
				val keep = if (field.annotationsList.contains<JTranscKeep>()) "@:keep " else ""
				line("$keep$static$visibility var $fieldName:${fieldType.targetTypeTag} = ${escapeConstant(defaultValue, fieldType)}; // /*${field.name}*/")
			}
		}

		fun writeMethod(method: AstMethod, isInterface: Boolean): Indenter {
			setCurrentMethod(method)
			return Indenter.gen {
				val static = if (method.isStatic) "static " else ""
				val visibility = if (isInterface) " " else method.visibility.haxe
				refs.add(method.methodType)
				val margs = method.methodType.args.map { it.name + ":" + it.type.targetTypeTag }
				val override = if (method.haxeIsOverriding) "override " else ""
				val inline = if (method.isInline) "inline " else ""
				val rettype = if (method.methodVoidReturnThis) method.containingClass.astType else method.methodType.ret
				val decl = try {
					"@:unreflective $static $visibility $inline $override function ${method.targetName}/*${method.name}*/(${margs.joinToString(", ")}):${rettype.targetTypeTag}".trim()
				} catch (e: RuntimeException) {
					println("@TODO abstract interface not referenced: ${method.containingClass.fqname} :: ${method.name} : $e")
					throw e
				}

				if (isInterface) {
					if (!method.isImplementing) line("$decl;")
				} else {
					val meta = method.annotationsList.getTyped<HaxeMeta>()?.value
					if (meta != null) line(meta)
					val rbody = if (method.body != null) {
						method.body
					} else if (method.bodyRef != null) {
						program[method.bodyRef!!]?.body
					} else {
						null
					}
					line(decl) {
						try {
							// @TODO: Do not hardcode this!
							if (method.name == "throwParameterIsNullException") line("N.debugger();")
							val javaBody = if (rbody != null) {
								rbody.genBodyWithFeatures(method)
							} else Indenter.gen {
								line("throw 'No method body';")
							}
							line(method.getHaxeNativeBody(javaBody).toString().template())
							if (method.methodVoidReturnThis) line("return this;")
						} catch (e: Throwable) {
							//e.printStackTrace()
							log.warn("WARNING haxe_gen.writeMethod:" + e.message)

							line("N.debugger(); throw " + "Errored method: ${clazz.name}.${method.name} :: ${method.desc} :: ${e.message}".quote() + ";")
						}
					}
				}
			}
		}

		fun addClassInit(clazz: AstClass) = Indenter.gen {
			line("static public var SII = false;");
			for (e in getClassStrings(clazz.name)) {
				line("static private var ${getStringId(e.id)}:$JAVA_LANG_STRING;")
			}

			line("@:unreflective static public function SI()") {
				line("if (SII) return;")
				line("SII = true;")

				for (e in getClassStrings(clazz.name)) {
					line("${getStringId(e.id)} = ${escapeConstant(e.str)};")
				}

				if (clazz.hasStaticInit) {
					val methodName = clazz.staticInitMethod!!.targetName
					line("$methodName();")
				}
			}
		}

		val classCodeIndenter = Indenter.gen {
			line("package ${clazz.name.haxeGeneratedFqPackage};")

			if (isAbstract) line("// ABSTRACT")
			var declaration = "$classType $simpleClassName"
			if (isInterface) {
				if (clazz.implementing.isNotEmpty()) declaration += getInterfaceList("extends")
			} else {
				if (clazz.extending != null && clazz.name.fqname != "java.lang.Object") declaration += " extends ${clazz.extending!!.haxeClassFqName}"
				if (clazz.implementing.isNotEmpty()) declaration += getInterfaceList("implements")
			}

			// Additional imports!
			val imports = clazz.annotationsList.getTyped<HaxeImports>()?.value
			if (imports != null) for (i in imports) line(i)

			val meta = clazz.annotationsList.getTyped<HaxeMeta>()?.value
			val availableOnTargets = clazz.annotationsList.getTyped<HaxeAvailableOnTargets>()?.value

			if (availableOnTargets != null) {
				//println("availableOnTargets:" + availableOnTargets.joinToString(" || "))
				line("#if (" + availableOnTargets.joinToString(" || ") + ")")
			}
			if (meta != null) line(meta)
			line(declaration) {
				if (!isInterface) {
					if (isRootObject) {
						line("public var _CLASS_ID__HX:Int;")
					}
					line("@:unreflective public function new()") {
						line(if (isRootObject) "" else "super();")
						line("SI();")
						line("this._CLASS_ID__HX = ${clazz.classId};")
					}
				}

				val nativeMembers = clazz.annotationsList.getTyped<HaxeAddMembers>()?.value?.toList() ?: listOf()

				for (member in nativeMembers) line(member.template())

				if (!isInterface) {
					for (field in clazz.fields) {
						line(writeField(field, isInterface))
					}
				}

				for (method in clazz.methods) {
					if (isInterface && method.isStatic) continue
					line(writeMethod(method, isInterface))
				}

				if (isRootObject) {
					line("@:unreflective public function toString():String { return N.toNativeString(this.$toStringTargetName()); }")
					line("@:unreflective public function hashCode():Int { return this.$hashCodeTargetName(); }")
				}

				if (!isInterface) {
					line(addClassInit(clazz))
					//line(dumpClassInfo(clazz))
				}
			}

			if (availableOnTargets != null) {
				line("#end")
			}

			//if (isInterfaceWithStaticMembers) {
			if (isInterface) {
				val javaLangObjectClass = program[FqName("java.lang.Object")]

				line("@:unreflective class ${simpleClassName}_IFields") {
					line("@:unreflective public function new() {}")
					for (field in clazz.fields) line(writeField(field, isInterface = false))
					for (method in clazz.methods.filter { it.isStatic }) line(writeMethod(method, isInterface = false))
					line(addClassInit(clazz))
					//line(dumpClassInfo(clazz))
				}
			}
		}

		val lineMappings = hashMapOf<Int, Int>()

		val fileStr = classCodeIndenter.toString { sb, line, data ->
			if (data is AstStm.LINE) {
				//println("MARKER: ${sb.length}, $line, $data, ${clazz.source}")
				lineMappings[line] = data.line
				//clazzName.internalFqname + ".java"
			}
		}

		val haxeFilePath = clazz.name.haxeFilePath
		vfs[haxeFilePath] = fileStr
		vfs["$haxeFilePath.map"] = Sourcemaps.encodeFile(vfs[haxeFilePath].realpathOS, fileStr, clazz.source, lineMappings)
	}

	//val FqName.as3Fqname: String get() = this.fqname
	//fun AstMethod.getHaxeMethodName(program: AstProgram): String = this.ref.getHaxeMethodName(program)

	override val stringPoolType: StringPoolType = StringPoolType.PER_CLASS

	override fun buildConstructor(method: AstMethod): String = "new ${getClassFqName(method.containingClass.name)}().${getHaxeMethodName(method)}"

	override fun buildStaticInit(clazz: AstClass): String = getClassStaticInit(clazz.ref, "template sinit")

	override fun buildMethod(method: AstMethod, static: Boolean): String {
		val clazz = getClassFqName(method.containingClass.name)
		val name = getHaxeMethodName(method)
		return if (static) "$clazz.$name" else name
	}

	override fun getNativeName2(local: LocalParamRef): String = super.getNativeName2(local)
	override fun getNativeName(field: FieldRef): String = getFieldName(field)
	override fun getNativeName(methodRef: MethodRef): String = getHaxeMethodName(methodRef.ref)
	override fun getNativeName(clazz: FqName): String = getClassFqName(clazz)
	override fun getNativeNameForFields(clazz: FqName): String = getClassFqNameInt(clazz)

	override fun buildTemplateClass(clazz: FqName): String = getClassFqName(clazz)
	override fun buildTemplateClass(clazz: AstClass): String = getClassFqName(clazz.name)
	private val cachedFieldNames = hashMapOf<AstFieldRef, String>()

	//val ENABLED_MINIFY = false
	val ENABLED_MINIFY = true
	private val ENABLED_MINIFY_MEMBERS = ENABLED_MINIFY && minimize
	private val ENABLED_MINIFY_CLASSES = ENABLED_MINIFY && minimize

	//private val ENABLED_MINIFY_CLASSES = true
	//private val ENABLED_MINIFY_MEMBERS = false

	private val classNames = hashMapOf<FqName, FqName>()
	private val methodNames = hashMapOf<Any?, String>()
	private val fieldNames = hashMapOf<Any?, String>()

	val minClassPrefix = "z."
	//val minClassPrefix = ""


	fun getHaxeMethodName(method: AstMethod): String = getHaxeMethodName(method.ref)
	fun getHaxeMethodName(method: AstMethodRef): String {
		val realmethod = program[method] ?: invalidOp("Can't find method $method")
		val realclass = realmethod.containingClass
		val methodWithoutClass = method.withoutClass

		val objectToCache: Any = if (method.isClassOrInstanceInit) method else methodWithoutClass

		return if (realclass.isNative) {
			// No cache
			realmethod.nativeName ?: method.name
		} else {
			methodNames.getOrPut2(objectToCache) {
				if (ENABLED_MINIFY_MEMBERS && !realmethod.keepName) {
					allocMemberName()
				} else {
					if (realmethod.nativeMethod != null) {
						realmethod.nativeMethod!!
					} else {
						val name2 = "${method.name}${method.desc}"
						val name = when (method.name) {
							"<init>", "<clinit>" -> "${method.containingClass}$name2"
							else -> name2
						}
						cleanName(name)
					}
				}
			}
		}
	}

	private fun cleanName(name: String): String {
		val out = CharArray(name.length)
		for (n in 0 until name.length) out[n] = if (name[n].isLetterOrDigit()) name[n] else '_'
		return String(out)
	}

	override fun getFunctionalType2(type: AstType.METHOD): String {
		return type.argsPlusReturnVoidIsEmpty.map { getNativeType(it, CommonGenerator.TypeKind.TYPETAG) }.joinToString(" -> ")
	}

	override fun getDefault(type: AstType): Any? = type.getNull()

	@Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
	private fun _getHaxeFqName(name: FqName): FqName {
		val realclass = if (name in program) program[name]!! else null
		return classNames.getOrPut2(name) {
			if (realclass?.nativeName != null) {
				FqName(realclass!!.nativeName!!)
			} else if (ENABLED_MINIFY_CLASSES && !realclass.keepName) {
				FqName(minClassPrefix + allocClassName())
			} else {
				FqName(name.packageParts.map { if (it in keywords) "${it}_" else it }.map { it.decapitalize() }, "${name.simpleName.replace('$', '_')}_".capitalize())
			}
		}
	}

	override fun getFilePath(name: FqName): String = getGeneratedFqName(name).internalFqname + ".hx"
	override fun getGeneratedFqPackage(name: FqName): String = _getHaxeFqName(name).packagePath
	override fun getGeneratedFqName(name: FqName): FqName = _getHaxeFqName(name)
	override fun getGeneratedSimpleClassName(name: FqName): String = _getHaxeFqName(name).simpleName

	override fun getClassFqName(name: FqName): String {
		val clazz = if (name in program) program[name] else null
		return clazz?.nativeName ?: getGeneratedFqName(name).fqname
	}

	override fun getFieldName(field: AstFieldRef): String {
		val realfield = program[field]
		val realclass = program[field.containingClass]
		//val keyToUse = if (realfield.keepName) field else field.name
		//val keyToUse = if (ENABLED_MINIFY_FIELDS) field else field.name
		val keyToUse = field

		val normalizedFieldName = normalizeName(field.name)

		return if (realclass.isNative) {
			// No cache
			realfield.nativeName ?: normalizedFieldName
		} else {
			fieldNames.getOrPut2(keyToUse) {
				if (ENABLED_MINIFY_MEMBERS && !realfield.keepName) {
					allocMemberName()
				} else {
					// @TODO: Move to CommonNames
					if (field !in cachedFieldNames) {
						val fieldName = normalizedFieldName
						var name = if (fieldName in keywords) "${fieldName}_" else fieldName

						val clazz = program[field].containingClass
						val clazzAncestors = clazz.ancestors.reversed()
						val names = clazzAncestors.flatMap { it.fields }.filter { it.name == field.name }.map { getFieldName(it.ref) }.toHashSet()
						val fieldsColliding = clazz.fields.filter {
							(it.ref == field) || (normalizeName(it.name) == normalizedFieldName)
						}.map { it.ref } ?: listOf(field)

						// JTranscBugInnerMethodsWithSameName.kt
						for (f2 in fieldsColliding) {
							while (name in names) name += "_"
							cachedFieldNames[f2] = name
							names += name
						}
						cachedFieldNames[field] ?: unexpected("Unexpected. Not cached: $field")
					}
					cachedFieldNames[field] ?: unexpected("Unexpected. Not cached: $field")
				}
			}
		}
	}

	override fun getClassFqNameInt(name: FqName): String {
		val clazz = program[name]
		val simpleName = getGeneratedSimpleClassName(name)
		val suffix = if (clazz.isInterface) ".${simpleName}_IFields" else ""
		return getClassFqName(clazz.name) + suffix
	}

	override fun getClassFqNameLambda(name: FqName): String {
		val clazz = program[name]
		val simpleName = getGeneratedSimpleClassName(name)
		return getClassFqName(clazz.name) + ".${simpleName}_Lambda"
	}

	override fun getClassStaticInit(classRef: AstType.REF, reason: String): String {
		val clazz = program[classRef.name]
		if (clazz.nativeName != null) {
			return ""
		} else {
			return "${getClassFqNameInt(classRef.name)}.SI();"
		}
	}

	override fun getClassStaticClassInit(classRef: AstType.REF): String = "${getClassFqNameInt(classRef.name)}.HAXE_CLASS_INIT"

	override fun getAnnotationProxyName(classRef: AstType.REF): String = "AnnotationProxy_${getGeneratedFqName(classRef.name).fqname.replace('.', '_')}"

	override fun getFullAnnotationProxyName(classRef: AstType.REF): String {
		return getClassFqName(classRef.name) + ".AnnotationProxy_${getGeneratedFqName(classRef.name).fqname.replace('.', '_')}"
	}

	override val NullType = FqName("Dynamic")
	override val VoidType = FqName("Void")
	override val BoolType = FqName("Bool")
	override val IntType = FqName("Int")
	override val FloatType = FqName("Float32")
	override val DoubleType = FqName("Float64")
	override val LongType = FqName("haxe.Int64")
	override val BaseArrayType = FqName("JA_0")
	override val BoolArrayType = FqName("JA_Z")
	override val ByteArrayType = FqName("JA_B")
	override val CharArrayType = FqName("JA_C")
	override val ShortArrayType = FqName("JA_S")
	override val IntArrayType = FqName("JA_I")
	override val LongArrayType = FqName("JA_J")
	override val FloatArrayType = FqName("JA_F")
	override val DoubleArrayType = FqName("JA_D")
	override val ObjectArrayType = FqName("JA_L")

	override val NegativeInfinityString = "Math.NEGATIVE_INFINITY"
	override val PositiveInfinityString = "Math.POSITIVE_INFINITY"
	override val NanString = "Math.NaN"

	///////////////////////////

	//val actualSubtarget = configActualSubtarget.subtarget
	override val srcFolder = HaxeGenTools.getSrcFolder(tempdir)
	val mergedAssetsDir = haxeConfigMergedAssetsFolder?.mergedAssetsFolder

	init {
		if (actualSubtarget != null) {
			HaxeCompiler.ensureHaxeSubtarget(actualSubtarget.name)
		}
		params["defaultBuildCommand"] = {
			Minitemplate("""
				haxe
				-cp
				{{ srcFolder }}
				-main
				{{ entryPointFile }}
				{% if debug %}
					-debug
				{% end %}
				{{ actualSubtarget.cmdSwitch }}
				{{ outputFile }}
				{% for flag in haxeExtraFlags %}
					{{ flag.first }}
					{{ flag.second }}
				{% end %}
				{% for define in haxeExtraDefines %}
					-D
					define
				{% end %}
			""").invoke(params)
		}
		params["actualSubtarget"] = actualSubtarget
		params["tempAssetsDir"] = mergedAssetsDir?.absolutePath // @deprecated
		params["mergedAssetsDir"] = mergedAssetsDir?.absolutePath
		params["srcFolder"] = srcFolder.realpathOS
		params["buildFolder"] = srcFolder.parent.realpathOS
		params["haxeExtraFlags"] = program.haxeExtraFlags(settings)
		params["haxeExtraDefines"] = program.haxeExtraDefines(settings)
	}

	open val AstMethod.haxeIsOverriding: Boolean get() = this.isOverriding && !this.isInstanceInit
}


//val HaxeFeatures = setOf(GotosFeature::class.java, SwitchesFeature::class.java)

private val HAXE_LIBS_KEY = UserKey<List<HaxeLib.LibraryRef>>()

fun AstProgram.haxeLibs(settings: AstBuildSettings): List<HaxeLib.LibraryRef> = this.getCached(HAXE_LIBS_KEY) {
	this.classes
		.map { it.annotationsList.getTyped<HaxeAddLibraries>()?.value }
		.filterNotNull()
		.flatMap { it.toList() }
		.map { HaxeLib.LibraryRef.fromVersion(it) }
}

fun AstProgram.haxeExtraFlags(settings: AstBuildSettings): List<Pair<String, String>> = this.haxeLibs(settings).map { "-lib" to it.nameWithVersion }
fun AstProgram.haxeExtraDefines(settings: AstBuildSettings): List<String> = if (settings.analyzer) listOf() else listOf("no-analyzer")

fun AstProgram.haxeInstallRequiredLibs(settings: AstBuildSettings) {
	val libs = this.haxeLibs(settings)
	log(":: REFERENCED LIBS: $libs")
	for (lib in libs) {
		log(":: TRYING TO INSTALL LIBRARY $lib")
		HaxeLib.installIfNotExists(lib)
	}
}

object HaxeGenTools {
	fun getSrcFolder(tempdir: String): SyncVfsFile {
		val baseDir = "$tempdir/jtransc-haxe"
		log("Temporal haxe files: $baseDir")
		File("$baseDir/src").mkdirs()
		return LocalVfs(File(baseDir)).ensuredir()["src"]
	}
}

val cmpvfs: SyncVfsFile by lazy { HaxeCompiler.ensureHaxeCompilerVfs() }

@Singleton
class HaxeConfigMergedAssetsFolder(configTargetDirectory: ConfigTargetDirectory) {
	val targetDirectory = configTargetDirectory.targetDirectory
	val mergedAssetsFolder: File get() = File("$targetDirectory/merged-assets")
}
