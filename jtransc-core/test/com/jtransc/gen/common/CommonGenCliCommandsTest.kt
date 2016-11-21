package com.jtransc.gen.common

import com.jtransc.io.ProcessUtils
import org.junit.Assert
import org.junit.Test

class CommonGenCliCommandsTest {
	//val cmd = JTranscRegisterCommand(
	//	target = "js",
	//	name = "electron",
	//	check = arrayOf("electron", "--version"),
	//	getFolderCmd = arrayOf("npm", "root", "-g"),
	//	install = arrayOf("npm", "-g", "install", "electron")
	//)
	@Test
	fun getGetPaths() {
		Assert.assertTrue(CommonGenCliCommands.getPaths().size > 2)
		println(ProcessUtils.locateCommand("electron"))
	}


	@Test
	fun testCheck() {
		Assert.assertEquals(true, CommonGenCliCommands.execCmd("node", "--version"))
	}
}