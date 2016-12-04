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

import big.HelloWorldTest
import com.jtransc.ConfigClassPaths
import com.jtransc.JTranscBuild
import com.jtransc.JTranscVersion
import com.jtransc.ast.AstBuildSettings
import com.jtransc.ast.AstTypes
import com.jtransc.ast.ConfigMinimizeNames
import com.jtransc.ast.ConfigTreeShaking
import com.jtransc.gen.js.JsTarget
import com.jtransc.injector.Injector
import javatest.GenTargetDescriptor
import jtransc.ExtraKeywordsTest
import jtransc.ExtraRefsTest
import jtransc.jtransc.nativ.JTranscJsNativeMixedTest
import org.junit.Test
import testservice.test.ServiceLoaderTest

class JsTest : Base() {
	@Test fun testHelloWorld() = testClass<HelloWorldTest>(minimize = false, target = JsTarget(), log = false)

	@Test fun testServiceLoaderTest() = testNativeClass<ServiceLoaderTest>("""
		TestServiceImpl1.test:ss
		TestServiceJs
	""", target = JsTarget(), minimize = false)

	@Test fun customRun() = testNativeClass<JTranscJsNativeMixedTest>("""
		17
		-333
		Services:
		TestServiceImpl1.test:ss
		/Services:
		2
		hello
		world
		10
		jtransc_jtransc_JTranscInternalNamesTest
		main([Ljava/lang/String;)V
		___hello
		Error !(10 < 10)
		ok
		JTranscReinterpretArrays:
		bytes:8 : [0, 0, 0, 0, 0, 0, 0, 0]
		floats:2 : [0.0, 0.0]
		bytes:8 : [0, 0, -128, 63, 0, 0, -128, -65]
		floats:2 : [1.0, -1.0]
	""", target = JsTarget(), minimize = false)

	@Test fun extraKeywordsJs() = testNativeClass<ExtraKeywordsTest>("""
		1
		2
		3
		4
		5
		6
		7
		8
		9
	""", minimize = true, target = JsTarget())

	@Test fun extraRefsTest() = testNativeClass<ExtraRefsTest>("""
		OK
	""", minimize = true, target = JsTarget())
}