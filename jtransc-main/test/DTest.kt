import big.BigTest
import big.HelloWorldTest
import com.jtransc.BuildBackend
import com.jtransc.gen.d.DTarget
import issues.Issue100Double
import issues.issue130.Issue130
import jtransc.bug.JTranscBug127
import jtransc.jtransc.nativ.JTranscDNativeMixedTest
import jtransc.micro.MicroHelloWorld
import org.junit.Ignore
import org.junit.Test
import testservice.test.ServiceLoaderTest
import threading.ThreadingTest

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

class DTest : _Base() {
	override val DEFAULT_TARGET = DTarget()

	//@Test fun testMiniHelloWorld() = testClass<MiniHelloWorldTest>(minimize = false, log = false)
	@Test fun testHelloWorld() = testClass(Params(clazz = HelloWorldTest::class.java, minimize = false, log = false))

	@Test fun testMicroHelloWorldAsm1() = testClass(Params(clazz = MicroHelloWorld::class.java, minimize = false, log = false, treeShaking = true, backend = BuildBackend.ASM))

	//@Test fun testMicroHelloWorldAsm2() = testClass<MicroHelloWorld>(minimize = false, log = false, treeShaking = true, backend = BuildBackend.ASM2)
	//@Test fun testMicroHelloWorldAsm2() = testClass<BenchmarkTest>(minimize = false, log = false, treeShaking = true, backend = BuildBackend.ASM2)

	@Test fun testThreading() = testClass(Params(clazz = ThreadingTest::class.java, minimize = false, log = false))

	@Test fun testIssue100Double() = testClass(Params(clazz = Issue100Double::class.java, minimize = false, log = false))

	@Test fun testBig() = testClass(Params(clazz = BigTest::class.java, minimize = false, log = false, debug = true))

	@Ignore
	@Test fun testJTranscBug127() = testClass(Params(clazz = JTranscBug127::class.java, minimize = false, log = false, debug = true))

	@Ignore("Already included in BigTest")
	@Test fun testDescentIssue130() = testClass(Params(clazz = Issue130::class.java, minimize = false, log = false, treeShaking = true))

	@Test fun testServiceLoaderTest() = testNativeClass("""
		TestServiceImpl1.test:ss
		TestServiceD
	""", Params(clazz = ServiceLoaderTest::class.java, minimize = false))

	@Test fun testMixed() = testNativeClass("""
		JTranscReinterpretArrays:
	""", Params(clazz = JTranscDNativeMixedTest::class.java, minimize = false))
}
