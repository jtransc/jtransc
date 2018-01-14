package com.jtransc.gen.js

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
import com.jtransc.gen.common._Base
import com.jtransc.plugin.service.ConfigServiceLoader
import issues.*
import issues.issue130.Issue130
import issues.issue146.Issue146
import issues.issue158.Issue158
import javatest.ExtendedCharsetsTest
import javatest.MemberCollisionsTest
import javatest.MessageDigestTest
import javatest.misc.BenchmarkTest
import javatest.misc.Misc2Test
import javatest.misc.TryFinallyCheck
import javatest.net.ServerSocketTest
import javatest.net.URLEncoderDecoderTest
import javatest.sleep.SleepTest
import javatest.time.PeriodTest
import javatest.utils.KotlinInheritanceTest
import javatest.utils.OptionalTest
import javaxtest.sound.SimpleSoundTest
import jtransc.ExtraKeywordsTest
import jtransc.ExtraRefsTest
import jtransc.ProcessTest
import jtransc.bug.JTranscBug110
import jtransc.bug.JTranscBug127
import jtransc.bug.JTranscBug244
import jtransc.java8.InnerLambdaTest
import jtransc.java8.Java8Test
import jtransc.java8.Java8Test2
import jtransc.jtransc.js.ScriptEngineTest
import jtransc.jtransc.nativ.JTranscJsNativeMixedTest
import jtransc.micro.MicroHelloWorld
import jtransc.micro.NanoHelloWorldTest
import jtransc.ref.MethodBodyReferencesTest
import jtransc.staticinit.StaticInitTest
import jtransc.staticinit.StaticInitTest2
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import testservice.test.ServiceLoaderTest
import testservice.test.TestServiceJs2

class JsTest : _Base() {
	override val DEFAULT_TARGET = JsTarget()
	//override val TREESHAKING_TRACE = true

	@Test fun testBigWithoutTreeShaking() = testClass(Params(clazz = BigTest::class.java, minimize = false, log = false, treeShaking = false))

	@Test fun testBigWithoutTreeShakingAndAsync() = testClass(Params(clazz = BigTest::class.java, minimize = false, log = false, treeShaking = false, extra = mapOf("js_enable_async" to "true")))

	@Test fun testSideEffects() = testClass(Params(clazz = SideEffectsTest::class.java, minimize = false, log = false, treeShaking = true, debug = true))

	@Test fun testBig() = testClass(Params(clazz = BigTest::class.java, minimize = false, log = false))
	@Test fun testBigMin() = testClass(Params(clazz = BigTest::class.java, minimize = true, log = false))
	//@Test fun testBigIO() = testClass(Params(clazz = BigIOTest::class.java, minimize = true, log = false, treeShaking = true))
	@Test fun testBigIO() = testClass(Params(clazz = BigIOTest::class.java, minimize = false, log = false, treeShaking = true))
	@Test fun testProcess() = testClass(Params(clazz = ProcessTest::class.java, minimize = true, log = false, treeShaking = true))
	@Test fun testJTranscBug110() = testClass(Params(clazz = JTranscBug110::class.java, minimize = false, log = false, treeShaking = true))
	@Test fun testScriptEngine() = testClass(Params(clazz = ScriptEngineTest::class.java, minimize = false, log = false, treeShaking = true))
	@Test fun testJavaEightJs() = testClass(Params(clazz = Java8Test::class.java, minimize = false, log = false))
	@Test fun testNanoHelloWorld() = testClass(Params(clazz = NanoHelloWorldTest::class.java, minimize = false, log = false, treeShaking = true))

	@Test fun testStaticInitIssue135() = testClass(Params(clazz = Issue135::class.java, minimize = false, log = false, treeShaking = true))

	@Ignore("Already included in BigTest")
	@Test fun testJTranscBug127() = testClass(Params(clazz = JTranscBug127::class.java, minimize = false, log = false, treeShaking = true))

	@Ignore("Already included in BigTest")
	@Test fun testDescentIssue130() = testClass(Params(clazz = Issue130::class.java, minimize = false, log = false, treeShaking = true))

