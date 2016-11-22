package com.jtransc.gen.d

import com.jtransc.ConfigOutputFile
import com.jtransc.ConfigTargetDirectory
import com.jtransc.ast.*
import com.jtransc.ast.feature.method.GotosFeature
import com.jtransc.ast.feature.method.SwitchFeature
import com.jtransc.gen.GenTargetDescriptor
import com.jtransc.gen.common.*
import com.jtransc.injector.Injector
import com.jtransc.injector.Singleton
import com.jtransc.io.ProcessResult2
import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.LocalVfsEnsureDirs
import com.jtransc.vfs.SyncVfsFile
import java.io.File

// Supports GOTO keyword
// Supports static fields and methods on interfaces
class DTarget() : GenTargetDescriptor() {
	override val name = "d"
	override val outputExtension = "bin"
	override val extraLibraries = listOf<String>()
	override val extraClasses = listOf<String>()
	override val runningAvailable = true
	override val programFeatures: Set<Class<AstProgramFeature>> = setOf()

	override fun getGenerator(injector: Injector): CommonGenerator {
		val settings = injector.get<AstBuildSettings>()
		val configTargetDirectory = injector.get<ConfigTargetDirectory>()
		val configOutputFile = injector.get<ConfigOutputFile>()
		val targetFolder = LocalVfsEnsureDirs(File("${configTargetDirectory.targetDirectory}/jtransc-d"))
		injector.mapInstance(CommonGenFolders(settings.assets.map { LocalVfs(it) }))
		injector.mapInstance(ConfigTargetFolder(targetFolder))
		injector.mapInstance(ConfigSrcFolder(targetFolder))
		injector.mapInstance(ConfigOutputFile2(targetFolder[configOutputFile.outputFileBaseName].realfile))
		return injector.get<DGenerator>()
	}

	override fun getTargetByExtension(ext: String): String? = when (ext) {
		"exe" -> "cpp"
		"bin" -> "cpp"
		else -> null
	}
}

@Singleton
class DGenerator(injector: Injector) : SingleFileCommonGenerator(injector) {
//class DGenerator(injector: Injector) : FilePerClassCommonGenerator(injector) {
	override val methodFeatures = setOf(SwitchFeature::class.java, GotosFeature::class.java)
	override val keywords = setOf<String>()

	override fun genCompilerCommand(programFile: File, debug: Boolean, libs: List<String>): List<String> {
		return DCompiler.genCommand(programFile, debug, libs)
	}

	override fun run(redirect: Boolean): ProcessResult2 {
		return ProcessResult2(0)
	}

	override fun writeProgram(output: SyncVfsFile) {
		super.writeProgram(output)
		println(output)
	}

	override val NullType = "Object"
	override val VoidType = "void"
	override val BoolType = "bool"
	override val IntType = "int"
	override val FloatType = "float"
	override val DoubleType = "double"
	override val LongType = "long"

}