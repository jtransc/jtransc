package com.jtransc.gradle.tasks

import com.jtransc.*
import com.jtransc.ast.*
import com.jtransc.ds.split
import com.jtransc.error.invalidOp
import com.jtransc.gradle.JTranscGradleExtension
import com.jtransc.gradle.get
import com.jtransc.gradle.getIfExists
import com.jtransc.injector.Injector
import com.jtransc.lang.mergeMapListWith
import com.jtransc.log.log
import com.jtransc.plugin.service.ConfigServiceLoader
import org.gradle.api.DefaultTask
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.tasks.SourceSetContainer
import java.io.File

@Suppress("unused")
open class AbstractJTranscGradleTask : DefaultTask() {
	var target: String? = null
	var outputFile: String? = null
	var mainClassName: String? = null
	var minimizedNames: Boolean? = null
	var relooper: Boolean? = null
	var debug: Boolean? = null
	var optimize: Boolean? = null
	var compile: Boolean? = null
	var treeshaking: Boolean? = null
	var treeshakingTrace: Boolean? = null
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
	var extraVars = hashMapOf<String, ArrayList<String>>()
	var assets = arrayListOf<String>()
	val newAssets = arrayListOf<File>()
	var libraries = arrayListOf<String>()
	var packagePath: String? = null
	var company: String? = null
	var version: String? = null
	var productName: String? = null
	var title: String? = null
	var skipServiceLoaderClasses: ArrayList<String> = arrayListOf()

	fun skipServiceLoader(serviceLoader: String) = skipServiceLoaderClasses.add(serviceLoader)
	//val types: AstTypes by lazy { AstTypes() }
	fun assets(vararg folders: String) = run { newAssets += folders.map { File(project.buildFile.parentFile, it) } }
	fun param(key: String, value: String?) = run { extra[key] = value }
	fun param(key: String) = param(key, "true")
	fun appendVar(key: String, value: List<String>) = extraVars.getOrPut(key) { arrayListOf() }.addAll(value)
	fun appendVar(key: String, value: String) = extraVars.getOrPut(key) { arrayListOf() }.add(value)

