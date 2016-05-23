package com.jtransc.gradle.tasks

import org.gradle.api.tasks.TaskAction

open class JTranscDistTask() : AbstractJTranscTask() {
	companion object {
		val name: String = JTranscRunTask::class.java.name
	}

	@Suppress("unused")
	@TaskAction open fun task() {
		logger.info("buildWithoutRunning $name : $target")
		//println("buildWithoutRunning $name : $target")
		prepare().buildWithoutRunning()
	}
}
