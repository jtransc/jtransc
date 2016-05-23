package com.jtransc.gradle.tasks

import org.gradle.api.tasks.TaskAction

open class JTranscRunTask() : AbstractJTranscTask() {
	companion object {
		val name: String = JTranscRunTask::class.java.name
	}

	@Suppress("unused")
	@TaskAction open fun task() {
		logger.info("buildAndRunRedirecting $name : $target")
		//println("buildAndRunRedirecting $name : $target")
		prepare().buildAndRunRedirecting()
	}
}
