package com.jtransc.gen.common

import com.jtransc.*
import com.jtransc.ast.AstBuildSettings
import com.jtransc.ast.AstProgram
import com.jtransc.ast.FqName
import com.jtransc.gen.TargetName
import com.jtransc.injector.Injector
import com.jtransc.plugin.reflection.createClass
import com.jtransc.vfs.MemoryVfs
import java.io.File

fun <T : CommonGenerator> Injector.genCommonGeneratorDouble(clazz: Class<T>, target:String, subtarget:String = ""): T {
	mapInstance(ConfigTargetDirectory(""))
	mapInstance(ConfigResourcesVfs(MemoryVfs()))
	mapInstance(ConfigEntryPoint(FqName("dummy")))
	mapInstance(ConfigSrcFolder(MemoryVfs()))
	mapInstance(AstBuildSettings())
	mapInstance(CommonGenFolders(listOf()))
	mapInstance(TargetName("$target:$subtarget"))
	mapInstance(ConfigOutputFile("output.bin"))
	mapInstance(ConfigOutputFile2(File("output.bin")))
	mapInstance(ConfigSubtarget(subtarget))
	get<AstProgram>().createClass(FqName("java.lang.Object"), parent = null) {

	}
	return getInstance(clazz)
}