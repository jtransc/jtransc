package com.jtransc.gradle.tasks

open class JTranscGradleRunTask() : AbstractJTranscGradleRunTask(false) {
	companion object {
		val name: String = JTranscGradleRunTask::class.java.name
	}
}
