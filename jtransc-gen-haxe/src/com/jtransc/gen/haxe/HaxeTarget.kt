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
import com.jtransc.ast.transform.reduceSwitch
import com.jtransc.ds.concatNotNull
import com.jtransc.ds.getOrPut2
import com.jtransc.ds.split
import com.jtransc.gen.GenTargetDescriptor
import com.jtransc.gen.TargetBuildTarget
import com.jtransc.gen.common.*
import com.jtransc.injector.Injector
import com.jtransc.injector.Singleton
import com.jtransc.io.ProcessResult2
import com.jtransc.io.ProcessUtils
import com.jtransc.log.log
import com.jtransc.sourcemaps.Sourcemaps
import com.jtransc.template.Minitemplate
import com.jtransc.text.Indenter
import com.jtransc.text.quote
import com.jtransc.time.measureProcess
import com.jtransc.vfs.*
import java.io.File
import java.util.*

class HaxeTarget : GenTargetDescriptor() {
	override val priority = 1000
	override val name = "haxe"
	override val longName = "Haxe"
	override val sourceExtension = "hx"
	override val outputExtension = "bin"
	override val extraLibraries = listOf<String>()
	override val extraClasses = listOf<String>()
	override val runningAvailable: Boolean = true

	override val buildTargets: List<TargetBuildTarget> = listOf(
		TargetBuildTarget("haxeJs", "haxe:js", "program.js", minimizeNames = true),
		TargetBuildTarget("swf", "haxe:swf", "program.swf"),
		TargetBuildTarget("haxeCpp", "haxe:cpp", "program.exe"),
		TargetBuildTarget("neko", "haxe:neko", "program.n"),
		TargetBuildTarget("php", "haxe:php", "program.php")
	)

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
class HaxeGenerator(injector: Injector) : CommonGenerator(injector) {
	override val SINGLE_FILE: Boolean = false
	val haxeConfigMergedAssetsFolder: HaxeConfigMergedAssetsFolder? = injector.getOrNull()
	val configHaxeAddSubtarget: ConfigHaxeAddSubtarget? = injector.getOrNull()
	val MAX_SWITCH_SIZE = 10
	override val floatHasFPrefix: Boolean = false

	//val unreflective = "@:unreflective"
	val unreflective = ""

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
	override val stringPoolType = StringPool.Type.PER_CLASS
	override val defaultGenStmSwitchHasBreaks = false

	val actualSubtarget = configHaxeAddSubtarget?.subtarget
	//override val tempdir = configTargetDirectory.targetDirectory
	val mergedAssetsFolder = haxeConfigMergedAssetsFolder?.mergedAssetsFolder
	val mergedAssetsVfs by lazy { LocalVfs(mergedAssetsFolder!!) }
	//override val outputFile2 = configOutputFile2.file

