package com.jtransc.gradle

import com.jtransc.AllBuild
import com.jtransc.AllBuildTargets
import com.jtransc.ast.AstBuildSettings
import com.jtransc.error.invalidOp
import com.jtransc.gen.haxe.HaxeGenDescriptor
import com.jtransc.gen.haxe.HaxeGenTargetProcessor
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction

open class JTranscTask : DefaultTask() {
	@TaskAction open fun jtransc() {
		val extension = project.getIfExists<JTranscExtension>(JTranscExtension.NAME)!!
		val mainClassName = project.getIfExists<String>("mainClassName")
		logger.info("JTranscTask.jtransc() extension: $extension, ${extension.demo}");
		//println(project.property("output.classesDir"))
		val sourceSet = project.property("sourceSets") as SourceSetContainer
		val classesDir = sourceSet["main"].output.classesDir
		logger.info("output classesDir: $classesDir")
		logger.info("sourceSet: $sourceSet")
		logger.info("mainClassName: $mainClassName")

		val settings = AstBuildSettings(
			title = extension.tittle
		)

		val build = AllBuild(
			target = HaxeGenDescriptor,
			classPaths = listOf(classesDir.absolutePath),
			entryPoint = mainClassName ?: invalidOp("JTransc: Not defined mainClassName in build.gradle!"),
			output = "test.js",
			subtarget = "js",
			settings = settings
			//targetDirectory = ""
		)

		build.buildWithoutRunning()
	}
}

// Kotlin extensions
fun <T> Project.getIfExists(name:String): T? = if (this.hasProperty(name)) this.property(name) as T else null
operator fun <T> NamedDomainObjectSet<T>.get(key:String):T = this.getByName(key)