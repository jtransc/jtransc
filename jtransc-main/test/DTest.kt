import big.HelloWorldTest
import big.MiniHelloWorldTest
import com.jtransc.BuildBackend
import com.jtransc.gen.d.DTarget
import com.jtransc.gen.js.JsTarget
import issues.Issue100Double
import javatest.misc.BenchmarkTest
import jtransc.jtransc.nativ.JTranscDNativeMixedTest
import jtransc.micro.MicroHelloWorld
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

class DTest : Base() {
	//@Test fun testMiniHelloWorld() = testClass<MiniHelloWorldTest>(minimize = false, target = DTarget(), log = false)
	@Test fun testHelloWorld() = testClass<HelloWorldTest>(minimize = false, target = DTarget(), log = false)

	@Test fun testMicroHelloWorldAsm2() = testClass<MicroHelloWorld>(minimize = false, target = DTarget(), log = false, treeShaking = true, backend = BuildBackend.ASM2)
	//@Test fun testMicroHelloWorldAsm2() = testClass<BenchmarkTest>(minimize = false, target = DTarget(), log = false, treeShaking = true, backend = BuildBackend.ASM2)

	@Test fun testThreading() = testClass<ThreadingTest>(minimize = false, target = DTarget(), log = false)

	@Test fun testIssue100Double() = testClass<Issue100Double>(minimize = false, target = DTarget(), log = false)

	@Test fun testServiceLoaderTest() = testNativeClass<ServiceLoaderTest>("""
		TestServiceImpl1.test:ss
		TestServiceD
	""", target = DTarget(), minimize = false)

	@Test fun testMixed() = testNativeClass<JTranscDNativeMixedTest>("""
		JTranscReinterpretArrays:
		bytes:8 : [0, 0, 0, 0, 0, 0, 0, 0]
		floats:2 : [0.0, 0.0]
		bytes:8 : [0, 0, -128, 63, 0, 0, -128, -65]
		floats:2 : [1.0, -1.0]
	""", target = DTarget(), minimize = false)
}
