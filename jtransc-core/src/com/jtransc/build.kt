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
import com.jtransc.ast.References.getClassReferences
import com.jtransc.ast.References.getMethodReferences
import com.jtransc.ast.dependency.genStaticInitOrder
import com.jtransc.ast.treeshaking.TreeShaking
import com.jtransc.backend.asm1.AsmToAst1
import com.jtransc.backend.asm2.AsmToAst2
import com.jtransc.error.InvalidOperationException
import com.jtransc.error.invalidOp
import com.jtransc.gen.GenTargetDescriptor
import com.jtransc.gen.GenTargetSubDescriptor
import com.jtransc.gen.TargetName
import com.jtransc.gen.common.CommonGenerator
import com.jtransc.injector.Injector
import com.jtransc.io.ProcessResult2
import com.jtransc.log.log
import com.jtransc.maven.MavenLocalRepository
import com.jtransc.plugin.JTranscPlugin
import com.jtransc.plugin.JTranscPluginGroup
import com.jtransc.plugin.reflection.*
import com.jtransc.plugin.toGroup
import com.jtransc.time.measureProcess
import com.jtransc.time.measureTime
import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.MergedLocalAndJars
import com.jtransc.vfs.SyncVfsFile
import java.io.File
import java.util.*

fun Iterable<GenTargetDescriptor>.locateTargetByName(target: String): GenTargetSubDescriptor {
	val parts = target.split(":")

	return GenTargetSubDescriptor(
		descriptor = this.firstOrNull { it.name == parts[0] } ?: invalidOp("Unknown target $target among $this : ${this.map { it.name }}"),
		sub = parts.getOrElse(1) { "default" }
	)
}

enum class BuildBackend { ASM, ASM2 }
data class ConfigSubtarget(val subtarget: String)
data class ConfigTargetDirectory(val targetDirectory: String)
data class ConfigCaptureRunOutput(val captureRunOutput: Boolean)
data class ConfigRun(val run: Boolean, val args: List<String>)
data class ConfigLibraries(val libs: List<String>)
data class ConfigClassPaths(val classPaths: List<String>)
data class ConfigEntryPoint(val entrypoint: FqName)
data class ConfigMainClass(val mainClass: String)
data class ConfigInitialClasses(val initialClasses: List<String>, val keepClasses: List<String>)
data class ConfigResourcesVfs(val resourcesVfs: SyncVfsFile)

data class ConfigOutputFile(val output: String) {
	val outputFileBaseName by lazy { File(output).name }
}

data class ConfigOutputPath(val outputPath: SyncVfsFile)

