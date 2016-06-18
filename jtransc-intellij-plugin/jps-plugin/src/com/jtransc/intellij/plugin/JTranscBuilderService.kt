package com.jtransc.intellij.plugin

import com.jtransc.AllBuild
import com.jtransc.JTranscVersion
import com.jtransc.ast.AstBuildSettings
import com.jtransc.ast.AstTypes
import com.jtransc.gen.haxe.HaxeTarget
import org.jetbrains.jps.ModuleChunk
import org.jetbrains.jps.builders.DirtyFilesHolder
import org.jetbrains.jps.builders.java.JavaSourceRootDescriptor
import org.jetbrains.jps.incremental.*
import org.jetbrains.jps.incremental.messages.BuildMessage
import org.jetbrains.jps.incremental.messages.CompilerMessage
import org.jetbrains.jps.model.java.JpsJavaExtensionService
import org.jetbrains.jps.model.library.JpsOrderRootType
import org.jetbrains.jps.model.module.JpsLibraryDependency
import org.jetbrains.jps.model.module.JpsModule
import org.jetbrains.jps.model.module.JpsModuleDependency
import java.util.*

class JTranscBuilderService : BuilderService() {
	override fun createModuleLevelBuilders(): MutableList<out ModuleLevelBuilder> {
		return arrayListOf(object : ModuleLevelBuilder(BuilderCategory.CLASS_POST_PROCESSOR) {
			override fun getPresentableName(): String {
				return "JTranscModuleBuilder"
			}

			override fun build(context: CompileContext, chunk: ModuleChunk, dirtyFilesHolder: DirtyFilesHolder<JavaSourceRootDescriptor, ModuleBuildTarget>?, outputConsumer: OutputConsumer): ExitCode {
				context.markNonIncremental(chunk.representativeTarget())
				fun warn(message: String) {
					context.processMessage(CompilerMessage("JTransc", BuildMessage.Kind.WARNING, message))
				}

				//File("c:/projects/lol.txt").writeText("JTranscBuilderService!")
				//println("build!")
				//return ExitCode.ABORT
				warn("JTranscBuilderService: message!")

				var doneSomething = false

				val classPaths = chunk.modules.flatMap { getClassPaths(it) }

				for (classPath in classPaths) {
					warn("classPath: $classPath")
				}
//
				val build = AllBuild(
					HaxeTarget,
					classPaths = classPaths,
					entryPoint = "Test",
					output = "C:/temp/output.js",
					targetDirectory = "C:/temp",
					subtarget = "js",
					settings = AstBuildSettings(
						jtranscVersion = JTranscVersion.getVersion()
					),
					types = AstTypes()
				)
				val result = build.buildWithoutRunning()
				println(result)

				/*
				for (module in chunk.modules) {
					warn("CHUNK: $module")
					val classPaths = getClassPaths(module)

					doneSomething = true
					//doneSomething = doneSomething or build.perform(module, chunk.representativeTarget())
					if (context.cancelStatus.isCanceled) {
						return ModuleLevelBuilder.ExitCode.ABORT
					}
				}
				*/

				//context.projectDescriptor.project.runConfigurations.first().

				return if (doneSomething) ExitCode.OK else ExitCode.NOTHING_DONE
			}
		})
	}

	fun getClassPaths(module: JpsModule, visited: HashSet<JpsModule> = hashSetOf()): List<String> {
		if (module in visited) return listOf()
		visited += module
		val out = arrayListOf<String>()

		val moduleTargetDirectory = JpsJavaExtensionService.getInstance().getOutputDirectory(module, false);

		if (moduleTargetDirectory != null) {
			out += moduleTargetDirectory.absolutePath
		}
		for (dep in module.dependenciesList.dependencies) {
			when (dep) {
				is JpsModuleDependency -> {
					val module = dep.module
					if (module != null) out.addAll(getClassPaths(module, visited))
				}
				is JpsLibraryDependency -> {
					val library = dep.library
					if (library != null) {
						for (root in library.getRoots(JpsOrderRootType.COMPILED)) {
							out.add(root.url)
						}
					}
				}
			}
		}
		return out
	}
}