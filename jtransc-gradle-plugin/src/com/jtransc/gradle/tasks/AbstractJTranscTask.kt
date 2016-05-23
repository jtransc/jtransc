package com.jtransc.gradle.tasks

import com.jtransc.AllBuild
import com.jtransc.AllBuildSimple
import com.jtransc.BuildBackend
import com.jtransc.JTranscVersion
import com.jtransc.ast.AstBuildSettings
import com.jtransc.error.invalidOp
import com.jtransc.gradle.JTranscExtension
import com.jtransc.gradle.get
import com.jtransc.gradle.getIfExists
import com.jtransc.log.log
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.SourceSetContainer
import java.io.File

open class AbstractJTranscTask : DefaultTask() {
	var target: String? = null
	var outputFile: String? = null
	var mainClassName: String? = null
	var minimizedNames: Boolean? = null
	var relooper: Boolean? = null
	var debug: Boolean? = null
	var analyzer: Boolean? = null
	var orientation: String? = null
	var icon: String? = null
	var vsync: Boolean? = null
	var resizable: Boolean? = null
	var fullscreen: Boolean? = null
	var borderless: Boolean? = null
	var embedResources: Boolean? = null
	var initialWidth: Int? = null
	var initialHeight: Int? = null
	var extra = hashMapOf<String?, String?>()
	var assets = arrayListOf<File>()
	var libraries = arrayListOf<String>()
	var package_: String? = null
	var company: String? = null
	var version: String? = null
	var productName: String? = null
	var title: String? = null

	open protected fun prepare(): AllBuild {
		val extension = project.getIfExists<JTranscExtension>(JTranscExtension.NAME)!!
		val mainClassName = project.getIfExists<String>("mainClassName")
		val buildDir = project.buildDir
		val dependencies = project.dependencies!!
		val configurations = project.configurations!! // https://docs.gradle.org/current/dsl/org.gradle.api.artifacts.Configuration.html
		val compileConfiguration = configurations["compile"]

		log.logger = { logger.info(it.toString()) }

		//for (file in compileConfiguration.files) println("file: $file")

		logger.info("JTranscTask.jtransc() extension: $extension");
		//println(project.property("output.classesDir"))
		val sourceSet = project.property("sourceSets") as SourceSetContainer
		val mainSourceSet = sourceSet["main"]
		val classesDir = mainSourceSet.output.classesDir
		logger.info("output classesDir: $classesDir")
		logger.info("sourceSet: $sourceSet")
		logger.info("mainClassName: $mainClassName")
		//println("mainSourceSet.output.asPath:" + mainSourceSet.)

		val default = AstBuildSettings.DEFAULT

		val settings = AstBuildSettings(
			jtranscVersion = JTranscVersion.getVersion(),
			backend = BuildBackend.ASM,
			title = title ?: extension.title ?: default.title,
			name = productName ?: extension.name ?: default.name,
			version = version ?: extension.version ?: default.version,
			company = company ?: extension.company ?: default.company,
			package_ = package_ ?: extension.package_ ?: default.package_,
			embedResources = embedResources ?: extension.embedResources ?: default.embedResources,
			libraries = (libraries + extension.libraries).map { AstBuildSettings.Library.fromInfo(it) },
			assets = assets + extension.assets,
			debug = debug ?: extension.debug ?: default.debug,
			initialWidth = initialWidth ?: extension.initialWidth ?: default.initialWidth,
			initialHeight = initialHeight ?: extension.initialHeight ?: default.initialHeight,
			vsync = vsync ?: extension.vsync ?: default.vsync,
			resizable = resizable ?: extension.resizable ?: default.resizable,
			borderless = borderless ?: extension.borderless ?: default.borderless,
			fullscreen = fullscreen ?: extension.fullscreen ?: default.fullscreen,
			icon = icon ?: extension.icon ?: default.icon,
			orientation = AstBuildSettings.Orientation.fromString(orientation ?: extension.orientation ?: default.orientation.name),
			relooper = relooper ?: extension.relooper ?: default.relooper,
			minimizeNames = minimizedNames ?: extension.minimizeNames ?: default.minimizeNames,
			analyzer = analyzer ?: extension.analyzer ?: default.analyzer,
			extra = extra + extension.extra,
			rtAndRtCore = default.rtAndRtCore
		)

		return AllBuildSimple(
			entryPoint = this.mainClassName ?: mainClassName ?: invalidOp("JTransc: Not defined mainClassName in build.gradle!"),
			classPaths = listOf(classesDir.absolutePath) + compileConfiguration.files.map { it.absolutePath },
			//AllBuildTargets = AllBuildTargets,
			target = target ?: extension.target ?: "haxe:js",
			output = outputFile ?: extension.output,
			settings = settings,
			targetDirectory = buildDir.absolutePath
		)
	}
}

