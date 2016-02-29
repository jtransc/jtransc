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
import com.jtransc.gen.haxe.GenHaxe.haxeLibs
import com.jtransc.io.ProcessResult2
import com.jtransc.io.ProcessUtils
import com.jtransc.time.measureProcess
import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.SyncVfsFile
import com.jtransc.vfs.UserKey
import com.jtransc.vfs.getCached
import jtransc.JTranscVersion
import jtransc.annotation.haxe.HaxeAddLibraries
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

private val HAXE_LIBS_KEY = UserKey<List<HaxeLib.LibraryRef>>()

interface GenHaxeBase {

	val AstProgram.haxeLibs: List<HaxeLib.LibraryRef> get() = this.getCached(HAXE_LIBS_KEY) {
		this.classes
			.map { it.annotations[HaxeAddLibraries::value] }
			.filterNotNull()
			.flatMap { it.toList() }
			.map { HaxeLib.LibraryRef.fromVersion(it) }
	}

	val AstProgram.haxeExtraFlags: List<Pair<String, String>> get() = this.haxeLibs.map { "-lib" to it.nameWithVersion }

	fun AstProgram.haxeInstallRequiredLibs() {
		val libs = this.haxeLibs
		println(":: REFERENCED LIBS: $libs")
		for (lib in libs) {
			println(":: TRYING TO INSTALL LIBRARY $lib")
			HaxeLib.installIfNotExists(lib)
		}
	}
}

object GenHaxe : GenTarget, GenHaxeBase {
	//val copyFiles = HaxeCopyFiles
	//val mappings = HaxeMappings()
	val mappings = ClassMappings()

	val INIT_MODE = InitMode.LAZY

	override val runningAvailable: Boolean = true

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
				info = GenHaxeGen(
					program = program,
					mappings = mappings,
					features = AstFeatures(),
					srcFolder = srcFolder,
					featureSet = HaxeFeatures
				)._write()
			}

			override fun compile(): Boolean {
				if (info == null) throw InvalidOperationException("Must call .buildSource first")
				outputFile2.delete()
				println("haxe.build (" + JTranscVersion.getVersion() + ") source path: " + srcFolder.realpathOS)

				val buildArgs = arrayListOf(
					"-cp", ".",
					"-main", info!!.entryPointFile,
					"-dce", "no"
				)
				val releaseArgs = if (tinfo.settings.release) listOf() else listOf("-debug")
				val subtargetArgs = listOf(actualSubtarget.switch, outputFile2.absolutePath)
				val libs = program.classes.map { it.annotations[HaxeAddLibraries::value] }.filterNotNull().flatMap { it.toList() }

				program.haxeInstallRequiredLibs()
				buildArgs += program.haxeExtraFlags.flatMap { listOf(it.first, it.second) }

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
				val fileSize = outputFile2.length()
				println("run: ${outputFile2.absolutePath} ($fileSize bytes)")
				val parentDir = outputFile2.parentFile

				val runner = actualSubtarget.interpreter ?: "echo"

				return measureProcess("Running") {
					ProcessUtils.run(parentDir, runner, listOf(outputFile2.absolutePath), redirect = redirect)
				}
			}
		}
	}

	data class ProgramInfo(val entryPointClass: FqName, val entryPointFile: String, val vfs: SyncVfsFile) {
		//fun getEntryPointFq(program: AstProgram) = getHaxeClassFqName(program, entryPointClass)
	}
}