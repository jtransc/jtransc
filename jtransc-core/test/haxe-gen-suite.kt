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
import com.jtransc.maven.MavenLocalRepository
import com.jtransc.text.captureStdout
import org.junit.Assert
import org.junit.Test
import samplehx.MainHaxe
import javatest.KotlinCollections
import javatest.lang.BasicTypesTest
import javatest.lang.StringsTest
import javatest.lang.SystemTest
import javatest.utils.CollectionsTest
import jtransc.JTranscVersion
import java.io.File

class HaxeGenSuite {

    //-----------------------------------------------------------------
    // Java Lang

    //@Test fun langBasicTypesTest() = testClass<BasicTypesTest>()
    @Test fun langStringsTest() = testClass<StringsTest>()
    @Test fun langSystemTest() = testClass<SystemTest>()

    //-----------------------------------------------------------------
    // Java Utils

    //@Test fun utilsCollectionsTest() = testClass<CollectionsTest>()

    //-----------------------------------------------------------------
    // Kotlin Collections
    @Test fun kotlinCollectionsTest() = testClass<KotlinCollections>()


    // Shortcut
    inline fun <reified T : Any> testClass() = testClass(T::class.java)

    fun <T : Any> testClass(clazz: Class<T>) {
		val testClassName = clazz.name
		val testClassesPath = File("target/test-classes").absolutePath
        val kotlinPaths = listOf<String>() +
                MavenLocalRepository.locateJars("org.jetbrains.kotlin:kotlin-runtime:1.0.0-rc-1036") +
                MavenLocalRepository.locateJars("org.jetbrains.kotlin:kotlin-stdlib:1.0.0-rc-1036")

		val expected = ClassUtils.callMain(clazz).replace(
                "java.runtime.name:Java(TM) SE Runtime Environment", "java.runtime.name:jtransc-haxe"
        )

		val build = AllBuild(
				target = HaxeGenDescriptor,
				classPaths = listOf(testClassesPath) + kotlinPaths,
				entryPoint = testClassName,
				output = "program.haxe.js", subtarget = "js",
				//output = "program.haxe.cpp", subtarget = "cpp",
				targetDirectory = System.getProperty("java.io.tmpdir")
		)
		val result = build.buildAndRunCapturingOutput(AstBuildSettings(jtranscVersion = JTranscVersion.getVersion(), debug = false)).output

		Assert.assertEquals(expected, result)
	}
}
