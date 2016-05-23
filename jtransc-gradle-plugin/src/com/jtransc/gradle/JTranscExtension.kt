package com.jtransc.gradle

import org.gradle.api.Project
import java.io.File

open class JTranscExtension(val project: Project) {
	companion object {
		@JvmStatic val NAME = "jtransc"
	}

	init {
		project.logger.debug("Instantiated JTranscExtension! with project '$project' name: $NAME")
	}

	var extra = hashMapOf<String?, String?>()
	var libraries = arrayListOf<String>()
	var assets = arrayListOf<File>()
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
}