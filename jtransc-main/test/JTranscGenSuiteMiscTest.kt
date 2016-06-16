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

import com.jtransc.env.CI
import com.jtransc.gen.haxe.HaxeTarget
import com.jtransc.gen.js.JsTarget
import com.jtransc.log.log
import javatest.lang.SystemTest
import javatest.misc.MiscTest
import org.junit.Test

class JTranscGenSuiteMiscTest : JTranscTestBase() {
	// Moved to BigTest

	//@Test fun langSystemTest() = testClass<SystemTest>()
//
	//@Test fun miscTestHaxeJs() = testClass<MiscTest>(analyze = true, target = HaxeTarget, minimize = false, lang = "js")
//
	//@Test fun miscTestNativeJs() {
	//	if (!CI.isOnCiSystem) {
	//		testClass<MiscTest>(lang = "js", target = JsTarget, analyze = true, minimize = false, debug = false,
	//			//log = true
	//			log = null
	//		)
	//	}
	//}

	//@Test fun miscTestPhp() = testClass<MiscTest>(lang = "php", minimize = false) { normalize2(it) }

	//@Test fun miscTestNeko() = testClass<MiscTest>(lang = "neko", minimize = false) {
	//	it.replace("java.runtime.name:Java(TM) SE Runtime Environment", "java.runtime.name:jtransc-haxe")
	//}
}

