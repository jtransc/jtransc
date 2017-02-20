package com.jtransc.gen.common

import com.jtransc.ast.AstClass
import com.jtransc.ast.FqName
import com.jtransc.ds.getOrPut2
import com.jtransc.injector.Injector
import com.jtransc.vfs.SyncVfsFile

open class FilePerClassCommonGenerator(injector: Injector) : CommonGenerator(injector) {
	override fun writeProgramAndFiles() {
		val output = configTargetFolder.targetFolder
		writeClasses(output)
		setTemplateParamsAfterBuildingSource()
		for (file in getFilesToCopy(targetName.name)) {
			val str = program.resourcesVfs[file.src].readString()
			val strr = if (file.process) str.template("includeFile") else str
			output[file.dst] = strr
		}
	}

	override fun writeClasses(output: SyncVfsFile) {
		for (clazz in sortedClasses) {
			output[getClassFilename(clazz)] = genClass(clazz).toString()
		}
	}

	open fun getClassBaseFilename(clazz: AstClass) = clazz.actualFqName.fqname.replace('.', '/')

	open fun getClassFilename(clazz: AstClass) = getClassBaseFilename(clazz)
}
