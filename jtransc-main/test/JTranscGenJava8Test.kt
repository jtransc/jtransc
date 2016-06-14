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
import jtransc.bug.JTranscClinitNotStatic
import jtransc.java8.DefaultMethodsTest
import jtransc.java8.Java8Test
import org.junit.Test

class JTranscGenJava8Test : JTranscTestBase() {
	@Test fun java8Test() = testClass<Java8Test>(minimize = false, target = JsTarget, log = null)
	@Test fun defaultMethodsTest() = testClass<DefaultMethodsTest>(minimize = false)
	@Test fun clinitNotStaticTest() = testClass<JTranscClinitNotStatic>(minimize = false)
}

