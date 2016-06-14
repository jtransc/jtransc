package com.jtransc.gen.js

import com.jtransc.ast.*
import com.jtransc.ast.feature.SwitchesFeature
import com.jtransc.error.invalidOp
import com.jtransc.gen.GenTarget
import com.jtransc.gen.GenTargetInfo
import com.jtransc.gen.GenTargetProcessor
import com.jtransc.gen.common.CommonProgramInfo
import com.jtransc.gen.common.ProgramTemplate
import com.jtransc.io.ProcessResult2
import com.jtransc.log.log
import com.jtransc.template.Minitemplate
import com.jtransc.text.quote
import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.MergeVfs
import com.jtransc.vfs.SyncVfsFile
import com.jtransc.vfs.parent
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
	val source: String,
	val sourceMap: String?
	//override val vfs: SyncVfsFile
) : CommonProgramInfo {
	//fun getEntryPointFq(program: AstProgram) = getHaxeClassFqName(program, entryPointClass)
}


val JsFeatures = setOf(SwitchesFeature)

class JsTemplateString(names: JsNames, tinfo: GenTargetInfo, settings: AstBuildSettings) : ProgramTemplate(names, tinfo, settings) {

}

val GenTargetInfo.mergedAssetsFolder: File get() = File("${this.targetDirectory}/merged-assets")
//val GenTargetInfo.mergedAssetsFolder: File get() = File("${this.targetDirectory}/program.js")

class JsGenTargetProcessor(val tinfo: GenTargetInfo, val settings: AstBuildSettings) : GenTargetProcessor() {
	val outputFile2 = File(File(tinfo.outputFile).absolutePath)
	//val tempdir = System.getProperty("java.io.tmpdir")
	val tempdir = tinfo.targetDirectory
	var info: JsProgramInfo? = null
	lateinit var gen: GenJsGen
	val program = tinfo.program
	val mergedAssetsFolder = tinfo.mergedAssetsFolder
	val mergedAssetsVfs = LocalVfs(mergedAssetsFolder)
	val names = JsNames(program, minimize = settings.minimizeNames)
	val jsTemplateString = JsTemplateString(names, tinfo, settings)

	override fun buildSource() {
		gen = GenJsGen(
			tinfo = tinfo,
			program = program,
			features = AstFeatures(),
			featureSet = JsFeatures,
			settings = settings,
			names = names,
			jsTemplateString = jsTemplateString
		)
		info = gen._write()
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
		//val folder= LocalVfs(File(tinfo.targetDirectory))
		val outputFile = LocalVfs(File(File(tinfo.outputFile).absolutePath))
		val outputFileMap = LocalVfs(File(File(tinfo.outputFile).absolutePath + ".map"))
		outputFile.write(info!!.source)
		if (info!!.sourceMap != null) outputFileMap.write(info!!.sourceMap!!)
		log.info("Generated javascript at..." + outputFile.realpathOS)
		//println("Generated javascript at..." + outputFile.realpathOS)

		if (run) {
			val result = NodeJs.run(outputFile.realpathOS, listOf(), passthru = redirect)
			return ProcessResult2(result)
		} else {
			return ProcessResult2(0)
		}
	}

	override fun run(redirect: Boolean): ProcessResult2 {
		return ProcessResult2(0)
	}
}