	protected open fun prepare(isTest: Boolean, forcedMain: String? = null, args: List<String> = listOf()): JTranscBuild {
		val extension = project.getIfExists<JTranscGradleExtension>(JTranscGradleExtension.NAME)!!
		val mainClassName: String = when {
			forcedMain != null -> forcedMain
			isTest -> "org.junit.runner.JUnitCore"
			else -> mainClassName ?: extension.mainClassName ?: project.getIfExists<String>("mainClassName") ?: invalidOp("JTransc: Not defined mainClassName in build.gradle!")
		}
		val buildDir = project.buildDir
		val dependencies = project.dependencies!!
		val configurations = project.configurations!! // https://docs.gradle.org/current/dsl/org.gradle.api.artifacts.Configuration.html
		//val apiConfiguration = configurations["apiElements"]
		val configurationName = if (isTest) "testRuntimeClasspath" else "runtimeClasspath"
		val runtimeClasspathConfig = configurations[configurationName]
		//val compileConfiguration = configurations["compile"]
		val jtranscConfiguration = configurations["jtransc"]
		val nojtranscConfiguration = configurations["nojtransc"]
		val runtimeConfiguration = configurations["jtranscRuntime"]

		log.logger = { content, level ->
			when (level) {
				log.Level.DEBUG -> logger.debug(content.toString())
				log.Level.INFO -> logger.info(content.toString())
				log.Level.WARN -> logger.warn(content.toString())
				log.Level.ERROR -> logger.error(content.toString())
			}
		}

		logger.info("jtransc.configurations: ${project.configurations.names}")
		for (file in runtimeConfiguration.files) logger.info("jtranscRuntime: $file")
		for (file in jtranscConfiguration.files) logger.info("jtransc: $file")
		for (file in runtimeClasspathConfig.files) logger.info("$configurationName: $file")
		//for (file in apiConfiguration.files) logger.info("api: $file")
		for (file in nojtranscConfiguration.files) logger.info("nojtransc: $file")

		val blacklist = nojtranscConfiguration.files.toSet()

		logger.info("JTranscTask.jtransc() extension: $extension");
		//println(project.property("output.classesDir"))
		val sourceSet = project.property("sourceSets") as SourceSetContainer

		val mainSourceSet = sourceSet["main"]
		val testSourceSet = sourceSet["test"]
		val classesDirs = mainSourceSet.output.classesDirs
		val testClassesDirs = testSourceSet.output.classesDirs
		//val classesDirs = listOf(mainSourceSet.output.classesDir)
		logger.info("output classesDir: ${classesDirs.joinToString(", ")}")
		logger.info("output testClassesDirs: ${testClassesDirs.joinToString(", ")}")
		logger.info("sourceSet: $sourceSet")
		logger.info("mainClassName: $mainClassName")

		//println("mainSourceSet.output.asPath:" + mainSourceSet.)

		val default = AstBuildSettings.DEFAULT

		val rtarget = target ?: extension.target ?: "js"

		val settings = AstBuildSettings(
			jtranscVersion = JTranscVersion.getVersion(),
			title = title ?: extension.title ?: default.title,
			name = productName ?: extension.name ?: default.name,
			version = version ?: extension.version ?: default.version,
			company = company ?: extension.company ?: default.company,
			package_ = packagePath ?: extension.package_ ?: default.package_,
			embedResources = embedResources ?: extension.embedResources ?: default.embedResources,
			libraries = (libraries + extension.libraries).map { AstBuildSettings.Library.fromInfo(it) },
			assets = (assets + extension.assets).map(::File) + newAssets + extension.newAssets,
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
			analyzer = analyzer ?: extension.analyzer ?: default.analyzer,
			optimize = optimize ?: extension.optimize ?: default.optimize,
			extra = extra + extension.extra,
			extraVars = extraVars.mergeMapListWith(extension.extraVars),
			rtAndRtCore = runtimeConfiguration.files.map { it.absolutePath }
		)

		val injector = Injector()
		injector.mapInstance(BuildBackend.ASM)
		injector.mapInstance(ConfigMinimizeNames(extension.minimizeNames ?: minimizedNames ?: false))
		injector.mapInstance(ConfigCompile(compile ?: true))
		injector.mapInstance(ConfigRunningTests(isTest))
		injector.mapInstance(ConfigTreeShaking(
			treeshaking ?: extension.treeshaking ?: false,
			if (isTest) false else treeshakingTrace ?: extension.treeshakingTrace ?: false
		))

		injector.mapInstance(ConfigServiceLoader(
			skipServiceLoaderClasses + extension.skipServiceLoaderClasses
		))

		val files = arrayListOf<File>()
		files += classesDirs.map { File(it.absolutePath) }
		if (isTest) files += testClassesDirs.map { File(it.absolutePath) }
		files += jtranscConfiguration.files
		files += runtimeClasspathConfig.files
		files += mainSourceSet.resources.srcDirs.toList()
		if (isTest) files += testSourceSet.resources.srcDirs.toList()
		val actualFiles = files - blacklist

		injector.mapInstances(ConfigClassPaths(actualFiles.map { it.absolutePath }))

		//val classPaths = injector.get<ConfigClassPaths>().classPaths
		//log.info("ConfigClassPaths:")
		//for (clazzPath in classPaths) log.info("\"clazzPath\"")
		val keepClasses = arrayListOf<String>()

		if (isTest) {
			keepClasses += "junit.textui.TestRunner"
			keepClasses += "junit.framework.JUnit4TestAdapter"
			for (files in testClassesDirs.map { File(it.absolutePath) }) {
				for (file in files.walk()) {
					if (file.name.endsWith(".class")) {
						val rfile = file.relativeTo(files)
						val clazzName = rfile.path.replace("/", ".").replace("\\", ".").removeSuffix(".class")
						//println("jsTest: ${rfile.path} :: $clazzName")
						keepClasses.add(clazzName)
					}
				}
			}
		}


		logger.info("keepClasses: $keepClasses")

		val result = AllBuildSimple(
			injector,
			entryPoint = mainClassName,
			//AllBuildTargets = AllBuildTargets,
			target = rtarget,
			output = outputFile ?: extension.output,
			settings = settings,
			targetDirectory = buildDir.absolutePath,
			initialClasses = keepClasses,
			keepClasses = keepClasses
		)

		return result
	}

	fun afterBuild(build: JTranscBuild) {
		val extra = project.extensions.findByType(ExtraPropertiesExtension::class.java)
		extra?.set("JTRANSC_LIBS", build.injector.get<ConfigLibraries>().libs)
		//project.properties["JTRANSC_LIBS"] =
		//project.setProperty("JTRANSC_LIBS", build.injector.get<ConfigLibraries>().libs)
	}
}

