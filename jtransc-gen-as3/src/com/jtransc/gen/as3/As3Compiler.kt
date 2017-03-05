package com.jtransc.gen.as3

import com.jtransc.ast.FqName
import com.jtransc.env.OS
import com.jtransc.error.invalidOp
import com.jtransc.serialization.xml.Xml
import com.jtransc.vfs.LocalVfs
import org.intellij.lang.annotations.Language
import java.io.File

// @TODO: Should check that mm.cfg:
// http://help.adobe.com/en_US/air/build/WSfffb011ac560372f-6fa6d7e0128cca93d31-8000.html
// Note: If your trace() statements do not display on the console, ensure that you have not specified ErrorReportingEnable or TraceOutputFileEnable in the mm.cfg file.
// For more information on the platform-specific location of this file, see Editing the mm.cfg file.
object As3Compiler {
	val AIRSDK_HOME by lazy {
		System.getenv("AIRSDK_HOME") ?: System.getenv("AIRSDK") ?: invalidOp("AIRSDK_HOME or AIRSDK environment variables not defined")
	}

	val AIRSDK_BIN by lazy {
		val sdk = AIRSDK_HOME
		if (sdk.isNullOrBlank()) "" else sdk + "/bin/"
	}

	val MM_CFG_FILES by lazy {
		when {
			OS.isWindows -> listOf(File(System.getenv("HOMEDRIVE") + "\\" + System.getenv("HOMEPATH") + "\\mm.cfg"))
			else -> {
				listOf(File(System.getenv("HOME") + "/mm.cfg"), File("/Library/Application Support/Macromedia/mm.cfg"))
			}
		}
	}

	val AIR_COMPILER by lazy { "${AIRSDK_BIN}amxmlc" }
	val SWF_COMPILER by lazy { "${AIRSDK_BIN}mxmlc" }
	val ADL by lazy { "${AIRSDK_BIN}adl" }

	fun genCommand(sourceFolder: File, programFile: File, debug: Boolean = false, libs: List<String> = listOf()): List<String> {
		//mxmlc.exe src/Editor.as -output=Editor.swf  -compiler.source-path=src1 -compiler.source-path=../src2 -compiler.library-path+=libs -compiler.library-path=../libs

		return listOf(AIR_COMPILER, "-compiler.source-path+=${sourceFolder.absolutePath}", programFile.absolutePath)
	}

	fun getSdkVersionFromString(@Language("xml") str: String): String {
		val xml = Xml(str)
		return xml["version"].firstOrNull()?.text ?: "unknown"
	}

	fun getSdkIntVersionFromString(@Language("xml") str: String): Int {
		return Regex("^\\w+").find(getSdkVersionFromString(str))?.groupValues?.get(0)?.toInt() ?: 0
	}

	val AIRSDK_DESCRIPTOR by lazy { LocalVfs(File(AIRSDK_HOME))["air-sdk-description.xml"].readString() }
	val AIRSDK_VERSION by lazy { getSdkVersionFromString(AIRSDK_DESCRIPTOR) }
	val AIRSDK_VERSION_INT by lazy { getSdkIntVersionFromString(AIRSDK_DESCRIPTOR) }
}
