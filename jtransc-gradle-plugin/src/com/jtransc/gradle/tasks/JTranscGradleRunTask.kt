package com.jtransc.gradle.tasks

import com.jtransc.error.invalidOp
import org.apache.tools.ant.types.Commandline
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.util.*

open class JTranscGradleRunTask() : AbstractJTranscGradleTask() {
	companion object {
		val name: String = JTranscGradleRunTask::class.java.name
	}

	@Option(option = "args", description = "Command line arguments passed to the main class.")
	open fun setArgsString(args: String?): JTranscGradleRunTask {
		return this.setArgs(Commandline.translateCommandline(args).toList())
	}

	var args = listOf<String>()

	open fun setArgs(args: List<String>): JTranscGradleRunTask {
		this.args = args;
		return this
	}

	@Suppress("unused")
	@TaskAction open fun task() {
		logger.info("buildAndRunRedirecting $name : $target")
		//println("buildAndRunRedirecting $name : $target")
		val build = prepare()
		val result = build.buildAndRunRedirecting(args)
		afterBuild(build)
		val process = result.process
		if (!process.success) {
			invalidOp("Process exited with code ${process.exitValue}")
		}
	}
}
