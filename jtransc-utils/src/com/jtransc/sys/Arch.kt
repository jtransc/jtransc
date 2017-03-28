package com.jtransc.sys

enum class Arch(val bits: Int) {
	X86(32), X64(64), ARM(32), ARM64(64), SPARC(32), PPC(32), OTHER(32);

	companion object {
		val CURRENT by lazy {
			when (System.getProperty("os.arch").toLowerCase()) {
				"x86", "i386" -> X86
				"x86_64", "amd64" -> X64
				"arm" -> ARM
				"arm64" -> ARM64
				"sparc" -> SPARC
				"ppc" -> PPC
				else -> OTHER
			}
		}
	}
}