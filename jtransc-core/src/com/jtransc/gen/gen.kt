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

package com.jtransc.gen

import com.jtransc.*
import com.jtransc.annotation.JTranscAddLibrariesList
import com.jtransc.ast.AstProgram
import com.jtransc.ast.AstProgramFeature
import com.jtransc.ast.ConfigCompile
import com.jtransc.ast.getTypedList
import com.jtransc.gen.common.CommonGenerator
import com.jtransc.injector.Injector
import com.jtransc.io.ProcessResult2
import com.jtransc.log.log
import com.jtransc.time.measureTime

data class GenTargetSubDescriptor(val descriptor: GenTargetDescriptor, val sub: String, val ext: String = sub) {
	val fullName: String get() = "${descriptor.name}:$sub"
	override fun toString(): String = "$fullName(.$ext)"
}

data class TargetName(val name: String) {
	val parts = name.split(':')
	val primary = parts.getOrElse(0) { "" }
	val secondary = parts.getOrElse(1) { "" }

	companion object {
		fun matches(target: String, pattern: String): Boolean {
			if (pattern == "") return true
			if (pattern == "all") return true
			return pattern == target
		}
	}

	fun haxeMatches(pattern: String): Boolean {
		return (primary == "haxe") && (secondary == pattern || pattern == "")
	}

	fun matches(pattern: String): Boolean = TargetName.matches(this.name, pattern) || TargetName.matches(this.primary, pattern)

	fun matches(pattern: List<String>): Boolean = pattern.any { matches(it) }
}

data class TargetBuildTarget(val name: String, val target: String?, val outputFile: String?, val minimizeNames: Boolean = false)

abstract class GenTargetDescriptor {
	open val priority: Int = 0
	abstract val name: String
	open val longName: String get() = name
	open val sourceExtension: String get() = name
	open val outputExtension: String get() = "out"
	open val programFeatures: Set<Class<out AstProgramFeature>> = setOf()
	open val defaultSubtarget: GenTargetSubDescriptor? = null
	open val extraLibraries = listOf<String>()
	open val extraClasses = listOf<String>()
	abstract fun getGenerator(injector: Injector): CommonGenerator
	open fun getTargetByExtension(ext: String): String? = null
	abstract val runningAvailable: Boolean
	val targetName by lazy { TargetName(name) }

	open val buildTargets = listOf<TargetBuildTarget>()

	open val outputFile: String get() = "program.$sourceExtension"

	fun build(injector: Injector): JTranscBuild.Result {
		val captureRunOutput = injector.get<ConfigCaptureRunOutput>().captureRunOutput
		val run = injector.get<ConfigRun>().run
		val compile = injector.get<ConfigCompile>(default = { ConfigCompile(compile = true) }).compile
		val generator = log.logAndTime("Preparing generator") {
			injector.mapInstance(ConfigOutputFile(outputFile))
			injector.mapInstance(TargetName(name))

			val generator = this.getGenerator(injector)
			injector.mapInstance<CommonGenerator>(generator)
			generator
		}
		log.logAndTime("Building source") { generator.writeProgramAndFiles() }

		JTranscAddLibrariesList::class.java

		val program: AstProgram = injector.get()
		val libraries = program.allAnnotationsList.getTypedList(JTranscAddLibrariesList::value).flatMap {
			it.value.toList()
		}
		injector.mapInstance(ConfigLibraries(libraries.distinct()))

		if (compile) {
			if (run) {
				log("Compiling and running...")
				val (compileTime, result) = measureTime { generator.compileAndRun(!captureRunOutput) }
				if (result.success) {
					log("Ok ($compileTime)")
				} else {
					log.error("ERROR ($compileTime) (${result.exitValue})")
				}
				return JTranscBuild.Result(result, generator)
			} else {
				log("Compiling...")
				val (compileTime, compileResult) = measureTime { generator.compile() }
				if (compileResult.success) {
					log("Ok ($compileTime)")
				} else {
					log.error("ERROR ($compileTime) ($compileResult)")
				}

				return JTranscBuild.Result(ProcessResult2(compileResult.exitValue), generator)
			}
		}
		return JTranscBuild.Result(ProcessResult2(0), generator)
	}

	override fun toString(): String = this.javaClass.simpleName
}