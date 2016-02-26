package jtransc.bug;

object JTranscBug12Test2 {
	@JvmStatic fun main(args: Array<String>) {
		println("[1]");
		val field = Demo::field
		println("[2]");
	}

	class Demo {
		var field: String = "test"
	}
}

