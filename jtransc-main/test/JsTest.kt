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

import big.*
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
import javatest.utils.KotlinInheritanceTest
import jtransc.ExtraKeywordsTest
import jtransc.ExtraRefsTest
import jtransc.ProcessTest
import jtransc.bug.JTranscBug110
import jtransc.java8.Java8Test
import jtransc.java8.Java8Test2
import jtransc.jtransc.js.ScriptEngineTest
import jtransc.jtransc.nativ.JTranscJsNativeMixedTest
import jtransc.micro.MicroHelloWorld
import jtransc.ref.MethodBodyReferencesTest
import jtransc.staticinit.StaticInitTest
import jtransc.staticinit.StaticInitTest2
import org.junit.Test
import testservice.test.ServiceLoaderTest
import testservice.test.TestServiceJs2

class JsTest : _Base() {
	override val DEFAULT_TARGET = JsTarget()

	@Test fun testJTranscBug110() = testClass<JTranscBug110>(minimize = false, log = false, treeShaking = true)

	@Test fun testScriptEngine() = testClass<ScriptEngineTest>(minimize = false, log = false, treeShaking = true)

	@Test fun testJavaEightJs() = testClass<Java8Test>(minimize = false, log = false)

	@Test fun testMicroHelloWorld() = testClass<MicroHelloWorld>(minimize = true, log = false, treeShaking = true)

	@Test fun testKotlinInheritanceTestJs() = testClass<KotlinInheritanceTest>(minimize = false, log = false)

	@Test fun testTwoJavaEightTwoJs() = testClass<Java8Test2>(minimize = false, log = false)

	@Test fun testURLEncoderDecoder() = testClass<URLEncoderDecoderTest>(minimize = false, log = false, treeShaking = true)

	//@Test fun testIssue100Double() = testClass<Issue100Double>(minimize = true, log = true, treeShaking = true, debug = true)
	@Test fun testIssue100Double() = testClass<Issue100Double>(minimize = true, log = false, treeShaking = true)

	@Test fun testIssue105() = testClass<Issue105>(minimize = false, log = false, treeShaking = true)

	@Test fun testExtendedCharsets() = testClass<ExtendedCharsets>(minimize = false, log = false, treeShaking = true)

	@Test fun testMessageDigestTest() = testClass<MessageDigestTest>(minimize = false, log = false, treeShaking = true)

	//@Test fun testMicroHelloWorldAsm2() = testClass<MicroHelloWorld>(minimize = false, log = false, treeShaking = true, backend = BuildBackend.ASM2)
	//@Test fun testMicroHelloWorldAsm2() = testClass<MicroHelloWorld>(minimize = false, log = false, treeShaking = true, backend = BuildBackend.ASM2)
	//@Test fun testMicroHelloWorldAsm2() = testClass<HelloWorldTest>(minimize = false, log = false, treeShaking = true, backend = BuildBackend.ASM2)
	//@Test fun testMicroHelloWorldAsm2() = testClass<BenchmarkTest>(minimize = false, log = false, treeShaking = true, backend = BuildBackend.ASM2)

	@Test fun testMicroStaticInitTest() = testClass<StaticInitTest>(minimize = false, log = false, backend = BuildBackend.ASM, treeShaking = true)

	@Test fun testMicroStaticInitTest2() = testClass<StaticInitTest2>(minimize = false, log = false, backend = BuildBackend.ASM, treeShaking = true)

	@Test fun testHelloWorld() = testClass<HelloWorldTest>(minimize = false, log = false)
	@Test fun testBenchmarkTest() = testClass<BenchmarkTest>(minimize = false, log = false)

	@Test fun testServiceLoaderTest() = testNativeClass<ServiceLoaderTest>("""
		TestServiceImpl1.test:ss
		TestServiceJs10
	""", minimize = false, configureInjector = {
		mapInstance(ConfigServiceLoader(
			classesToSkip = listOf(
				TestServiceJs2::class.java.name
			)
		))
	})

	@Test fun nativeJsTest() = testNativeClass<JTranscJsNativeMixedTest>("""
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
		1234567
		1
		hello
		777
		999
		21
		Timeout!
	""", minimize = false, treeShaking = true)

	@Test fun referencesTest() = testNativeClass<MethodBodyReferencesTest>("""
		MethodBodyReferencesTestJs:true
		MethodBodyReferencesTestCpp:false
		MethodBodyReferencesTestJvm:false
	""", minimize = true, treeShaking = false)

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
	""", minimize = true)

	@Test fun extraRefsTest() = testNativeClass<ExtraRefsTest>("""
		OK
	""", minimize = true)

	@Test fun testBig() = testClass<BigTest>(minimize = false, log = false)
	@Test fun testBigMin() = testClass<BigTest>(minimize = true, log = false)
	@Test fun testBigIO() = testClass<BigIOTest>(minimize = true, log = false, treeShaking = true)
	@Test fun testProcess() = testClass<ProcessTest>(minimize = true, log = false, treeShaking = true)

	@Test fun testNumberFormatTest2() = testClass<NumberFormatTest2>(minimize = false, log = false)

	@Test fun testTryFinallyCheck() = testClass<TryFinallyCheck>(minimize = false, log = false)

	@Test fun testMemberCollisionsTest() = testClass<MemberCollisionsTest>(minimize = false, log = false)

	@Test fun testAsyncIO() = testClass<AsyncIOTest>(minimize = false, log = false, treeShaking = true)
}