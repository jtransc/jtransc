import big.BigTest
import big.HelloWorldTest
import com.jtransc.BuildBackend
import com.jtransc.gen.as3.As3Target
import com.jtransc.gen.cs.CSharpTarget
import jtransc.micro.MicroHelloWorld
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

	//@Test fun testMicroHelloWorldAsm() = testClass<MicroHelloWorld>(minimize = false, log = false, treeShaking = true, backend = BuildBackend.ASM)
}
