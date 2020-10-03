package com.jtransc.gradle.tasks

open class JTranscGradleTestTask() : AbstractJTranscGradleRunTask(true) {
	companion object {
		val name: String = JTranscGradleTestTask::class.java.name
	}
}
