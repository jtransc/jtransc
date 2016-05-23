package com.jtransc.gradle

import com.jtransc.JTranscVersion
import org.gradle.api.Plugin
import org.gradle.api.Project

open class JTranscPlugin : Plugin<Project> {
	companion object {
		@Suppress("unused")
		@JvmStatic fun getJTranscVersion() = JTranscVersion.getVersion()
	}

	override fun apply(project: Project) {
		println("JTranscPlugin.apply")

		project.getExtensions().create(JTranscExtension.NAME, JTranscExtension::class.java, project)

		project.task(mapOf("type" to JTranscTask::class.java), "testJTranscTask")
	}
}