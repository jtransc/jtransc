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
import com.jtransc.ast.AstBuildSettings
import com.jtransc.gen.haxe.HaxeGenDescriptor
import com.jtransc.lang.getResourceAsString
import com.jtransc.text.captureStdout
import org.junit.Assert
import org.junit.Test
import java.io.File

class HaxeGenTest {
	private fun String.normalize() = this.trim().lines().map { it.trim() }.filter { it.isNotEmpty() }.joinToString("\n")

	@Test fun test1() {
		val mainAs3Class = samplehx.MainHaxe::class.java
		val testClassesPath = File("target/test-classes").absolutePath

		val expectedOutput = captureStdout {
			samplehx.MainHaxe.main(arrayOf())
		}.replace(
			"java.runtime.name:Java(TM) SE Runtime Environment", "java.runtime.name:jtransc-haxe"
		)

		val build = AllBuild(
			target = HaxeGenDescriptor,
			classPaths = listOf(testClassesPath),
			entryPoint = "samplehx.MainHaxe",
			output = "program.haxe.js", subtarget = "js",
			//output = "program.haxe.cpp", subtarget = "cpp",
			targetDirectory = System.getProperty("java.io.tmpdir")
		)
		val result = build.buildAndRunCapturingOutput(AstBuildSettings(debug = false)).output.normalize()

		//println(expectedOutput.normalize())
		//println(result.normalize())
		Assert.assertEquals(
			//javaClass.getResourceAsString("/haxe_integration.expected").normalize(),
			expectedOutput.normalize(),
			result.normalize()
		)
	}
}
