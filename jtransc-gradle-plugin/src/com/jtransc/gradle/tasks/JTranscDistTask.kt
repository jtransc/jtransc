package com.jtransc.gradle.tasks

import com.jtransc.error.invalidOp
import org.gradle.api.tasks.TaskAction

open class JTranscDistTask() : AbstractJTranscTask() {
	companion object {
		val name: String = JTranscRunTask::class.java.name
	}

	@Suppress("unused")
	@TaskAction open fun task() {
		logger.info("buildWithoutRunning $name : $target")
		//println("buildWithoutRunning $name : $target")
		val result = prepare().buildWithoutRunning()
		val process = result.process
		if (!process.success) {
			invalidOp("Process exited with code ${process.exitValue}")
		}
	}
}
