package com.jtransc.gen.cs

import com.jtransc.error.invalidOp
import com.jtransc.vfs._MemoryVfs
import org.junit.Assert
import org.junit.Test

class CSharpCompilerTest {
	fun getenv1(key: String): String? {
		return when (key.toUpperCase()) {
			"PATH" -> "/"
			"SYSTEMROOT" -> "/"
			"PROGRAMFILES(X86)" -> "/programfiles"
			else -> invalidOp(key.toUpperCase())
		}
	}

	@Test
	fun name() {
		val rootVfs = object : _MemoryVfs() {
			override fun getenv(key: String): String? = getenv1(key)
		}.root()

		rootVfs["/csc"] = ""

		val csharpCompiler = CSharpCompiler(rootVfs)

		Assert.assertEquals("csc", csharpCompiler.CSC.trim('/', '\\'))
		Assert.assertEquals("csc2", csharpCompiler.getCompiler(mapOf("CSHARP_CMD" to "csc2")).path)
	}
}