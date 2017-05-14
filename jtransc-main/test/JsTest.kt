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

import big.BigIOTest
import big.BigTest
import big.HelloWorldTest
import big.NumberFormatTest2
import com.jtransc.BuildBackend
import com.jtransc.gen.js.JsTarget
import com.jtransc.plugin.service.ConfigServiceLoader
import issues.Issue100Double
import issues.Issue105
import javatest.ExtendedCharsets
import javatest.MemberCollisionsTest
import javatest.MessageDigestTest
import javatest.misc.BenchmarkTest
import javatest.misc.TryFinallyCheck
import javatest.net.URLEncoderDecoderTest
import jtransc.ExtraKeywordsTest
import jtransc.ExtraRefsTest
import jtransc.ProcessTest
import jtransc.bug.JTranscBug110
import jtransc.jtransc.js.ScriptEngineTest
import jtransc.jtransc.nativ.JTranscJsNativeMixedTest
import jtransc.micro.MicroHelloWorld
import jtransc.ref.MethodBodyReferencesTest
import jtransc.staticinit.StaticInitTest
import org.junit.Ignore
import org.junit.Test
import testservice.test.ServiceLoaderTest
import testservice.test.TestServiceJs2

class JsTest : Base() {
	@Test fun testJTranscBug110() = testClass<JTranscBug110>(minimize = false, target = JsTarget(), log = false, treeShaking = true)

	@Test fun testScriptEngine() = testClass<ScriptEngineTest>(minimize = false, target = JsTarget(), log = false, treeShaking = true)

	@Test fun testMicroHelloWorld() = testClass<MicroHelloWorld>(minimize = true, target = JsTarget(), log = false, treeShaking = true)

	@Test fun testURLEncoderDecoder() = testClass<URLEncoderDecoderTest>(minimize = false, target = JsTarget(), log = false, treeShaking = true)

	//@Test fun testIssue100Double() = testClass<Issue100Double>(minimize = true, target = JsTarget(), log = true, treeShaking = true, debug = true)
	@Test fun testIssue100Double() = testClass<Issue100Double>(minimize = true, target = JsTarget(), log = false, treeShaking = true)

	@Test fun testIssue105() = testClass<Issue105>(minimize = false, target = JsTarget(), log = false, treeShaking = true)

	@Test fun testExtendedCharsets() = testClass<ExtendedCharsets>(minimize = false, target = JsTarget(), log = false, treeShaking = true)

	@Test fun testMessageDigestTest() = testClass<MessageDigestTest>(minimize = false, target = JsTarget(), log = false, treeShaking = true)

	//@Test fun testMicroHelloWorldAsm2() = testClass<MicroHelloWorld>(minimize = false, target = JsTarget(), log = false, treeShaking = true, backend = BuildBackend.ASM2)
	//@Test fun testMicroHelloWorldAsm2() = testClass<MicroHelloWorld>(minimize = false, target = JsTarget(), log = false, treeShaking = true, backend = BuildBackend.ASM2)
	//@Test fun testMicroHelloWorldAsm2() = testClass<HelloWorldTest>(minimize = false, target = JsTarget(), log = false, treeShaking = true, backend = BuildBackend.ASM2)
	//@Test fun testMicroHelloWorldAsm2() = testClass<BenchmarkTest>(minimize = false, target = JsTarget(), log = false, treeShaking = true, backend = BuildBackend.ASM2)

	@Test fun testMicroStaticInitTest() = testClass<StaticInitTest>(minimize = false, target = JsTarget(), log = false, backend = BuildBackend.ASM, treeShaking = true)

	@Test fun testHelloWorld() = testClass<HelloWorldTest>(minimize = false, target = JsTarget(), log = false)
	@Test fun testBenchmarkTest() = testClass<BenchmarkTest>(minimize = false, target = JsTarget(), log = false)

	@Test fun testServiceLoaderTest() = testNativeClass<ServiceLoaderTest>("""
		TestServiceImpl1.test:ss
		TestServiceJs10
	""", target = JsTarget(), minimize = false, configureInjector = {
		mapInstance(ConfigServiceLoader(
			classesToSkip = listOf(
				TestServiceJs2::class.java.name
			)
		))
	})

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
		MethodBodyReferencesTestJs:true
		MethodBodyReferencesTestCpp:false
		MethodBodyReferencesTestJvm:false
		OK!
		MixedJsKotlin.main[1]
		MixedJsKotlin.main[2]
		[ 1, 2, 3 ]
		<Buffer 00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f>
		{"a":10,"b":"c","c":[1,2,3]}
		.txt
		1
		2
		0
		true
		Timeout!
	""", target = JsTarget(), minimize = false, treeShaking = true)

	@Test fun referencesTest() = testNativeClass<MethodBodyReferencesTest>("""
		MethodBodyReferencesTestJs:true
		MethodBodyReferencesTestCpp:false
		MethodBodyReferencesTestJvm:false
	""", target = JsTarget(), minimize = true, treeShaking = false)

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

	@Test fun testPlainJs() = testClass<BigTest>(minimize = false, target = JsTarget(), log = false)
	@Test fun testPlainJsMin() = testClass<BigTest>(minimize = true, target = JsTarget(), log = false)
	@Test fun testBigIO() = testClass<BigIOTest>(minimize = true, target = JsTarget(), log = false, treeShaking = true)

	@Test fun testNumberFormatTest2() = testClass<NumberFormatTest2>(minimize = false, target = JsTarget(), log = false)

	@Test fun testTryFinallyCheck() = testClass<TryFinallyCheck>(minimize = false, target = JsTarget(), log = false)

	@Test fun testMemberCollisionsTest() = testClass<MemberCollisionsTest>(minimize = false, target = JsTarget(), log = false)

}