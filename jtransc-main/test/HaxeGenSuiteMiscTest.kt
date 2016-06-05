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

import javatest.KotlinCollections
import javatest.lang.AtomicTest
import javatest.lang.BasicTypesTest
import javatest.lang.StringsTest
import javatest.lang.SystemTest
import javatest.misc.MiscTest
import javatest.utils.DateTest
import jtransc.ProcessTest
import jtransc.WrappedTest
import jtransc.bug.*
import jtransc.java8.DefaultMethodsTest
import jtransc.java8.Java8Test
import jtransc.jtransc.FastMemoryTest
import jtransc.rt.test.*
import org.junit.Test

class HaxeGenSuiteMiscTest : HaxeTestBase() {
	@Test fun langSystemTest() = testClass<SystemTest>() { normalize2(it) }

	@Test fun miscTestJs() = testClass<MiscTest>(analyze = true, minimize = false) { normalize2(it) }

	@Test fun miscTestPhp() = testClass<MiscTest>(lang = "php", minimize = false) { normalize2(it) }

	private fun normalize2(str:String):String {
		return str
			.replace("java.runtime.name:Java(TM) SE Runtime Environment", "java.runtime.name:jtransc-haxe")
			.replace("java.runtime.name:OpenJDK Runtime Environment", "java.runtime.name:jtransc-haxe")
			.replace("path.separator:;", "path.separator::")
	}

	//@Test fun miscTestNeko() = testClass<MiscTest>(lang = "neko", minimize = false) {
	//	it.replace("java.runtime.name:Java(TM) SE Runtime Environment", "java.runtime.name:jtransc-haxe")
	//}
}

