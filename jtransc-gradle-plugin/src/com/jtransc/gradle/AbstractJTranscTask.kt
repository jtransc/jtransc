package com.jtransc.gradle

import com.jtransc.AllBuild
import com.jtransc.AllBuildSimple
import com.jtransc.BuildBackend
import com.jtransc.JTranscVersion
import com.jtransc.ast.AstBuildSettings
import com.jtransc.error.invalidOp
import com.jtransc.log.log
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction

abstract class AbstractJTranscTask : DefaultTask() {
	var target: String? = null
	var outputFile: String? = null

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

		val settings = AstBuildSettings(
			title = extension.tittle,
			jtranscVersion = JTranscVersion.getVersion(),
			name = extension.name,
			version = extension.version,
			company = extension.company,
			package_ = extension.package_,
			embedResources = extension.embedResources,
			libraries = extension.libraries.map { AstBuildSettings.Library.fromInfo(it) },
			assets = extension.assets,
			debug = extension.debug,
			initialWidth = extension.initialWidth,
			initialHeight = extension.initialHeight,
			vsync = extension.vsync,
			resizable = extension.resizable,
			borderless = extension.borderless,
			fullscreen = extension.fullscreen,
			icon = extension.icon,
			orientation = AstBuildSettings.Orientation.fromString(extension.orientation),
			backend = BuildBackend.ASM,
			relooper = extension.relooper,
			minimizeNames = extension.minimizeNames,
			analyzer = extension.analyzer,
			extra = extension.extra
			//,rtAndRtCore =
		)

		return AllBuildSimple(
			entryPoint = mainClassName ?: invalidOp("JTransc: Not defined mainClassName in build.gradle!"),
			classPaths = listOf(classesDir.absolutePath) + compileConfiguration.files.map { it.absolutePath },
			//AllBuildTargets = AllBuildTargets,
			target = target ?: extension.target,
			output = outputFile ?: extension.output,
			settings = settings,
			targetDirectory = buildDir.absolutePath
		)
	}
}

