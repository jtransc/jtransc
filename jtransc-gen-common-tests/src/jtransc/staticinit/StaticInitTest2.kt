package jtransc.staticinit

import com.jtransc.io.JTranscConsole

object StaticInitTest2 {
	@JvmStatic fun main(args: Array<String>) {
		JTranscConsole.log(MyClass("a") != null)
		val v = MyClass("test")
		println(v.parent != null)
	}
}

class MyClass(path: String) : MyClass2(path) {
	val parent: MyClass by lazy { MyClass(path + ".") }
}

open class MyClass2(val path: String) {
	val parent2: MyClass2 by lazy { MyClass2(path + ".") }
}