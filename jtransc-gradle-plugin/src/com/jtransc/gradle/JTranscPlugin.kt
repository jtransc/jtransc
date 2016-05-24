package com.jtransc.gradle

import com.jtransc.JTranscVersion
import com.jtransc.gradle.tasks.JTranscDistTask
import com.jtransc.gradle.tasks.JTranscRunTask
import groovy.lang.Closure
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
		project.logger.info("JTranscPlugin.apply")

		project.extensions.create(JTranscExtension.NAME, JTranscExtension::class.java, project)

		project.logger.info(JTranscDistTask.name)
		project.logger.info(JTranscRunTask.name)

		//project.setProperty(JTranscDistTask::class.java.simpleName, JTranscDistTask::class.java)
		//project.setProperty(JTransRunTask::class.java.simpleName, JTransRunTask::class.java)

		fun addBuildTarget(name: String, target: String?, outputFile: String?, minimizeNames: Boolean = false) {
			JTranscExtension.addBuildTargetExtra(project, name, target, outputFile, minimizeNames = minimizeNames)
		}

		JTranscExtension.addBuildTargetInternal(project, "distJtransc", null, null, run = false, minimizeNames = false)
		JTranscExtension.addBuildTargetInternal(project, "runJtransc", null, null, run = true, minimizeNames = false)

		addBuildTarget("js", "haxe:js", "program.js", minimizeNames = true)
		addBuildTarget("swf", "haxe:swf", "program.swf")
		addBuildTarget("cpp", "haxe:cpp", "program.exe")
		addBuildTarget("neko", "haxe:neko", "program.n")
		addBuildTarget("php", "haxe:php", "program.php")
	}

	open class LambdaClosure<T, TR>(val lambda: (value: T) -> TR) : Closure<T>(Unit) {
		fun doCall(vararg arguments: T) = lambda(arguments[0])

		override fun getProperty(property: String): Any = "lambda"
	}
}
