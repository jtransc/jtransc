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
import com.jtransc.gen.haxe.HaxeTarget
import issues.Issue103
import issues.Issue94Enum
import jtransc.bug.JTranscBug110
import jtransc.jtransc.nativ.JTranscHaxeNativeMixedTest
import jtransc.micro.MicroHelloWorld
import org.junit.Ignore
import org.junit.Test

class HaxeTest : Base() {
	@Test fun testMicroHelloWorld() = testClass<MicroHelloWorld>(minimize = false, target = HaxeTarget(), lang = "js", log = null, treeShaking = true)

	@Test fun testHelloWorldHaxeJsTreeShaking() = testClass<HelloWorldTest>(minimize = false, target = HaxeTarget(), lang = "js", log = null, treeShaking = true)
	@Test fun testHelloWorldHaxeJs() = testClass<HelloWorldTest>(minimize = false, target = HaxeTarget(), lang = "js", log = null, treeShaking = false)

	@Test fun testEnumBugIssue94() = testClass<Issue94Enum>(minimize = false, target = HaxeTarget(), log = false, treeShaking = true)
	@Test fun testBigSwitchIssue103() = testClass<Issue103>(minimize = false, target = HaxeTarget(), lang = "js", log = false, treeShaking = true)

	@Test fun testJTranscBug110() = testClass<JTranscBug110>(minimize = false, target = HaxeTarget(), lang = "js", log = false, treeShaking = true)

	@Test fun haxeNativeCallTest() = testNativeClass<JTranscHaxeNativeMixedTest>("""
		true
		true
		false
		true
		true
		false
		STATIC:851975
		INSTANCE:851975
		MAP:851975
		FIELD:851975
		INPUT:16909060
		&lt;hello&gt;"&amp;"&lt;/hello&gt;
		&lt;hello&gt;&quot;&amp;&quot;&lt;/hello&gt;
		mult:861
		mult:246
		INT:777
		true
		true
		false
		true
		true
		false
		false
		flush
		methodToExecute1:1
		Class1.method1
		Class1.method2
		10
		jtransc.jtransc.JTranscInternalNamesTest_
		main__Ljava_lang_String__V
		_jt___hello
		JTranscReinterpretArrays:
		bytes:8 : [0, 0, 0, 0, 0, 0, 0, 0]
		floats:2 : [0.0, 0.0]
		bytes:8 : [0, 0, -128, 63, 0, 0, -128, -65]
		floats:2 : [1.0, -1.0]
	""", target = HaxeTarget(), minimize = false)
}
