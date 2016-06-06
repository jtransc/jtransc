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
import jtransc.jtransc.SimdTest
import jtransc.rt.test.*
import org.junit.Test

class HaxeGenSuiteTest : HaxeTestBase() {
	@Test fun langStringsTest() = testClass<StringsTest>()

	@Test fun multidimensionalArrayTest() = testClass<MultidimensionalArrayTest>(minimize = false)

	@Test fun kotlinCollectionsTest() = testClass<KotlinCollections>()

	@Test fun fastMemoryTest() = testClass<FastMemoryTest>(minimize = false)
	@Test fun simdTest() = testClass<SimdTest>(minimize = false)

	@Test fun jtranscBugWithStaticInits() = testClass<JTranscBugWithStaticInits>()

	@Test fun arrayListTest() = testClass<JTranscCollectionsTest>()

	@Test fun cloneTest() = testClass<JTranscCloneTest>(minimize = false)
	@Test fun cloneTestMinimized() = testClass<JTranscCloneTest>(minimize = true)

	@Test fun stringBuilderTest() = testClass<StringBuilderTest>()
	@Test fun stackTraceTest() = testClass<JTranscStackTraceTest>()
	@Test fun reflectionTestMinimized() = testClass<JTranscReflectionTest>(minimize = true)
	@Test fun reflectionTestNotMinimized() = testClass<JTranscReflectionTest>(minimize = false)
	@Test fun nioTest() = testClass<JTranscNioTest>()
	@Test fun arithmeticTest() = testClass<JTranscArithmeticTest>()
	@Test fun mathTest() = testClass<MathTest>()

	@Test fun processTest() = testClass<ProcessTest>()

	@Test fun basicTypesTest() = testClass<BasicTypesTest>()

	@Test fun regexTests() = testClass<javatest.utils.regex.RegexTest>(minimize = false)

	@Test fun dateTests() = testClass<DateTest>(minimize = false)

	@Test fun atomicTest() = testClass<AtomicTest>()

	@Test fun bug12Test() = testClass<JTranscBug12Test>()
	@Test fun bug12Test2Kotlin() = testClass<JTranscBug12Test2Kotlin>()
	@Test fun bug14Test() = testClass<JTranscBug14Test>()
	@Test fun bugArrayGetClass() = testClass<JTranscBugArrayGetClass>()
	@Test fun bugArrayDynamicInstantiate() = testClass<JTranscBugArrayDynamicInstantiate>()

	@Test fun bugAbstractInheritance1() = testClass<JTranscBugAbstractInheritance1>()
	@Test fun bugAbstractInheritance2() = testClass<JTranscBugAbstractInheritance2>()

	@Test fun jtranscBug41Test() = testClass<JTranscBug41Test>(minimize = false)

	@Test fun bugClassRefTest() = testClass<JTranscBugClassRefTest>()

	@Test fun bugLongNotInitialized() = testClass<JTranscBugLongNotInitialized>()

	@Test fun bugClInitConflictInAsm() = testClass<JTranscBugClInitConflictInAsm>()

	@Test fun bugInnerMethodsWithSameName() = testClass<JTranscBugInnerMethodsWithSameName>()

	@Test fun bugCompareInterfaceAndObject() = testClass<JTranscBugCompareInterfaceAndObject>()

	@Test fun bugInterfaceWithToString() = testClass<JTranscBugInterfaceWithToString>()

	@Test fun regressionTest1() = testClass<JTranscRegression1Test>()
	@Test fun regressionTest2() = testClass<JTranscRegression2Test>()
	@Test fun regressionTest3() = testClass<JTranscRegression3Test>()

	@Test fun zipTest() = testClass<JTranscZipTest>(minimize = false)

	@Test fun proxyTest() = testClass<ProxyTest>()

	@Test fun wrappedTest() = testClass<WrappedTest>()
}
