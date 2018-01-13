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

import com.jtransc.ast.AstTypes
import com.jtransc.ast.mangle
import com.jtransc.gen.TargetName
import org.junit.Assert
import org.junit.Test

class MangleTest {
	val types = AstTypes(TargetName("js"))
	private fun testMangle(info: String) = Assert.assertEquals(info, types.demangle(info).mangle())

	@Test
	fun testName() {
		testMangle("I")
		testMangle("Ljava/util/HashMap<Ljava/lang/Integer;Ljava/lang/Long;>;")
	}

	@Test
	fun test2() {
		data class Test(val a: Int)
		println(Test::class.java.declaredMethods.map { it.name })
		println(Test::class.java.declaredConstructors.map { it.name })
	}

	@Test fun test3() {
		testMangle("<U:Ljava/lang/Object;>(Ljava/lang/Class<TU;>;)Ljava/lang/Class<+TU;>;")
		//testMangle("<A::Ljava/lang/annotation/Annotation;>(Ljava/lang/Class<TA;>;)TA;")
	}

	@Test fun test4() {
		testMangle("Ljava/util/LinkedList<TE;>.ListItr;")
		//testMangle("<A::Ljava/lang/annotation/Annotation;>(Ljava/lang/Class<TA;>;)TA;")
	}

	@Test fun test5() {
		testMangle("()Ljtransc/rt/test/ATest1<TA;TB;>.ATest2<TB;>;")
	}
}