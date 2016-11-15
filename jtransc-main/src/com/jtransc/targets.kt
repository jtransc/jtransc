package com.jtransc

import com.jtransc.gen.GenTargetDescriptor
import java.util.*

val AllBuildTargets by lazy {
	ServiceLoader.load(GenTargetDescriptor::class.java).toList()
}

fun List<GenTargetDescriptor>.locateTargetByOutExt(ext: String): String? = this.map { it.getTargetByExtension(ext) }.filterNotNull().firstOrNull()

