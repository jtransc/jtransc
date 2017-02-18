package com.jtransc.gen.as3

import java.io.File

object As3Compiler {
	val airsdk by lazy {
		val sdk = System.getenv("AIRSDK")
		if (sdk.isNullOrBlank()) "" else sdk + "/bin/"
	}

	val AIR_COMPILER by lazy { "${airsdk}amxmlc" }
	val SWF_COMPILER by lazy { "${airsdk}mxmlc" }
	val ADL by lazy { "${airsdk}adl" }

	fun genCommand(programFile: File, debug: Boolean = false, libs: List<String> = listOf()): List<String> {
		return listOf(AIR_COMPILER, programFile.absolutePath)
	}
}
