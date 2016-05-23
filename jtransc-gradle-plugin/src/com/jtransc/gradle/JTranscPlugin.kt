package com.jtransc.gradle

import com.jtransc.JTranscVersion
import com.jtransc.gradle.tasks.AbstractJTranscTask
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

		// https://docs.gradle.org/current/dsl/org.gradle.api.Project.html#org.gradle.api.Project:task(java.util.Map, java.lang.String)
		fun addBuildTarget(name: String, target: String?, outputFile: String?, run: Boolean) {
			val clazz = if (run) JTranscRunTask::class.java else JTranscDistTask::class.java
			//project.task(mapOf("type" to clazz, "group" to "distribution", "overwrite" to true), name, LambdaClosure({ it: AbstractJTranscTask -> it.target = target; it.outputFile = outputFile })).dependsOn("build")
			project.task(mapOf("type" to clazz, "group" to "jtransc"), name, LambdaClosure({ it: AbstractJTranscTask -> it.target = target; it.outputFile = outputFile })).dependsOn("build")
		}

		addBuildTarget("distJtransc", null, null, run = false)
		addBuildTarget("runJtransc", null, null, run = true)

		addBuildTarget("distJs", "haxe:js", "program.js", run = false)
		addBuildTarget("distSwf", "haxe:swf", "program.swf", run = false)
		addBuildTarget("distCpp", "haxe:cpp", "program.exe", run = false)
		addBuildTarget("distNeko", "haxe:neko", "program.n", run = false)
		addBuildTarget("distPhp", "haxe:php", "program.php", run = false)

		addBuildTarget("runNodeJs", "haxe:js", "program.js", run = true)
		addBuildTarget("runSwf", "haxe:swf", "program.swf", run = true)
		addBuildTarget("runCpp", "haxe:cpp", "program.exe", run = true)
		addBuildTarget("runNeko", "haxe:neko", "program.n", run = true)
		addBuildTarget("runPhp", "haxe:php", "program.php", run = true)
	}

	private open class LambdaClosure<T, TR>(val lambda: (value: T) -> TR) : Closure<T>(Unit) {
		fun doCall(vararg arguments: T) = lambda(arguments[0])

		override fun getProperty(property: String): Any = "lambda"
	}
}
