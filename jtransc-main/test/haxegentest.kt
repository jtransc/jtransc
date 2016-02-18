/**
 * Created by soywiz on 18/02/16.
 */

class HaxeGenTest {
	private fun String.normalize() = this.trim().lines().map { it.trim() }.filter { it.isNotEmpty() }.joinToString("\n")

	/*
	@Test fun test1() {
		val mainAs3Class = samplehx.MainHaxe::class.java
		val testClassesPath = File("target/test-classes").absolutePath

		val expectedOutput = captureStdout {
			samplehx.MainHaxe.main(arrayOf())
		}.replace(
			"java.runtime.name:Java(TM) SE Runtime Environment", "java.runtime.name:jtransc-haxe"
		)

		val build = AllBuild(
			target = HaxeGenDescriptor,
			classPaths = listOf(testClassesPath),
			entryPoint = "samplehx.MainHaxe",
			output = "program.haxe.js", subtarget = "js",
			//output = "program.haxe.cpp", subtarget = "cpp",
			targetDirectory = System.getProperty("java.io.tmpdir")
		)
		val result = build.buildAndRunCapturingOutput(AstBuildSettings(debug = false)).output.normalize()

		//println(expectedOutput.normalize())
		//println(result.normalize())
		Assert.assertEquals(
			//javaClass.getResourceAsString("/haxe_integration.expected").normalize(),
			expectedOutput.normalize(),
			result.normalize()
		)
	}
	*/
}

/*
class DemoTest {
	@org.junit.Test
	fun testName() {
		Assert.assertEquals(-1536, doShortCalc())
	}

	fun doShortCalc(): Int {
		val a = 32000
		val b = 32000
		return (a + b).toShort().toInt()
	}
}
 */