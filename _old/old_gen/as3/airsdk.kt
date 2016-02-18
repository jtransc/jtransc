package com.jtransc.gen.as3

import com.jtransc.env.OS
import com.jtransc.error.InvalidOperationException
import java.io.File

object AirSdk {
	val AIRSDK by lazy {
		listOf(
			System.getProperty("air.sdk"),
			System.getenv("AIRSDK"),
			if (OS.isWindows) "c:\\dev\\airsdk18" else "/Developer/airsdk18"
		).filterNotNull().filter {
			File("$it/lib/mxmlc-cli.jar").exists()
		}.firstOrNull() ?: throw InvalidOperationException("Can't find a suitable airsdk folder! please specify with 'air.sdk' property or 'AIRSDK' environment variable")
	}
}