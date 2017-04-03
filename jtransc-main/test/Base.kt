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

import com.jtransc.*
import com.jtransc.ast.AstBuildSettings
import com.jtransc.ast.AstTypes
import com.jtransc.ast.ConfigMinimizeNames
import com.jtransc.ast.ConfigTreeShaking
import com.jtransc.error.invalidOp
import com.jtransc.gen.GenTargetDescriptor
import com.jtransc.gen.js.JsTarget
import com.jtransc.injector.Injector
import com.jtransc.log.log
import com.jtransc.maven.MavenGradleLocalRepository
import com.jtransc.util.ClassUtils
import com.jtransc.vfs.SyncVfsFile
import com.jtransc.vfs.UnjailedLocalVfs
import com.jtransc.vfs.parent
import org.junit.Assert
import java.io.File

open class Base {
	open val DEFAULT_TARGET: GenTargetDescriptor = JsTarget()

	open val BACKEND = BuildBackend.ASM
	open val TREESHAKING = true
	open val TREESHAKING_TRACE = false

	companion object {
		const val MINIMIZE = true
		//const val TREESHAKING = false
		const val RELOOPER = true
		const val ANALYZER = false
		const val DEBUG = false
		//val DEFAULT_TARGET = HaxeTarget
	}

	class Config<T>(
		minimize: Boolean? = null, analyze: Boolean? = null, lang: String, clazz: Class<T>, debug: Boolean? = null, target: GenTargetDescriptor? = null,
		transformer: (String) -> String,
		transformerOut: (String) -> String
	)

	init {
		if (!DEBUG) log.logger = { content, level -> }
	}

	inline fun <reified T : Any> testClass(
		minimize: Boolean? = null,
		lang: String = "js",
		analyze: Boolean? = null,
		target: GenTargetDescriptor? = null,
		debug: Boolean? = null,
		log: Boolean? = null,
		treeShaking: Boolean? = null,
		backend: BuildBackend? = null,
		noinline transformer: (String) -> String = { it },
		noinline transformerOut: (String) -> String = { it }
	) {
		com.jtransc.log.log.setTempLogger({ content, level -> if (log ?: DEBUG) println(content) }) {
			testClass(
				minimize = minimize, analyze = analyze, lang = lang,
				backend = backend,
				clazz = T::class.java, transformer = transformer, transformerOut = transformerOut,
				target = target, debug = debug, treeShaking = treeShaking
			)
		}
	}

	val kotlinPaths = listOf<String>() + listOf(
		MavenGradleLocalRepository.locateJars("org.jetbrains.kotlin:kotlin-runtime:$KotlinVersion")
		, MavenGradleLocalRepository.locateJars("org.jetbrains.kotlin:kotlin-stdlib:$KotlinVersion")
		, MavenGradleLocalRepository.locateJars("org.jetbrains:annotations:13.0")
	).flatMap { it }

	val testClassesPaths = listOf(
		File("build/classes/test").absolutePath,
		File("build/resources/test").absolutePath,
		//File("build/kotlin-classes/test").absolutePath,
		File("target/test-classes").absolutePath
	)

	fun <T : Any> testClass(
		minimize: Boolean? = null, analyze: Boolean? = null, lang: String, clazz: Class<T>, debug: Boolean? = null, target: GenTargetDescriptor? = null,
		backend: BuildBackend? = null,
		treeShaking: Boolean? = null,
		transformer: (String) -> String,
		transformerOut: (String) -> String
	) {
		println(clazz.name)
		val expected = transformer(ClassUtils.callMain(clazz))
		val result = transformerOut(runClass(clazz, minimize = minimize, analyze = analyze, lang = lang, target = target, debug = debug, treeShaking = treeShaking, backend = backend))
		Assert.assertEquals(normalize(expected), normalize(result))
	}

	fun normalize(str: String) = str.replace("\r\n", "\n").replace('\r', '\n').trim()

