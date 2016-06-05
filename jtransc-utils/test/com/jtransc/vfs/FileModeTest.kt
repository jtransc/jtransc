package com.jtransc.vfs

import com.jtransc.numeric.toInt
import org.junit.Assert
import org.junit.Test

class FileModeTest {
	@Test
	fun Name() {
		Assert.assertEquals("0000".toInt(8), FileMode.fromString("----------").value)
		Assert.assertEquals("0111".toInt(8), FileMode.fromString("---x--x--x").value)
		Assert.assertEquals("0333".toInt(8), FileMode.fromString("--wx-wx-wx").value)
		Assert.assertEquals("0444".toInt(8), FileMode.fromString("-r--r--r--").value)
		Assert.assertEquals("0555".toInt(8), FileMode.fromString("-r-xr-xr-x").value)
		Assert.assertEquals("0666".toInt(8), FileMode.fromString("-rw-rw-rw-").value)
		Assert.assertEquals("0777".toInt(8), FileMode.fromString("-rwxrwxrwx").value)
		Assert.assertEquals("0740".toInt(8), FileMode.fromString("-rwxr-----").value)
	}
}