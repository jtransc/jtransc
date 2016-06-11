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

import com.jtransc.AllBuild
import com.jtransc.BuildBackend
import com.jtransc.JTranscVersion
import com.jtransc.KotlinVersion
import com.jtransc.ast.AstBuildSettings
import com.jtransc.error.invalidOp
import com.jtransc.gen.GenTargetDescriptor
import com.jtransc.gen.haxe.HaxeTarget
import com.jtransc.log.log
import com.jtransc.maven.MavenLocalRepository
import com.jtransc.util.ClassUtils
import com.jtransc.vfs.SyncVfsFile
import com.jtransc.vfs.UnjailedLocalVfs
import com.jtransc.vfs.parent
import org.junit.Assert
import java.io.File

open class HaxeTestBase {
	companion object {
		val BACKEND = BuildBackend.ASM
		const val MINIMIZE = true
		const val RELOOPER = true
		const val ANALYZER = false
		const val DEBUG = false
	}

	init {
		if (!DEBUG) log.logger = { content, level -> }
	}

	inline fun <reified T : Any> testClass(minimize: Boolean? = null, lang: String = "js", analyze: Boolean? = null, target: GenTargetDescriptor = HaxeTarget, debug:Boolean? = null, noinline transformer: (String) -> String = { it }) {
		testClass(minimize = minimize, analyze = analyze, lang = lang, clazz = T::class.java, transformer = transformer, target = target, debug = debug)
	}

	val kotlinPaths = listOf<String>() + listOf(
		MavenLocalRepository.locateJars("org.jetbrains.kotlin:kotlin-runtime:$KotlinVersion")
		, MavenLocalRepository.locateJars("org.jetbrains.kotlin:kotlin-stdlib:$KotlinVersion")
	).flatMap { it }

	val testClassesPath = File("target/test-classes").absolutePath

	fun <T : Any> testClass(minimize: Boolean? = null, analyze: Boolean? = null, lang: String, clazz: Class<T>, debug: Boolean? = null, target: GenTargetDescriptor = HaxeTarget, transformer: (String) -> String) {
		println(clazz.name)
		val expected = transformer(ClassUtils.callMain(clazz))
		val result = runClass(clazz, minimize = minimize, analyze = analyze, lang = lang, target = target, debug = debug)
		Assert.assertEquals(normalize(expected), normalize(result))
	}

	fun normalize(str: String) = str.replace("\r\n", "\n").replace('\r', '\n').trim()

	inline fun <reified T : Any> runClass(minimize: Boolean? = null, analyze: Boolean? = null, lang: String = "js", debug: Boolean? = null, target: GenTargetDescriptor = HaxeTarget): String {
		return runClass(T::class.java, minimize = minimize, analyze = analyze, lang = lang, debug = debug, target = target)
	}

	inline fun <reified T : Any> testNativeClass(expected: String, minimize: Boolean? = null, debug: Boolean? = null) {
		Assert.assertEquals(expected.trimIndent(), runClass<T>(minimize = minimize, debug = debug).trim())
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

	fun <T : Any> runClass(clazz: Class<T>, lang: String, minimize: Boolean?, analyze: Boolean?, debug: Boolean? = null, target: GenTargetDescriptor = HaxeTarget): String {
		val projectRoot = locateProjectRoot()
		return AllBuild(
			target = target,
			classPaths = listOf(testClassesPath) + kotlinPaths,
			entryPoint = clazz.name,
			output = "program.haxe.$lang", subtarget = "$lang",
			//output = "program.haxe.cpp", subtarget = "cpp",
			targetDirectory = System.getProperty("java.io.tmpdir"),
			settings = AstBuildSettings(
				jtranscVersion = JTranscVersion.getVersion(),
				debug = debug ?: DEBUG,
				backend = BACKEND,
				minimizeNames = minimize ?: MINIMIZE,
				relooper = RELOOPER,
				analyzer = analyze ?: ANALYZER,
				rtAndRtCore = listOf(
					projectRoot["jtransc-rt/target/classes"].realpathOS,
					//projectRoot["jtransc-rt-full/target/classes"].realpathOS,
					projectRoot["jtransc-rt-core/target/classes"].realpathOS
				)
			)
		).buildAndRunCapturingOutput().process.outerr
	}
}

