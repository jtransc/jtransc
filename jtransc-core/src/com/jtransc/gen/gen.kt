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

import com.jtransc.ast.AstBuildSettings
import com.jtransc.ast.AstFieldRef
import com.jtransc.ast.AstMethodRef
import com.jtransc.ast.AstProgram
import com.jtransc.io.ProcessResult2
import com.jtransc.time.measureTime

data class GenTargetInfo(
	val program: AstProgram,
	val outputFile: String,
	val settings: AstBuildSettings,
	val subtarget: String,
	val targetDirectory: String
)

interface GenTarget {
	val runningAvailable: Boolean
	fun getProcessor(tinfo: GenTargetInfo): GenTargetProcessor
}

fun GenTarget.build(program: AstProgram, outputFile: String, settings: AstBuildSettings, captureRunOutput: Boolean = true, run: Boolean = false, subtarget: String = "", targetDirectory: String = "target"): ProcessResult2 {
	print("Preparing...")
	val (preparingTime, processor) = measureTime {
		this.getProcessor(GenTargetInfo(
			program, outputFile, settings, subtarget, targetDirectory
		))
	}
	println("Ok ($preparingTime)")

	print("Building source...")
	val (buildTime, sourceResult) = measureTime { processor.buildSource() }
	println("Ok ($buildTime)")

	print("Compiling...")
	val (compileTime, compileResult) = measureTime { processor.compile() }
	if (compileResult) {
		println("Ok ($compileTime)")
	} else {
		println("ERROR ($compileTime)")
	}

	if (!compileResult) return ProcessResult2("", -1)
	if (run) {
		return processor.run(redirect = !captureRunOutput)
	} else {
		return ProcessResult2("", 0)
	}
}

interface GenTargetProcessor {
	fun buildSource(): Unit
	fun compile(): Boolean
	fun run(redirect: Boolean = true): ProcessResult2
}

fun GenTargetProcessor.process(redirect: Boolean = true): ProcessResult2 {
	this.buildSource()
	this.compile()
	return this.run(redirect)
}

abstract class GenTargetDescriptor {
	abstract val name: String
	abstract val longName: String
	abstract val sourceExtension: String
	abstract val outputExtension: String
	open val extraLibraries: List<String> = listOf<String>()
	open val extraClasses: List<String> = listOf<String>()
	open val extraMethods: List<AstMethodRef> = listOf()
	open val extraFields: List<AstFieldRef> = listOf()
	abstract fun getGenerator(): GenTarget
}