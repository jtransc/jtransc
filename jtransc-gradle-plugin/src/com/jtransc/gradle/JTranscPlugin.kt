package com.jtransc.gradle

import com.jtransc.JTranscVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

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
		project.logger.info("JTranscPlugin.apply")

		project.extensions.create(JTranscExtension.NAME, JTranscExtension::class.java, project)

		project.task(mapOf("type" to JTranscBuildTask::class.java), "jtransc").dependsOn("build")
		project.task(mapOf("type" to JTransBuildAndRunTask::class.java), "runJtransc").dependsOn("build")
	}

	open class JTranscBuildTask : AbstractJTranscTask() {
		@TaskAction open fun task() {
			logger.info("buildWithoutRunning")
			prepare().buildWithoutRunning()
		}
	}

	open class JTransBuildAndRunTask : AbstractJTranscTask() {
		@TaskAction open fun task() {
			logger.info("buildAndRunRedirecting")
			prepare().buildAndRunRedirecting()
		}
	}
}