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

import com.jtransc.JTranscVersion
import com.jtransc.annotation.haxe.*
import com.jtransc.ast.*
import com.jtransc.ast.feature.SwitchesFeature
import com.jtransc.ds.split
import com.jtransc.gen.GenTarget
import com.jtransc.gen.GenTargetDescriptor
import com.jtransc.gen.GenTargetInfo
import com.jtransc.gen.GenTargetProcessor
import com.jtransc.gen.common.CommonGenFolders
import com.jtransc.gen.common.CommonProgramInfo
import com.jtransc.gen.common.CommonProgramTemplate
import com.jtransc.io.ProcessResult2
import com.jtransc.io.ProcessUtils
import com.jtransc.log.log
import com.jtransc.template.Minitemplate
import com.jtransc.time.measureProcess
import com.jtransc.vfs.*
import java.io.File

object HaxeTarget : GenTargetDescriptor() {
	override val name = "haxe"
	override val longName = "Haxe"
	override val sourceExtension = "hx"
	override val outputExtension = "bin"
	override val extraLibraries = listOf<String>()
	override val extraClasses = listOf<String>()
	override fun getGenerator() = GenHaxe
	override fun getTargetByExtension(ext: String): String? = when (ext) {
		"js" -> "haxe:js"
		"php" -> "haxe:php"
		"exe" -> "haxe:cpp"
		"swf" -> "haxe:swf"
		else -> null
	}
}

//val HaxeFeatures = setOf(GotosFeature, SwitchesFeature)
val HaxeFeatures = setOf(SwitchesFeature)

private val HAXE_LIBS_KEY = UserKey<List<HaxeLib.LibraryRef>>()

fun AstProgram.haxeLibs(settings: AstBuildSettings): List<HaxeLib.LibraryRef> = this.getCached(HAXE_LIBS_KEY) {
	this.classes
		.map { it.annotationsList.getTyped<HaxeAddLibraries>()?.value }
		.filterNotNull()
		.flatMap { it.toList() }
		.map { HaxeLib.LibraryRef.fromVersion(it) }
}

fun AstProgram.haxeExtraFlags(settings: AstBuildSettings): List<Pair<String, String>> {
	return this.haxeLibs(settings).map { "-lib" to it.nameWithVersion }
}

fun AstProgram.haxeExtraDefines(settings: AstBuildSettings): List<String> {
	//-D no-analyzer
	//--times : measure compilation times
	//--no-inline : disable inlining
	//--no-opt : disable code optimizations
	//const_propagation: Implements sparse conditional constant propagation to promote values that are known at compile-time to usage places. Also detects dead branches.
	//copy_propagation: Detects local variables that alias other local variables and replaces them accordingly.
	//local_dce: Detects and removes unused local variables.
	//fusion: Moves variable expressions to its usage in case of single-occurrence. Disabled on Flash and Java.
	//purity_inference: Infers if fields are "pure", i.e. do not have any side-effects. This can improve the effect of the fusion module.
	//unreachable_code: Reports unreachable code.

	return if (settings.analyzer) listOf() else listOf("no-analyzer")
}

fun AstProgram.haxeInstallRequiredLibs(settings: AstBuildSettings) {
	val libs = this.haxeLibs(settings)
	log(":: REFERENCED LIBS: $libs")
	for (lib in libs) {
		log(":: TRYING TO INSTALL LIBRARY $lib")
		HaxeLib.installIfNotExists(lib)
	}
}

fun GenTargetInfo.haxeCopyEmbeddedResourcesToFolder(assetsFolder: File?) {
	val program = this.program
	//program.allAnnotationsList.getTyped<>()
	//val files = program.classes.map { it.annotationsList.getTyped<HaxeAddAssets>()?.value }.filterNotNull().flatMap { it.toList() }
	val files = program.allAnnotationsList.getAllTyped<HaxeAddAssets>().flatMap { it.value.toList() }
	//val assetsFolder = settings.assets.firstOrNull()
	val resourcesVfs = program.resourcesVfs
	log("GenTargetInfo.haxeCopyResourcesToAssetsFolder: $assetsFolder")
	if (assetsFolder != null) {
		assetsFolder.mkdirs()
		val outputVfs = LocalVfs(assetsFolder)
		for (file in files) {
			log("GenTargetInfo.haxeCopyResourcesToAssetsFolder.copy: $file")
			outputVfs[file] = resourcesVfs[file]
		}
	}
}

