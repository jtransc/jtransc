package com.jtransc.gradle

import org.gradle.api.Project

open class JTranscExtension(val project: Project) {
	companion object {
		@JvmStatic val NAME = "jtransc"
	}

	var tittle = "App Title"
	var demo = "demo"

	init {

		println("Instantiated JTranscExtension! with project '$project' name: $NAME, demo: $demo")
	}
}