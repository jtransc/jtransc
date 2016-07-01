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
		var `strange,field` = 10

		fun test() {
			println("InstanceTest:")
			println(this.`strange,field`)
			this.`strange,field` = 7
			println(this.`strange,field`)
		}
	}

	object StaticTest {
		@JvmStatic var `strange,field` = 10
		@JvmStatic var `strange=field` = 10

		@JvmStatic fun test() {
			println("StaticTest:")
			println(this.`strange=field`)
			println(this.`strange,field`)
			this.`strange,field` = 7
			println(this.`strange,field`)
			this.`strange,field` = 10 // restore
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