	@Test fun testNanoHelloWorldShouldNotReferenceGetMethodsAndBeSmallEnough() {
		val result = _action(Params(clazz = NanoHelloWorldTest::class.java, minimize = false, log = false, treeShaking = true), run = false)
		val generator = result.generator as JsGenerator
		val outputFile = generator.jsOutputFile
		val output = outputFile.readString()
		Assert.assertEquals(false, output.contains("getMethod"))
		println("OutputSize: ${outputFile.size}")
		Assert.assertEquals(true, outputFile.size < 260 * 1024) // Size should be < 222 KB
	}

	@Test fun testMicroHelloWorld() = testClass(Params(clazz = MicroHelloWorld::class.java, minimize = true, log = false, treeShaking = true))

	@Test fun testKotlinInheritanceTestJs() = testClass(Params(clazz = KotlinInheritanceTest::class.java, minimize = false, log = false))

	@Test fun testTwoJavaEightTwoJs() = testClass(Params(clazz = Java8Test2::class.java, minimize = false, log = false))

	@Test fun testURLEncoderDecoder() = testClass(Params(clazz = URLEncoderDecoderTest::class.java, minimize = false, log = false, treeShaking = true))

	//@Test fun testIssue100Double() = testClass(Params(clazz = Issue100Double::class.java, minimize = true, log = false, treeShaking = true))
	@Test fun testIssue100Double() = testClass(Params(clazz = Issue100Double::class.java, minimize = false, log = false, treeShaking = true))

	//@Ignore
	@Test fun testIssue105() = testClass(Params(clazz = Issue105::class.java, minimize = false, log = false, treeShaking = true))

	@Test fun testExtendedCharsets() = testClass(Params(clazz = ExtendedCharsetsTest::class.java, minimize = false, log = false, treeShaking = true))

	@Test fun testMessageDigestTest() = testClass(Params(clazz = MessageDigestTest::class.java, minimize = false, log = false, treeShaking = true))

	//@Test fun testMicroHelloWorldAsm2() = testClass<MicroHelloWorld>(minimize = false, log = false, treeShaking = true, backend = BuildBackend.ASM2)
	//@Test fun testMicroHelloWorldAsm2() = testClass<MicroHelloWorld>(minimize = false, log = false, treeShaking = true, backend = BuildBackend.ASM2)
	//@Test fun testMicroHelloWorldAsm2() = testClass<HelloWorldTest>(minimize = false, log = false, treeShaking = true, backend = BuildBackend.ASM2)
	//@Test fun testMicroHelloWorldAsm2() = testClass<BenchmarkTest>(minimize = false, log = false, treeShaking = true, backend = BuildBackend.ASM2)

	@Test fun testMicroStaticInitTest() = testClass(Params(clazz = StaticInitTest::class.java, minimize = false, log = false, backend = BuildBackend.ASM, treeShaking = true))

	@Test fun testMicroStaticInitTest2() = testClass(Params(clazz = StaticInitTest2::class.java, minimize = false, log = false, backend = BuildBackend.ASM, treeShaking = true))

	@Test fun testHelloWorld() = testClass(Params(clazz = HelloWorldTest::class.java, minimize = false, log = false))
	@Test fun testBenchmarkTest() = testClass(Params(clazz = BenchmarkTest::class.java, minimize = false, log = false))

	@Test fun testServiceLoaderTest() = testNativeClass(Params(clazz = ServiceLoaderTest::class.java, minimize = false, configureInjector = {
            mapInstance(ConfigServiceLoader(
                classesToSkip = listOf(
                    TestServiceJs2::class.java.name
                )
            ))
        }), """
            TestServiceImpl1.test:ss
            TestServiceJs10
        """)

	@Test fun nativeJsTest() = testNativeClass(Params(clazz = JTranscJsNativeMixedTest::class.java, minimize = false, treeShaking = true), """
		17
		-333
		Services:
		testservice.TestServiceImpl1
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
		HELLO WORLD1demo
		HELLO WORLD2test
		77
		js100
		js.instance
		Timeout!
		Shutdown hook!
	""")

