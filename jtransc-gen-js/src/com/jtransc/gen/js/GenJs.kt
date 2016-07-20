package com.jtransc.gen.js

import com.jtransc.annotation.JTranscRegisterCommand
import com.jtransc.annotation.JTranscRegisterCommandList
import com.jtransc.annotation.JTranscRunCommandList
import com.jtransc.ast.AstBuildSettings
import com.jtransc.ast.AstFeatures
import com.jtransc.ast.AstTypes
import com.jtransc.ast.FqName
import com.jtransc.ast.feature.SwitchesFeature
import com.jtransc.gen.GenTarget
import com.jtransc.gen.GenTargetInfo
import com.jtransc.gen.GenTargetProcessor
import com.jtransc.gen.common.*
import com.jtransc.io.ProcessResult2
import com.jtransc.log.log
import com.jtransc.vfs.ExecOptions
import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.LocalVfsEnsureDirs
import com.jtransc.vfs.SyncVfsFile
import java.io.File

object GenJs : GenTarget {
	//val copyFiles = HaxeCopyFiles
	//val mappings = HaxeMappings()

	override val runningAvailable: Boolean = true

	override fun getProcessor(tinfo: GenTargetInfo, settings: AstBuildSettings): GenTargetProcessor {
		return JsGenTargetProcessor(tinfo, settings)
	}
}

data class JsProgramInfo(
	override val entryPointClass: FqName,
	override val entryPointFile: String,
    val javascriptOutput: SyncVfsFile
	//override val vfs: SyncVfsFile
) : CommonProgramInfo {
	//fun getEntryPointFq(program: AstProgram) = getHaxeClassFqName(program, entryPointClass)
}


val JsFeatures = setOf(SwitchesFeature)

class JsTemplateString(
	names: JsNames, tinfo: GenTargetInfo, settings: AstBuildSettings, folders: CommonGenFolders, outputFile2: File, types: AstTypes
) : CommonProgramTemplate(
	names, tinfo, settings, folders, outputFile2, types
) {

}

class JsGenTargetProcessor(val tinfo: GenTargetInfo, val settings: AstBuildSettings) : GenTargetProcessor() {
	var info: JsProgramInfo? = null
	lateinit var gen: GenJsGen
	val program = tinfo.program
	val names = JsNames(program, minimize = settings.minimizeNames)
	val targetFolder = LocalVfsEnsureDirs(File(tinfo.targetDirectory + "/jtransc-js"))
	val folders = CommonGenFolders(settings.assets.map { LocalVfs(it) })
	//val outputFile2 = File(File(tinfo.outputFile).absolutePath)
	val outputFile2 = targetFolder[tinfo.outputFileBaseName].realfile
	val jsTemplateString = JsTemplateString(names, tinfo, settings, folders, outputFile2, tinfo.types)

	override fun buildSource() {
		gen = GenJsGen(CommonGenGen.Input(
			tinfo = tinfo,
			program = program,
			features = AstFeatures(),
			featureSet = JsFeatures,
			settings = settings,
			names = names,
			templateString = jsTemplateString,
			folders = folders,
			srcFolder = targetFolder
		))
		info = gen._write(targetFolder)
		jsTemplateString.setProgramInfo(info!!)
	}

	//val BUILD_COMMAND = listOf("haxelib", "run", "lime", "@@SWITCHES", "build", "@@SUBTARGET")

	override fun compileAndRun(redirect: Boolean): ProcessResult2 {
		return _compileRun(run = true, redirect = redirect)
	}

	override fun compile(): ProcessResult2 {
		return _compileRun(run = false, redirect = false)
	}

	fun _compileRun(run: Boolean, redirect: Boolean): ProcessResult2 {
		val info = info!!
		val outputFile = info.javascriptOutput

		log.info("Generated javascript at..." + outputFile.realpathOS)

		if (run) {
			val result = CommonGenCliCommands.runProgramCmd(
				program,
				target = "js",
				default = listOf("node", "{{ outputFile }}"),
				template = jsTemplateString,
				options = ExecOptions(passthru = redirect)
			)
			return ProcessResult2(result)
		} else {
			return ProcessResult2(0)
		}
	}

	override fun run(redirect: Boolean): ProcessResult2 {
		return ProcessResult2(0)
	}
}
