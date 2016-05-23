package com.jtransc

import com.jtransc.ast.AstBuildSettings
import java.io.File

fun AllBuildSimple(
	entryPoint: String,
	classPaths: List<String>,
	settings: AstBuildSettings,
	target: String = "haxe:js",
	output: String? = null,
	targetDirectory:String = System.getProperty("java.io.tmpdir")
): AllBuild {
	val targetParts = target.split(":")
	val targetName = targetParts.getOrElse(0) { "haxe" }
	val subtargetName = targetParts.getOrElse(1) { "" }
	//val actualTarget = AllBuildTargets.locateTargetByName(targetName)
	val actualOutput = output ?: "program.$subtargetName"



	//println("actualOutput:$actualOutput")

	return AllBuild(
		AllBuildTargets = AllBuildTargets,
		target = targetName,
		classPaths = classPaths,
		entryPoint = entryPoint,
		output = if (File(actualOutput).isAbsolute) actualOutput else File(targetDirectory, actualOutput).absolutePath,
		subtarget = subtargetName,
		settings = settings,
		targetDirectory = targetDirectory
	)
}