object HaxeGenTools {
	fun getSrcFolder(tempdir: String): SyncVfsFile {
		//val randomId = "" + System.currentTimeMillis() + ":" + Math.floor(Math.random() * 1000)
		val baseDir = "$tempdir/jtransc-haxe"
		log("Temporal haxe files: $baseDir")
		File("$baseDir/src").mkdirs()
		return LocalVfs(File("$baseDir")).ensuredir()["src"]
	}
}

val GenTargetInfo.mergedAssetsFolder: File get() = File("${this.targetDirectory}/merged-assets")
val cmpvfs: SyncVfsFile by lazy { HaxeCompiler.ensureHaxeCompilerVfs() }

// @TODO: Make this generic!
class HaxeTemplateString(
	names: HaxeNames,
	tinfo: GenTargetInfo,
	settings: AstBuildSettings,
	val actualSubtarget: HaxeAddSubtarget,
	folders: CommonGenFolders,
	outputFile2: File,
    types: AstTypes
)
: CommonProgramTemplate(names, tinfo, settings, folders, outputFile2, types) {
	val srcFolder = HaxeGenTools.getSrcFolder(tempdir)
	val mergedAssetsDir = tinfo.mergedAssetsFolder

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

class HaxeGenTargetProcessor(val tinfo: GenTargetInfo, val settings: AstBuildSettings) : GenTargetProcessor() {
	val actualSubtargetName = tinfo.subtarget
	val availableHaxeSubtargets = tinfo.program.allAnnotations
		.map { it.toObject<HaxeAddSubtargetList>() ?: it.toObject<HaxeAddSubtarget>() }
		.flatMap {
			if (it == null) {
				listOf()
			} else if (it is HaxeAddSubtargetList) {
				it.value.toList()
			} else if (it is HaxeAddSubtarget) {
				listOf(it)
			} else {
				listOf()
			}
		}
		.filterNotNull()
	val actualSubtarget: HaxeAddSubtarget = availableHaxeSubtargets.last { it.name == actualSubtargetName || actualSubtargetName in it.alias }

	val outputFile2 = File(File(tinfo.outputFile).absolutePath)
	//val tempdir = System.getProperty("java.io.tmpdir")
	val tempdir = tinfo.targetDirectory
	var info: GenHaxe.ProgramInfo? = null
	lateinit var gen: GenHaxeGen
	val program = tinfo.program
	val srcFolder = HaxeGenTools.getSrcFolder(tempdir)
	val mergedAssetsFolder = tinfo.mergedAssetsFolder
	val mergedAssetsVfs = LocalVfs(mergedAssetsFolder)
	val names = HaxeNames(program, minimize = settings.minimizeNames)
	val folders = CommonGenFolders(settings.assets.map { LocalVfs(it) })
	val haxeTemplateString = HaxeTemplateString(names, tinfo, settings, actualSubtarget, folders, outputFile2, tinfo.types)

	override fun buildSource() {
		gen = GenHaxeGen(
			tinfo = tinfo,
			program = program,
			features = AstFeatures(),
			srcFolder = srcFolder,
			featureSet = HaxeFeatures,
			settings = settings,
			names = names,
			haxeTemplateString = haxeTemplateString
		)
		info = gen._write()
		haxeTemplateString.setProgramInfo(info!!)
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
		tinfo.haxeCopyEmbeddedResourcesToFolder(mergedAssetsFolder)
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

object GenHaxe : GenTarget {
	//val copyFiles = HaxeCopyFiles
	//val mappings = HaxeMappings()

	override val runningAvailable: Boolean = true

	override fun getProcessor(tinfo: GenTargetInfo, settings: AstBuildSettings): GenTargetProcessor {
		return HaxeGenTargetProcessor(tinfo, settings)
	}

	data class ProgramInfo(override val entryPointClass: FqName, override val entryPointFile: String, val vfs: SyncVfsFile) : CommonProgramInfo {
		//fun getEntryPointFq(program: AstProgram) = getHaxeClassFqName(program, entryPointClass)
	}
}