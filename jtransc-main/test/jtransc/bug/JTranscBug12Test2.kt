package jtransc.bug;

import java.util.*

object JTranscBug12Test2 {
	@JvmStatic fun main(args: Array<String>) {
		println("[1]");
		val field = Demo::field
		var abc = "cba".reversed()
		println("[2]");

		println("abc".equals("abc"))
		test1("a")
		test1("b")
		test1("c")
		test1("abcdef")
		test1(abc + "def")
		test1("abcdefg")
		test2()
		test3()
	}

	enum class TestEnum { A, B, C; }

	fun test3() {
		//val set = RegularEnumSet(TestEnum::class.java, arrayOf(TestEnum.A, TestEnum.B, TestEnum.C))
	}

	fun test1(str:String) {
		when (str) {
			"a" -> println("my:a")
			"b" -> println("my:b")
			"abcdef" -> println("my:abcdef")
			else -> println("my:else:$str")
		}
	}

	fun test2() {
		//val range = 11L until 1000
		//println(range.start)
		//println(range.step)
		//println(range.endInclusive)
	}

	class Demo {
		var field: String = "test"
	}
}

