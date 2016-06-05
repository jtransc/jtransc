package com.jtransc.vfs

import com.jtransc.numeric.toInt

data class FileMode(val value: Int) {
	companion object {
		fun fromInt(value:Int) = FileMode(value)
		fun fromOctal(str:String) = FileMode(str.toInt(8, 0))
		val FULL_ACCESS = fromOctal("0777")
	}
}
