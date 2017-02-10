import big.HelloWorldTest
import big.MiniHelloWorldTest
import com.jtransc.BuildBackend
import com.jtransc.gen.d.CSharpTarget
import com.jtransc.gen.d.DTarget
import com.jtransc.gen.js.JsTarget
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

class CSharpTest : Base() {
	//@Test fun testMicroHelloWorldAsm2() = testClass<MicroHelloWorld>(minimize = false, target = CSharpTarget(), log = false, treeShaking = true, backend = BuildBackend.ASM2)
	@Test fun testMicroHelloWorldAsm() = testClass<MicroHelloWorld>(minimize = false, target = CSharpTarget(), log = false, treeShaking = true, backend = BuildBackend.ASM)
}
