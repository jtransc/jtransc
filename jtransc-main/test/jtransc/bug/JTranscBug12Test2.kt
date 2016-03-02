package jtransc.bug;

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
	}

	fun test1(str:String) {
		when (str) {
			"a" -> println("my:a")
			"b" -> println("my:b")
			"abcdef" -> println("my:abcdef")
			else -> println("my:else:$str")
		}
	}

	class Demo {
		var field: String = "test"
	}
}