	//@Test fun referencesTest() = testNativeClass("""
	//	MethodBodyReferencesTestJs:true
	//	MethodBodyReferencesTestCpp:false
	//	MethodBodyReferencesTestJvm:false
	//""", Params(clazz = MethodBodyReferencesTest::class.java, minimize = true, treeShaking = false))

	@Test fun referencesTest() = testNativeClass(Params(clazz = MethodBodyReferencesTest::class.java, minimize = false, treeShaking = false), """
		MethodBodyReferencesTestJs:true
		MethodBodyReferencesTestCpp:false
		MethodBodyReferencesTestJvm:false
	""")

	@Test fun extraKeywordsJs() = testNativeClass(Params(clazz = ExtraKeywordsTest::class.java, minimize = true), """
		1
		2
		3
		4
		5
		6
		7
		8
		9
	""")

	@Test fun extraRefsTest() = testNativeClass(Params(clazz = ExtraRefsTest::class.java, minimize = true), """
		OK
	""")

	@Test fun testNumberFormatTest2() = testClass(Params(clazz = NumberFormatTest2::class.java, minimize = false, log = false))

	@Test fun testTryFinallyCheck() = testClass(Params(clazz = TryFinallyCheck::class.java, minimize = false, log = false))

	@Test fun testMemberCollisionsTest() = testClass(Params(clazz = MemberCollisionsTest::class.java, minimize = false, log = false))

	@Test fun testAsyncIO() = testClass(Params(clazz = AsyncIOTest::class.java, minimize = false, log = false, treeShaking = true))

	@Ignore("Must fix #146")
	@Test fun testIssue146() = testClass(Params(clazz = Issue146::class.java, minimize = false, log = false, treeShaking = true))

	@Test fun testIssue158() = testClass(Params(clazz = Issue158::class.java, minimize = false, log = false, treeShaking = true))

	@Test fun testInnerLambda() = testClass(Params(clazz = InnerLambdaTest::class.java, minimize = false, log = false, treeShaking = true))

	@Test fun testIssue219() = testClass(Params(clazz = Issue219::class.java, minimize = false, log = false, treeShaking = true, debug = true))

	@Test fun testIssue219NoOptimized() = testClass(Params(clazz = Issue219::class.java, minimize = false, log = false, treeShaking = true, debug = true, optimize = false))

	@Ignore("Covered in bigtest")
	@Test fun testOptionalTest() = testClass(Params(clazz = OptionalTest::class.java, minimize = false, log = false, treeShaking = true, debug = true, optimize = false))

	@Ignore("Covered in bigtest")
	@Test fun testServerSocketTest() = testClass(Params(clazz = ServerSocketTest::class.java, minimize = false, log = false, treeShaking = true, debug = true, optimize = false))

	@Ignore("Covered in bigtest")
	@Test fun testSimpleSoundTest() = testClass(Params(clazz = SimpleSoundTest::class.java, minimize = false, log = false, treeShaking = true, debug = true, optimize = false))

	@Ignore("Covered in bigtest") // libgdx
	@Test fun testIssue246Test() = testClass(Params(clazz = Issue246::class.java, minimize = false, log = false, treeShaking = true, debug = true, optimize = false))

	@Ignore("Covered BigTest")
	@Test fun testJTranscBug244() = testClass(Params(clazz = JTranscBug244::class.java, minimize = false, log = false, debug = true))

	@Ignore("Covered BigTest")
	@Test fun testPeriodTest() = testClass(Params(clazz = PeriodTest::class.java, minimize = false, log = false, debug = true))

	// Requires await/async ES6: https://caniuse.com/#search=await (71.5% and growing)
	// You can transform output using a ES6->ES5 transpiler
	@Test fun testSleep() = testClass(Params(clazz = SleepTest::class.java, minimize = false, log = false, debug = true))

	@Test fun testThreadTest() = testClass(Params(clazz = ThreadTest::class.java, minimize = false, log = false, debug = false, extra = mapOf("js_enable_async" to "true")))

	@Test fun testMisc2() = testClass(Params(clazz = Misc2Test::class.java, minimize = false, log = false))
}
