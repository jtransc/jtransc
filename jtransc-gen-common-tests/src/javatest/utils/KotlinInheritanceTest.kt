package javatest.utils

object KotlinInheritanceTest {
	@JvmStatic fun main(args: Array<String>) {
		C().demo()
	}

	open class A {
		@JvmField var ademo = "A.demo"

		open fun demo2() {
			println("A.demo2:$ademo")
		}
	}

	open class B : A() {
		@JvmField var demo = "B.demo"

		fun gen(callback: () -> Unit) {
			println("start")
			try {
				callback()
			} finally {
				println("end")
			}
		}

		open fun demo() {
			println("demo: $demo")
		}
	}

	open class C : B() {
		init {
			this.demo = "B.demo"
		}

		override fun demo2() {
			println("C.demo2():$demo")
		}

		override fun demo() {
			gen {
				super.demo()
				super.demo2()
				//this.demo()
				this.demo2()
			}
		}
	}
}