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
import com.jtransc.error.invalidOp
import com.jtransc.gen.GenTargetDescriptor
import com.jtransc.gen.GenTargetSubDescriptor
import com.jtransc.gen.build
import com.jtransc.input.AsmToAst
import com.jtransc.input.BaseProjectContext
import com.jtransc.internal.JTranscAnnotationBase
import com.jtransc.io.ProcessResult2
import com.jtransc.log.log
import com.jtransc.maven.MavenLocalRepository
import com.jtransc.time.measureProcess
import com.jtransc.time.measureTime
import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.MergedLocalAndJars
import com.jtransc.vfs.SyncVfsFile
import java.io.File

fun Iterable<GenTargetDescriptor>.locateTargetByName(target: String): GenTargetSubDescriptor {
	val parts = target.split(":")
	val target = this.firstOrNull { it.name == parts[0] } ?: throw Exception("Unknown target $target")
	return if (parts.size >= 2) {
		GenTargetSubDescriptor(target, parts[1])
	} else {
		GenTargetSubDescriptor(target, "default")
	}
}
enum class BuildBackend {
	ASM,
}

class AllBuild(
	val target: GenTargetDescriptor,
	val classPaths: List<String>,
	val entryPoint: String,
	val output: String,
	val subtarget: String,
	val settings: AstBuildSettings,
	val targetDirectory: String = System.getProperty("java.io.tmpdir")
) {
	constructor(AllBuildTargets: List<GenTargetDescriptor>, target: String, classPaths: List<String>, entryPoint: String, output: String, subtarget: String, settings: AstBuildSettings, targetDirectory: String = System.getProperty("java.io.tmpdir")) : this(
		AllBuildTargets.locateTargetByName(target).descriptor,
		classPaths, entryPoint, output, subtarget, settings, targetDirectory
	)

	val tempdir = System.getProperty("java.io.tmpdir")

	val gen = target.getGenerator()
	val runningAvailable: Boolean = gen.runningAvailable

	/*
	fun build(release: Boolean = false, run: Boolean = false): Boolean {
		return _buildAndRun(release, redirect = true, run = run).success
	}
	*/

	fun buildAndRunCapturingOutput() = _buildAndRun(captureRunOutput = true, run = true)
	fun buildAndRunRedirecting() = _buildAndRun(captureRunOutput = false, run = true)
	fun buildWithoutRunning() = _buildAndRun(captureRunOutput = false, run = false)
	fun buildAndRun(captureRunOutput: Boolean, run: Boolean = true) = _buildAndRun(captureRunOutput = captureRunOutput, run = run)

	class Result(val process: ProcessResult2)

	private fun locateRootPath(): String {
		println(File("").absolutePath)
		return File("").absolutePath
	}

	private fun _buildAndRun(captureRunOutput: Boolean = true, run: Boolean = false): Result {
		val jtranscVersion = settings.jtranscVersion

		// Previously downloaded manually or with maven plugin!
		//val rtAndRtCore = MavenLocalRepository.locateJars(
		//	"com.jtransc:jtransc-rt:$jtranscVersion",
		//	"com.jtransc:jtransc-rt-core:$jtranscVersion"
		//)
		val classPaths2 = (settings.rtAndRtCore + target.extraLibraries.flatMap { MavenLocalRepository.locateJars(it) } + classPaths).distinct()

		log("AllBuild.build(): language=$target, subtarget=$subtarget, entryPoint=$entryPoint, output=$output, targetDirectory=$targetDirectory")
		for (cp in classPaths2) log("ClassPath: $cp")

		// @TODO: We should be able to add these references to java.lang.Object using some kind of annotation!!
		@SuppressWarnings("all")
		@Suppress("ALL")
		var initialClasses = listOf(
			java.lang.Object::class.java.name,
			java.lang.Void::class.java.name,
			java.lang.Byte::class.java.name,
			java.lang.Character::class.java.name,
			java.lang.Short::class.java.name,
			java.lang.Integer::class.java.name,
			java.lang.Long::class.java.name,
			java.lang.Float::class.java.name,
			java.lang.Double::class.java.name,
			java.lang.Class::class.java.name,
			java.lang.Class::class.java.name,
			java.lang.reflect.Method::class.java.name,
			java.lang.reflect.Field::class.java.name,
			java.lang.reflect.Constructor::class.java.name,
			java.lang.annotation.Annotation::class.java.name,
			java.lang.reflect.InvocationHandler::class.java.name,
			com.jtransc.internal.JTranscAnnotationBase::class.java.name,
			com.jtransc.JTranscWrapped::class.java.name,
			entryPoint.fqname.fqname
		)

		var program = measureProcess("Generating AST") {
			createProgramAst(
				when (settings.backend) {
					BuildBackend.ASM -> AsmToAst()
					else -> invalidOp("Unsupported backend")
				},
				initialClasses, entryPoint, classPaths2,
				LocalVfs(File("$tempdir/out_ast"))
			)
		}
		//val programDced = measureProcess("Simplifying AST") { SimpleDCE(program, programDependencies) }
		return gen.build(program, outputFile = output, settings = settings, captureRunOutput = captureRunOutput, run = run, subtarget = subtarget, targetDirectory = targetDirectory)
	}

	fun createProgramAst(generator: AstClassGenerator, classNames: List<String>, mainClass: String, classPaths: List<String>, outputPath: SyncVfsFile): AstProgram {
		return generateProgram(BaseProjectContext(classNames, mainClass, classPaths, outputPath, generator))
	}

	fun generateProgram(projectContext: BaseProjectContext): AstProgram {
		val generator = projectContext.generator
		val program = AstProgram(
			entrypoint = FqName(projectContext.mainClass),
			resourcesVfs = MergedLocalAndJars(projectContext.classPaths),
			generator = generator
		)

		// Preprocesses classes
		projectContext.classNames.forEach {
			program.addReference(AstType.REF(it), AstType.REF(it))
		}

		log("Processing classes...")

		val (elapsed) = measureTime {
			while (program.hasClassToGenerate()) {
				val className = program.readClassToGenerate()
				//val nativeClassTag = clazz.clazz.getTag("libcore.NativeClass", "")

				//print("Processing class: " + clazz.clazz.name + "...")

				//print("  CLASS: $className...");
				val time = measureTime {
					val generatedClass = generator.generateClass(program, className.name)

					for (ref in References.get(generatedClass)) {
						program.addReference(ref, className)
					}
				}

				//println("Ok(${time.time})");
			}

			// Add synthetic methods to abstracts to simulate in haxe
			// @TODO: Maybe we could generate those methods in haxe generator since the requirement for this
			// @TODO: is target dependant
			for (clazz in program.classes.filter { it.isAbstract }) {
				for (method in clazz.allMethodsToImplement) {
					if (clazz.getMethodInAncestors(method) == null) {
						clazz.add(generateDummyMethod(clazz, method.name, method.type, false, AstVisibility.PUBLIC))
					}
				}
			}
		}

		//for (dep in projectContext.deps2!!) println(dep)

		val methodCount = program.classes.sumBy { it.methods.size }

		log("Ok classes=${program.classes.size}, methods=$methodCount, time=$elapsed")

		return program
	}

	fun generateDummyMethod(containingClass: AstClass, name: String, methodType: AstType.METHOD, isStatic: Boolean, visibility: AstVisibility) = AstMethod(
		containingClass = containingClass,
		id = -1,
		annotations = listOf(),
		name = name,
		type = methodType,
		generateBody = { null },
		signature = methodType.mangle(),
		genericSignature = methodType.mangle(),
		defaultTag = null,
		modifiers = AstModifiers.withFlags(AstModifiers.ACC_NATIVE, if (isStatic) AstModifiers.ACC_STATIC else 0).withVisibility(visibility)
	)
}
