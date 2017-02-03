package big

import android.AndroidArgsTest
import com.jtransc.JTranscSystem
import com.jtransc.annotation.JTranscKeepConstructors
import com.jtransc.util.JTranscStrings
import javatest.KotlinCollections
import javatest.KotlinPropertiesTest
import javatest.KotlinStaticInitOrderTest
import javatest.MemberCollisionsTest
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
import jtransc.ProcessTest
import jtransc.WrappedTest
import jtransc.bug.*
import jtransc.java8.DefaultMethodsTest
import jtransc.java8.Java8Test
import jtransc.jtransc.FastMemoryTest
import jtransc.jtransc.JTranscSystemTest
import jtransc.jtransc.SimdTest
import jtransc.rt.test.*
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.text.NumberFormat
import java.util.*

object BigTest {
	@Throws(Throwable::class)
	@JvmStatic fun main(args: Array<String>) {
		KotlinPropertiesTest.main(args)

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

		servicesTest()

		keepConstructorsTest()

		val `is` = InputStreamReader(ByteArrayInputStream(byteArrayOf('A'.toByte(), 'B'.toByte(), 0xC3.toByte(), 0xA1.toByte())))
		println("readLine:" + BufferedReader(`is`).readLine())

		// Hello World functionality!
		HelloWorldTest.main(args)
		NumberFormatTest.main(args);

		NumberFormatTest2.main(args);

		KotlinStaticInitOrderTest.main(args)

		MemberCollisionsTest.main(args)
	}

	private fun servicesTest() {
		val load = ServiceLoader.load(testservice.ITestService::class.java)
		println("Services:")
		for (testService in load) {
			println(testService.test())
		}
		println("/Services:")
	}

	private fun keepConstructorsTest() {
		println("keepConstructorsTest:")
		println(Demo::class.java.declaredConstructors.size)
	}
}

object NumberFormatTest {
	@JvmStatic fun main(args: Array<String>) {
		val ints = intArrayOf(0, 1, 12, 123, 1234, 12345, 123456, 1234567, 12345678)
		val locales = arrayOf(Locale.ENGLISH, Locale.UK, Locale.US, Locale.FRENCH, Locale.forLanguageTag("es"), Locale.forLanguageTag("ja"))

		for (i in ints) {
			for (locale in locales) {
				val s = NumberFormat.getIntegerInstance(locale).format(i.toLong())
				println(locale.language + ":" + s)
				if (s.length == 5) {
					println(s[1].toInt())
				}
			}
		}

		val strings = arrayOf("", "1", "12", "123", "1234", "12345", "123456", "1234567")
		for (s in strings) println(JTranscStrings.join(JTranscStrings.splitInChunks(s, 3), "-"))
		for (s in strings) println(JTranscStrings.join(JTranscStrings.splitInChunksRightToLeft(s, 3), "-"))

		/*
		//LinkedHashMap<String, Integer> stringIntegerLinkedHashMap = new LinkedHashMap<>();
		//stringIntegerLinkedHashMap.put("a", 10);
		//stringIntegerLinkedHashMap.put("b", 20);
		//stringIntegerLinkedHashMap.put("a", 11);
		//System.out.println("Hello World! : " + stringIntegerLinkedHashMap.get("a"));
		System.out.println("Hello World!");
		try {
			Thread.sleep(10000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Hello World!");
		*/
		//System.out.println(new File("c:/temp/2.bin").length());
		//JTranscConsole.log("Hello World!");

		//ProgramReflection.dynamicInvoke(0, null, null);
		//System.out.println("HelloWorldTest.class.field[10]: " + HelloWorldTest.class.getDeclaredField("a").get(null));
		//HelloWorldTest.class.getDeclaredField("a").set(null, 20);
		//System.out.println("HelloWorldTest.class.field[20]: " + HelloWorldTest.class.getDeclaredField("a").get(null));
		//System.out.println("HelloWorldTest.class.method: " + HelloWorldTest.class.getDeclaredMethod("hello").invoke(null));
		//
		//System.out.println(HelloWorldTest.class.getConstructor().newInstance().demo);
		//System.out.println(HelloWorldTest.class.getConstructor().newInstance());
		//System.out.println("####");

	}
}

object NumberFormatTest2 {
	@JvmStatic fun main(args: Array<String>) {
		val numbers = listOf(
			"",
			"\t",
			"\n",
			"    ",
			"1",
			"10",
			"-10",
			"+10",
			"10.3",
			"a10",
			"+a10",
			"10a",
			"10e",
			"10e10",
			"1.12345",
			"5e-10",
			"5.3e-10"
		)

		//println("NumberFormat2(${JTranscSystem.getRuntimeName()}):")
		println("NumberFormat2:")
		for (num in numbers) {
			println(" - $num : int=${checkInt(num)} : double=${checkDouble(num)}")
		}
	}

	fun checkInt(str: String) = try {
		java.lang.Integer.parseInt(str)
	} catch (e: NumberFormatException) {
		-1
	}

	fun checkDouble(str: String) = try {
		java.lang.Double.parseDouble(str)
		true
	} catch (e: NumberFormatException) {
		//-1.0
		false
	}
}

@JTranscKeepConstructors
annotation class KeepConstructorsAnnotation()

@KeepConstructorsAnnotation
class Demo(val a: Int, val s: String)