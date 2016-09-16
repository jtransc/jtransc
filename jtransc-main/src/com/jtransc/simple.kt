package com.jtransc

import com.jtransc.ast.AstBuildSettings
import com.jtransc.ast.AstTypes
import com.jtransc.injector.Injector
import java.io.File

fun AllBuildSimple(
	injector: Injector,
	entryPoint: String,
	settings: AstBuildSettings,
	target: String = "haxe:js",
	output: String? = null,
	targetDirectory:String = System.getProperty("java.io.tmpdir")
): JTranscBuild {
	val targetParts = target.split(":")
	val targetName = targetParts.getOrElse(0) { "haxe" }
	val subtargetName = targetParts.getOrElse(1) { "" }
	//val actualTarget = AllBuildTargets.locateTargetByName(targetName)
	val actualOutput = output ?: "program.$subtargetName"

	//println("actualOutput:$actualOutput")

	return JTranscBuild(
		injector = injector,
		target = AllBuildTargets.locateTargetByName(target).descriptor,
		entryPoint = entryPoint,
		output = if (File(actualOutput).isAbsolute) actualOutput else File(targetDirectory, actualOutput).absolutePath,
		subtarget = subtargetName,
		settings = settings,
		targetDirectory = targetDirectory
	)
}