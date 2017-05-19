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
import jtransc.jtransc.SimdTest
import org.junit.Ignore
import org.junit.Test

class CppTest : _Base() {
	override val DEFAULT_TARGET = CppTarget()

	//override val TREESHAKING: Boolean = false
	//override val TREESHAKING_TRACE: Boolean = false

	//@Ignore("Ignored until stabilized C++ target to avoid problems with travis")
	@Test fun testHelloWorld() = testClass<HelloWorldTest>(minimize = false, log = true, treeShaking = true, debug = false)

	@Ignore
	@Test fun testSimdTest() = testClass<SimdTest>(minimize = false, log = true, treeShaking = true, debug = false)

	@Ignore("Must implement UndeterministicParameterEvaluationFeature to pass parameter evaluation order tests")
	@Test fun testBigTest() = testClass<BigTest>(minimize = false, log = false, treeShaking = true, debug = false)

	//@Test fun testMixed() = testNativeClass<JTranscCppNativeMixedTest>("""
	//	JTranscReinterpretArrays:
	//	bytes:8 : [0, 0, 0, 0, 0, 0, 0, 0]
	//	floats:2 : [0.0, 0.0]
	//	bytes:8 : [0, 0, -128, 63, 0, 0, -128, -65]
	//	floats:2 : [1.0, -1.0]
	//""", minimize = false)

}
