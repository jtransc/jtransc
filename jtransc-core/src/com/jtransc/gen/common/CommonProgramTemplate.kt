package com.jtransc.gen.common

import com.jtransc.ConfigTargetDirectory
import com.jtransc.ast.*
import com.jtransc.ast.template.CommonTagHandler
import com.jtransc.error.InvalidOperationException
import com.jtransc.error.invalidOp
import com.jtransc.injector.Injector
import com.jtransc.injector.Singleton
import com.jtransc.template.Minitemplate
import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.MergeVfs
import com.jtransc.vfs.SyncVfsFile
import java.io.File
import java.util.*

class ConfigOutputFile2(val file: File)
class ConfigTargetFolder(val targetFolder: SyncVfsFile)
class ConfigEntryPointFile(val entryPointFile: String)
class ConfigEntryPointClass(val entryPointClass: FqName)

@Singleton
open class CommonProgramTemplate(val injector: Injector) {
	val names: CommonNames = injector.get()
	val program: AstProgram = injector.get()
	val settings: AstBuildSettings = injector.get()
	val folders: CommonGenFolders = injector.get()
	val configOutputFile2: ConfigOutputFile2 = injector.get()
	val types: AstTypes = injector.get()
	val outputFile2 = configOutputFile2.file
	val configTargetDirectory: ConfigTargetDirectory = injector.get()

	//val outputFile2 = File(File(tinfo.outputFile).absolutePath)
	val tempdir = configTargetDirectory.targetDirectory

	val params = hashMapOf(
		"outputFolder" to outputFile2.parent,
		"outputFile" to outputFile2.absolutePath,
		"outputFileBase" to outputFile2.name,
		"release" to settings.release,
		"debug" to !settings.release,
		"releasetype" to if (settings.release) "release" else "debug",
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

	fun setInfoAfterBuildingSource() {
		params["entryPointFile"] = injector.get<ConfigEntryPointFile>().entryPointFile
		params["entryPointClass"] = names.buildTemplateClass(injector.get<ConfigEntryPointClass>().entryPointClass)
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
		val ref = CommonTagHandler.getRef(program, type, desc, params)
		return when (ref) {
			is CommonTagHandler.SINIT -> names.buildStaticInit(ref.method.containingClass);
			is CommonTagHandler.CONSTRUCTOR -> names.buildConstructor(ref.method)
			is CommonTagHandler.METHOD -> names.buildMethod(ref.method, static = ref.isStatic)
			is CommonTagHandler.FIELD -> names.buildField(ref.field, static = ref.isStatic)
			is CommonTagHandler.CLASS -> names.buildTemplateClass(ref.clazz)
			else -> invalidOp("Unsupported result")
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

	fun gen(template: String): String = gen(template, extra = hashMapOf())

	fun gen(template: String, extra: HashMap<String, Any?> = hashMapOf()): String = Minitemplate(template, miniConfig).invoke(HashMap(params + extra))

	fun gen(template: String, process: Boolean): String = if (process) Minitemplate(template, miniConfig).invoke(params) else template

	fun gen(template: String, context: AstGenContext, type: String): String {
		//System.out.println("WARNING: templates not implemented! : $type : $context : $template");
		context.rethrowWithContext {
			return Minitemplate(template, miniConfig).invoke(params)
		}
	}
}
