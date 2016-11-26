package big

import android.AndroidArgsTest
import android.AndroidTest8019
import com.jtransc.annotation.JTranscInvisible
import com.jtransc.annotation.JTranscMethodBody
import javatest.KotlinCollections
import javatest.StrangeNamesTest
import javatest.lang.AtomicTest
import javatest.lang.BasicTypesTest
import javatest.lang.StringsTest
import javatest.lang.SystemTest
import javatest.misc.BenchmarkTest
import javatest.misc.MiscTest
import javatest.sort.CharCharMapTest
import javatest.sort.ComparableTimSortTest
import javatest.utils.Base64Test
import javatest.utils.CopyTest
import javatest.utils.DateTest
import javatest.utils.KotlinInheritanceTest
import jtransc.ProcessTest
import jtransc.WrappedTest
import jtransc.bug.*
import jtransc.java8.DefaultMethodsTest
import jtransc.java8.Java8Test
import jtransc.jtransc.FastMemoryTest
import jtransc.jtransc.SimdTest
import jtransc.rt.test.*

object BigTest {
	@Throws(Throwable::class)
	@JvmStatic fun main(args: Array<String>) {
		// Misc tests
		StringsTest.main(args)
		SystemTest.main(args)
		CopyTest.main(args)
		AtomicTest.main(args);
		FastMemoryTest.main(args)
		FastMemoryTest.main(args)
		MultidimensionalArrayTest.main(args)
		KotlinCollections.main(args)
		//KotlinInheritanceTest.main(args)
		SimdTest.main(args)
		MiscTest.main(args)
		BenchmarkTest.main(args);

		// Suite tests
		JTranscBugWithStaticInits.main(args)
		JTranscCollectionsTest.main(args)
		JTranscCloneTest.main(args)
		StringBuilderTest.main(args)
		JTranscStackTraceTest.main(args)
		JTranscReflectionTest.main(args)
		JTranscNioTest.main(args)
		JTranscArithmeticTest.main(args)
		MathTest.main(args)
		BasicTypesTest.main(args)
		DateTest.main(args)
		AtomicTest.main(args)
		JTranscBug12Test.main(args)
		JTranscBug12Test2Kotlin.main(args)
		JTranscBug14Test.main(args)
		JTranscBugArrayGetClass.main(args)
		JTranscBugArrayDynamicInstantiate.main(args)
		JTranscBugAbstractInheritance1.main(args)
		JTranscBugAbstractInheritance2.main(args)
		JTranscBug41Test.main(args)
		JTranscBugClassRefTest.main(args)
		JTranscBugLongNotInitialized.main(args)
		JTranscBugClInitConflictInAsm.main(args)
		JTranscBugInnerMethodsWithSameName.main(args)
		JTranscBugCompareInterfaceAndObject.main(args)
		JTranscBugInterfaceWithToString.main(args)
		JTranscRegression1Test.main(args)
		JTranscRegression2Test.main(args)
		JTranscRegression3Test.main(args)


		ProxyTest.main(args)
		WrappedTest.main(args)

		// Android
		AndroidArgsTest.main(args)
		//AndroidTest8019.main(args)

		// Kotlin
		//StrangeNamesTest.main(args)
		ComparableTimSortTest.main(args)

		// Java8 tests
		JTranscClinitNotStatic.main(args)
		DefaultMethodsTest.main(args)
		Java8Test.main(args)

		// Misc
		Base64Test.main(args);
		JTranscZipTest.main(args)
		ProcessTest.main(args)
		CharCharMapTest.main(args);

		// Regex
		javatest.utils.regex.RegexTest.main(args)
	}
}
