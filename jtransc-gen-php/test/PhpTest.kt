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
import com.jtransc.gen.common._Base
import com.jtransc.gen.php.PhpTarget
import javatest.misc.ArrayListTest
import javatest.misc.TryFinallyCheck
import jtransc.bug.JTranscBug244
import jtransc.micro.MicroHelloWorld
import org.junit.Ignore
import org.junit.Test

class PhpTest : _Base() {
	override val DEFAULT_TARGET = PhpTarget()

	@Ignore
	@Test fun MicroHelloWorld() = testClass(Params(clazz = MicroHelloWorld::class.java, minimize = false, log = false, treeShaking = true))

	@Ignore
	@Test fun BigTest() = testClass(Params(clazz = BigTest::class.java, minimize = false, log = false, treeShaking = true))

	@Ignore
	@Test fun ArrayListTest() = testClass(Params(clazz = ArrayListTest::class.java, minimize = false, log = false, treeShaking = true))

	@Ignore
	@Test fun TryFinallyCheck() = testClass(Params(clazz = TryFinallyCheck::class.java, minimize = false, log = false, treeShaking = true))

	@Ignore("Covered BigTest")
	@Test fun testJTranscBug244() = testClass(Params(clazz = JTranscBug244::class.java, minimize = false, log = false, debug = true))
}