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

import com.jtransc.ast.AstFeatures
import com.jtransc.ast.AstProgram
import com.jtransc.ast.FqName
import com.jtransc.ast.feature.SwitchesFeature
import com.jtransc.ast.get
import com.jtransc.error.InvalidOperationException
import com.jtransc.gen.*
import com.jtransc.io.ProcessResult2
import com.jtransc.io.ProcessUtils
import com.jtransc.log.log
import com.jtransc.time.measureProcess
import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.SyncVfsFile
import com.jtransc.vfs.UserKey
import com.jtransc.vfs.getCached
import jtransc.JTranscVersion
import jtransc.annotation.haxe.HaxeAddAssets
import jtransc.annotation.haxe.HaxeAddLibraries
import java.io.File

object HaxeGenDescriptor : GenTargetDescriptor() {
	override val name = "haxe"
	override val longName = "Haxe"
	override val sourceExtension = "hx"
	override val outputExtension = "bin"
	override val subtargets = HaxeSubtarget.values().map {
		GenTargetSubDescriptor(HaxeGenDescriptor, it.name.toLowerCase(), it.extension)
	}
	override val extraLibraries = listOf<String>()
	override val extraClasses = listOf<String>()
	override fun getGenerator() = GenHaxe
}

//val HaxeFeatures = setOf(GotosFeature, SwitchesFeature)
val HaxeFeatures = setOf(SwitchesFeature)

enum class HaxeSubtarget(val switch: String, val singleFile: Boolean, val interpreter: String? = null, val extension: String = "bin") {
	JS(switch = "-js", singleFile = true, interpreter = "node", extension = "js"),
	CPP(switch = "-cpp", singleFile = false, interpreter = null, extension = "exe"),
	SWF(switch = "-swf", singleFile = true, interpreter = null, extension = "swf"),
	NEKO(switch = "-neko", singleFile = true, interpreter = "neko", extension = "n"),
	PHP(switch = "-php", singleFile = false, interpreter = "php", extension = "php"),
	CS(switch = "-cs", singleFile = false, interpreter = null, extension = "exe"),
	JAVA(switch = "-java", singleFile = false, interpreter = "java -jar", extension = "jar"),
	PYTHON(switch = "-python", singleFile = true, interpreter = "python", extension = "py")
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

private val HAXE_LIBS_KEY = UserKey<List<HaxeLib.LibraryRef>>()

val AstProgram.haxeLibs: List<HaxeLib.LibraryRef> get() = this.getCached(HAXE_LIBS_KEY) {
	this.classes
		.map { it.annotations[HaxeAddLibraries::value] }
		.filterNotNull()
		.flatMap { it.toList() }
		.map { HaxeLib.LibraryRef.fromVersion(it) }
}

val AstProgram.haxeExtraFlags: List<Pair<String, String>> get() {
	return this.haxeLibs.map { "-lib" to it.nameWithVersion } + listOf(
		//"-dce" to "no"
		//"-D" to "analyzer-no-module",
		//"--no-inline" to "1",
		//"--no-opt" to "1"
	)
}

val AstProgram.haxeExtraDefines: List<String> get() {

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
	return listOf(
		//"analyzer"
		"no-analyzer"
	)
}

fun AstProgram.haxeInstallRequiredLibs() {
	val libs = this.haxeLibs
	log(":: REFERENCED LIBS: $libs")
	for (lib in libs) {
		log(":: TRYING TO INSTALL LIBRARY $lib")
		HaxeLib.installIfNotExists(lib)
	}
}

fun GenTargetInfo.haxeCopyEmbeddedResourcesToFolder(assetsFolder:File?) {
	val program = this.program
	val files = program.classes.map { it.annotations[HaxeAddAssets::value] }.filterNotNull().flatMap { it.toList() }
	//val assetsFolder = settings.assets.firstOrNull()
	val resourcesVfs = program.resourcesVfs
	log("GenTargetInfo.haxeCopyResourcesToAssetsFolder: $assetsFolder")
	if (assetsFolder != null) {
		val outputVfs = LocalVfs(assetsFolder)
		for (file in files) {
			log("GenTargetInfo.haxeCopyResourcesToAssetsFolder.copy: $file")
			outputVfs[file] = resourcesVfs[file]
		}
	}

}

class HaxeGenTargetProcessor(val tinfo: GenTargetInfo) : GenTargetProcessor {
	val actualSubtarget = HaxeSubtarget.fromString(tinfo.subtarget)

	val outputFile2 = File(File(tinfo.outputFile).absolutePath)
	//val tempdir = System.getProperty("java.io.tmpdir")
	val tempdir = tinfo.targetDirectory
	var info: GenHaxe.ProgramInfo? = null
	val program = tinfo.program

	init {
		File("$tempdir/jtransc-haxe/src").mkdirs()
	}

	val srcFolder = LocalVfs(File("$tempdir/jtransc-haxe/src")).ensuredir()

	init {
		log("Temporal haxe files: $tempdir/jtransc-haxe")
	}

	override fun buildSource() {
		info = GenHaxeGen(
			program = program,
			features = AstFeatures(),
			srcFolder = srcFolder,
			featureSet = HaxeFeatures
		)._write()
	}

	override fun compile(): Boolean {
		if (info == null) throw InvalidOperationException("Must call .buildSource first")
		outputFile2.delete()
		log("haxe.build (" + JTranscVersion.getVersion() + ") source path: " + srcFolder.realpathOS)

		val buildArgs = arrayListOf(
			"-cp", srcFolder.realpathOS,
			"-main", info!!.entryPointFile
		)
		val releaseArgs = if (tinfo.settings.release) listOf() else listOf("-debug")
		val subtargetArgs = listOf(actualSubtarget.switch, outputFile2.absolutePath)

		program.haxeInstallRequiredLibs()
		buildArgs += program.haxeExtraFlags.flatMap { listOf(it.first, it.second) }
		buildArgs += program.haxeExtraDefines.flatMap { listOf("-D", it) }

		tinfo.haxeCopyEmbeddedResourcesToFolder(outputFile2.parentFile)

		log("Compiling... ")

		val args = releaseArgs + subtargetArgs + buildArgs

		log("Running: haxe ${args.joinToString(" ")}")
		return ProcessUtils.runAndRedirect(srcFolder.realfile, "haxe", args).success
	}

	override fun run(redirect: Boolean): ProcessResult2 {
		if (!outputFile2.exists()) {
			return ProcessResult2("file $outputFile2 doesn't exist", -1)
		}
		val fileSize = outputFile2.length()
		log("run: ${outputFile2.absolutePath} ($fileSize bytes)")
		val parentDir = outputFile2.parentFile

		val runner = actualSubtarget.interpreter ?: "echo"

		return measureProcess("Running") {
			ProcessUtils.run(parentDir, runner, listOf(outputFile2.absolutePath), redirect = redirect)
		}
	}
}

object GenHaxe : GenTarget {
	//val copyFiles = HaxeCopyFiles
	//val mappings = HaxeMappings()

	override val runningAvailable: Boolean = true

	override fun getProcessor(tinfo: GenTargetInfo): GenTargetProcessor {
		return HaxeGenTargetProcessor(tinfo)
	}

	data class ProgramInfo(val entryPointClass: FqName, val entryPointFile: String, val vfs: SyncVfsFile) {
		//fun getEntryPointFq(program: AstProgram) = getHaxeClassFqName(program, entryPointClass)
	}
}