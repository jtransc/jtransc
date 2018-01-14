package big

import android.AndroidArgsTest
import com.jtransc.annotation.JTranscKeepConstructors
import com.jtransc.io.JTranscConsole
import com.jtransc.util.JTranscStrings
import issues.*
import issues.issue130.Issue130
import javatest.*
import javatest.lang.*
import javatest.misc.BenchmarkTest
import javatest.misc.MiscTest
import javatest.net.ServerSocketTest
import javatest.nio.ModifiedUtf8Test
import javatest.sort.CharCharMapTest
import javatest.sort.ComparableTimSortTest
import javatest.time.PeriodTest
import javatest.utils.*
import javatest.utils.Base64Test
import javatest.utils.CopyTest
import javatest.utils.DateTest
import javatest.utils.FillTest
import javaxtest.sound.SimpleSoundTest
import jtransc.WrappedTest
import jtransc.bug.*
import jtransc.java8.Java8Test
import jtransc.jtransc.FastMemoryTest
import jtransc.jtransc.SimdTest
import jtransc.rt.test.*
import java.text.NumberFormat
import java.util.*

//@JTranscAddTemplateVars(target = "cpp", variable = "CMAKE_ARGS", list = arrayOf("--help"))
object BigTest {
	val i = arrayOf(9.0F, 2.0F)
	@Throws(Throwable::class)
	@JvmStatic fun main(args: Array<String>) {
		//Thread.sleep(5000L)
		//KotlinPropertiesTest.main(args)
		JTranscConsole.log("BigTest:")

		// Misc tests
		JTranscConsole.log("sleep[1]")
		Thread.sleep(1L)
		JTranscConsole.log("/sleep[1]")
		StringsTest.main(args)
		PropertiesTest.main(args)
		BasicTypesTest.main(args)
		SystemTest.main(args)
		CopyTest.main(args)
		FillTest.main(args)
		AtomicTest.main(args)
		FastMemoryTest.main(args)
		FastMemoryTest.main(args)
		MultidimensionalArrayTest.main(args)
		//KotlinCollections.main(args)
		//KotlinInheritanceTest.main(args)
		SimdTest.main(args)
		MiscTest.main(args)
		BenchmarkTest.main(args)

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
		DateTest.main(args)
		AtomicTest.main(args)
		JTranscBug12Test.main(args)
		//JTranscBug12Test2Kotlin.main(args)
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
		//JTranscClinitNotStatic.main(args)
		//DefaultMethodsTest.main(args)
		Java8Test.main(args)

		// Misc
		Base64Test.main(args)
		CharCharMapTest.main(args)

		// Regex
		javatest.utils.regex.RegexTest.main(args)

		servicesTest()

		keepConstructorsTest()

		Issue105.main(args)

		// Hello World functionality!
		HelloWorldTest.main(args)
		NumberFormatTest.main(args)

		//NumberFormatTest2.main(args);

		KotlinStaticInitOrderTest.main(args)

		MemberCollisionsTest.main(args)

		ConcurrentCollectionsTest.main(args)

		MessageDigestTest.main(args)

		Issue94Enum.main(args)
		Issue100Double.main(args)

		CaseInsensitiveOrder.main(args)

		Issue130.main(args)

		JTranscBug127.main(args)

		System.out.println(String.format("%d%%", 100))

		// check float mod
		System.out.println(i[0] % i[1])

		Issue209.main(args)

		ModifiedUtf8Test.main(args)

		OptionalTest.main(args)
		ServerSocketTest.main(args)
		SimpleSoundTest.main(args)
		Issue246.main(args)

		JTranscBug244.main(args)
		PeriodTest.main(args)
		SideEffectsTest.main(args)
	}

	private fun servicesTest() {
		val load = ServiceLoader.load(testservice.ITestService::class.java)
		val list = load.toList()
		println("Services(" + list.size + "):")
		for (testService in list) {
			println(testService.javaClass.name)
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
				println(locale.language + ":" + TestStringTools.escape(s))
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

private class CaseInsensitiveOrder {
	companion object {
		@JvmStatic fun main(args: Array<String>) {
			val tm = TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER)
			tm["Ab"] = "hello"
			println(tm["ab"])
			println(tm["aB"])
			println(tm["Ab"])
			println(tm["AB"])
		}
	}
}

@JTranscKeepConstructors
annotation class KeepConstructorsAnnotation

@KeepConstructorsAnnotation
class Demo(val a: Int, val s: String)