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

import com.jtransc.JTranscBuild
import com.jtransc.ConfigCaptureRunOutput
import com.jtransc.ConfigLibraries
import com.jtransc.ConfigRun
import com.jtransc.annotation.JTranscAddLibrariesList
import com.jtransc.ast.AstProgram
import com.jtransc.ast.AstProgramFeature
import com.jtransc.ast.ConfigCompile
import com.jtransc.injector.Injector
import com.jtransc.io.ProcessResult2
import com.jtransc.log.log
import com.jtransc.time.measureTime
import com.jtransc.vfs.ProcessResult

abstract class GenTargetProcessor() {
	abstract fun buildSource(): Unit
	open fun compileAndRun(redirect: Boolean = true): ProcessResult2 {
		val compileResult = compile()
		return if (!compileResult.success) {
			ProcessResult2(compileResult.exitValue)
		} else {
			this.run(redirect)
		}
	}

	abstract fun compile(): ProcessResult2
	abstract fun run(redirect: Boolean = true): ProcessResult2
}

data class GenTargetSubDescriptor(val descriptor: GenTargetDescriptor, val sub: String, val ext: String = sub) {
	val fullName: String get() = "${descriptor.name}:$sub"
	override fun toString(): String = "$fullName(.$ext)"
}

abstract class GenTargetDescriptor {
	abstract val name: String
	abstract val longName: String
	abstract val sourceExtension: String
	abstract val outputExtension: String
	open val programFeatures: Set<Class<out AstProgramFeature>> = setOf()
	open val defaultSubtarget: GenTargetSubDescriptor? = null
	open val extraLibraries = listOf<String>()
	open val extraClasses = listOf<String>()
	abstract fun getProcessor(injector: Injector): GenTargetProcessor
	open fun getTargetByExtension(ext: String): String? = null
	abstract val runningAvailable: Boolean

	fun build(injector: Injector): JTranscBuild.Result {
		val captureRunOutput = injector.get<ConfigCaptureRunOutput>().captureRunOutput
		val run = injector.get<ConfigRun>().run
		val compile = injector.get<ConfigCompile>(default = { ConfigCompile(compile = true) }).compile
		val processor = log.logAndTime("Preparing processor") {
			//, settings
			this.getProcessor(injector)
		}
		log.logAndTime("Building source") { processor.buildSource() }

		JTranscAddLibrariesList::class.java

		val program: AstProgram = injector.get()
		val libraries = program.allAnnotationsList.getTypedList(JTranscAddLibrariesList::value).flatMap {
			it.value.toList()
		}
		injector.mapInstance(ConfigLibraries(libraries.distinct()))

		if (compile) {
			if (run) {
				log("Compiling and running...")
				val (compileTime, result) = measureTime { processor.compileAndRun(!captureRunOutput) }
				if (result.success) {
					log("Ok ($compileTime)")
				} else {
					log.error("ERROR ($compileTime) (${result.exitValue})")
				}
				return JTranscBuild.Result(result)
			} else {
				log("Compiling...")
				val (compileTime, compileResult) = measureTime { processor.compile() }
				if (compileResult.success) {
					log("Ok ($compileTime)")
				} else {
					log.error("ERROR ($compileTime) ($compileResult)")
				}

				return JTranscBuild.Result(ProcessResult2(compileResult.exitValue))
			}
		}
		return JTranscBuild.Result(ProcessResult2(0))
	}

	override fun toString(): String = this.javaClass.simpleName
}