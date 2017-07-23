package com.jtransc.gradle

import com.jtransc.gradle.tasks.AbstractJTranscGradleTask
import com.jtransc.gradle.tasks.JTranscGradleDistTask
import com.jtransc.gradle.tasks.JTranscGradleRunTask
import org.gradle.api.Project
import java.io.File

@Suppress("unused")
open class JTranscGradleExtension(val project: Project) {
	companion object {
		@JvmStatic val NAME = "jtransc"

		fun addBuildTarget(project: Project, name: String, target: String?, outputFile: String?) {
			addBuildTargetExtra(project, name, target, outputFile, minimizeNames = false)
		}

		fun addBuildTargetMinimized(project: Project, name: String, target: String?, outputFile: String?) {
			addBuildTargetExtra(project, name, target, outputFile, minimizeNames = true)
		}

		fun addBuildTargetExtra(project: Project, name: String, target: String?, outputFile: String?, minimizeNames: Boolean) {
			JTranscGradleExtension.addBuildTargetInternal(project, "gensrc" + name.capitalize(), target, outputFile, run = false, debug = false, compile = false, minimizeNames = minimizeNames)
			JTranscGradleExtension.addBuildTargetInternal(project, "dist" + name.capitalize(), target, outputFile, run = false, debug = false, compile = true, minimizeNames = minimizeNames)
			JTranscGradleExtension.addBuildTargetInternal(project, "run" + name.capitalize(), target, outputFile, run = true, debug = false, compile = true, minimizeNames = minimizeNames)
			JTranscGradleExtension.addBuildTargetInternal(project, "debug" + name.capitalize(), target, outputFile, run = true, debug = true, compile = true, minimizeNames = minimizeNames)
		}

		fun addBuildTargetInternal(project: Project, name: String, target: String?, outputFile: String?, run: Boolean, debug: Boolean, compile: Boolean, minimizeNames: Boolean) {
			val justBuild = !run
			val clazz = if (run) JTranscGradleRunTask::class.java else JTranscGradleDistTask::class.java
			val group = if (run) "application" else "distribution"
			val verb = if (compile) (if (run) "Runs" else "Packages") else "Generate source"
			// https://docs.gradle.org/current/dsl/org.gradle.api.Project.html#org.gradle.api.Project:task(java.util.Map, java.lang.String)
			project.task(mapOf(
				"type" to clazz,
				"group" to group,
				"description" to "$verb the application as $name; target=$target, debug=$debug, outputFile=$outputFile",
				"overwrite" to true
			), name, JTranscGradlePlugin.LambdaClosure({ it: AbstractJTranscGradleTask ->
				it.target = target
				it.outputFile = outputFile
				it.minimizedNames = justBuild && minimizeNames
				it.debug = debug
				it.compile = compile
			})).dependsOn("build")
		}
	}

	init {
		project.logger.debug("Instantiated JTranscExtension! with project '$project' name: $NAME")
	}

	var extra = hashMapOf<String?, String?>()
	var extraVars = hashMapOf<String, ArrayList<String>>()
	var libraries = arrayListOf<String>()
	var assets = arrayListOf<String>()
	val newAssets = arrayListOf<File>()
	var title: String? = null
	var name: String? = null
	var version: String? = null
	var target: String? = null
	var output: String? = null
	var company: String? = null
	var package_: String? = null
	var embedResources: Boolean? = null
	var debug: Boolean? = null
	var optimize: Boolean? = null
	var initialWidth: Int? = null
	var initialHeight: Int? = null
	var vsync: Boolean? = null
	var resizable: Boolean? = null
	var borderless: Boolean? = null
	var fullscreen: Boolean? = null
	var icon: String? = null
	var orientation: String? = null
	var relooper: Boolean? = null
	var minimizeNames: Boolean? = null
	var analyzer: Boolean? = null
	var mainClassName: String? = null
	var treeshaking: Boolean? = null
	var treeshakingTrace: Boolean? = null
	var skipServiceLoaderClasses: ArrayList<String> = arrayListOf()

	fun assets(vararg folders: String) = run { newAssets += folders.map { File(project.buildFile.parentFile, it) } }
	fun skipServiceLoader(serviceLoader: String) = skipServiceLoaderClasses.add(serviceLoader)
	fun param(key: String, value: String?) = run { extra[key] = value }
	fun param(key: String) = param(key, "true")
	fun appendVar(key: String, value: List<String>) = extraVars.getOrPut(key) { arrayListOf() }.addAll(value)
	fun appendVar(key: String, value: String) = extraVars.getOrPut(key) { arrayListOf() }.add(value)

	/*
	Alias for:

	import com.jtransc.gradle.tasks.JTranscDistTask
	import com.jtransc.gradle.tasks.JTranscRunTask

	task distWindows(type: JTranscDistTask) {
		target = "haxe:windows"
		outputFile = "program.exe"
		minimizedNames = false
		debug = false
	}

	task runWindows(type: JTranscRunTask) {
		target = "haxe:windows"
		outputFile = "program.exe"
		minimizedNames = false
		debug = true
	}
	*/
	@Suppress("unused")
	fun customTarget(name: String, target: String, extension: String) {
		JTranscGradleExtension.addBuildTarget(project, name, target, "program.$extension")
	}

	@Suppress("unused")
	fun customTargetMinimized(name: String, target: String, extension: String) {
		JTranscGradleExtension.addBuildTargetMinimized(project, name, target, "program.$extension")
	}
}