package com.jtransc

import com.jtransc.gen.GenTargetDescriptor
import com.jtransc.gen.haxe.HaxeGenDescriptor
import com.jtransc.gen.js.JsGenDescriptor

val AllBuildTargets = listOf(
	HaxeGenDescriptor,
	JsGenDescriptor
)

fun List<GenTargetDescriptor>.locateTargetByOutExt(ext:String):String? = this.map { it.getTargetByExtension(ext) }.filterNotNull().firstOrNull()
