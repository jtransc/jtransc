package javatest

object StrangeNamesTest {

	@JvmStatic fun main(args: Array<String>) {
		println("StrangeNamesTest:")
		InstanceTest().test()
		StaticTest.test()
		StrangeMethodTest().test()
		//`Strange,Class,Test`().test() // <- FAILS
	}

	class InstanceTest {
		var `strange,field,instance` = 10

		fun test() {
			println("InstanceTest:")
			println(this.`strange,field,instance`)
			this.`strange,field,instance` = 7
			println(this.`strange,field,instance`)
		}
	}

	object StaticTest {
		@JvmStatic var `static,strange,field` = 10
		@JvmStatic var `static,strange=field` = 10

		@JvmStatic fun test() {
			println("StaticTest:")
			println(this.`static,strange=field`)
			println(this.`static,strange,field`)
			this.`static,strange,field` = 7
			println(this.`static,strange,field`)
			this.`static,strange,field` = 10 // restore
		}
	}

	class StrangeMethodTest {
		companion object {
			@JvmStatic fun `strange,static,method`() {
				println("strange,static,method:")
			}
		}
		fun `strange,instance,method`() {
			println("strange,instance,method:")
		}

		fun test() {
			`strange,static,method`()
			`strange,instance,method`()
		}
	}

	//class `Strange,Class,Test` {
	//	fun test() {
	//		println("Strange,Class,Test:")
	//	}
	//}
}