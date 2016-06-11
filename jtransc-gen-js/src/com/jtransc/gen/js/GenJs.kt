package com.jtransc.gen.js

import com.jtransc.ast.*
import com.jtransc.ast.feature.SwitchesFeature
import com.jtransc.error.invalidOp
import com.jtransc.gen.GenTarget
import com.jtransc.gen.GenTargetInfo
import com.jtransc.gen.GenTargetProcessor
import com.jtransc.io.ProcessResult2
import com.jtransc.log.log
import com.jtransc.template.Minitemplate
import com.jtransc.text.quote
import com.jtransc.vfs.LocalVfs
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

data class JsProgramInfo(val entryPointClass: FqName, val entryPointFile: String, val source: String, val sourceMap: String?) {
	//fun getEntryPointFq(program: AstProgram) = getHaxeClassFqName(program, entryPointClass)
}


val JsFeatures = setOf(SwitchesFeature)

class JsTemplateString(val names: JsNames, val tinfo: GenTargetInfo, val settings: AstBuildSettings) {
	val params = hashMapOf(
		//"srcFolder" to srcFolder.realpathOS,
		//"buildFolder" to srcFolder.parent.realpathOS,
		//"haxeExtraFlags" to program.haxeExtraFlags(settings),
		//"haxeExtraDefines" to program.haxeExtraDefines(settings),
		//"actualSubtarget" to actualSubtarget,
		//"outputFile" to outputFile2.absolutePath,
		"release" to tinfo.settings.release,
		"debug" to !tinfo.settings.release,
		"releasetype" to if (tinfo.settings.release) "release" else "debug",
		"settings" to settings,
		"title" to settings.title,
		"name" to settings.name,
		"package" to settings.package_,
		"version" to settings.version,
		"company" to settings.company,
		"initialWidth" to settings.initialWidth,
		"initialHeight" to settings.initialHeight,
		"orientation" to settings.orientation.lowName,
		//"tempAssetsDir" to mergedAssetsDir.absolutePath, // @deprecated
		//"mergedAssetsDir" to mergedAssetsDir.absolutePath,
		"embedResources" to settings.embedResources,
		"assets" to settings.assets,
		"hasIcon" to !settings.icon.isNullOrEmpty(),
		"icon" to settings.icon,
		"libraries" to settings.libraries,
		"extra" to settings.extra
	)

	class ProgramRefNode(val ts: JsTemplateString, val type:String, val desc:String) : Minitemplate.BlockNode {
		override fun eval(context: Minitemplate.Context) {
			context.write(ts.evalReference(type, desc))
		}
	}

	val miniConfig = Minitemplate.Config(
		extraTags = listOf(
			Minitemplate.Tag(
				":programref:", setOf(), null,
				aliases = listOf(
					//"sinit", "constructor", "smethod", "method", "sfield", "field", "class",
					"SINIT", "CONSTRUCTOR", "SMETHOD", "METHOD", "SFIELD", "FIELD", "CLASS"
				)
			) { ProgramRefNode(this, it.first().token.name, it.first().token.content) }
			//, Minitemplate.Tag("copyfile", setOf(), null) {
			//	CopyFileNode(this, it.first().token.name, Minitemplate.ExprNode.parse(it.first().token.content))
			//}
		),
		extraFilters = listOf(
		)
	)

	fun gen(template: String, context: AstGenContext, type: String): String {
		//System.out.println("WARNING: templates not implemented! : $type : $context : $template");
		return Minitemplate(template, miniConfig).invoke(params)
	}

	fun setExtraData(mapOf: Map<String, String>) {
		this.params.putAll(mapOf)
		//throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	fun setProgramInfo(info: JsProgramInfo) {
		//throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
		params["entryPointFile"] = info.entryPointFile
		params["entryPointClass"] = names.getJsClassFqName(info.entryPointClass)
	}

	private fun getOrReplaceVar(name:String):String {
		val out = if (name.startsWith("#")) {
			params[name.substring(1)].toString()
		} else {
			name
		}
		return out
	}

	private fun evalReference(type: String, desc: String): String {
		val dataParts = desc.split(':').map { getOrReplaceVar(it) }
		val desc2 = dataParts.joinToString(":")
		val clazz = names.program[dataParts[0].fqname]
		return when (type.toUpperCase()) {
			"SINIT" -> {
				names.getJsClassStaticInit(clazz.ref, "template sinit")
			}
			"CONSTRUCTOR" -> {
				"new ${names.getJsClassFqNameForCalling(clazz.name)}().${names.getJsMethodName(AstMethodRef(clazz.name, "<init>", AstType.demangleMethod(dataParts[1])))}"
			}
			"SMETHOD", "METHOD" -> {
				val methodName = if (dataParts.size >= 3) {
					names.getJsMethodName(AstMethodRef(clazz.name, dataParts[1], AstType.demangleMethod(dataParts[2])))
				} else {
					val methods = clazz.getMethodsInAncestorsAndInterfaces(dataParts[1])
					if (methods.isEmpty()) invalidOp("Can't find method $desc2")
					if (methods.size > 1) invalidOp("Several signatures, please specify signature")
					names.getJsMethodName(methods.first())
				}
				if (type == "SMETHOD") names.getJsClassFqNameForCalling(clazz.name) + "[" + methodName.quote() + "]" else methodName
			}
			"SFIELD", "FIELD" -> {
				val fieldName = names.getJsFieldName(clazz.fieldsByName[dataParts[1]] ?: invalidOp("Can't find field $desc"))
				if (type == "SFIELD") names.getJsClassFqNameForCalling(clazz.name) + "[" + fieldName.quote() + "]" else fieldName
			}
			"CLASS" -> names.getJsClassFqNameForCalling(clazz.name)
			else -> invalidOp("Unknown type!")
		}
	}
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
