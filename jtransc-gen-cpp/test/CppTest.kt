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
import big.HelloWorldTest
import big.SideEffectsTest
import big.ThreadTest
import com.jtransc.gen.common._Base
import com.jtransc.gen.cpp.CppTarget
import issues.issue130.Issue130
import javatest.finalize.FinalizeTest
import javatest.misc.ExecutionOrderTest
import jtransc.bug.JTranscBug127
import jtransc.bug.JTranscBug244
import jtransc.java8.Java8Test
import jtransc.jtransc.SimdTest
import jtransc.micro.NanoHelloWorldTest
import org.junit.Ignore
import org.junit.Test
import threading.ThreadingTest

class CppTest : _Base() {
	override val DEFAULT_TARGET = CppTarget()

	//override val TREESHAKING: Boolean = false
	//override val TREESHAKING_TRACE: Boolean = false

	@Test fun testBigTest() = testClass(Params(clazz = BigTest::class.java, minimize = false, log = false, treeShaking = true, debug = true)) // debug=true makes builds much faster

	@Ignore("Failing for now")
	@Test fun testThread() = testClass(Params(clazz = ThreadTest::class.java, minimize = false, log = false, treeShaking = true, debug = true)) // debug=true makes builds much faster

	@Ignore
	@Test fun testBigTestRelease() = testClass(Params(clazz = BigTest::class.java, minimize = false, log = false, treeShaking = true, debug = false))

	@Test fun testSideEffects() = testClass(Params(clazz = SideEffectsTest::class.java, minimize = false, log = false, treeShaking = true, debug = true))

	@Ignore
	@Test fun testNanoHelloWorld() = testClass(Params(clazz = NanoHelloWorldTest::class.java, minimize = false, log = true, treeShaking = true, debug = true))

	@Ignore("Already included in BigTest")
	@Test fun testJTranscBug127() = testClass(Params(clazz = JTranscBug127::class.java, minimize = false, log = false, treeShaking = true, debug = true))

	@Ignore
	@Test fun testHelloWorld() = testClass(Params(clazz = HelloWorldTest::class.java, minimize = false, log = true, treeShaking = true, debug = true))
	//@Test fun testHelloWorld() = testClass<HelloWorldTest>(minimize = false, log = true, treeShaking = true, debug = false)

	@Ignore
	@Test fun testSimdTest() = testClass(Params(clazz = SimdTest::class.java, minimize = false, log = true, treeShaking = true, debug = false))

	@Ignore
	@Test fun testExecutionOrder() = testClass(Params(clazz = ExecutionOrderTest::class.java, minimize = false, log = false, treeShaking = true, debug = true)) // debug=true makes builds much faster

	@Ignore
	@Test fun testJava8() = testClass(Params(clazz = Java8Test::class.java, minimize = false, log = false, treeShaking = true, debug = true)) // debug=true makes builds much faster

	@Ignore
	@Test fun testDescentIssue130() = testClass(Params(clazz = Issue130::class.java, minimize = false, log = false, treeShaking = true))

	@Ignore("Not working yet")
	@Test fun testFinalize() = testClass(Params(clazz = FinalizeTest::class.java, minimize = false, log = false, treeShaking = true))


	//@Test fun testMixed() = testNativeClass<JTranscCppNativeMixedTest>("""
	//	JTranscReinterpretArrays:
	//	bytes:8 : [0, 0, 0, 0, 0, 0, 0, 0]
	//	floats:2 : [0.0, 0.0]
	//	bytes:8 : [0, 0, -128, 63, 0, 0, -128, -65]
	//	floats:2 : [1.0, -1.0]
	//""", minimize = false)

    @Test fun testThreading() = testClass(Params(clazz = ThreadingTest::class.java, minimize = false, log = false))

	@Ignore("Covered BigTest")
	@Test fun testJTranscBug244() = testClass(Params(clazz = JTranscBug244::class.java, minimize = false, log = false, debug = true))
}
