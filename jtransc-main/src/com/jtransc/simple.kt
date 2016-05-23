package com.jtransc

import com.jtransc.ast.AstBuildSettings

fun AllBuildSimple(
	entryPoint: String,
	classPaths: List<String>,
	settings: AstBuildSettings,
	target: String = "haxe:js",
	output: String = "output.bin",
	targetDirectory:String = System.getProperty("java.io.tmpdir")
): AllBuild {
	val targetParts = target.split(":")

	return AllBuild(
		AllBuildTargets = AllBuildTargets,
		target = targetParts.getOrElse(0) { "haxe" },
		classPaths = classPaths,
		entryPoint = entryPoint,
		output = output,
		subtarget = targetParts.getOrElse(1) { "" },
		settings = settings,
		targetDirectory = targetDirectory
	)
}