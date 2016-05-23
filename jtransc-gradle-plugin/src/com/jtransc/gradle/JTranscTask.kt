package com.jtransc.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class JTranscTask : DefaultTask() {
	@TaskAction open fun testJTranscTask() {
		val extension = project.property(JTranscExtension.NAME) as JTranscExtension
		System.out.println("JTranscTask.testJTranscTask() extension: $extension, ${extension.demo}");
	}
}