package com.jtransc.gradle

import com.jtransc.gradle.tasks.AbstractJTranscTask
import com.jtransc.gradle.tasks.JTranscDistTask
import com.jtransc.gradle.tasks.JTranscRunTask
import org.gradle.api.Project

open class JTranscExtension(val project: Project) {
	companion object {
		@JvmStatic val NAME = "jtransc"

		fun addBuildTarget(project: Project, name: String, target: String?, outputFile: String?) {
			addBuildTargetExtra(project, name, target, outputFile, minimizeNames = false)
		}

		fun addBuildTargetMinimized(project: Project, name: String, target: String?, outputFile: String?) {
			addBuildTargetExtra(project, name, target, outputFile, minimizeNames = true)
		}

		fun addBuildTargetExtra(project: Project, name: String, target: String?, outputFile: String?, minimizeNames: Boolean) {
			JTranscExtension.addBuildTargetInternal(project, "dist" + name.capitalize(), target, outputFile, run = false, debug = false, minimizeNames = minimizeNames)
			JTranscExtension.addBuildTargetInternal(project, "run" + name.capitalize(), target, outputFile, run = true, debug = false, minimizeNames = minimizeNames)
			JTranscExtension.addBuildTargetInternal(project, "debug" + name.capitalize(), target, outputFile, run = true, debug = true, minimizeNames = minimizeNames)
		}

		fun addBuildTargetInternal(project: Project, name: String, target: String?, outputFile: String?, run: Boolean, debug: Boolean, minimizeNames: Boolean) {
			val justBuild = !run
			val clazz = if (run) JTranscRunTask::class.java else JTranscDistTask::class.java
			val group = if (run) "application" else "distribution"
			val verb = (if (run) "Runs" else "Packages")
			// https://docs.gradle.org/current/dsl/org.gradle.api.Project.html#org.gradle.api.Project:task(java.util.Map, java.lang.String)
			project.task(mapOf(
				"type" to clazz,
				"group" to group,
				"description" to "$verb the application as $name; target=$target, debug=$debug, outputFile=$outputFile",
				"overwrite" to true
			), name, JTranscPlugin.LambdaClosure({ it: AbstractJTranscTask ->
				it.target = target
				it.outputFile = outputFile
				it.minimizedNames = justBuild && minimizeNames
				it.debug = if (debug) true else false
			})).dependsOn("build")
		}
	}

	init {
		project.logger.debug("Instantiated JTranscExtension! with project '$project' name: $NAME")
	}

	var extra = hashMapOf<String?, String?>()
	var libraries = arrayListOf<String>()
	var assets = arrayListOf<String>()
	var title: String? = null
	var name: String? = null
	var version: String? = null
	var target: String? = null
	var output: String? = null
	var company: String? = null
	var package_: String? = null
	var embedResources: Boolean? = null
	var debug: Boolean? = null
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
		JTranscExtension.addBuildTarget(project, name, target, "program.$extension")
	}

	@Suppress("unused")
	fun customTargetMinimized(name: String, target: String, extension: String) {
		JTranscExtension.addBuildTargetMinimized(project, name, target, "program.$extension")
	}
}