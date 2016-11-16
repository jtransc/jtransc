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
import com.jtransc.error.InvalidOperationException
import com.jtransc.error.invalidOp
import com.jtransc.error.unexpected
import com.jtransc.ffi.StdCall
import com.jtransc.gen.GenTargetDescriptor
import com.jtransc.gen.GenTargetProcessor
import com.jtransc.gen.MinimizedNames
import com.jtransc.gen.common.*
import com.jtransc.injector.Injector
import com.jtransc.injector.Singleton
import com.jtransc.io.ProcessResult2
import com.jtransc.io.ProcessUtils
import com.jtransc.lang.nullMap
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
	override val name = "haxe"
	override val longName = "Haxe"
	override val sourceExtension = "hx"
	override val outputExtension = "bin"
	override val extraLibraries = listOf<String>()
	override val extraClasses = listOf<String>()
	override val runningAvailable: Boolean = true
	override fun getProcessor(injector: Injector): GenTargetProcessor {
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

		injector.mapInstance(ConfigFeatureSet(HaxeFeatures))

		injector.mapInstance(ConfigHaxeAddSubtarget(availableHaxeSubtargets.last { it.name == actualSubtargetName || actualSubtargetName in it.alias }))

		injector.mapInstance(ConfigSrcFolder(HaxeGenTools.getSrcFolder(targetDirectory)))
		injector.mapImpl<CommonNames, HaxeNames>()
		injector.mapInstance(CommonGenFolders(settings.assets.map { LocalVfs(it) }))

		injector.mapInstance(ConfigTargetFolder(targetFolder))
		injector.mapInstance(ConfigOutputFile2(targetFolder[outputFileBaseName].realfile))

		injector.mapImpl<CommonProgramTemplate, HaxeTemplateString>()

		return injector.get<HaxeGenTargetProcessor>()
	}

	override fun getTargetByExtension(ext: String): String? = when (ext) {
		"js" -> "haxe:js"
		"php" -> "haxe:php"
		"exe" -> "haxe:cpp"
		"swf" -> "haxe:swf"
		else -> null
	}
}

//val HaxeFeatures = setOf(GotosFeature::class.java, SwitchesFeature::class.java)
val HaxeFeatures = setOf(SwitchFeature::class.java)

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
		return LocalVfs(File("$baseDir")).ensuredir()["src"]
	}
}

val cmpvfs: SyncVfsFile by lazy { HaxeCompiler.ensureHaxeCompilerVfs() }

// @TODO: Make this generic!
@Singleton
class HaxeTemplateString(
	injector: Injector,
	settings: AstBuildSettings,
	configActualSubtarget: ConfigHaxeAddSubtarget,
	haxeConfigMergedAssetsFolder: HaxeConfigMergedAssetsFolder
) : CommonProgramTemplate(injector) {
	val actualSubtarget = configActualSubtarget.subtarget
	val srcFolder = HaxeGenTools.getSrcFolder(tempdir)
	val mergedAssetsDir = haxeConfigMergedAssetsFolder.mergedAssetsFolder

	init {
		HaxeCompiler.ensureHaxeSubtarget(actualSubtarget.name)
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
		params["tempAssetsDir"] = mergedAssetsDir.absolutePath // @deprecated
		params["mergedAssetsDir"] = mergedAssetsDir.absolutePath
		params["srcFolder"] = srcFolder.realpathOS
		params["buildFolder"] = srcFolder.parent.realpathOS
		params["haxeExtraFlags"] = program.haxeExtraFlags(settings)
		params["haxeExtraDefines"] = program.haxeExtraDefines(settings)
	}
}

@Singleton
class HaxeConfigMergedAssetsFolder(configTargetDirectory: ConfigTargetDirectory) {
	val targetDirectory = configTargetDirectory.targetDirectory
	val mergedAssetsFolder: File get() = File("$targetDirectory/merged-assets")
}

@Singleton
class HaxeGenTargetProcessor(
	val injector: Injector,
	val program: AstProgram,
	val configTargetDirectory: ConfigTargetDirectory,
	val configHaxeAddSubtarget: ConfigHaxeAddSubtarget,
	val haxeConfigMergedAssetsFolder: HaxeConfigMergedAssetsFolder,
	val settings: AstBuildSettings,
	val folders: CommonGenFolders,
	val haxeTemplateString: CommonProgramTemplate,
	val configOutputFile2: ConfigOutputFile2,
	val configSrcFolder: ConfigSrcFolder,
	val names: CommonNames,
	val gen: GenHaxeGen
) : GenTargetProcessor() {
	val actualSubtarget = configHaxeAddSubtarget.subtarget
	val targetDirectory = configTargetDirectory.targetDirectory
	val tempdir = configTargetDirectory.targetDirectory
	val mergedAssetsFolder = haxeConfigMergedAssetsFolder.mergedAssetsFolder
	val mergedAssetsVfs by lazy { LocalVfs(mergedAssetsFolder) }
	val outputFile2 = configOutputFile2.file
	val srcFolder = configSrcFolder.srcFolder

	override fun buildSource() {
		gen._write()
		haxeTemplateString.setInfoAfterBuildingSource()
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
		for (file in copyFilesBeforeBuildTemplate) buildVfs[file] = haxeTemplateString.gen(program.resourcesVfs[file].readString())

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

		val cmdAll = haxeTemplateString.gen(lines2.joinToString("\n")).split("\n").map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }
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

		val runner = actualSubtarget.interpreter
		val arguments = listOf(outputFile2.absolutePath + actualSubtarget.interpreterSuffix)

		log.info("Running: $runner ${arguments.joinToString(" ")}")
		return measureProcess("Running") {
			ProcessUtils.run(parentDir, runner, arguments, options = ExecOptions(passthru = redirect))
		}
	}
}

