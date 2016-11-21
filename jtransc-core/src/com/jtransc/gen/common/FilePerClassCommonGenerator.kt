package com.jtransc.gen.common

import com.jtransc.ast.AstClass
import com.jtransc.injector.Injector
import com.jtransc.vfs.SyncVfsFile

open class FilePerClassCommonGenerator(injector: Injector) : CommonGenerator(injector) {
	override fun writeProgram(output: SyncVfsFile) {
		for (clazz in sortedClasses) {
			output[getClassFilename(clazz)] = genClass(clazz).toString()
		}
	}

	fun getClassFilename(clazz: AstClass) = clazz.fqname.replace('.', '/')
}
