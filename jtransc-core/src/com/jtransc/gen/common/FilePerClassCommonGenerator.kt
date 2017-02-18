package com.jtransc.gen.common

import com.jtransc.ast.AstClass
import com.jtransc.injector.Injector
import com.jtransc.vfs.SyncVfsFile

open class FilePerClassCommonGenerator(injector: Injector) : CommonGenerator(injector) {
	override fun writeProgram(output: SyncVfsFile) {
		for (clazz in sortedClasses) {
			output[getClassFilename(clazz)] = genClass(clazz).toString()
		}

		//copyFiles()
		for (file in getFilesToCopy(targetName.name)) {
			val str = program.resourcesVfs[file.src].readString()
			val strr = if (file.process) str.template("includeFile") else str
			output[file.dst] = strr
		}
	}

	open fun getClassFilename(clazz: AstClass) = clazz.fqname.replace('.', '/')
}
