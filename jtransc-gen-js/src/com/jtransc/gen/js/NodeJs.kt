package com.jtransc.gen.js

import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.ProcessResult
import java.io.File

object NodeJs {
	fun run(script: String, args: List<String>): ProcessResult {
		return LocalVfs(File(".")).exec("node", listOf(script) + args)
	}
}