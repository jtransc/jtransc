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

import big.HelloWorldTest
import com.jtransc.gen.cpp.CppTarget
import com.jtransc.gen.d.DTarget
import jtransc.jtransc.nativ.JTranscCppNativeMixedTest
import jtransc.jtransc.nativ.JTranscDNativeMixedTest
import org.junit.Test

class CppTest : Base() {
	//override val TREESHAKING: Boolean = false
	//override val TREESHAKING_TRACE: Boolean = false

	//@Test fun testHelloWorld() = testClass<HelloWorldTest>(minimize = false, target = CppTarget(), log = false)

	//@Test fun testMixed() = testNativeClass<JTranscCppNativeMixedTest>("""
	//	JTranscReinterpretArrays:
	//	bytes:8 : [0, 0, 0, 0, 0, 0, 0, 0]
	//	floats:2 : [0.0, 0.0]
	//	bytes:8 : [0, 0, -128, 63, 0, 0, -128, -65]
	//	floats:2 : [1.0, -1.0]
	//""", target = CppTarget(), minimize = false)

}
