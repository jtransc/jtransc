package com.jtransc

import com.jtransc.gen.GenTargetDescriptor
import java.util.*

val AllBuildTargets by lazy {
	ServiceLoader.load(GenTargetDescriptor::class.java).toList().sortedBy { it.priority }
}

fun List<GenTargetDescriptor>.locateTargetByOutExt(ext: String): String? {
	return this.map { it.getTargetByExtension(ext) }.filterNotNull().firstOrNull()
}

