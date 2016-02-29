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
import com.jtransc.input.BaseProjectContext
import com.jtransc.input.SootToAst
import com.jtransc.input.SootUtils
import com.jtransc.io.ProcessResult2
import com.jtransc.maven.MavenLocalRepository
import com.jtransc.time.measureProcess
import com.jtransc.time.measureTime
import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.MergedLocalAndJars
import com.jtransc.vfs.SyncVfsFile

fun Iterable<GenTargetDescriptor>.locateTargetByName(target: String) = this.firstOrNull { it.name == target } ?: throw Exception("Unknown target $target")
fun Iterable<GenTargetDescriptor>.locateTargetByOutExt(ext: String) = this.firstOrNull { it.outputExtension == ext } ?: throw Exception("Can't find target by extension $ext")

class AllBuild(
	val target: GenTargetDescriptor,
	val classPaths: List<String>,
	val entryPoint: String,
	val output: String,
	val subtarget: String,
	val targetDirectory: String = System.getProperty("java.io.tmpdir")
) {
	constructor(AllBuildTargets: List<GenTargetDescriptor>, target: String, classPaths: List<String>, entryPoint: String, output: String, subtarget: String, targetDirectory: String = System.getProperty("java.io.tmpdir")) : this(
		AllBuildTargets.locateTargetByName(target),
		classPaths, entryPoint, output, subtarget, targetDirectory
	)

	val tempdir = System.getProperty("java.io.tmpdir")

	val gen = target.getGenerator()
	val runningAvailable: Boolean = gen.runningAvailable

	/*
	fun build(release: Boolean = false, run: Boolean = false): Boolean {
		return _buildAndRun(release, redirect = true, run = run).success
	}
	*/

	fun buildAndRunCapturingOutput(settings: AstBuildSettings) = buildAndRun(captureRunOutput = true, settings = settings, run = true)
	fun buildAndRunRedirecting(settings: AstBuildSettings) = buildAndRun(captureRunOutput = false, settings = settings, run = true)
	fun buildWithoutRunning(settings: AstBuildSettings) = buildAndRun(captureRunOutput = false, settings = settings, run = false)
	fun buildAndRun(captureRunOutput: Boolean, settings: AstBuildSettings, run: Boolean = true): ProcessResult2 {
		return _buildAndRun(settings = settings, captureRunOutput = captureRunOutput, run = run)
	}

	private fun _buildAndRun(settings: AstBuildSettings, captureRunOutput: Boolean = true, run: Boolean = false): ProcessResult2 {
		val jtranscVersion = settings.jtranscVersion

		// Previously downloaded manually or with maven plugin!
		val classPaths2 = (MavenLocalRepository.locateJars(listOf(
			"com.jtransc:jtransc-rt:$jtranscVersion",
			"com.jtransc:jtransc-rt-core:$jtranscVersion"
		)) + target.extraLibraries.flatMap {
			MavenLocalRepository.locateJars(it)
		} + classPaths).distinct()

		println("AllBuild.build(): language=$target, subtarget=$subtarget, entryPoint=$entryPoint, output=$output, targetDirectory=$targetDirectory")
		for (cp in classPaths2) println("ClassPath: $cp")

		var initialClasses = listOf(
			"java.lang.Object",
			"java.lang.Void",
			"java.lang.Byte",
			"java.lang.Character",
			"java.lang.Short",
			"java.lang.Integer",
			"java.lang.Long",
			"java.lang.Float",
			"java.lang.Double",
			"java.lang.Class",
			"java.lang.reflect.Method",
			"java.lang.reflect.Field",
			"java.lang.reflect.Constructor",
			"java.lang.annotation.Annotation",
			"jtransc.internal.JTranscAnnotationBase",
			entryPoint.fqname.fqname
		)

		var program = measureProcess("Generating AST") {
			createProgramAst(
				SootToAst(),
				initialClasses, entryPoint, classPaths2,
				LocalVfs("$tempdir/out_ast")
			)
		}
		//val programDced = measureProcess("Simplifying AST") { SimpleDCE(program, programDependencies) }
		return gen.build(program, outputFile = output, settings = settings, captureRunOutput = captureRunOutput, run = run, subtarget = subtarget, targetDirectory = targetDirectory)
	}

	fun createProgramAst(generator: AstClassGenerator, classNames: List<String>, mainClass: String, classPaths: List<String>, outputPath: SyncVfsFile): AstProgram {
		SootUtils.init(classPaths)
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
			program.addReference(AstClassRef(it))
		}

		print("Processing classes...")

		val (elapsed) = measureTime {
			while (program.hasClassToGenerate()) {
				val className = program.readClassToGenerate()
				//val clazz = projectContext.getSootClass(className.name)
				//val nativeClassTag = clazz.clazz.getTag("libcore.NativeClass", "")

				//print("Processing class: " + clazz.clazz.name + "...")

				val generatedClass = generator.generateClass(program, className.name)

				for (ref in References.get(generatedClass)) {
					program.addReference(ref)
				}
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

		println("Ok classes=${projectContext.classNames.size}, time=$elapsed")

		return program
	}

	fun generateDummyMethod(containingClass: AstClass, name: String, methodType: AstType.METHOD_TYPE, isStatic: Boolean, visibility: AstVisibility) = AstMethod(
		containingClass = containingClass,
		annotations = listOf(),
		name = name,
		type = methodType,
		body = null,
		signature = methodType.mangle(),
		genericSignature = methodType.mangle(),
		defaultTag = null,
		modifiers = -1,
		isStatic = isStatic,
		visibility = visibility,
		isNative = true
	)

	object References {
		fun get(clazz:AstClass) = clazz.getClassReferences()

		fun AstClass.getClassReferences(): List<AstClassRef> {
			val me = AstClassRef(this.fqname)
			val parent = if (this.extending != null) AstClassRef(this.extending) else null
			val interfaces = this.implementing.map { AstClassRef(it) }
			val fields = this.fields.flatMap { it.getClassReferences() }
			val methods = this.methods.flatMap { it.getClassReferences() }
			val annotations = this.annotations.getClassReferences()

			return (listOf(me) + listOf(parent) + interfaces + fields + methods + annotations).filterNotNull()
		}

		fun AstField.getClassReferences(): List<AstClassRef> {
			return this.type.getRefClasses() + this.annotations.getClassReferences()
		}

		fun AstMethod.getClassReferences(): List<AstClassRef> {
			val signatureRefs = this.methodType.getRefClasses()
			val refs = this.body?.getClassReferences() ?: listOf()
			val annotations = this.annotations.getClassReferences()
			return signatureRefs + refs + annotations
		}

		fun List<AstAnnotation>.getClassReferences(): List<AstClassRef> {
			return this.flatMap { it.getClassReferences() }
		}

		fun AstAnnotation.getClassReferences(): List<AstClassRef> {
			return listOf(this.type.classRef)
		}

		fun AstBody.getClassReferences(): List<AstClassRef> {
			val locals = this.locals.flatMap { it.type.getRefClasses() }
			val traps = this.traps.map { it.exception.classRef }
			val stms = this.stm.getClassReferences()
			return locals + traps + stms
		}

		fun AstStm.getClassReferences(): List<AstClassRef> {
			val refs = ReferencesVisitor()
			refs.visit(this)
			return (refs.classReferences + refs.fieldReferences.map { it.classRef } + refs.methodReferences.flatMap { it.allClassRefs }).toList()
		}

		class ReferencesVisitor : AstVisitor() {
			val classReferences = hashSetOf<AstClassRef>()
			val fieldReferences = hashSetOf<AstFieldRef>()
			val methodReferences = hashSetOf<AstMethodRef>()

			override fun visit(ref: AstClassRef) {
				super.visit(ref)
				classReferences += ref
			}

			override fun visit(ref: AstFieldRef) {
				super.visit(ref)
				fieldReferences += ref
			}

			override fun visit(ref: AstMethodRef) {
				super.visit(ref)
				methodReferences += ref
			}
		}
	}

}
