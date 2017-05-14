package com.jtransc.gradle

import com.jtransc.BaseRuntimeArtifactsForVersion
import com.jtransc.JTranscVersion
import com.jtransc.gen.GenTargetDescriptor
import com.jtransc.gradle.tasks.JTranscGradleDistTask
import com.jtransc.gradle.tasks.JTranscGradleReport
import com.jtransc.gradle.tasks.JTranscGradleRunTask
import groovy.lang.Closure
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.*

/**
 * References:
 * - https://docs.gradle.org/current/userguide/java_plugin.html
 * - https://github.com/gradle/gradle/blob/master/subprojects/plugins/src/main/groovy/org/gradle/api/plugins/JavaPlugin.java
 */
open class JTranscGradlePlugin : Plugin<Project> {
	companion object {
		val jtranscVersion = JTranscVersion.getVersion()

		@Suppress("unused")
		@JvmStatic fun getJTranscVersion() = jtranscVersion
	}

	override fun apply(project: Project) {
		project.logger.info("JTranscPlugin.apply")

		project.extensions.create(JTranscGradleExtension.NAME, JTranscGradleExtension::class.java, project)

		project.logger.info(JTranscGradleDistTask.name)
		project.logger.info(JTranscGradleRunTask.name)

		//project.setProperty(JTranscDistTask::class.java.simpleName, JTranscDistTask::class.java)
		//project.setProperty(JTransRunTask::class.java.simpleName, JTransRunTask::class.java)

		fun addBuildTarget(name: String, target: String?, outputFile: String?, minimizeNames: Boolean = false) {
			JTranscGradleExtension.addBuildTargetExtra(project, name, target, outputFile, minimizeNames = minimizeNames)
		}

		JTranscGradleExtension.addBuildTargetInternal(project, "distJtransc", null, null, run = false, debug = false, compile = true, minimizeNames = false)
		JTranscGradleExtension.addBuildTargetInternal(project, "runJtransc", null, null, run = true, debug = false, compile = true, minimizeNames = false)
		JTranscGradleExtension.addBuildTargetInternal(project, "debugJtransc", null, null, run = true, debug = true, compile = true, minimizeNames = false)
		JTranscGradleExtension.addBuildTargetInternal(project, "gensrcJtransc", null, null, run = false, debug = true, compile = false, minimizeNames = false)

		val targets = ServiceLoader.load(GenTargetDescriptor::class.java).toList()

		//println("JTransc Targets:")
		//println(targets)

		for (buildTarget in targets.flatMap { it.buildTargets }) {
			addBuildTarget(buildTarget.name, buildTarget.target, buildTarget.outputFile, minimizeNames = buildTarget.minimizeNames)
		}

		project.task(mapOf(
			"type" to JTranscGradleReport::class.java,
			"group" to "other",
			"description" to "Reports the status of the runtime implementation",
			"overwrite" to true
		), "jtranscReport", JTranscGradlePlugin.LambdaClosure({ it: JTranscGradleReport ->
		})).dependsOn("build")

		project.configurations.create("jtranscRuntime")
		project.configurations.create("jtransc")
		project.configurations.create("nojtransc")

		project.configurations["nojtransc"]!!.isTransitive = false
		//project.configurations["nojtransc"]!!.isTransitive = false
		//project.configurations["compile"]!!.extendsFrom(project.configurations["nojtransc"]!!)
		for (artifact in BaseRuntimeArtifactsForVersion(jtranscVersion)) {
			project.dependencies.add("jtranscRuntime", artifact.str)
		}
	}

	open class LambdaClosure<T, TR>(val lambda: (value: T) -> TR) : Closure<T>(Unit) {
		fun doCall(vararg arguments: T) = lambda(arguments[0])

		override fun getProperty(property: String): Any = "lambda"
	}
}
