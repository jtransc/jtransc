package com.jtransc.gen.common

import com.jtransc.injector.Injector
import com.jtransc.vfs.SyncVfsFile

open class SingleFileCommonGenerator(injector: Injector) : CommonGenerator(injector) {
	override fun writeClasses(output: SyncVfsFile) {
		output[outputFileBaseName] = genClasses(output).toString()
	}
}
