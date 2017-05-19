package com.jtransc.gen.common

import com.jtransc.annotation.JTranscRegisterCommand
import com.jtransc.annotation.JTranscRegisterCommandList
import com.jtransc.annotation.JTranscRunCommand
import com.jtransc.annotation.JTranscRunCommandList
import com.jtransc.ast.AstProgram
import com.jtransc.ast.getTypedList
import com.jtransc.io.ProcessUtils
import com.jtransc.log.log
import com.jtransc.vfs.ExecOptions
import com.jtransc.vfs.ProcessResult
import com.jtransc.vfs.RootLocalVfs

@Suppress("UNUSED_PARAMETER")
//@JTranscRegisterCommand(target = "js", name = "electron", check = { "electron", "--version"}, getFolderCmd = "npm list -g", install = "npm -g install electron-prebuilt")
//@JTranscRunCommand(target = "js", value = *arrayOf("@js/template/runelectron.cmd"))
object CommonGenCliCommands {
	fun getPaths(): List<String> {
		val env = System.getenv("PATH") ?: ":"
		return env.split(ProcessUtils.pathSeparator)
	}

	fun getAllCustomCommandsForTarget(program: AstProgram, target: String): Map<String, JTranscRegisterCommand> {
		return program.allAnnotationsList.getTypedList(JTranscRegisterCommandList::value).filter { it.target == "js" }.associateBy { it.name }
	}

	fun getAllRunCommandForTarget(program: AstProgram, target: String): List<JTranscRunCommand> {
		return program.allAnnotationsList.getTypedList(JTranscRunCommandList::value).filter { it.target == "js" }
	}

	fun getRunProgramCmd(program: AstProgram, target: String, default: List<String>): List<String> {
		return CommonGenCliCommands.getAllRunCommandForTarget(program, target = target).map { it.value }.firstOrNull()?.toList() ?: default
	}

	fun runProgramCmd(program: AstProgram, target: String, default: List<String>, template: IProgramTemplate, options: ExecOptions): ProcessResult {
		val commands = getAllCustomCommandsForTarget(program = program, target = target)
		val templateParts = getRunProgramCmd(program, target = target, default = default)
		var lines = template.gen(templateParts.joinToString("\n")).split("\n").map { it.trim() }.filter { it.isNotEmpty() }
		//println("Commands:")
		//println(commands)
		if (lines[0] in commands) {
			val command = commands[lines[0]]!!
			//println("Using command:" + command)
			installOnce(command)
			lines = listOf(command.command) + lines.drop(1)
		}
		return RootLocalVfs().exec(lines, options = options)
	}

	fun execCmd(vararg cmd: String): Boolean {
		return RootLocalVfs().exec(cmd.toList(), ExecOptions(sysexec = true)).success
		//val process = Runtime.getRuntime().exec(cmd)
		//process.waitFor()
		//return process.exitValue() == 0
	}

	fun check(cmd: JTranscRegisterCommand): Boolean {
		log.info("Checking ${cmd.name}...")
		try {
			return execCmd(*cmd.check)
		} catch (e: Throwable) {
			log.warn(e)
			return false
		}
	}

	fun install(cmd: JTranscRegisterCommand): Boolean {
		log.info("Installing ${cmd.name}...")
		return execCmd(*cmd.install)
	}

	fun installOnce(cmd: JTranscRegisterCommand) {
		if (!check(cmd)) install(cmd)
	}
}