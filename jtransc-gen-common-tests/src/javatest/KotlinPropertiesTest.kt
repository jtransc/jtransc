package javatest

object KotlinPropertiesTest {
	@JvmStatic fun main(args: Array<String>) {
		println(Demo().a)
	}
}

class Demo {
	val a by lazy { 10 }
}