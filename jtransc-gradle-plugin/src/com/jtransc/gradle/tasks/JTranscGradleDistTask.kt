package com.jtransc.gradle.tasks

import com.jtransc.ConfigLibraries
import com.jtransc.error.invalidOp
import org.gradle.api.tasks.TaskAction

open class JTranscGradleDistTask() : AbstractJTranscGradleTask() {
	companion object {
		val name: String = JTranscGradleRunTask::class.java.name
	}

	@Suppress("unused")
	@TaskAction open fun task() {
		logger.info("buildWithoutRunning $name : $target")
		//println("buildWithoutRunning $name : $target")
		val build = prepare()
		val result = build.buildWithoutRunning()
		afterBuild(build)
		val process = result.process
		if (!process.success) {
			invalidOp("Process exited with code ${process.exitValue}")
		}
	}
}
