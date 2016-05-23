package com.jtransc.gradle

import com.jtransc.ast.AstBuildSettings
import org.gradle.api.Project
import java.io.File

open class JTranscExtension(val project: Project) {
	companion object {
		@JvmStatic val NAME = "jtransc"
	}

	init {
		project.logger.debug("Instantiated JTranscExtension! with project '$project' name: $NAME")
	}

	var tittle = "App Title"
	var name = "AppName"
	var version = "0.0.1"
	var target = "haxe:js"
	var output: String? = null
	var extra = hashMapOf<String?, String?>()
	var company = "MyCompany"
	var package_ = "com.test"
	var embedResources = true
	var libraries = arrayListOf<String>()
	var assets = arrayListOf<File>()
	var debug = true
	var initialWidth = 1280
	var initialHeight = 720
	var vsync = true
	var resizable = true
	var borderless = false
	var fullscreen = false
	var icon: String? = null
	var orientation = "auto"
	var relooper = false
	var minimizeNames = false
	var analyzer = false
}