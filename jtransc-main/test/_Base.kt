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

open class _Base {
	open val DEFAULT_TARGET: GenTargetDescriptor = JsTarget()

	open val BACKEND = BuildBackend.ASM
	//open val BACKEND = BuildBackend.ASM2
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

	fun testClass(params: Params) {
		com.jtransc.log.log.setTempLogger({ content, level -> if (params.log ?: DEBUG) println(content) }) {
			testClassNoLog(params)
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

	fun testClassNoLog(params: Params) {
		println(params.clazz.name)
		val expected = params.transformerOut(ClassUtils.callMain(params.clazz))
		val result = params.transformerOut(runClass(params))
		Assert.assertEquals(normalize(expected), normalize(result))
	}

	fun normalize(str: String) = str.replace("\r\n", "\n").replace('\r', '\n').trim()

	fun testNativeClass(expected: String, params: Params) {
		Assert.assertEquals(normalize(expected.trimIndent()), normalize(params.transformerOut(runClass(params).trim())))
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

	data class Params(
		val clazz: Class<*>,
		val lang: String? = null,
		val minimize: Boolean? = null,
		val analyze: Boolean? = null,
		val debug: Boolean? = DEBUG,
		val treeShaking: Boolean? = null,
		val backend: BuildBackend? = null,
		val configureInjector: Injector.() -> Unit = {},
		val target: GenTargetDescriptor? = null,
		val log: Boolean? = null,
		val transformerOut: (String) -> String = { it }
	)

	fun runClass(params: Params): String {
		return action(params)
	}

	fun action(params: Params): String {
		val injector = Injector()
		val projectRoot = locateProjectRoot()

		//val threadId = Thread.currentThread().id
		//val pid = ManagementFactory.getRuntimeMXBean().getName()

		val threadId = 0
		val pid = 0

		injector.mapInstances(
			params.backend ?: BACKEND, ConfigClassPaths(testClassesPaths + kotlinPaths)
		)

		injector.mapImpl<AstTypes, AstTypes>()
		injector.mapInstance(ConfigMinimizeNames(params.minimize ?: MINIMIZE))
		injector.mapInstance(ConfigTreeShaking(params.treeShaking ?: TREESHAKING, TREESHAKING_TRACE))

		params.configureInjector(injector)

		return log.setTempLogger({ v, l -> }) {
			JTranscBuild(
				injector = injector,
				target = params.target ?: DEFAULT_TARGET,
				entryPoint = params.clazz.name,
				output = "program.${params.lang}",
				subtarget = params.lang ?: "js",
				//output = "program.haxe.cpp", subtarget = "cpp",
				targetDirectory = System.getProperty("java.io.tmpdir") + "/jtransc/${pid}_$threadId",
				settings = AstBuildSettings(
					jtranscVersion = JTranscVersion.getVersion(),
					debug = params.debug ?: DEBUG,
					relooper = RELOOPER,
					//relooper = false,
					analyzer = params.analyze ?: ANALYZER,
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
						projectRoot["jtransc-rt-extended-charsets/target/classes"].realpathOS,
						projectRoot["jtransc-rt-extended-charsets/build/classes/main"].realpathOS,
						projectRoot["jtransc-rt-extended-charsets/build/resources/main"].realpathOS,
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

