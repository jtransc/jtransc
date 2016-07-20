package com.jtransc.gen.cpp

import com.jtransc.ast.*
import com.jtransc.gen.GenTarget
import com.jtransc.gen.GenTargetInfo
import com.jtransc.gen.GenTargetProcessor
import com.jtransc.gen.common.CommonGenFolders
import com.jtransc.gen.common.CommonGenGen
import com.jtransc.gen.common.CommonNames
import com.jtransc.gen.common.CommonProgramTemplate
import com.jtransc.io.ProcessResult2
import java.io.File

object GenCpp : GenTarget {
	override val runningAvailable: Boolean = true

	override fun getProcessor(tinfo: GenTargetInfo, settings: AstBuildSettings): GenTargetProcessor {
		return CppGenTargetProcessor(tinfo, settings)
	}
}

class CppGenTargetProcessor(val tinfo: GenTargetInfo, val settings: AstBuildSettings) : GenTargetProcessor() {
	override fun buildSource() {
	}

	override fun compile(): ProcessResult2 {
		return ProcessResult2(exitValue = 0)
	}

	override fun run(redirect: Boolean): ProcessResult2 {
		return ProcessResult2(exitValue = 0)
	}
}

class GenCppGen(input:Input) : CommonGenGen(input) {

}

class CppTemplateString(
	names: CppNames, tinfo: GenTargetInfo, settings: AstBuildSettings, folders: CommonGenFolders, outputFile2: File, types: AstTypes
) : CommonProgramTemplate(
	names, tinfo, settings, folders, outputFile2, types
) {

}

class CppNames(
	program: AstResolver,
	val minimize: Boolean
) : CommonNames(program) {
	override fun buildTemplateClass(clazz: FqName): String {
		throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun buildTemplateClass(clazz: AstClass): String {
		throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun buildMethod(method: AstMethod, static: Boolean): String {
		throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun buildStaticInit(clazz: AstClass): String {
		throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun buildConstructor(method: AstMethod): String {
		throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
}