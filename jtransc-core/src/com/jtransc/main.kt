/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jtransc

import com.jtransc.ast.AstBuildSettings
import com.jtransc.gen.GenTargetDescriptor
import java.io.File
import java.util.*

object JTranscMain {
	@JvmStatic fun main(args: Array<String>) {
		// @TODO: allow a plugin system
		val targets = AllBuildTargets

		fun help() {
			val targetNames = targets.map { it.name }.joinToString(", ")
			val executableTypes = targets.map { it.outputExtension }.joinToString(", ")

			println("jtransc <list of class paths or jar files>")
			println("")
			println("Performs an aot compilation that transform a java/kotlin compiled program (class and jar files)")
			println("into an executable file ($executableTypes) file at the moment.")
			println("")
			println("  -main   <fqname> - Specifies class with static void main method that will be the entry point of the app")
			println("  -target <target> - Language target to do the AOT possible values ($targetNames)")
			println("  -out    <file>   - Output file that will hold the generated aot result file")
			println("  -release         - Optimizes and performs compression minimization to the output")
			println("")
			println("  -run             - Runs generated executable")
			println("")
			println("  -help            - Displays help")
			println("  -v               - Verbose")
			println("")
			println("Examples:")
			println("")
			println("  jtransc dependency.jar target/classes -main com.test.Main -out program.js")
			println("  jtransc dependency.jar target/classes -main com.test.Main -target as3 -out program.swf")
			println("")
			System.exit(0)
		}

		data class ProgramConfig(
			val target: GenTargetDescriptor,
			val classPaths: List<String>,
			val entryPoint: String,
			val output: String,
			val run: Boolean,
			val targetDirectory: String,
			val settings: AstBuildSettings
		)

		fun parseArgs(_args: List<String>): ProgramConfig {
			val args: Queue<String> = ArrayDeque(_args)

			val classPaths = arrayListOf<String>()
			var entryPoint = ""
			var targetName: String? = null
			var out: String? = null
			var run = false
			val settings = AstBuildSettings()

			if (args.isEmpty()) {
				help()
			}

			while (args.isNotEmpty()) {
				val arg = args.remove()
				if (arg.startsWith('-')) {
					when (arg) {
						"-help" -> help()
						"-main" -> entryPoint = args.remove()
						"-target" -> targetName = args.remove()
						"-release" -> settings.debug = false
						"-out" -> out = args.remove()
						"-run" -> run = true
						else -> throw Exception("Unknown switch $arg")
					}
				} else {
					classPaths.add(arg)
				}
			}

			if (targetName == null && out != null) {
				targetName = targets.locateTargetByOutExt(File(out).extension).name
			}

			val target = targets.locateTargetByName(targetName ?: "js")

			return ProgramConfig(
				classPaths = classPaths.toList(),
				entryPoint = entryPoint ?: throw Exception("Main class not provided"),
				target = target,
				settings = settings,
				output = out ?: "program.${target.outputExtension}",
				targetDirectory = System.getProperty("java.io.tmpdir"),
				run = run
			)
		}

		try {
			val config = parseArgs(args.toList())
			val build = AllBuild(
				target = config.target,
				classPaths = config.classPaths,
				entryPoint = config.entryPoint,
				output = config.output,
				subtarget = "",
				targetDirectory = config.targetDirectory
			)
			build.buildAndRun(settings = config.settings, captureRunOutput = false, run = config.run)
		} catch (e: Throwable) {
			e.printStackTrace(System.err)
			System.exit(-1)
		}
	}
}

fun main(args: Array<String>) {
	JTranscMain.main(args)
}