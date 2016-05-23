package com.jtransc.gradle

import com.jtransc.JTranscVersion
import groovy.lang.Closure
import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.runtime.MethodClosure
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

		//project.setProperty(JTranscBuildTask::class.java.name, JTranscBuildTask::class.java)
		//project.setProperty(JTransBuildAndRunTask::class.java.name, JTransBuildAndRunTask::class.java)

		// https://docs.gradle.org/current/dsl/org.gradle.api.Project.html#org.gradle.api.Project:task(java.util.Map, java.lang.String)
		fun addBuildTarget(name:String, target:String?, outputFile: String?, run:Boolean) {
			val clazz = if (run) JTransBuildAndRunTask::class.java else JTranscBuildTask::class.java
			project.task(mapOf("type" to clazz, "group" to "jtransc", "overwrite" to true), name, LambdaClosure({ it:AbstractJTranscTask -> it.target = target; it.outputFile = outputFile })).dependsOn("build")
		}

		addBuildTarget("jtransc", null, null, run = false)
		addBuildTarget("runJtransc", null, null, run = true)

		addBuildTarget("js", "haxe:js", "program.js", run = false)
		addBuildTarget("swf", "haxe:swf", "program.swf", run = false)
		addBuildTarget("cpp", "haxe:cpp", "program.exe", run = false)
		addBuildTarget("neko", "haxe:neko", "program.n", run = false)
		addBuildTarget("php", "haxe:php", "program.php", run = false)

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

open class JTranscBuildTask() : AbstractJTranscTask() {
	@Suppress("unused")
	@TaskAction open fun task() {
		logger.info("buildWithoutRunning $name : $target")
		//println("buildWithoutRunning $name : $target")
		prepare().buildWithoutRunning()
	}
}

open class JTransBuildAndRunTask() : AbstractJTranscTask() {
	@Suppress("unused")
	@TaskAction open fun task() {
		logger.info("buildAndRunRedirecting $name : $target")
		//println("buildAndRunRedirecting $name : $target")
		prepare().buildAndRunRedirecting()
	}
}
