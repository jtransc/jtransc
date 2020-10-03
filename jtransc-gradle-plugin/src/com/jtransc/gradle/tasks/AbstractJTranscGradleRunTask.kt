package com.jtransc.gradle.tasks

import com.jtransc.error.invalidOp
import org.apache.tools.ant.types.Commandline
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

abstract class AbstractJTranscGradleRunTask(val isTest: Boolean) : AbstractJTranscGradleTask() {
	@Option(option = "args", description = "Command line arguments passed to the main class.")
	open fun setArgsString(args: String?): AbstractJTranscGradleRunTask {
		return this.setArgs(Commandline.translateCommandline(args).toList())
	}

	var args = listOf<String>()

	open fun setArgs(args: List<String>): AbstractJTranscGradleRunTask {
		this.args = args;
		return this
	}

	@Suppress("unused")
	@TaskAction
	open fun task() {
		logger.info("buildAndRunRedirecting $name : $target")
		//println("buildAndRunRedirecting $name : $target")
		val build = prepare(isTest, args = args)
		val result = build.buildAndRunRedirecting(args)
		afterBuild(build)
		val process = result.process
		if (!process.success) {
			invalidOp("Process exited with code ${process.exitValue}")
		}
	}
}
