package com.jtransc

import com.jtransc.gen.GenTargetDescriptor
import com.jtransc.gen.cpp.CppTarget
import com.jtransc.gen.haxe.HaxeTarget
import com.jtransc.gen.js.JsTarget

val AllBuildTargets = listOf(CppTarget, JsTarget, HaxeTarget)

fun List<GenTargetDescriptor>.locateTargetByOutExt(ext: String): String? = this.map { it.getTargetByExtension(ext) }.filterNotNull().firstOrNull()
