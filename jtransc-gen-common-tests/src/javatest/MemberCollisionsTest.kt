package javatest

object MemberCollisionsTest {
	@JvmStatic fun main(args: Array<String>) {
		val b = B()
		println("MemberCollisionsTest:" + b.a + ":" + b.b)
	}

	//internal open class A0 {
	//	val ctx: String = "3"
	//}

	internal open class A {
		val _ctx: String = ""
		//val ctx: Int = 2

		val a: String get() = _ctx
	}

	internal class B : A() {
		val ctx: Int = 5

		val b: Int get() = ctx
	}
}