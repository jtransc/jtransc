package jtransc.staticinit

import com.jtransc.io.JTranscConsole
import java.util.EnumSet

object StaticInitTest {
	private val demo = EnumSet.allOf(StaticInitEnum::class.java)

	@JvmStatic fun main(args: Array<String>) {
		JTranscConsole.log("ok")
	}
}
