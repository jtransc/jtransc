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

import com.jtransc.gen.haxe.HaxeTarget
import com.jtransc.gen.js.JsTarget
import jtransc.annotation.ClassMembersTest
import jtransc.annotation.MethodBodyTest
import jtransc.jtransc.*
import jtransc.rt.test.AssertionTests
import org.junit.Test

class HaxeGenSuiteNativeTest : HaxeTestBase() {
	@Test fun customBuild() = testNativeClass<CustomBuildTest>("""
		true
		true
		false
		true
		true
		false
	""", target = HaxeTarget)

	@Test fun methodBodyTest() = testNativeClass<MethodBodyTest>("""
		INT:777
	""", target = HaxeTarget)

	@Test fun classMembersTest() = testNativeClass<ClassMembersTest>("""
		mult:246
	""", target = HaxeTarget)

	@Test fun haxeNativeCallTest() = testNativeClass<HaxeNativeCallTest>("""
		STATIC:851975
		INSTANCE:851975
		MAP:851975
		FIELD:851975
		INPUT:16909060
		&lt;hello&gt;"&amp;"&lt;/hello&gt;
		&lt;hello&gt;&quot;&amp;&quot;&lt;/hello&gt;
	""", target = HaxeTarget)

	@Test fun jtranscSystemTest() = testNativeClass<JTranscSystemTest>("""
		true
		true
		false
		true
		true
		false
		false
		flush
	""", target = HaxeTarget)

	@Test fun UseMinitemplatesTest() = testNativeClass<UseMinitemplatesTest>("""
		methodToExecute1:1
		Class1.method1
		Class1.method2
	""", minimize = false, target = HaxeTarget)

	@Test fun AssertionTests() = testNativeClass<AssertionTests>("""
		Error !(10 < 10)
		ok
	""", minimize = false, debug = true, target = JsTarget)

	@Test fun JTranscInternalNamesHaxe() = testNativeClass<JTranscInternalNames>("""
		jtransc_jtransc_JTranscInternalNames_$
		main__Ljava_lang_String__V
	""", minimize = false, debug = true, target = HaxeTarget)

	@Test fun JTranscInternalNamesJs() = testNativeClass<JTranscInternalNames>("""
		jtransc_jtransc_JTranscInternalNames
		main([Ljava/lang/String;)V
	""", minimize = false, debug = true, target = JsTarget)
}
