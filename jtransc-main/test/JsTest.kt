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
import com.jtransc.gen.js.JsTarget
import javatest.awt.AWTTest
import jtransc.ExtraKeywordsTest
import jtransc.jtransc.nativ.JTranscJsNativeMixedTest
import org.junit.Test

class JsTest : Base() {
	@Test fun testHelloWorld() = testClass<HelloWorldTest>(minimize = false, target = JsTarget(), log = false)

	@Test fun customRun() = testNativeClass<JTranscJsNativeMixedTest>("""
		17
		-333
		Services:
		TestServiceImpl1.test:ss
		/Services:
		2
		hello
		world
		10
		jtransc_jtransc_JTranscInternalNamesTest
		main([Ljava/lang/String;)V
		___hello
		Error !(10 < 10)
		ok
	""", target = JsTarget(), minimize = false)

	@Test fun testAwtJs() = testNativeClass<AWTTest>("""
		JTranscWidgets.Component(0:frame).init()
		JTranscWidgets.Component(1:label).init()
		JTranscWidgets.Component(2:image).init()
		JTranscWidgets.Component(3:text).init()
		JTranscWidgets.Component(2:image).setParent(JTranscWidgets.Component(1:label))
		JTranscWidgets.Component(3:text).setParent(JTranscWidgets.Component(1:label))
		JTranscWidgets.Component(3:text).setText('Hello World')
		JTranscWidgets.Component(1:label).setParent(JTranscWidgets.Component(0:frame))
		JTranscWidgets.Component(0:frame).setVisible(true)
	""", minimize = false, target = JsTarget())

	@Test fun extraKeywordsJs() = testNativeClass<ExtraKeywordsTest>("""
		1
		2
		3
		4
		5
		6
		7
		8
		9
	""", minimize = true, target = JsTarget())
}