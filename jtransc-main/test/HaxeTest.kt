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
import com.jtransc.gen.haxe.HaxeTarget
import issues.Issue103
import issues.Issue135
import issues.Issue94Enum
import issues.issue130.Issue130
import issues.issue136.Issue136
import javatest.haxe.HaxeStringBuilderTestIssue138
import jtransc.bug.JTranscBug110
import jtransc.bug.JTranscBug127
import jtransc.jtransc.nativ.JTranscHaxeNativeMixedTest
import jtransc.micro.MicroHelloWorld
import org.junit.Ignore
import org.junit.Test

class HaxeTest : _Base() {
	override val DEFAULT_TARGET = HaxeTarget()

	@Test fun testMicroHelloWorld() = testClass(Params(clazz = MicroHelloWorld::class.java, minimize = false, lang = "js", log = null, treeShaking = true))

	@Test fun testHelloWorldHaxeJsTreeShaking() = testClass(Params(clazz = HelloWorldTest::class.java, minimize = false, lang = "js", log = null, treeShaking = true))
	@Test fun testHelloWorldHaxeJs() = testClass(Params(clazz = HelloWorldTest::class.java, minimize = false, lang = "js", log = null, treeShaking = false))

	@Test fun testEnumBugIssue94() = testClass(Params(clazz = Issue94Enum::class.java, minimize = false, log = false, treeShaking = true))
	@Test fun testBigSwitchIssue103() = testClass(Params(clazz = Issue103::class.java, minimize = false, lang = "js", log = false, treeShaking = true))

	@Test fun testJTranscBug110() = testClass(Params(clazz = JTranscBug110::class.java, minimize = false, lang = "js", log = false, treeShaking = true))

	@Ignore("Already included in BigTest")
	@Test fun testDescentIssue130() = testClass(Params(clazz = Issue130::class.java, minimize = false, lang = "js", log = false, treeShaking = true))

	@Test fun testGetGenericType() = testClass(Params(clazz = Issue136::class.java, minimize = false, lang = "js", log = false, treeShaking = true))

	@Test fun testBig() = testClass(Params(clazz = BigTest::class.java, minimize = false, lang = "js", log = null))

	@Test fun testBigMinimized() = testClass(Params(clazz = BigTest::class.java, minimize = true, lang = "js", log = null))

	@Ignore
	@Test fun testBigCpp() = testClass(Params(clazz = BigTest::class.java, minimize = false, lang = "cpp", log = null, debug = true))

	@Test fun haxeNativeCallTest() = testNativeClass("""
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
	""", Params(clazz = JTranscHaxeNativeMixedTest::class.java, minimize = false))

	@Ignore
	@Test fun testJTranscBug127() = testClass(Params(clazz = JTranscBug127::class.java, minimize = false, log = false, lang = "js", debug = true))

	// WORKS
	//@Ignore
	//@Test fun testHaxeStringBuilderTestIssue138() = testClass(Params(clazz = HaxeStringBuilderTestIssue138::class.java, minimize = false, log = false, lang = "js", debug = true))

	// FAILS
	@Ignore
	@Test fun testHaxeStringBuilderTestIssue138() = testClass(Params(clazz = HaxeStringBuilderTestIssue138::class.java, minimize = false, log = false, lang = "cpp", debug = true))

}
