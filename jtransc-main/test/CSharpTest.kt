import big.AsyncIOTest
import big.BigIOTest
import big.BigTest
import big.HelloWorldTest
import com.jtransc.BuildBackend
import com.jtransc.gen.cs.CSharpTarget
import issues.issue130.Issue130
import jtransc.ProcessTest
import jtransc.bug.JTranscBug127
import jtransc.micro.MicroHelloWorld
import jtransc.staticinit.StaticInitTest
import org.junit.Ignore
import org.junit.Test
import testservice.test.ServiceLoaderTest

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

class CSharpTest : _Base() {
	override val DEFAULT_TARGET = CSharpTarget()

	@Test fun testHelloWorld() = testClass(Params(clazz = HelloWorldTest::class.java, minimize = false, log = false))

	//@Test fun testMicroHelloWorldAsm2() = testClass<MicroHelloWorld>(minimize = false, target = CSharpTarget(), log = false, treeShaking = true, backend = BuildBackend.ASM2)
	@Test fun testMicroHelloWorldAsm() = testClass(Params(clazz = MicroHelloWorld::class.java, minimize = false, log = false, treeShaking = true, backend = BuildBackend.ASM))

	@Test fun testServiceLoaderTest() = testNativeClass("""
		TestServiceImpl1.test:ss
		TestServiceCS
	""", Params(clazz = ServiceLoaderTest::class.java, minimize = false))

	@Test fun testBig() = testClass(Params(clazz = BigTest::class.java, minimize = false, debug = false, log = false))

	@Ignore("Already included in BigTest")
	@Test fun testDescentIssue130() = testClass(Params(clazz = Issue130::class.java, minimize = false, log = false, treeShaking = true))

	@Ignore
	@Test fun testJTranscBug127() = testClass(Params(clazz = JTranscBug127::class.java, minimize = false, log = false, debug = true))

	//@Test fun testMicroStaticInitTestAsm2() = testClass<StaticInitTest>(minimize = false, target = CSharpTarget(), log = false, backend = BuildBackend.ASM2, treeShaking = true)
	@Test fun testMicroStaticInitTestAsm1() = testClass(Params(clazz = StaticInitTest::class.java, minimize = false, target = CSharpTarget(), log = false, backend = BuildBackend.ASM, treeShaking = true))

	@Ignore("Not working fine yet")
	@Test fun testBigIO() = testClass(Params(clazz = BigIOTest::class.java, minimize = false, log = false))

	@Ignore("Not working fine yet")
	@Test fun testProcess() = testClass(Params(clazz = ProcessTest::class.java, minimize = false, log = false))

	@Test fun testAsyncIO() = testClass(Params(clazz = AsyncIOTest::class.java, minimize = false, log = false, treeShaking = true))
}
