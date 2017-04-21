package com.jtransc.gen.common

import com.jtransc.injector.Injector
import com.jtransc.vfs.SyncVfsFile

open class SingleFileCommonGenerator(injector: Injector) : CommonGenerator(injector) {
	open val ADD_UTF8_BOM = false

	override fun writeClasses(output: SyncVfsFile) {
		if (ADD_UTF8_BOM) {
			output[outputFileBaseName] = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + genClasses(output).toString().toByteArray()
		} else {
			output[outputFileBaseName] = genClasses(output).toString()
		}
	}
}