	inline fun <reified T : Any> runClass(minimize: Boolean? = null, analyze: Boolean? = null, lang: String = "js", debug: Boolean? = null, backend: BuildBackend? = null, target: GenTargetDescriptor? = null, treeShaking: Boolean? = null): String {
		return runClass(T::class.java, minimize = minimize, analyze = analyze, lang = lang, debug = debug, target = target, treeShaking = treeShaking, backend = backend)
	}

	inline fun <reified T : Any> testNativeClass(expected: String, minimize: Boolean? = null, debug: Boolean? = null, target: GenTargetDescriptor? = null, backend: BuildBackend? = null, treeShaking: Boolean? = null) {
		Assert.assertEquals(normalize(expected.trimIndent()), normalize(runClass<T>(minimize = minimize, debug = debug, target = target, treeShaking = treeShaking, backend = backend).trim()))
	}

	fun locateProjectRoot(): SyncVfsFile {
		var current = UnjailedLocalVfs(File(""))
		var count = 0
		while ("jtransc-rt" !in current) {
			//println("${current.realpathOS}")
			current = current.parent
			if (count++ > 20) invalidOp("Can't find project root")
		}

		return current
	}

	fun <T : Any> runClass(
		clazz: Class<T>, lang: String, minimize: Boolean?,
		analyze: Boolean?, debug: Boolean? = null,
		treeShaking: Boolean? = null,
		backend: BuildBackend? = null,
		//target: GenTargetDescriptor = HaxeTarget
		target: GenTargetDescriptor? = null
	): String {
		val injector = Injector()
		val projectRoot = locateProjectRoot()

		//val threadId = Thread.currentThread().id
		//val pid = ManagementFactory.getRuntimeMXBean().getName()

		val threadId = 0
		val pid = 0

		injector.mapInstances(
			backend ?: BACKEND, ConfigClassPaths(testClassesPaths + kotlinPaths)
		)

		injector.mapImpl<AstTypes, AstTypes>()
		injector.mapInstance(ConfigMinimizeNames(minimize ?: MINIMIZE))
		injector.mapInstance(ConfigTreeShaking(treeShaking ?: TREESHAKING, TREESHAKING_TRACE))

		return log.setTempLogger({ v, l -> }) {
			JTranscBuild(
				injector = injector,
				target = target ?: DEFAULT_TARGET,
				entryPoint = clazz.name,
				output = "program.haxe.$lang", subtarget = "$lang",
				//output = "program.haxe.cpp", subtarget = "cpp",
				targetDirectory = System.getProperty("java.io.tmpdir") + "/jtransc/${pid}_$threadId",
				settings = AstBuildSettings(
					jtranscVersion = JTranscVersion.getVersion(),
					debug = debug ?: DEBUG,
					relooper = RELOOPER,
					//relooper = false,
					analyzer = analyze ?: ANALYZER,
					rtAndRtCore = listOf(
						projectRoot["jtransc-rt/target/classes"].realpathOS,
						projectRoot["jtransc-rt/build/classes/main"].realpathOS,
						projectRoot["jtransc-rt/build/resources/main"].realpathOS,
						projectRoot["jtransc-rt-core/target/classes"].realpathOS,
						projectRoot["jtransc-rt-core/build/classes/main"].realpathOS,
						projectRoot["jtransc-rt-core/build/resources/main"].realpathOS,
						projectRoot["jtransc-rt-core-kotlin/target/classes"].realpathOS,
						projectRoot["jtransc-rt-core-kotlin/build/classes/main"].realpathOS,
						projectRoot["jtransc-rt-core-kotlin/build/resources/main"].realpathOS,
						projectRoot["jtransc-annotations/target/classes"].realpathOS,
						projectRoot["jtransc-annotations/build/classes/main"].realpathOS,
						projectRoot["jtransc-annotations/build/resources/main"].realpathOS
					)
				)
			).buildAndRunCapturingOutput().process.outerr
		}
	}

	val types = ThreadLocal.withInitial { AstTypes() }
}