class JTranscBuild(
	val injector: Injector,
	val target: GenTargetDescriptor,
	val entryPoint: String,
	val output: String,
	val subtarget: String,
	val settings: AstBuildSettings,
	val targetDirectory: String = System.getProperty("java.io.tmpdir"),
	val initialClasses: List<String> = listOf(),
	val keepClasses: List<String> = listOf()
) {
	val types = injector.get<AstTypes>()
	val configClassPaths = injector.get<ConfigClassPaths>()
	val backend = injector.get<BuildBackend>()
	val tempdir = System.getProperty("java.io.tmpdir")

	fun buildAndRunCapturingOutput(args: List<String> = listOf()) = _buildAndRun(captureRunOutput = true, run = true, args = args)
	fun buildAndRunRedirecting(args: List<String> = listOf()) = _buildAndRun(captureRunOutput = false, run = true, args = args)
	fun buildWithoutRunning() = _buildAndRun(captureRunOutput = false, run = false)
	fun buildAndRun(captureRunOutput: Boolean, run: Boolean = true, args: List<String> = listOf()) = _buildAndRun(captureRunOutput = captureRunOutput, run = run, args = args)

	class Result(val process: ProcessResult2, val generator: CommonGenerator)

	private fun _buildAndRun(captureRunOutput: Boolean = true, run: Boolean = false, args: List<String> = listOf()): Result {
		val targetName = target.targetName

		val classPaths2 = (settings.rtAndRtCore + target.extraLibraries.flatMap { MavenLocalRepository.locateJars(it) } + configClassPaths.classPaths).distinct()

		val initialClasses = listOf(entryPoint.fqname.fqname) + this.initialClasses

		injector.mapInstances(
			ConfigClassPaths(classPaths2),
			ConfigInitialClasses(initialClasses, keepClasses),
			settings,
			ConfigSubtarget(subtarget),
			ConfigTargetDirectory(targetDirectory),
			ConfigOutputFile(output),
			ConfigCaptureRunOutput(captureRunOutput),
			ConfigRun(run, args),
			ConfigOutputPath(LocalVfs(File("$tempdir/out_ast"))),
			ConfigMainClass(entryPoint),
			targetName
		)

		val plugins = ServiceLoader.load(JTranscPlugin::class.java).toList().sortedBy { it.priority }.toGroup(injector)
		val pluginNames = plugins.plugins.map { it.javaClass.simpleName }

		injector.mapInstance(plugins)

		log("AllBuild.build(): language=$target, subtarget=$subtarget, entryPoint=$entryPoint, output=$output, targetDirectory=$targetDirectory, plugins=$pluginNames")
		//for (cp in classPaths2) log("ClassPath: $cp")


		plugins.initialize(injector)

		when (backend) {
			BuildBackend.ASM -> injector.mapImpl<AstClassGenerator, AsmToAst1>()
			BuildBackend.ASM2 -> injector.mapImpl<AstClassGenerator, AsmToAst2>()
			else -> invalidOp("Unsupported backend")
		}

		val programBase = measureProcess("Generating AST") {
			generateProgram(plugins)
		}

		plugins.processBeforeTreeShaking(programBase)

		val configTreeShaking = injector.get<ConfigTreeShaking>()
		val program = if (configTreeShaking.treeShaking) {
			TreeShaking(programBase, target.name, configTreeShaking.trace, plugins, initialClasses, keepClasses)
		} else {
			programBase
		}

		plugins.processAfterTreeShaking(program)

		injector.mapInstance(program)
		injector.mapInstance(program, AstResolver::class.java)

		val settings = injector.get<AstBuildSettings>()

		val AllPluginFeatures = ServiceLoader.load(AstProgramFeature::class.java).toSet()
		val AllPluginFeaturesMap = AllPluginFeatures.map { it.javaClass to it }.toMap()

		val SupportedFeatureClasses = target.programFeatures
		val MissingFeatureClasses = AllPluginFeatures.map { it.javaClass } - SupportedFeatureClasses

		for (featureClass in MissingFeatureClasses) AllPluginFeaturesMap[featureClass]!!.onMissing(program, settings, types)
		for (featureClass in SupportedFeatureClasses) AllPluginFeaturesMap[featureClass]!!.onSupported(program, settings, types)

		plugins.onAfterAppliedClassFeatures(program)

		genStaticInitOrder(program, plugins)

		//val programDced = measureProcess("Simplifying AST") { SimpleDCE(program, programDependencies) }
		return target.build(injector)
	}

	fun generateProgram(plugins: JTranscPluginGroup): AstProgram {
		val injector: Injector = injector.get()
		val configClassNames: ConfigInitialClasses = injector.get()
		val configMainClass: ConfigMainClass = injector.get()
		val generator: AstClassGenerator = injector.get()
		val classNames = configClassNames.initialClasses
		val keepClasses = configClassNames.keepClasses
		val mainClass = configMainClass.mainClass

		val classPaths: List<String> = injector.get<ConfigClassPaths>().classPaths

		for (classPath in classPaths) {
			log("classPaths.add(\"$classPath\")")
		}
		//println(classPaths)

		injector.mapInstances(
			ConfigEntryPoint(FqName(mainClass)),
			ConfigResourcesVfs(MergedLocalAndJars(classPaths))
		)
		val program = injector.get<AstProgram>()

		// Preprocesses classes
		classNames.forEach {
			val clazzRef = AstType.REF(it)
			program.addReference(AstMethodRef(clazzRef.name, "main", AstType.METHOD(AstType.VOID, listOf(AstType.ARRAY(AstType.STRING)))), clazzRef)
			//program.addReference(AstType.REF(it), AstType.REF(it))
		}


		log("Processing classes...")

		val targetName = TargetName(target.name)
		val jruntime = Runtime.getRuntime()

		val (elapsed) = measureTime {
			plugins.onStartBuilding(program)

			var numProcessedClasses = 0

			//data class LongClassInfo(val className: AstType.REF, val timeMs: Long)
			//val longClasses = arrayListOf<LongClassInfo>()


			if (true) {
				while (true) {
					if (!program.hasMethodToGenerate()) {
						break
					}

					val methodRef = program.readMethodToGenerate()
					val methodRefWithoutClass = methodRef.withoutClass

					program.addReference(methodRef.classRef, methodRef.classRef)

					while (program.hasClassToGenerate()) {
						val clazzRef = program.readClassToGenerate()
						val classFqname = clazzRef.name
						println("Exploring class... $clazzRef")
						val clazz = program.getOrNull(classFqname) ?: generator.generateClass(program, classFqname)
						for (impl in listOfNotNull(clazz.extending) + clazz.implementing) {
							program.addReference(impl.ref, clazzRef)
						}
						val clinit = AstMethodWithoutClassRef.CLINIT
						val clinitMethod = clazz.classNodeMethods?.get(clinit)
						if (clinitMethod != null) {
							program.addReference(clinit.withClass(clazzRef.name), clazzRef)
						}
						//println(clazz.classNodeMethods)
					}

					val classFqname = methodRef.classRef.name
					println("Exploring method... $methodRef")

					val clazz = program.getOrNull(classFqname) ?: generator.generateClass(program, classFqname)
					if (!clazz.hasBasicMethod(methodRefWithoutClass)) {
						println(clazz.parentClass)
					}
					val method = generator.generateAndAddMethod(program, methodRef)
					for (methodRef in method.getMethodReferences(targetName)) {
						program.addReference(methodRef, clazz.ref)
						//println("methodRef: $methodRef")
					}
				}
			} else {
				while (true) {
					if (!program.hasClassToGenerate()) {
						plugins.onAfterAllClassDiscovered(program)

						if (!program.hasClassToGenerate()) {
							break
						}
					}
					val className = program.readClassToGenerate()
					log(
						"Processing class [$numProcessedClasses]... $className : ${
							jruntime.freeMemory().toDouble() / jruntime.maxMemory()
						}"
					)

					numProcessedClasses++

					plugins.onAfterClassDiscovered(className, program)

					val time = measureTime {
						try {
							val generatedClass = generator.generateClass(program, className.name)
							for (ref in References.get(generatedClass, targetName)) {
								//println("$ref : $className")
								program.addReference(ref, className)
							}
						} catch (e: InvalidOperationException) {
							System.err.println("ERROR! : " + e.message)
						}
					}

					//if (time.time >= 100L) longClasses += LongClassInfo(className, time.time)
				}
			}

			//for (longClass in longClasses) println("Long Class: ${longClass.className.fqname}, timeMs=${longClass.timeMs}")

			// Reference default methods
			for (clazz in program.classes) {
				for (method in clazz.allDirectInterfaces.flatMap { it.methods }) {
					val methodRef = method.ref.withoutClass
					if (method.hasBody && !method.isStatic && !method.isClassOrInstanceInit && (clazz.getMethodInAncestors(methodRef) == null)) {
						clazz.add(program.generateDummyMethod(clazz, method.name, methodRef.type, false, AstVisibility.PUBLIC, method.ref))
					}
				}
			}

			// @TODO: Maybe we could generate those methods in hax generator since the requirement for this is target dependant
			for (clazz in program.classes.filter { it.isAbstract }) {
				for (method in clazz.allMethodsToImplement.filter { clazz.getMethodInAncestors(it) == null }) {
					clazz.add(program.generateDummyMethod(clazz, method.name, method.type, false, AstVisibility.PUBLIC))
				}
			}
		}

		//for (dep in projectContext.deps2!!) println(dep)

		val methodCount = program.classes.sumBy { it.methods.size }

		log("Ok classes=${program.classes.size}, methods=$methodCount, time=$elapsed")

		return program
	}

	fun AstProgram.generateDummyMethod(containingClass: AstClass, name: String, methodType: AstType.METHOD, isStatic: Boolean, visibility: AstVisibility, bodyRef: AstMethodRef? = null) = AstMethod(
		containingClass = containingClass,
		annotations = listOf(),
		name = name,
		methodType = methodType,
		generateBody = { null },
		signature = methodType.mangle(),
		genericSignature = methodType.mangle(),
		defaultTag = null,
		bodyRef = bodyRef,
		modifiers = AstModifiers.withFlags(AstModifiers.ACC_NATIVE, if (isStatic) AstModifiers.ACC_STATIC else 0).withVisibility(visibility)
	)
}
