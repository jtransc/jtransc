package com.jtransc.env

object CI {
	val isOnCiSystem by lazy {
		listOf("CI", "TRAVIS", "APPVEYOR")
			.map { (System.getenv(it) ?: "").toLowerCase() }
			.any { it in listOf("true", "yes") }
	}
}