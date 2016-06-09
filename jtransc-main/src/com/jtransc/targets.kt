package com.jtransc

import com.jtransc.gen.GenTargetDescriptor
import com.jtransc.gen.haxe.HaxeGenDescriptor

val AllBuildTargets = listOf(
	HaxeGenDescriptor
)

fun List<GenTargetDescriptor>.locateTargetByOutExt(ext:String):String? = this.map { it.getTargetByExtension(ext) }.filterNotNull().firstOrNull()
