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
import com.jtransc.gen.haxe.HaxeTarget
import com.jtransc.gen.js.JsTarget
import javatest.sort.ComparableTimSortTest
import javatest.utils.CopyTest
import org.junit.Test

class JTranscPerTargetBigTest : JTranscTestBase() {
	@Test fun testPlainJs() = testClass<BigTest>(minimize = false, target = JsTarget, log = null)
	//@Test fun testHaxeJs() = testClass<BigTest>(minimize = false, target = HaxeTarget, lang = "js", log = null)
	@Test fun testHaxeJsMinimized() = testClass<BigTest>(minimize = true, target = HaxeTarget, lang = "js", log = null)

	//@Test fun testSmallPlainJs() = testClass<CopyTest>(minimize = false, target = JsTarget, log = null)
}