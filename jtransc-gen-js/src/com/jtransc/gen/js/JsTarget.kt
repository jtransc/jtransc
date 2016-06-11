package com.jtransc.gen.js

import com.jtransc.gen.GenTargetDescriptor

object JsTarget : GenTargetDescriptor() {
	override val name = "js"
	override val longName = "Javascript"
	override val sourceExtension = "js"
	override val outputExtension = "js"
	override val extraLibraries = listOf<String>()
	override val extraClasses = listOf<String>()
	override fun getGenerator() = GenJs
	override fun getTargetByExtension(ext:String): String? = when (ext) {
		"js" -> "js"
		else -> null
	}
}