	override fun writeProgramAndFiles() {
		_write()
		setTemplateParamsAfterBuildingSource()
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

	//override fun genStmSwitch(stm: AstStm.SWITCH): Indenter = if (stm.cases.size > MAX_SWITCH_SIZE) {
	//	this.genStm2(stm.reduceSwitch(maxChunkSize = 10))
	//} else {
	//	super.genStmSwitch(stm)
	//}

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

	internal fun _write() {
		val vfs = srcFolder
		for (clazz in program.classes.filter { !it.isNative }) {
			if (clazz.implCode != null) {
				vfs[clazz.name.targetFilePath] = clazz.implCode!!
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
		val mainClassFqName = mainClassFq.targetClassFqName
		val mainClass = program[mainClassFq]
		val mainMethod = mainClass.getMethod("main", types.build { METHOD(VOID, ARRAY(STRING)) }.desc)!!.targetName
		entryPointClass = FqName(mainClassFq.fqname + "_EntryPoint")
		entryPointFilePath = entryPointClass.targetFilePath
		val entryPointFqName = entryPointClass.targetGeneratedFqName
		val entryPointSimpleName = entryPointClass.targetSimpleName
		val entryPointPackage = entryPointFqName.packagePath

		fun inits() = Indenter.gen {
			line("HaxePolyfills.install();")
			line("haxe.CallStack.callStack();")
			line(genStaticConstructorsSorted())
			//line(buildStaticInit())
			//line(buildStaticInit(mainClass.name))
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
			"mainClass" to mainClassFqName,
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

	override val AstLocal.decl: String get() = "var ${this.targetName}: ${this.type.targetName} = ${this.type.nativeDefaultString};"

	override fun genBodyTrapsPrefix(): Indenter = indent { line("var J__exception__:Dynamic = null;") }
	override fun genBodyStaticInitPrefix(clazzRef: AstType.REF, reasons: ArrayList<String>) = indent {
		line(buildStaticInit(clazzRef.name))
	}

	override fun genExprThis(e: AstExpr.THIS): String = "this"

	private fun getStringId(id: Int) = "__str$id"
	override val String.escapeString: String get() = getStringId(allocString(context.clazz.name, this))

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
			return (if (toTarget) "N.unbox($text)" else "cast(N.box($text), ${type.targetName})")
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
	override fun N_i2f(str: String) = "($str)"
	override fun N_i2d(str: String) = "($str)"
	override fun N_f2f(str: String) = "($str)"
	override fun N_f2d(str: String) = "($str)"
	override fun N_d2f(str: String) = "(($str))"
	override fun N_d2d(str: String) = "($str)"
	override fun N_d2i(str: String) = "Std.int($str)"
	override fun N_l2i(str: String) = "(($str).low)"
	override fun N_l2l(str: String) = "($str)"
	override fun N_l2f(str: String) = "N.longToFloat($str)"
	override fun N_l2d(str: String) = "N.longToFloat($str)"
	override fun N_getFunction(str: String) = "N.getFunction($str)"
	override fun N_c(str: String, from: AstType, to: AstType) = "N.c($str, ${to.targetName})"
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

	private fun AstMethod.getHaxeNativeBody(defaultContentGen: () -> Indenter): Indenter {
		val method = this

		val bodies = this.getHaxeNativeBodyList()

		return if (bodies.isNotEmpty()) {
			val pre = method.annotationsList.getTyped<HaxeMethodBodyPre>()?.value ?: ""
			val post = method.annotationsList.getTyped<HaxeMethodBodyPost>()?.value ?: ""

			val bodiesmap = bodies.map { it.target to it.value }.toMap()
			val defaultbody: Indenter = if ("" in bodiesmap) Indenter.gen { line(bodiesmap[""]!!) } else defaultContentGen()
			val extrabodies = bodiesmap.filterKeys { it != "" }
			Indenter.gen {
				line(pre)
				if (extrabodies.isEmpty()) {
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
			defaultContentGen()
		}
	}

	fun writeClass(clazz: AstClass, vfs: SyncVfsFile) {
		setCurrentClass(clazz)

		val isRootObject = clazz.name.fqname == "java.lang.Object"
		val isInterface = clazz.isInterface
		val isAbstract = clazz.isAbstract
		val classType = if (isInterface) "interface" else "class"
		val simpleClassName = clazz.name.targetSimpleName
		fun getInterfaceList(keyword: String) = (if (clazz.implementing.isNotEmpty()) " $keyword " else "") + clazz.implementing.map { it.targetClassFqName }.joinToString(" $keyword ")
		refs._usedDependencies.clear()

		if (!clazz.extending?.fqname.isNullOrEmpty()) refs.add(AstType.REF(clazz.extending!!))
		for (impl in clazz.implementing) refs.add(AstType.REF(impl))

		fun writeField(field: AstField, isInterface: Boolean): Indenter = Indenter.gen {
			val static = if (field.isStatic) "static " else ""
			val visibility = if (isInterface) " " else "public"
			val fieldType = field.type
			refs.add(fieldType)
			val defaultValue: Any? = if (field.hasConstantValue) field.constantValue else fieldType.nativeDefault
			val fieldName = field.targetName
			if (!field.annotationsList.contains<HaxeRemoveField>()) {
				val keep = if (field.annotationsList.contains<JTranscKeep>()) "@:keep " else ""
				line("$keep$static$visibility var $fieldName:${fieldType.targetName} = ${defaultValue.escapedConstantOfType(fieldType)};")
			}
		}

		fun writeMethod(method: AstMethod, isInterface: Boolean): Indenter {
			setCurrentMethod(method)
			return Indenter.gen {
				val static = if (method.isStatic) "static " else ""
				val visibility = if (isInterface) " " else "public"
				refs.add(method.methodType)
				val margs = method.methodType.args.map { it.name + ":" + it.type.targetName }
				val override = if (method.targetIsOverriding) "override " else ""
				val inline = if (method.isInline) "inline " else ""
				val rettype = if (method.methodVoidReturnThis) method.containingClass.astType else method.methodType.ret
				val decl = try {
					"$unreflective $static $visibility $inline $override function ${method.targetName}/*${method.name}*/(${margs.joinToString(", ")}):${rettype.targetName}".trim()
				} catch (e: RuntimeException) {
					println("@TODO abstract interface not referenced: ${method.containingClass.fqname} :: ${method.name} : $e")
					throw e
				}

				if (isInterface) {
					if (!method.isImplementing) line("$decl;")
				} else {
					val meta = method.annotationsList.getTyped<HaxeMeta>()?.value
					if (meta != null) line(meta)
					val rbody = if (method.body != null) method.body else if (method.bodyRef != null) program[method.bodyRef!!]?.body else null
					line(decl) {
						try {
							// @TODO: Do not hardcode this!
							if (method.name == "throwParameterIsNullException") line("N.debugger();")
							val str : String = "${clazz.name}.${method.name} :: ${method.desc}: No method body".replace('$', '_');
							line(method.getHaxeNativeBody { rbody?.genBodyWithFeatures(method) ?: Indenter("throw '${str}';") }.toString().template())
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
			for (e in getClassStrings(clazz.name)) {
				line("$unreflective static private var ${getStringId(e.id)}:$JAVA_LANG_STRING;")
			}

			line("$unreflective static public function SI()") {
				for (e in getClassStrings(clazz.name)) line("${getStringId(e.id)} = N.strLit(${e.str.quote()});")
				if (clazz.hasStaticInit) {
					val methodName = clazz.staticInitMethod!!.targetName
					line("$methodName();")
				}
			}
		}

		val classCodeIndenter = Indenter.gen {
			line("package ${clazz.name.targetGeneratedFqPackage};")

			if (isAbstract) line("// ABSTRACT")
			var declaration = "$classType $simpleClassName"
			if (isInterface) {
				if (clazz.implementing.isNotEmpty()) declaration += getInterfaceList("extends")
			} else {
				val clazzExtending = clazz.extending
				if (clazzExtending != null && clazz.name.fqname != "java.lang.Object") declaration += " extends ${clazzExtending.targetClassFqName}"
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
					line("$unreflective public function new()") {
						line(if (isRootObject) "" else "super();")
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
					line("$unreflective public function toString():String { return N.toNativeString(this.$toStringTargetName()); }")
					line("$unreflective public function hashCode():Int { return this.$hashCodeTargetName(); }")
				}

				if (!isInterface) {
					line(addClassInit(clazz))
					//line(dumpClassInfo(clazz))
				}
			}

			if (availableOnTargets != null) {
				line("#end")
			}

			if (isInterface) {
				line("$unreflective class ${simpleClassName}_IFields") {
					line("$unreflective public function new() {}")
					for (field in clazz.fields) line(writeField(field, isInterface = false))
					for (method in clazz.methods.filter { it.isStatic }) line(writeMethod(method, isInterface = false))
					line(addClassInit(clazz))
				}
			}
		}

		val lineMappings = hashMapOf<Int, Int>()

		val fileStr = classCodeIndenter.toString { sb, line, data ->
			if (data is AstStm.LINE) lineMappings[line] = data.line
		}

		val haxeFilePath = clazz.name.targetFilePath
		vfs[haxeFilePath] = fileStr
		vfs["$haxeFilePath.map"] = Sourcemaps.encodeFile(vfs[haxeFilePath].realpathOS, fileStr, clazz.source, lineMappings)
	}

	override fun buildStaticInit(clazzName: FqName) = null

	override val FqName.targetNameForStatic: String get() {
		val clazz = program[this]
		val simpleName = this.targetSimpleName
		val suffix = if (clazz.isInterface) ".${simpleName}_IFields" else ""
		return clazz.name.targetClassFqName + suffix
	}

	override val FqName.targetName: String get() = this.targetClassFqName

	override fun buildTemplateClass(clazz: FqName): String = clazz.targetClassFqName

	val ENABLED_MINIFY = true
	private val ENABLED_MINIFY_MEMBERS = ENABLED_MINIFY && minimize
	private val ENABLED_MINIFY_CLASSES = ENABLED_MINIFY && minimize

	val minClassPrefix = "z."

	@Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
	private fun _getHaxeFqName(name: FqName): FqName {
		val realclass = if (name in program) program[name] else null
		return FqName(classNames.getOrPut2(name) {
			if (realclass?.nativeName != null) {
				realclass!!.nativeName!!
			} else if (ENABLED_MINIFY_CLASSES && !realclass.keepName) {
				minClassPrefix + allocClassName()
			} else {
				FqName(name.packageParts.map { if (it in keywords) "${it}_" else it }.map(String::decapitalize), "${name.simpleName.replace('$', '_')}_".capitalize()).fqname
			}
		})
	}

	override var baseElementPrefix = "jt_"

	override val FqName.targetFilePath: String get() = this.targetGeneratedFqName.internalFqname + ".hx"
	override val FqName.targetGeneratedFqPackage: String get() = _getHaxeFqName(this).packagePath
	override val FqName.targetGeneratedFqName: FqName get() = _getHaxeFqName(this)
	override val FqName.targetSimpleName: String get() = _getHaxeFqName(this).simpleName
	override val FqName.targetClassFqName: String get() = program.getOrNull(this)?.nativeName ?: this.targetGeneratedFqName.fqname

	override val NullType = "Dynamic"
	override val VoidType = "Void"
	override val BoolType = "Bool"
	override val IntType = "Int"
	override val FloatType = "Float32"
	override val DoubleType = "Float64"
	override val LongType = "haxe.Int64"
	override val BaseArrayType = "JA_0"
	override val BoolArrayType = "JA_Z"
	override val ByteArrayType = "JA_B"
	override val CharArrayType = "JA_C"
	override val ShortArrayType = "JA_S"
	override val IntArrayType = "JA_I"
	override val LongArrayType = "JA_J"
	override val FloatArrayType = "JA_F"
	override val DoubleArrayType = "JA_D"
	override val ObjectArrayType = "JA_L"

	override val DoubleNegativeInfinityString = "Math.NEGATIVE_INFINITY"
	override val DoublePositiveInfinityString = "Math.POSITIVE_INFINITY"
	override val DoubleNanString = "Math.NaN"

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

	//override val AstMethod.targetIsOverriding: Boolean get() = this.isOverriding && !this.isInstanceInit

	override val AstType.localDeclType: String get() = "var"
}

data class ConfigHaxeAddSubtarget(val subtarget: HaxeAddSubtarget)

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
