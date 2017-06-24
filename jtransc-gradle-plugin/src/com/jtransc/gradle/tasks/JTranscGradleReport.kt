package com.jtransc.gradle.tasks

import com.jtransc.JTranscRtReport
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class JTranscGradleReport : DefaultTask() {
	@Suppress("unused")
	@TaskAction open fun task() {
		JTranscRtReport.main(arrayOf<String>())
	}
}
