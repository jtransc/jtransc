package com.jtransc.gen.common

import com.jtransc.ast.*
import com.jtransc.ds.toHashMap
import com.jtransc.error.invalidOp
import com.jtransc.gen.GenTargetInfo
import com.jtransc.template.Minitemplate
import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.MergeVfs
import java.io.File

open class CommonProgramTemplate(
	val names: CommonNames,
	val tinfo: GenTargetInfo,
	val settings: AstBuildSettings,
    val folders: CommonGenFolders,
    val outputFile2: File,
    val types: AstTypes
) {
	val program = tinfo.program
	//val outputFile2 = File(File(tinfo.outputFile).absolutePath)
	val tempdir = tinfo.targetDirectory

	val params = hashMapOf(
		"outputFolder" to outputFile2.parent,
		"outputFile" to outputFile2.absolutePath,
		"outputFileBase" to outputFile2.name,
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
		"assetFiles" to MergeVfs(settings.assets.map { LocalVfs(it) }).listdirRecursive().filter { it.isFile }.map { it.file },
		"embedResources" to settings.embedResources,
		"assets" to settings.assets,
		"hasIcon" to !settings.icon.isNullOrEmpty(),
		"icon" to settings.icon,
		"libraries" to settings.libraries,
		"extra" to settings.extra,
		"folders" to folders
	)


	fun setProgramInfo(info: CommonProgramInfo) {
		params["entryPointFile"] = info.entryPointFile
		params["entryPointClass"] = names.buildTemplateClass(info.entryPointClass)
	}

	fun setExtraData(map: Map<String, Any?>) {
		for ((key, value) in map) {
			this.params[key] = value
		}
	}

	private fun getOrReplaceVar(name: String): String {
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
		if (!names.program.contains(dataParts[0].fqname)) invalidOp("Can't find $desc2")
		val clazz = names.program[dataParts[0].fqname] ?: invalidOp("Can't find $desc2")

		return when (type.toUpperCase()) {
			"SINIT" -> {
				names.buildStaticInit(clazz);
			}
			"CONSTRUCTOR" -> {
				val method = program[AstMethodRef(clazz.name, "<init>", types.demangleMethod(dataParts[1]))]!!
				names.buildConstructor(method)
			}
			"SMETHOD", "METHOD" -> {
				val method = if (dataParts.size >= 3) {
					program[AstMethodRef(clazz.name, dataParts[1], types.demangleMethod(dataParts[2]))]!!
				} else {
					val methods = clazz.getMethodsInAncestorsAndInterfaces(dataParts[1])
					if (methods.isEmpty()) invalidOp("Can't find method $desc2")
					if (methods.size > 1) invalidOp("Several signatures, please specify signature")
					methods.first()
				}
				names.buildMethod(method, static = (type == "SMETHOD"))
			}
			"SFIELD", "FIELD" -> {
				val field = clazz.locateField(dataParts[1]) ?: invalidOp("Can't find field $desc2")
				names.buildField(field, static = (type == "SFIELD"))
			}
			"CLASS" -> names.buildTemplateClass(clazz)
			else -> invalidOp("Unknown type!")
		}
	}

	class ProgramRefNode(val ts: CommonProgramTemplate, val type: String, val desc: String) : Minitemplate.BlockNode {
		override fun eval(context: Minitemplate.Context) {
			context.write(ts.evalReference(type, desc))
		}
	}

	//class CopyFileNode(val ts: HaxeTemplateString, val type:String, val expr:Minitemplate.ExprNode) : Minitemplate.BlockNode {
	//	override fun eval(context: Minitemplate.Context) {
	//		val filetocopy = expr.eval(context)
	//	}
	//}

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

	fun gen(template: String): String = Minitemplate(template, miniConfig).invoke(params)

	fun gen(template: String, context: AstGenContext, type: String): String {
		//System.out.println("WARNING: templates not implemented! : $type : $context : $template");
		return Minitemplate(template, miniConfig).invoke(params)
	}
}
