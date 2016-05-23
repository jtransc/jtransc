package com.jtransc.gradle

import com.jtransc.JTranscVersion
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * References:
 * - https://docs.gradle.org/current/userguide/java_plugin.html
 * - https://github.com/gradle/gradle/blob/master/subprojects/plugins/src/main/groovy/org/gradle/api/plugins/JavaPlugin.java
 */
open class JTranscPlugin : Plugin<Project> {
	companion object {
		@Suppress("unused")
		@JvmStatic fun getJTranscVersion() = JTranscVersion.getVersion()
	}

	override fun apply(project: Project) {
		println("JTranscPlugin.apply")

		project.extensions.create(JTranscExtension.NAME, JTranscExtension::class.java, project)

		project.task(mapOf("type" to JTranscTask::class.java), JTranscTask::jtransc.name).dependsOn("build")
	}
}