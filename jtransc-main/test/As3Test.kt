import as3.As3PureTest
import big.BigTest
import big.HelloWorldTest
import com.jtransc.BuildBackend
import com.jtransc.gen.as3.As3Target
import com.jtransc.gen.cs.CSharpTarget
import com.jtransc.gen.js.JsTarget
import javatest.misc.BenchmarkTest
import jtransc.ExtraKeywordsTest
import jtransc.micro.MicroHelloWorld
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

class As3Test : Base() {
	override val DEFAULT_TARGET = As3Target()

	@Ignore
	//@Test fun testMicroHelloWorldAsm() = testClass<MicroHelloWorld>(minimize = false, log = false, treeShaking = true, debug = true, backend = BuildBackend.ASM, transformerOut = { it.replace("\r", "") })
	@Test fun testMicroHelloWorldAsm() = testClass<MicroHelloWorld>(minimize = false, log = false, treeShaking = true, debug = true, backend = BuildBackend.ASM2, transformerOut = { it.replace("\r", "") })
	//@Test fun testMicroHelloWorldAsm() = testClass<MicroHelloWorld>(minimize = false, log = false, treeShaking = false, debug = true, backend = BuildBackend.ASM, transformerOut = { it.replace("\r", "") })

	@Ignore
	@Test fun test2() = testClass<BenchmarkTest>(minimize = false, log = false, treeShaking = true, debug = true, backend = BuildBackend.ASM, transformerOut = { it.replace("\r", "") })

	@Ignore
	@Test fun testBig() {
		//testClass<BigTest>(minimize = false, log = false, treeShaking = true, debug = true, backend = BuildBackend.ASM, transformerOut = { it.replace("\r", "") })
		testClass<BigTest>(minimize = true, log = false, treeShaking = true, debug = false, backend = BuildBackend.ASM, transformerOut = { it.replace("\r", "") })
	}
	
	@Ignore
	@Test fun as3PureTest() = testNativeClass<As3PureTest>("""
		1024
		0
	""", minimize = false, debug = true, backend = BuildBackend.ASM2, target = As3Target(), transformerOut = { it.replace("\r", "") })
}
