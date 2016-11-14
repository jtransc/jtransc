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

import com.jtransc.gen.js.JsTarget
import javatest.awt.AWTTest
import jtransc.jtransc.nativ.JTranscJsNativeMixedTest
import org.junit.Test

class JTranscGenSuiteNativeJsTest : JTranscTestBase() {
	@Test fun customRun() = testNativeClass<JTranscJsNativeMixedTest>("""
		2
		hello
		world
		jtransc_jtransc_JTranscInternalNames
		main([Ljava/lang/String;)V
		Error !(10 < 10)
		ok
		17j
		-333
	""", target = JsTarget, minimize = false)

	@Test fun testAwtJs() = testNativeClass<AWTTest>("""
		JTranscWidgets.Component(0:frame).init()
		JTranscWidgets.Component(1:container).init()
		JTranscWidgets.Component(2:label).init()
		JTranscWidgets.Component(2:label).setText('Hello World')
		JTranscWidgets.Component(2:label).setParent(JTranscWidgets.Component(1:container))
		JTranscWidgets.Component(0:frame).setVisible(true)
	""", minimize = true, target = JsTarget)
}