package jtransc.bug

object JTranscBugInnerMethodsWithSameName {
	@JvmStatic fun main(args: Array<String>) {
		var b:String = "demo"

		fun inner(v: Int) = "$v:$b"
		fun inner(v: String) = "$v:$b"

		fun inner() {
			println(inner(10))
			println(inner("hello"))
		}

		inner()
	}
}