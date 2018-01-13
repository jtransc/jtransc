import as3.As3PureTest
import big.BigTest
import com.jtransc.BuildBackend
import com.jtransc.gen.as3.As3Target
import com.jtransc.gen.common._Base
import issues.issue130.Issue130
import javatest.misc.BenchmarkTest
import jtransc.bug.JTranscBug127
import jtransc.bug.JTranscBug244
import jtransc.micro.MicroHelloWorld
import org.junit.Ignore
import org.junit.Test

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

class As3Test : _Base() {
	override val DEFAULT_TARGET = As3Target()

	@Ignore
	//@Test fun testMicroHelloWorldAsm() = testClass<MicroHelloWorld>(minimize = false, log = false, treeShaking = true, debug = true, backend = BuildBackend.ASM, transformerOut = { it.replace("\r", "") })
	@Test fun testMicroHelloWorldAsm() = testClass(Params(clazz = MicroHelloWorld::class.java, minimize = false, log = false, treeShaking = true, debug = true, backend = BuildBackend.ASM, transformerOut = { it.replace("\r", "") }))
	//@Test fun testMicroHelloWorldAsm() = testClass<MicroHelloWorld>(minimize = false, log = false, treeShaking = false, debug = true, backend = BuildBackend.ASM, transformerOut = { it.replace("\r", "") })

	@Ignore
	@Test fun test2() = testClass(Params(clazz = BenchmarkTest::class.java, minimize = false, log = false, treeShaking = true, debug = true, backend = BuildBackend.ASM, transformerOut = { it.replace("\r", "") }))

	@Ignore
	@Test fun testBig() = testClass(Params(clazz = BigTest::class.java, minimize = true, log = false, treeShaking = true, debug = false, backend = BuildBackend.ASM, transformerOut = { it.replace("\r", "") }))

	@Ignore
	@Test fun as3PureTest() = testNativeClass(Params(clazz = As3PureTest::class.java, minimize = false, debug = true, backend = BuildBackend.ASM, target = As3Target(), transformerOut = { it.replace("\r", "") }), """
		1024
		0
	""")

	@Ignore
	@Test fun testJTranscBug127() = testClass(Params(clazz = JTranscBug127::class.java, minimize = false, log = false, debug = true, transformerOut = { it.replace("\r", "") }))

	@Ignore("Already included in BigTest")
	@Test fun testDescentIssue130() = testClass(Params(clazz = Issue130::class.java, minimize = false, log = false, treeShaking = true))

	@Ignore("Covered BigTest")
	@Test fun testJTranscBug244() = testClass(Params(clazz = JTranscBug244::class.java, minimize = false, log = false, debug = true))
}
