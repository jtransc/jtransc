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

import com.jtransc.ast.*
import com.jtransc.gen.GenTargetDescriptor
import com.jtransc.gen.build
import com.jtransc.gen.haxe.HaxeGenDescriptor
import com.jtransc.gen.haxe.HaxeLimeGenDescriptor
import com.jtransc.input.AnaProject
import com.jtransc.input.AsmToAst
import com.jtransc.io.ProcessResult2
import com.jtransc.maven.MavenLocalRepository
import com.jtransc.time.measureProcess
import com.jtransc.vfs.LocalVfs

//val AllBuildTargets = listOf(As3GenDescriptor, JsGenDescriptor, HaxeGenDescriptor, HaxeLimeGenDescriptor)
val AllBuildTargets = listOf(HaxeGenDescriptor, HaxeLimeGenDescriptor)

fun Iterable<GenTargetDescriptor>.locateTargetByName(target: String) = this.firstOrNull { it.name == target } ?: throw Exception("Unknown target $target")
fun Iterable<GenTargetDescriptor>.locateTargetByOutExt(ext: String) = this.firstOrNull { it.outputExtension == ext } ?: throw Exception("Can't find target by extension $ext")

val enableDeadCodeElimination = true
//val enableDeadCodeElimination = false

class AllBuild(
	val target: GenTargetDescriptor,
	val classPaths: List<String>,
	val entryPoint: String,
	val output: String,
	val subtarget: String,
	val targetDirectory: String = System.getProperty("java.io.tmpdir")
) {
	constructor(target: String, classPaths: List<String>, entryPoint: String, output: String, subtarget: String, targetDirectory: String = System.getProperty("java.io.tmpdir")) : this(
		AllBuildTargets.locateTargetByName(target),
		classPaths,
		entryPoint,
		output,
		subtarget,
		targetDirectory
	)

	val tempdir = System.getProperty("java.io.tmpdir")

	val gen = target.getGenerator()
	val runningAvailable: Boolean = gen.runningAvailable

	/*
	fun build(release: Boolean = false, run: Boolean = false): Boolean {
		return _buildAndRun(release, redirect = true, run = run).success
	}
	*/

	fun buildAndRunCapturingOutput(settings: AstBuildSettings): ProcessResult2 {
		return buildAndRun(captureRunOutput = true, settings = settings, run = true)
	}

	fun buildAndRunRedirecting(settings: AstBuildSettings): ProcessResult2 {
		return buildAndRun(captureRunOutput = false, settings = settings, run = true)
	}

	fun buildWithoutRunning(settings: AstBuildSettings): ProcessResult2 {
		return buildAndRun(captureRunOutput = false, settings = settings, run = false)
	}

	fun buildAndRun(captureRunOutput: Boolean, settings: AstBuildSettings, run: Boolean = true): ProcessResult2 {
		return _buildAndRun(settings = settings, captureRunOutput = captureRunOutput, run = run)
	}

	private fun _buildAndRun(settings: AstBuildSettings, captureRunOutput: Boolean = true, run: Boolean = false): ProcessResult2 {
		val jtranscVersion = settings.jtranscVersion

		// Previously downloaded manually or with maven plugin!
		val classPaths2 = MavenLocalRepository.locateJars(listOf(
			"com.jtransc:jtransc-rt:$jtranscVersion",
			"com.jtransc:jtransc-rt-core:$jtranscVersion"
		)) + target.extraLibraries.flatMap {
			MavenLocalRepository.locateJars(it)
		} + classPaths

		println("AllBuild.build(): language=$target, subtarget=$subtarget, entryPoint=$entryPoint, output=$output, targetDirectory=$targetDirectory")
		for (cp in classPaths2) println("ClassPath: $cp")

		val anaProject = AnaProject(classPaths2)
		var initialDeps = setOf(
			AstClassRef("java.lang.Object"),
			AstClassRef("java.lang.Void"),
			AstClassRef("java.lang.Byte"),
			AstClassRef("java.lang.Character"),
			AstClassRef("java.lang.Short"),
			AstClassRef("java.lang.Integer"),
			AstClassRef("java.lang.Long"),
			AstClassRef("java.lang.Float"),
			AstClassRef("java.lang.Double"),
			AstClassRef("java.lang.Class"),
			AstClassRef("java.lang.reflect.Method"),
			AstClassRef("java.lang.reflect.Field"),
			AstClassRef("java.lang.reflect.Constructor"),
			AstClassRef("java.lang.annotation.Annotation"),
			AstClassRef("jtransc.internal.JTranscAnnotationBase"),
			AstMethodRef(entryPoint.fqname, "main", AstTypeBuild { METHOD(VOID, ARRAY(STRING)) })
		) + settings.extraRefs + target.extraMethods


		var exploredDeps2 = measureProcess("Calculating dependencies") {
			anaProject.explore(
				initialDeps,
				exploreFullClasses = !enableDeadCodeElimination
			)
		}

		//for (dep in exploredDeps2) println("depp:$dep")

		var dependencies = exploredDeps2.filterIsInstance<AstClassRef>().map { it.fqname }
		//val dependencies = null

		//println(exploredDeps2.joinToString("\n"))

		var program = measureProcess("Generating AST") {
			AsmToAst.createProgramAst(
				dependencies,
				entryPoint,
				classPaths2,
				LocalVfs("$tempdir/out_ast"),
				if (enableDeadCodeElimination) exploredDeps2 else setOf()
			)
		}
		//val programDced = measureProcess("Simplifying AST") { SimpleDCE(program, programDependencies) }
		return gen.build(program, outputFile = output, settings = settings, captureRunOutput = captureRunOutput, run = run, subtarget = subtarget, targetDirectory = targetDirectory)
	}
}