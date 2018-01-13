package com.jtransc.gen.common

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

import big.BigTest
import com.jtransc.*
import com.jtransc.ast.AstBuildSettings
import com.jtransc.ast.AstTypes
import com.jtransc.ast.ConfigMinimizeNames
import com.jtransc.ast.ConfigTreeShaking
import com.jtransc.error.invalidOp
import com.jtransc.gen.GenTargetDescriptor
import com.jtransc.injector.Injector
import com.jtransc.log.log
import com.jtransc.maven.MavenGradleLocalRepository
import com.jtransc.util.ClassUtils
import com.jtransc.vfs.SyncVfsFile
import com.jtransc.vfs.UnjailedLocalVfs
import com.jtransc.vfs.parent
import org.junit.Assert
import java.io.File
import java.net.JarURLConnection
import java.net.URL

open class _Base {
	open val DEFAULT_TARGET: GenTargetDescriptor get() = TODO()

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
		const val OPTIMIZE = true
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

	fun testClassNoLog(params: Params) {
		println(params.clazz.name)
		val expected = params.transformerOut(ClassUtils.callMain(params.clazz))
		val result = params.transformerOut(action(params))
		Assert.assertEquals(normalize(expected), normalize(result))
	}

	fun normalize(str: String) = str.replace("\r\n", "\n").replace('\r', '\n').trim()

	fun testNativeClass(params: Params, expected: String) {
		Assert.assertEquals(normalize(expected.trimIndent()), normalize(params.transformerOut(action(params).trim())))
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
		val extra: Map<String?, String?>? = null,
		val debug: Boolean? = null,
		val optimize: Boolean? = null,
		val treeShaking: Boolean? = null,
		val backend: BuildBackend? = null,
		val configureInjector: Injector.() -> Unit = {},
		val target: GenTargetDescriptor? = null,
		val log: Boolean? = null,
		val transformerOut: (String) -> String = { it }
	)

	fun action(params: Params): String {
		val process = _action(params, run = true).process
		//if (!process.success) throw RuntimeException("Process exited with ${process.exitValue}")
		return process.outerr;
	}

	fun getRootFromUrl(url: URL, numberOfPackages: Int): File {
		val uri = url.toURI()
		log.info("locateTestRootByClass.URI: $uri")
		if (uri.toString().startsWith("jar:")) {
			return File((url.openConnection() as JarURLConnection).jarFileURL.file)
		} else {
			val file = File(uri)
			var out = file
			for (n in 0 until numberOfPackages) out = out.parentFile
			return out
		}
	}

	fun locateTestRootByClass(clazz: Class<*>): File {
		val canonicalName = clazz.canonicalName
		val numberOfPackages = canonicalName.count { it == '.' || it == '/' } + 1
		val bigTestPath = "${canonicalName.replace('.', '/')}.class"
		//val path = ClassLoader.getSystemClassLoader().getResource(bigTestPath)
		val url = this.javaClass.classLoader.getResource(bigTestPath)
		return getRootFromUrl(url, numberOfPackages)
	}

	fun locateTestRootByResource(resourceName: String): File {
		val numberOfPackages = resourceName.count { it == '/' } + 1
		val url = this.javaClass.classLoader.getResource(resourceName)
		return getRootFromUrl(url, numberOfPackages)
	}

	fun _action(params: Params, run: Boolean): JTranscBuild.Result {
		val injector = Injector()
		val projectRoot = locateProjectRoot()

		val testRoot = locateTestRootByClass(BigTest::class.java)
		val resourceRoot = locateTestRootByResource("filetoinclude.txt")

		log.info("testRoot: $testRoot")
		log.info("resourceRoot: $resourceRoot")

		val rtAndCoreUnfiltered = listOf(
			"jtransc-rt", "jtransc-rt-core",
			"jtransc-rt-core-kotlin", "jtransc-rt-extended-charsets",
			"jtransc-annotations"
		).flatMap {
			listOf(
				testRoot.absolutePath.replace("jtransc-gen-common-tests", it),
				resourceRoot.absolutePath.replace("jtransc-gen-common-tests", it)
			)
		}
		val rtAndCoreFiltered = rtAndCoreUnfiltered.filter { File(it).exists() }

		for (file in rtAndCoreUnfiltered) {
			log.info("- RT: '$file': exists=${File(file).exists()}")
		}
		injector.mapInstances(
			params.backend ?: BACKEND, ConfigClassPaths(
				listOf(testRoot.absolutePath, resourceRoot.absolutePath) + kotlinPaths
			)
		)

		val targetDescriptor = params.target ?: DEFAULT_TARGET
		injector.mapInstance(targetDescriptor.targetName)
		injector.mapImpl<AstTypes, AstTypes>()
		injector.mapInstance(ConfigMinimizeNames(params.minimize ?: MINIMIZE))
		injector.mapInstance(ConfigTreeShaking(params.treeShaking ?: TREESHAKING, TREESHAKING_TRACE))

		params.configureInjector(injector)

		return log.setTempLogger({ v, l -> }) {
			val build = JTranscBuild(
				injector = injector,
				target = targetDescriptor,
				entryPoint = params.clazz.name,
				output = "program.${params.lang}",
				subtarget = params.lang ?: "js",
				//output = "program.haxe.cpp", subtarget = "cpp",
				targetDirectory = System.getProperty("java.io.tmpdir") + "/jtransc",
				settings = AstBuildSettings(
					jtranscVersion = JTranscVersion.getVersion(),
					debug = params.debug ?: DEBUG,
					optimize = params.optimize ?: OPTIMIZE,
					relooper = RELOOPER,
					extra = params.extra ?: mapOf(),
					analyzer = params.analyze ?: ANALYZER,
					rtAndRtCore = rtAndCoreFiltered
				)
			)
			if (run) build.buildAndRunCapturingOutput() else build.buildWithoutRunning()
		}
	}
}