val HaxeKeywords = setOf(
	"haxe",
	"Dynamic",
	"Void",
	"java",
	"package",
	"import",
	"class", "interface", "extends", "implements",
	"internal", "private", "protected", "final",
	"function", "var", "const",
	"if", "else",
	"switch", "case", "default",
	"do", "while", "for", "each", "in",
	"try", "catch", "finally",
	"break", "continue",
	"int", "uint", "void",
	"goto"
)

@Singleton
class HaxeNames(
	program: AstResolver,
	configMinimizeNames: ConfigMinimizeNames
) : CommonNames(program, keywords = HaxeKeywords) {
	val minimize: Boolean = configMinimizeNames.minimizeNames

	companion object {
		val HaxeSpecial = setOf(
			"hx",
			"z", // used for package
			"N", // used for HaxeNatives
			"NN", // used for HaxeNatives without references to other classes
			"R", // used for reflect
			"SI", // STATIC INIT
			"SII", // STATIC INIT INITIALIZED
			"HAXE_CLASS_INIT", // Information about the class
			"HAXE_CLASS_NAME", // Information about the class
			"HaxeNatives", // used for HaxeNatives
			"unix",
			"OVERFLOW", // iphone sdk
			"UNDERFLOW" // iphone sdk
		)

		val HaxeKeywordsWithToStringAndHashCode: Set<String> = HaxeKeywords + HaxeSpecial + setOf("toString", "hashCode")
	}

	override val stringPoolType: StringPoolType = StringPoolType.PER_CLASS

	override fun buildConstructor(method: AstMethod): String = "new ${getClassFqName(method.containingClass.name)}().${getHaxeMethodName(method)}"

	override fun buildStaticInit(clazz: AstClass): String = getClassStaticInit(clazz.ref, "template sinit")

	override fun buildMethod(method: AstMethod, static: Boolean): String {
		val clazz = getClassFqName(method.containingClass.name)
		val name = getHaxeMethodName(method)
		return if (static) "$clazz.$name" else "$name"
	}

	override fun getNativeName(local: LocalParamRef): String = super.getNativeName(local)
	override fun getNativeName(field: FieldRef): String = getFieldName(field)
	override fun getNativeName(method: MethodRef): String = getHaxeMethodName(method.ref)
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

	private var minClassLastId: Int = 0
	private var minMemberLastId: Int = 0
	private val classNames = hashMapOf<FqName, FqName>()
	private val methodNames = hashMapOf<Any?, String>()
	private val fieldNames = hashMapOf<Any?, String>()

	val minClassPrefix = "z."
	//val minClassPrefix = ""

	private fun <T> Set<T>.runUntilNotInSet(callback: () -> T): T {
		while (true) {
			val result = callback()
			if (result !in this) return result
		}
	}

	fun allocClassName(): String = HaxeKeywordsWithToStringAndHashCode.runUntilNotInSet { MinimizedNames.getTypeNameById(minClassLastId++) }
	fun allocMemberName(): String = HaxeKeywordsWithToStringAndHashCode.runUntilNotInSet { MinimizedNames.getIdNameById(minMemberLastId++) }

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

	override fun getFunctionalType(type: AstType.METHOD): String {
		return type.argsPlusReturnVoidIsEmpty.map { getNativeType(it, GenCommonGen.TypeKind.TYPETAG) }.joinToString(" -> ")
	}

	override fun getDefault(type: AstType): Any? = type.getNull()

	private fun _getHaxeFqName(name: FqName): FqName {
		val realclass = if (name in program) program[name]!! else null
		return classNames.getOrPut2(name) {
			if (realclass?.nativeName != null) {
				FqName(realclass!!.nativeName!!)
			} else if (ENABLED_MINIFY_CLASSES && !realclass.keepName) {
				FqName(minClassPrefix + allocClassName())
			} else {
				FqName(name.packageParts.map { if (it in HaxeKeywords) "${it}_" else it }.map { it.decapitalize() }, "${name.simpleName.replace('$', '_')}_".capitalize())
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
			realfield?.nativeName ?: normalizedFieldName
		} else {
			fieldNames.getOrPut2(keyToUse) {
				if (ENABLED_MINIFY_MEMBERS && !realfield.keepName) {
					allocMemberName()
				} else {
					// @TODO: Move to CommonNames
					if (field !in cachedFieldNames) {
						val fieldName = normalizedFieldName
						var name = if (fieldName in HaxeKeywordsWithToStringAndHashCode) "${fieldName}_" else fieldName

						val clazz = program[field]?.containingClass
						val clazzAncestors = clazz?.ancestors?.reversed() ?: listOf()
						val names = clazzAncestors.flatMap { it.fields }.filter { it.name == field.name }.map { getFieldName(it.ref) }.toHashSet()
						val fieldsColliding = clazz?.fields?.filter {
							(it.ref == field) || (normalizeName(it.name) == normalizedFieldName)
						}?.map { it.ref } ?: listOf(field)

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
		val suffix = if (clazz?.isInterface ?: false) ".${simpleName}_IFields" else ""
		return getClassFqName(clazz?.name ?: name) + "$suffix"
	}

	override fun getClassFqNameLambda(name: FqName): String {
		val clazz = program[name]
		val simpleName = getGeneratedSimpleClassName(name)
		return getClassFqName(clazz?.name ?: name) + ".${simpleName}_Lambda"
	}

	override fun getClassStaticInit(classRef: AstType.REF, reason: String): String {
		val clazz = program[classRef.name]
		if (clazz?.nativeName != null) {
			return ""
		} else {
			return "${getClassFqNameInt(classRef.name)}.SI() /* $reason */;"
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
}

@Singleton
class GenHaxeGen(injector: Injector) : GenCommonGenFilePerClass(injector) {
	val subtarget = injector.get<ConfigSubtarget>().subtarget
	override val defaultGenStmSwitchHasBreaks = false

	companion object {
		//const val ENABLE_HXCPP_GOTO_HACK = true
		const val ENABLE_HXCPP_GOTO_HACK = false
	}

	override fun genBody2WithFeatures(method: AstMethod, body: AstBody): Indenter {
		return if (ENABLE_HXCPP_GOTO_HACK && (subtarget in setOf("cpp", "windows", "linux", "mac", "android"))) {
			features.apply(method, body, (featureSet + setOf(GotosFeature::class.java)), settings, types).genBody()
		} else {
			features.apply(method, body, featureSet, settings, types).genBody()
		}
	}

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
		val copyFilesTemplate = program.classes.flatMap { it.annotationsList.getTyped<HaxeAddFilesTemplate>()?.value?.toList() ?: listOf() }

		for (file in copyFilesRaw) vfs[file] = program.resourcesVfs[file]
		for (file in copyFilesTemplate) vfs[file] = templateString.gen(program.resourcesVfs[file].readString(), context, "copyFilesTemplate")

		val mainClassFq = program.entrypoint
		val mainClass = mainClassFq.haxeClassFqName
		val mainMethod = program[mainClassFq].getMethod("main", types.build { METHOD(VOID, ARRAY(STRING)) }.desc)!!.targetName
		val entryPointClass = FqName(mainClassFq.fqname + "_EntryPoint")
		val entryPointFilePath = entryPointClass.haxeFilePath
		val entryPointFqName = entryPointClass.haxeGeneratedFqName
		val entryPointSimpleName = entryPointClass.haxeGeneratedSimpleClassName
		val entryPointPackage = entryPointFqName.packagePath

		fun calcClasses(program: AstProgram, mainClass: AstClass): List<AstClass> {
			return sortDependenciesSimple(mainClass) {
				it.classDependencies.map { program.get3(it) }
			}
		}

		fun inits() = Indenter.gen {
			line("HaxePolyfills.install();");
			line("haxe.CallStack.callStack();")
			line(names.getClassStaticInit(program[mainClassFq].ref, "program main"))
		}

		val customMain = program.allAnnotationsList.getTyped<HaxeCustomMain>()?.value

		val plainMain = Indenter.genString {
			line("package {{ entryPointPackage }};")
			line("class {{ entryPointSimpleName }}") {
				line("static public function main()") {
					line("{{ inits }}")
					line("{{ mainClass }}.{{ mainMethod }}(HaxeNatives.strArray(HaxeNatives.args()));")
				}
			}
		}

		log("Using ... " + if (customMain != null) "customMain" else "plainMain")

		templateString.setExtraData(mapOf(
			"entryPointPackage" to entryPointPackage,
			"entryPointSimpleName" to entryPointSimpleName,
			"mainClass" to mainClass,
			"mainClass2" to mainClassFq.fqname,
			"mainMethod" to mainMethod,
			"inits" to inits().toString()
		))
		vfs[entryPointFilePath] = templateString.gen(customMain ?: plainMain)

		vfs["HaxeReflectionInfo.hx"] = Indenter.genString {
			line("class HaxeReflectionInfo") {
				line("static public function __registerClasses()") {
					for (clazz in program.classes) {
						if (clazz.nativeName == null) {

							val availableOnTargets = clazz.annotationsList.getTyped<HaxeAvailableOnTargets>()?.value

							if (availableOnTargets != null) line("#if (" + availableOnTargets.joinToString(" || ") + ")")
							line("R.register(${clazz.ref.fqname.quote()}, ${clazz.ref.name.haxeClassFqName.quote()}, ${names.getClassStaticClassInit(clazz.ref)});")
							if (availableOnTargets != null) line("#end")
						}
					}
				}
			}
			//line(annotationProxyTypes)
		}

		injector.mapInstance(ConfigEntryPointClass(entryPointClass))
		injector.mapInstance(ConfigEntryPointFile(entryPointFilePath))
		//injector.mapInstance(ConfigHaxeVfs(vfs))
	}

	fun annotation(a: AstAnnotation): String {
		fun escapeValue(it: Any?): String {
			return when (it) {
				null -> "null"
				is AstAnnotation -> annotation(it)
				is Pair<*, *> -> escapeValue(it.second)
				is AstFieldRef -> it.containingTypeRef.name.haxeClassFqName + "." + it.haxeName
				is AstFieldWithoutTypeRef -> program[it.containingClass].ref.name.haxeClassFqName + "." + program.get(it).haxeName
				is String -> "HaxeNatives.boxString(${it.quote()})"
				is Boolean, is Byte, is Short, is Char, is Int, is Long, is Float, is Double -> names.escapeConstant(it)
				is List<*> -> "[" + it.map { escapeValue(it) }.joinToString(", ") + "]"
				is com.jtransc.org.objectweb.asm.Type -> "HaxeNatives.resolveClass(" + it.descriptor.quote() + ")"
				else -> invalidOp("GenHaxeGen.annotation.escapeValue: Don't know how to handle value ${it.javaClass.name} : ${it.toBetterString()} while generating $context")
			}
		}
		//val itStr = a.elements.map { it.key.quote() + ": " + escapeValue(it.value) }.joinToString(", ")
		val annotation = program.get3(a.type)
		val itStr = annotation.methods.map { escapeValue(if (it.name in a.elements) a.elements[it.name]!! else it.defaultTag) }.joinToString(", ")
		return "new ${names.getFullAnnotationProxyName(a.type)}([$itStr])"
	}

	fun annotationInit(a: AstAnnotation): List<AstType.REF> {
		fun escapeValue(it: Any?): List<AstType.REF> {
			return when (it) {
				null -> listOf()
				is AstAnnotation -> annotationInit(it)
				is Pair<*, *> -> escapeValue(it.second)
				is AstFieldRef -> listOf(it.containingTypeRef)
				is AstFieldWithoutTypeRef -> listOf(it.containingClass.ref())
				is List<*> -> it.flatMap { escapeValue(it) }
				else -> listOf()
			}
		}
		//val itStr = a.elements.map { it.key.quote() + ": " + escapeValue(it.value) }.joinToString(", ")
		val annotation = program.get3(a.type)
		return annotation.methods.flatMap {
			escapeValue(if (it.name in a.elements) a.elements[it.name]!! else it.defaultTag)
		}
	}

	fun visibleAnnotations(annotations: List<AstAnnotation>): String = "[" + annotations.filter { it.runtimeVisible }.map { annotation(it) }.joinToString(", ") + "]"
	fun visibleAnnotationsList(annotations: List<List<AstAnnotation>>): String = "[" + annotations.map { visibleAnnotations(it) }.joinToString(", ") + "]"

	fun annotationsInit(annotations: List<AstAnnotation>): Indenter {
		return Indenter.gen {
			for (i in annotations.filter { it.runtimeVisible }.flatMap { annotationInit(it) }.toHashSet()) {
				line(names.getClassStaticInit(i, "annotationsInit"))
			}
		}
	}

	fun dumpClassInfo(clazz: AstClass) = Indenter.genString {
		line("static public var HAXE_CLASS_NAME = ${clazz.name.fqname.quote()};")
		line("static public function HAXE_CLASS_INIT(c:$JAVA_LANG_CLASS = null):$JAVA_LANG_CLASS") {
			line("if (c == null) c = new $JAVA_LANG_CLASS();")
			line("c.$JAVA_LANG_CLASS_name = N.strLit(HAXE_CLASS_NAME);")
			//line("info(c, \"${clazz.name.haxeGeneratedFqName}\", " + (clazz.extending?.fqname?.quote() ?: "null") + ", [" + clazz.implementing.map { "\"${it.fqname}\"" }.joinToString(", ") + "], ${clazz.modifiers}, " + annotations(clazz.runtimeAnnotations) + ");")
			line(annotationsInit(clazz.runtimeAnnotations))
			val proxyClassName = if (clazz.isInterface) clazz.name.haxeGeneratedFqName.fqname + "." + clazz.name.haxeGeneratedSimpleClassName + "_Proxy" else "null"
			val ffiClassName = if (clazz.hasFFI) clazz.name.haxeGeneratedFqName.fqname + "." + clazz.name.haxeGeneratedSimpleClassName + "_FFI" else "null"
			line("R.i(c, ${clazz.name.haxeGeneratedFqName}, $proxyClassName, $ffiClassName, " + (clazz.extending?.fqname?.quote() ?: "null") + ", [" + clazz.implementing.map { "\"${it.fqname}\"" }.joinToString(", ") + "], ${clazz.modifiers}, " + visibleAnnotations(clazz.runtimeAnnotations) + ");")
			if (clazz.isVisible()) {
				for ((slot, field) in clazz.fields.withIndex()) {
					val internalName = field.haxeName
					if (field.isVisible()) {
						line("R.f(c, ${internalName.quote()}, $slot, \"${field.name}\", \"${field.desc}\", ${field.modifiers}, ${field.genericSignature.quote()}, ${visibleAnnotations(field.annotations)});");
					}
				}
				for ((slot, method) in clazz.methods.withIndex()) {
					val internalName = method.targetName
					if (method.isVisible()) {
						if (method.name == "<init>") {
							line("R.c(c, ${internalName.quote()}, $slot, ${method.modifiers}, ${method.signature.quote()}, ${method.genericSignature.quote()}, ${visibleAnnotations(method.annotations)}, ${visibleAnnotationsList(method.parameterAnnotations)});");
						} else if (method.name == "<clinit>") {
						} else {
							line("R.m(c, ${method.id}, ${internalName.quote()}, $slot, \"${method.name}\", ${method.modifiers}, ${method.desc.quote()}, ${method.genericSignature.quote()}, ${visibleAnnotations(method.annotations)}, ${visibleAnnotationsList(method.parameterAnnotations)});");
						}
					}
				}
			}
			line("return c;")
		}
	}

	override fun genExprMethodClass(e: AstExpr.INVOKE_DYNAMIC_METHOD): String {
		val methodInInterfaceRef = e.methodInInterfaceRef
		val methodToConvertRef = e.methodToConvertRef
		val interfaceName = methodInInterfaceRef.classRef.name

		val interfaceLambdaFqname = interfaceName.haxeLambdaName
		return "new $interfaceLambdaFqname(" + Indenter.genString {
			//methodInInterfaceRef.type.args

			val argNameTypes = methodInInterfaceRef.type.args.map { it.haxeNameAndType }.joinToString(", ")

			line("function($argNameTypes)") {
				// @TODO: Static + non-static
				//val methodToCallClassName = methodToConvertRef.classRef.name.haxeClassFqName
				//val methodToCallName = methodToConvertRef.haxeName

				val args = methodInInterfaceRef.type.args.map { AstLocal(-1, it.name, it.type) }

				line("return " + genExpr2(AstExpr.CAST(AstExpr.CALL_STATIC(
					methodToConvertRef.containingClassType,
					methodToConvertRef,
					args.zip(methodToConvertRef.type.args).map { AstExpr.CAST(AstExpr.LOCAL(it.first), it.second.type) }
				), methodInInterfaceRef.type.ret)) + ";"
				)
			}
		} + ")"	}

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
		line(names.getClassStaticInit(clazzRef, reasons.joinToString(", ")))
	}

	private fun getStringId(id: Int) = "__str$id"
	private fun getStringId(clazz: FqName, str: String) = getStringId(names.allocString(clazz, str))

	override fun genExprThis(e: AstExpr.THIS): String = "this"
	override fun genLiteralString(v: String): String = getStringId(context.clazz.name, v)

	override fun genExprArrayLength(e: AstExpr.ARRAY_LENGTH): String {
		val type = e.array.type
		return if (type is AstType.ARRAY) {
			"(${e.array.genNotNull()}).length"
		} else {
			"cast(${e.array.genNotNull()}, ${names.BaseArrayType}).length"
		}
	}

	override fun convertToFromTarget(type: AstType, text: String, toTarget: Boolean): String {
		if (type is AstType.ARRAY) {
			return (if (toTarget) "HaxeNatives.unbox($text)" else "cast(HaxeNatives.box($text), ${names.getNativeType(type, TypeKind.CAST)})")
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
	override fun N_i2j(str: String) = "HaxeNatives.intToLong($str)"
	override fun N_i2f(str: String) = "Math.fround(($str))"
	override fun N_i2d(str: String) = "($str)"
	override fun N_f2f(str: String) = "Math.fround($str)"
	override fun N_f2d(str: String) = "($str)"
	override fun N_d2f(str: String) = "Math.fround(($str))"
	override fun N_d2d(str: String) = "($str)"
	override fun N_d2i(str: String) = "Std.int($str)"
	override fun N_l2i(str: String) = "(($str).low)"
	override fun N_l2l(str: String) = "($str)"
	override fun N_l2f(str: String) = "HaxeNatives.longToFloat($str)"
	override fun N_l2d(str: String) = "HaxeNatives.longToFloat($str)"
	override fun N_getFunction(str: String) = "HaxeNatives.getFunction($str)"
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

		return if (bodies.size > 0) {
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
		val isAbstract = (clazz.classType == AstClassType.ABSTRACT)
		val isNormalClass = (clazz.classType == AstClassType.CLASS)
		val classType = if (isInterface) "interface" else "class"
		val simpleClassName = clazz.name.haxeGeneratedSimpleClassName
		fun getInterfaceList(keyword: String) = (if (clazz.implementing.isNotEmpty()) " $keyword " else "") + clazz.implementing.map { it.haxeClassFqName }.joinToString(" $keyword ")
		//val implementingString = getInterfaceList("implements")
		val isInterfaceWithStaticMembers = isInterface && clazz.fields.any { it.isStatic }
		//val isInterfaceWithStaticFields = clazz.name.withSimpleName(clazz.name.simpleName + "\$StaticMembers")
		refs._usedDependencies.clear()

		if (!clazz.extending?.fqname.isNullOrEmpty()) refs.add(AstType.REF(clazz.extending!!))
		for (impl in clazz.implementing) refs.add(AstType.REF(impl))
		//val interfaceClassName = clazz.name.append("_Fields");

		fun writeField(field: AstField, isInterface: Boolean): Indenter = Indenter.gen {
			val static = if (field.isStatic) "static " else ""
			val visibility = if (isInterface) " " else field.visibility.haxe
			val fieldType = field.type
			refs.add(fieldType)
			val defaultValue: Any? = if (field.hasConstantValue) field.constantValue else fieldType.haxeDefault
			val fieldName = field.haxeName
			//if (field.name == "this\$0") println("field: $field : fieldRef: ${field.ref} : $fieldName")
			if (!field.annotationsList.contains<HaxeRemoveField>()) {
				val keep = if (field.annotationsList.contains<JTranscKeep>()) "@:keep " else ""
				line("$keep$static$visibility var $fieldName:${fieldType.targetTypeTag} = ${names.escapeConstant(defaultValue, fieldType)}; // /*${field.name}*/")
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
					"$static $visibility $inline $override function ${method.targetName}/*${method.name}*/(${margs.joinToString(", ")}):${rettype.targetTypeTag}".trim()
				} catch (e: RuntimeException) {
					println("@TODO abstract interface not referenced: ${method.containingClass.fqname} :: ${method.name} : $e")
					//null
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
							if (method.name == "throwParameterIsNullException") line("HaxeNatives.debugger();")
							val javaBody = if (rbody != null) {
								rbody.genBodyWithFeatures(method)
							} else Indenter.gen {
								line("throw R.n(HAXE_CLASS_NAME, ${method.id});")
							}
							line(method.getHaxeNativeBody(javaBody).toString().template())
							if (method.methodVoidReturnThis) line("return this;")
						} catch (e: Throwable) {
							//e.printStackTrace()
							log.warn("WARNING haxe_gen.writeMethod:" + e.message)

							line("HaxeNatives.debugger(); throw " + "Errored method: ${clazz.name}.${method.name} :: ${method.desc} :: ${e.message}".quote() + ";")
						}
					}
				}
			}
		}

		fun addClassInit(clazz: AstClass) = Indenter.gen {
			line("static public var SII = false;");
			for (e in names.getClassStrings(clazz.name)) {
				line("static private var ${getStringId(e.id)}:$JAVA_LANG_STRING;")
			}

			line("static public function SI()") {
				line("if (SII) return;")
				line("SII = true;")

				for (e in names.getClassStrings(clazz.name)) {
					line("${getStringId(e.id)} = ${names.escapeConstant(e.str)};")
				}

				if (clazz.hasStaticInit) {
					val methodName = clazz.staticInitMethod!!.targetName
					line("$methodName();")
				}
			}
		}


		//val annotationTypeHaxeName = AstMethodRef(java.lang.annotation.Annotation::class.java.name.fqname, "annotationType", AstType.build { METHOD(java.lang.annotation.Annotation::class.java.ast()) }).haxeName
		//val annotationTypeHaxeName = AstMethodRef(java.lang.annotation.Annotation::class.java.name.fqname, "annotationType", types.build { METHOD(CLASS) }).targetName
		// java.lang.annotation.Annotation
		//abstract fun annotationType():Class<out Annotation>

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
					line("public function new()") {
						line(if (isRootObject) "" else "super();")
						line("SI();")
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

				// @TODO: Check!
				//if (!isInterface) {
				//	//println(clazz.fqname + " -> " + program.getAllInterfaces(clazz))
				//	val isFunctionType = program.isImplementing(clazz, JTranscFunction::class.java.name)
				//
				//	if (isFunctionType) {
				//		val executeFirst = clazz.methodsByName["execute"]!!.first()
				//		line("public const _execute:Function = ${executeFirst.ref.haxeName};")
				//	}
				//}

				/*
				if (isNormalClass) {
					val override = if (isRootObject) " " else "override "
					line("$override public function toString():String { return HaxeNatives.toNativeString(this.toString__Ljava_lang_String_()); }")
				}
				*/
				if (isRootObject) {
					line("public function toString():String { return HaxeNatives.toNativeString(this.$toStringTargetName()); }")
					line("public function hashCode():Int { return this.$hashCodeTargetName(); }")
				}

				if (!isInterface) {
					line(addClassInit(clazz))
					line(dumpClassInfo(clazz))
				}
			}

			if (availableOnTargets != null) {
				line("#end")
			}

			//if (isInterfaceWithStaticMembers) {
			if (isInterface) {
				val javaLangObjectClass = program[FqName("java.lang.Object")]

				line("class ${simpleClassName}_IFields") {
					line("public function new() {}")
					for (field in clazz.fields) line(writeField(field, isInterface = false))
					for (method in clazz.methods.filter { it.isStatic }) line(writeMethod(method, isInterface = false))
					line(addClassInit(clazz))
					line(dumpClassInfo(clazz))
				}

				/*
				if (clazz in allAnnotationTypes) {
					line("// annotation type: ${clazz.name}")

					line("class ${names.getAnnotationProxyName(clazz.astType)} extends ${names.nativeName<JTranscAnnotationBase>()} implements ${clazz.name.haxeClassFqName}") {
						line("private var _data:Array<Dynamic>;")
						line("public function new(_data:Dynamic = null) { super(); this._data = _data; }")

						line("public function $annotationTypeHaxeName():$JAVA_LANG_CLASS { return HaxeNatives.resolveClass(${clazz.fqname.quote()}); }")
						line("override public function $getClassTargetName():$JAVA_LANG_CLASS { return HaxeNatives.resolveClass(${clazz.fqname.quote()}); }")
						for ((index, m) in clazz.methods.withIndex()) {
							line("public function ${m.targetName}():${m.methodType.ret.targetTypeTag} { return this._data[$index]; }")
						}
					}
				}

				if (clazz.hasFFI) {
					line("class ${simpleClassName}_FFI extends $JAVA_LANG_OBJECT implements $simpleClassName implements HaxeFfiLibrary") {
						val methods = clazz.allMethodsToImplement.map { clazz.getMethodInAncestorsAndInterfaces(it)!! }
						line("private var __ffi_lib:haxe.Int64 = 0;")
						for (method in methods) {
							line("private var __ffi_${method.name}:haxe.Int64 = 0;")
						}
						line("@:noStack public function _ffi__load(library:String)") {
							//line("trace('Loading... \$library');")
							line("#if cpp")
							line("__ffi_lib = HaxeDynamicLoad.dlopen(library);")
							line("if (__ffi_lib == 0) trace('Cannot open library: \$library');")
							for (method in methods) {
								line("__ffi_${method.name} = HaxeDynamicLoad.dlsym(__ffi_lib, '${method.name}');")
								line("if (__ffi_${method.name} == 0) trace('Cannot load method ${method.name}');")
							}
							line("#end")
						}
						line("@:noStack public function _ffi__close()") {
							line("#if cpp")
							line("HaxeDynamicLoad.dlclose(__ffi_lib);")
							line("#end")
						}

						fun AstType.castToHaxe(): String {
							return when (this) {
								AstType.VOID -> ""
								AstType.BOOL -> "(bool)"
								AstType.INT -> "(int)"
								AstType.LONG -> "(int)" // @TODO!
							//AstType.STRING -> "char*"
								else -> "(void*)"
							}
						}

						fun AstType.nativeType(): String {
							return when (this) {
								AstType.VOID -> "void"
								AstType.BOOL -> "bool"
								AstType.INT -> "int"
								AstType.LONG -> "int" // @TODO!
								AstType.STRING -> "char*"
								else -> "void*"
							}
						}

						fun AstType.castToNative(): String {
							return "(${this.nativeType()})"
						}

						fun AstType.castToNativeHx(str: String): String {
							return when (this) {
								AstType.STRING -> "cpp.NativeString.c_str(($str)._str)"
								else -> return str
							}
						}

						fun AstType.METHOD.toCast(stdCall: Boolean): String {
							val argTypes = this.args.map { it.type.nativeType() }
							val typeInfix = if (stdCall) "__stdcall " else " "
							return "(${this.ret.nativeType()} (${typeInfix}*)(${argTypes.joinToString(", ")}))(void *)(size_t)"
						}

						for (method in methods) {
							val methodName = method.ref.targetName
							val methodType = method.methodType
							val margs = methodType.args.map { it.name + ":" + it.type.targetTypeTag }.joinToString(", ")
							val rettype = methodType.ret.targetTypeTag

							val stdCall = method.annotationsList.contains<StdCall>()

							line("@:noStack public function $methodName($margs):$rettype") {
								val argIds = methodType.args.withIndex().map { "${it.value.type.castToNative()}{${(it.index + 1)}}" }.joinToString(", ")
								val cppArgs = (listOf("__ffi_${method.name}") + methodType.args.map { it.type.castToNativeHx(it.name) }).joinToString(", ")
								val mustReturn = methodType.ret != AstType.VOID
								val retstr = if (mustReturn) "return " else ""
								line("#if cpp untyped __cpp__('$retstr ${methodType.ret.castToHaxe()}((${methodType.toCast(stdCall)}{0})($argIds));', $cppArgs); #end")
								if (mustReturn) line("return cast 0;")
							}
						}
					}
				}

				line("class ${simpleClassName}_Proxy extends $JAVA_LANG_OBJECT implements $simpleClassName") {

					line("private var __clazz:$JAVA_LANG_CLASS;")
					line("private var __invocationHandler:$invocationHandlerTargetName;")
					line("private var __methods:Map<Int, $methodTargetName>;")
					line("public function new(handler:$invocationHandlerTargetName)") {
						line("super();")
						line("this.__clazz = HaxeNatives.resolveClass(\"${clazz.name.fqname}\");")
						line("this.__invocationHandler = handler;")
					}
					// public Object invoke(Object proxy, Method method, Object[] args)
					line("private function _invoke(methodId:Int, args:Array<$JAVA_LANG_OBJECT>):$JAVA_LANG_OBJECT") {
						line("var method = this.__clazz.locateMethodById(methodId);");
						line("return this.__invocationHandler.$invokeTargetName(this, method, ${names.ObjectArrayType}.fromArray(args, '[Ljava.lang.Object;'));")
					}

					for (methodRef in clazz.allMethodsToImplement) {
						val mainMethod = clazz.getMethodInAncestorsAndInterfaces(methodRef)
						if (mainMethod == null) {
							println("NULL methodRef: $methodRef")
							continue
						}
						val mainMethodName = mainMethod.ref.targetName
						val methodType = mainMethod.methodType
						val margs = methodType.args.map { it.name + ":" + it.type.targetTypeTag }.joinToString(", ")
						val rettype = methodType.ret.targetTypeTag
						val returnOrEmpty = if (methodType.retVoid) "" else "return "
						val margBoxedNames = methodType.args.map { it.type.box(it.name) }.joinToString(", ")
						val typeStr = methodType.functionalType
						val methodInObject = javaLangObjectClass[mainMethod.ref.withoutClass]
						val methodId = mainMethod.id

						line("${methodInObject.nullMap("override", "")} public function $mainMethodName($margs):$rettype { return " + methodType.ret.unbox("this._invoke($methodId, [$margBoxedNames]") + ");  }")
					}
				}

				val methodsWithoutBody = clazz.methods.filter { it.body == null }
				if (methodsWithoutBody.size == 1 && clazz.implementing.size == 0) {
					// @TODO: Probably it should allow interfaces extending!
					val mainMethod = methodsWithoutBody.first()
					val mainMethodName = mainMethod.ref.targetName
					val methodType = mainMethod.methodType
					val margs = methodType.args.map { it.name + ":" + it.type.targetTypeTag }.joinToString(", ")
					val rettype = methodType.ret.targetTypeTag
					val returnOrEmpty = if (methodType.retVoid) "" else "return "
					val margNames = methodType.args.map { it.name }.joinToString(", ")
					val typeStr = methodType.functionalType
					line("class ${simpleClassName}_Lambda extends $JAVA_LANG_OBJECT implements $simpleClassName") {
						line("private var ___func__:$typeStr;")
						line("public function new(func: $typeStr) { super(); this.___func__ = func; }")
						val methodInObject = javaLangObjectClass[mainMethod.ref.withoutClass]
						line("${methodInObject.nullMap("override", "")} public function $mainMethodName($margs):$rettype { $returnOrEmpty ___func__($margNames); }")
						for (dmethod in clazz.methods.filter { it.body != null }) {
							val dmethodName = dmethod.ref.targetName
							val dmethodArgs = dmethod.methodType.args.map { it.name + ":" + it.type.targetTypeTag }.joinToString(", ")
							val dmethodRettype = dmethod.methodType.ret.targetTypeTag
							line("${methodInObject.nullMap("override", "")} public function $dmethodName($dmethodArgs):$dmethodRettype") {
								line(dmethod.body!!.genBodyWithFeatures(dmethod))
							}
						}
					}
				}
				*/
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
		vfs["$haxeFilePath"] = fileStr
		vfs["$haxeFilePath.map"] = Sourcemaps.encodeFile(vfs["$haxeFilePath"].realpathOS, fileStr, clazz.source, lineMappings)
	}

	//val FqName.as3Fqname: String get() = this.fqname
	//fun AstMethod.getHaxeMethodName(program: AstProgram): String = this.ref.getHaxeMethodName(program)

	val AstVisibility.haxe: String get() = "public"

	val AstType.haxeDefault: Any? get() = names.getDefault(this)
	val AstType.haxeDefaultString: String get() = names.escapeConstant(names.getDefault(this), this)
	val AstType.METHOD.functionalType: String get() = names.getFunctionalType(this)

	fun AstType.box(arg: String): String {
		return when (this) {
			is AstType.Primitive -> N_box(this, arg)
			else -> "cast($arg)";
		}
	}

	fun AstType.unbox(arg: String): String {
		return when (this) {
			is AstType.Primitive -> N_unbox(this, arg)
			else -> "cast($arg)";
		}
	}

	val AstField.haxeName: String get() = names.getFieldName(this)
	val AstFieldRef.haxeName: String get() = names.getFieldName(this)
	//val AstFieldRef.haxeStaticText: String get() = names.getStaticFieldText(this)


	val AstMethod.haxeIsOverriding: Boolean get() = this.isOverriding && !this.isInstanceInit

	val FqName.haxeLambdaName: String get() = names.getClassFqNameLambda(this)
	val FqName.haxeClassFqName: String get() = names.getClassFqName(this)
	val FqName.haxeClassFqNameInt: String get() = names.getClassFqNameInt(this)
	val FqName.haxeFilePath: String get() = names.getFilePath(this)
	val FqName.haxeGeneratedFqPackage: String get() = names.getGeneratedFqPackage(this)
	val FqName.haxeGeneratedFqName: FqName get() = names.getGeneratedFqName(this)
	val FqName.haxeGeneratedSimpleClassName: String get() = names.getGeneratedSimpleClassName(this)
	val AstArgument.haxeNameAndType: String get() = this.name + ":" + this.type.targetTypeTag
}
