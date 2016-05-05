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

import com.jtransc.AllBuild
import com.jtransc.ast.AstBuildSettings
import com.jtransc.ast.AstProgram
import com.jtransc.io.ProcessResult2
import com.jtransc.log.log
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
	fun getProcessor(tinfo: GenTargetInfo, settings: AstBuildSettings): GenTargetProcessor
}

fun GenTarget.build(
	program: AstProgram,
	outputFile: String,
	settings: AstBuildSettings,
	captureRunOutput: Boolean = true,
	run: Boolean = false,
	subtarget: String = "",
	targetDirectory: String = "target"
): AllBuild.Result {
	val processor = log.logAndTime("Preparing processor") { this.getProcessor(GenTargetInfo(program, outputFile, settings, subtarget, targetDirectory), settings) }
	log.logAndTime("Building source") { processor.buildSource() }

	log("Compiling...")
	val (compileTime, compileResult) = measureTime { processor.compile() }
	if (compileResult) {
		log("Ok ($compileTime)")
	} else {
		log("ERROR ($compileTime) ($compileResult)")
	}

	val processResult = if (!compileResult) {
		ProcessResult2(-1)
	} else if (run) {
		processor.run(redirect = !captureRunOutput)
	} else {
		ProcessResult2(0)
	}

	return AllBuild.Result(processResult)
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

data class GenTargetSubDescriptor(val descriptor: GenTargetDescriptor, val sub: String, val ext: String = sub) {
	val fullName: String get() = "${descriptor.name}:$sub"
	override fun toString(): String = "$fullName(.$ext)"
}

abstract class GenTargetDescriptor {
	abstract val name: String
	abstract val longName: String
	abstract val sourceExtension: String
	abstract val outputExtension: String
	open val subtargets: List<GenTargetSubDescriptor> = listOf()
	open val defaultSubtarget: GenTargetSubDescriptor? = null
	open val extraLibraries = listOf<String>()
	open val extraClasses = listOf<String>()
	abstract fun getGenerator(): GenTarget
}