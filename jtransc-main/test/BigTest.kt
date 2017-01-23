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
import com.jtransc.gen.cpp.CppTarget
import com.jtransc.gen.d.DTarget
import com.jtransc.gen.haxe.HaxeTarget
import com.jtransc.gen.js.JsTarget
import javatest.D3Target
import javatest.utils.KotlinInheritanceTest
import jtransc.java8.Java8Test
import jtransc.java8.Java8Test2
import org.junit.Test

class BigTest : Base() {
	//@Test fun testPlainCpp() = testClass<BigTest>(minimize = false, target = CppTarget(), log = null, debug = true)

	//@Test fun testHelloWorldCpp() = testClass<HelloWorldTest>(minimize = false, target = CppTarget(), log = true)
	//@Test fun testHelloWorldCpp() = testClass<HelloWorldTest>(minimize = false, target = CppTarget(), log = true, debug = true)
	//@Test fun testHelloWorldCppRelease() = testClass<HelloWorldTest>(minimize = false, target = CppTarget(), log = true, debug = false)


	@Test fun testJavaEightJs() = testClass<Java8Test>(minimize = false, target = JsTarget(), log = false)

	@Test fun testD() = testClass<BigTest>(minimize = false, target = DTarget(), log = false)

	//@Test fun testPlainJs() = testClass<BigTest>(minimize = false, target = JsTarget(), log = true)

	//@Test fun testKotlinInheritanceTestCpp() = testClass<KotlinInheritanceTest>(minimize = false, target = CppTarget(), log = null, debug = true)
	@Test fun testKotlinInheritanceTestJs() = testClass<KotlinInheritanceTest>(minimize = false, target = JsTarget(), log = false)

	@Test fun testTwoJavaEightTwoJs() = testClass<Java8Test2>(minimize = false, target = JsTarget(), log = false)

	//@Test fun testHelloWorldKotlinTestJs() = testClass<HelloWorldKotlinTest>(minimize = false, target = JsTarget(), log = true)
	//@Test fun testHelloWorldKotlinTestCpp() = testClass<HelloWorldKotlinTest>(minimize = false, target = CppTarget(), log = true, debug = true)

	//@Test fun testHelloWorldCpp() = testClass<HelloWorldTest>(minimize = false, target = CppTarget, log = true, debug = false)

	@Test fun testHaxeJs() = testClass<BigTest>(minimize = false, target = HaxeTarget(), lang = "js", log = null)
	//@Test fun testHaxeJs() = testClass<BigTest>(minimize = false, target = HaxeTarget(), lang = "js", log = true)

	//@Test fun testHaxeJsMinimized() = testClass<BigTest>(minimize = true, target = HaxeTarget(), lang = "js", log = null)

	//@Test fun testSmallPlainJs() = testClass<CopyTest>(minimize = false, target = JsTarget, log = null)

	//@Test fun testSmallPlainCpp() = testClass<CopyTest>(minimize = false, target = CppTarget, log = null)
	//@Test fun testWeakCpp() = testClass<WeakTest>(minimize = false, target = CppTarget, log = null)

	//@Test fun testReflectionCpp() = testClass<JTranscReflectionTest>(minimize = false, target = CppTarget, log = null)
}